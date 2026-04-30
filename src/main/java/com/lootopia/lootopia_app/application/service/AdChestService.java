package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.OpenAdChestUseCase;
import com.lootopia.lootopia_app.application.port.out.TransactionPersistencePort;
import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.domain.model.Transaction;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.CooldownActiveException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import com.lootopia.lootopia_app.infrastructure.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

@Service
public class AdChestService implements OpenAdChestUseCase {

    public static final String TRANSACTION_TYPE = "AD_CHEST";

    private final UserPersistencePort userPersistencePort;
    private final TransactionPersistencePort transactionPersistencePort;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtDecoder jwtDecoder;
    private final Clock clock;
    private final Random random;
    private final int minAmount;
    private final int maxAmount;
    private final Duration cooldown;

    public AdChestService(
            UserPersistencePort userPersistencePort,
            TransactionPersistencePort transactionPersistencePort,
            JwtTokenProvider jwtTokenProvider,
            JwtDecoder jwtDecoder,
            Clock clock,
            Random random,
            @Value("${app.ad-chest.min-amount:50}") int minAmount,
            @Value("${app.ad-chest.max-amount:200}") int maxAmount,
            @Value("${app.ad-chest.cooldown:PT1H}") Duration cooldown) {
        this.userPersistencePort = userPersistencePort;
        this.transactionPersistencePort = transactionPersistencePort;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtDecoder = jwtDecoder;
        this.clock = clock;
        this.random = random;
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
        this.cooldown = cooldown;
    }

    @Override
    public StartAdChestResult startAdChest(Long userId) {
        UserEntity user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        ensureCooldownElapsed(user);
        String token = jwtTokenProvider.generateAdWatchToken(user);
        return new StartAdChestResult(token, jwtTokenProvider.getAdWatchMinDuration().toSeconds());
    }

    @Override
    @Transactional
    public OpenAdChestResult openAdChest(Long userId, String adWatchToken) {
        Long tokenUserId = decodeAndValidateToken(adWatchToken);
        if (!tokenUserId.equals(userId)) {
            throw new InvalidParameterException("Token does not belong to current user");
        }

        UserEntity user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        ensureCooldownElapsed(user);

        int amount = minAmount + random.nextInt(maxAmount - minAmount + 1);
        Instant now = Instant.now(clock);

        user.setBalance(user.getBalance() + amount);
        user.setLastAdChestAt(now);
        UserEntity saved = userPersistencePort.save(user);

        transactionPersistencePort.save(Transaction.builder()
                .utilisateur(saved)
                .montant(BigDecimal.valueOf(amount))
                .typeTransaction(TRANSACTION_TYPE)
                .date(Timestamp.from(now))
                .build());

        return new OpenAdChestResult(amount, saved.getBalance());
    }

    private void ensureCooldownElapsed(UserEntity user) {
        Instant last = user.getLastAdChestAt();
        if (last == null) return;
        Instant now = Instant.now(clock);
        Instant unlockAt = last.plus(cooldown);
        if (now.isBefore(unlockAt)) {
            Duration remaining = Duration.between(now, unlockAt);
            Duration roundedRemaining = ceilToWholeSeconds(remaining);
            throw new CooldownActiveException(
                    "Ad chest cooldown active. Retry in " + roundedRemaining.toSeconds() + "s",
                    roundedRemaining);
        }
    }

    private Duration ceilToWholeSeconds(Duration duration) {
        long seconds = duration.getSeconds();
        if (duration.getNano() > 0) {
            seconds++;
        }
        return Duration.ofSeconds(seconds);
    }
    private Long decodeAndValidateToken(String adWatchToken) {
        Jwt jwt;
        try {
            jwt = jwtDecoder.decode(adWatchToken);
        } catch (JwtException e) {
            throw new InvalidParameterException("Invalid or expired ad-watch token");
        }
        if (!JwtTokenProvider.TYPE_AD_WATCH.equals(jwt.getClaimAsString(JwtTokenProvider.CLAIM_TYPE))) {
            throw new InvalidParameterException("Token is not an ad-watch token");
        }
        return Long.valueOf(jwt.getSubject());
    }
}
