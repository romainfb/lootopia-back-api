package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.OpenAdChestUseCase.OpenAdChestResult;
import com.lootopia.lootopia_app.application.port.in.OpenAdChestUseCase.StartAdChestResult;
import com.lootopia.lootopia_app.application.port.out.TransactionPersistencePort;
import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.domain.AccountType;
import com.lootopia.lootopia_app.domain.model.Transaction;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.CooldownActiveException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import com.lootopia.lootopia_app.infrastructure.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdChestServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-04-29T10:00:00Z");
    private static final long USER_ID = 7L;

    private UserPersistencePort userPort;
    private TransactionPersistencePort txPort;
    private JwtTokenProvider tokenProvider;
    private JwtDecoder jwtDecoder;
    private AdChestService service;

    @BeforeEach
    void setUp() {
        userPort = mock(UserPersistencePort.class);
        txPort = mock(TransactionPersistencePort.class);
        tokenProvider = mock(JwtTokenProvider.class);
        jwtDecoder = mock(JwtDecoder.class);
        Clock clock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        Random fixedRandom = new Random() {
            @Override public int nextInt(int bound) { return 50; } // → amount = 50 + 50 = 100
        };
        service = new AdChestService(
                userPort, txPort, tokenProvider, jwtDecoder,
                clock, fixedRandom,
                50, 200, Duration.ofHours(1));
    }

    private UserEntity user(Instant lastAdChestAt, int balance) {
        return UserEntity.builder()
                .id(USER_ID)
                .email("u@x.io")
                .accountType(AccountType.USER)
                .balance(balance)
                .lastAdChestAt(lastAdChestAt)
                .build();
    }

    private Jwt jwtWith(Long subject, String type) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", String.valueOf(subject));
        claims.put(JwtTokenProvider.CLAIM_TYPE, type);
        Map<String, Object> headers = Map.of("alg", "RS256");
        return new Jwt("token-value", FIXED_NOW, FIXED_NOW.plusSeconds(300), headers, claims);
    }

    private Jwt validToken(Long subject) {
        return jwtWith(subject, JwtTokenProvider.TYPE_AD_WATCH);
    }

    @Test
    void constructor_throws_when_maxAmount_less_than_minAmount() {
        assertThatThrownBy(() -> new AdChestService(
                userPort, txPort, tokenProvider, jwtDecoder,
                Clock.fixed(FIXED_NOW, ZoneOffset.UTC), new Random(),
                200, 50, Duration.ofHours(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("max-amount");
    }

    @Test
    void startAdChest_returns_token_when_no_previous_use() {
        UserEntity u = user(null, 0);
        when(userPort.findById(USER_ID)).thenReturn(Optional.of(u));
        when(tokenProvider.generateAdWatchToken(u)).thenReturn("tok");
        when(tokenProvider.getAdWatchMinDuration()).thenReturn(Duration.ofSeconds(15));

        StartAdChestResult result = service.startAdChest(USER_ID);

        assertThat(result.adWatchToken()).isEqualTo("tok");
        assertThat(result.minWatchSeconds()).isEqualTo(15L);
    }

    @Test
    void startAdChest_throws_cooldown_when_under_one_hour() {
        UserEntity u = user(FIXED_NOW.minus(Duration.ofMinutes(30)), 0);
        when(userPort.findById(USER_ID)).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> service.startAdChest(USER_ID))
                .isInstanceOf(CooldownActiveException.class);
    }

    @Test
    void startAdChest_user_not_found() {
        when(userPort.findById(USER_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.startAdChest(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void openAdChest_credits_amount_in_range_and_records_transaction() {
        UserEntity u = user(null, 1000);
        when(userPort.findById(USER_ID)).thenReturn(Optional.of(u));
        when(jwtDecoder.decode("tok")).thenReturn(validToken(USER_ID));
        when(userPort.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OpenAdChestResult result = service.openAdChest(USER_ID, "tok");

        assertThat(result.amount()).isBetween(50, 200);
        assertThat(result.amount()).isEqualTo(100);
        assertThat(result.newBalance()).isEqualTo(1100);

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userPort).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getBalance()).isEqualTo(1100);
        assertThat(userCaptor.getValue().getLastAdChestAt()).isEqualTo(FIXED_NOW);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(txPort).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getTypeTransaction()).isEqualTo("AD_CHEST");
        assertThat(txCaptor.getValue().getMontant().intValueExact()).isEqualTo(100);
    }

    @Test
    void openAdChest_rejects_token_belonging_to_other_user() {
        when(jwtDecoder.decode("tok")).thenReturn(validToken(999L));
        assertThatThrownBy(() -> service.openAdChest(USER_ID, "tok"))
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void openAdChest_rejects_invalid_token() {
        when(jwtDecoder.decode("bad")).thenThrow(new JwtException("expired"));
        assertThatThrownBy(() -> service.openAdChest(USER_ID, "bad"))
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void openAdChest_rejects_wrong_token_type() {
        when(jwtDecoder.decode("tok")).thenReturn(jwtWith(USER_ID, JwtTokenProvider.TYPE_ACCESS));
        assertThatThrownBy(() -> service.openAdChest(USER_ID, "tok"))
                .isInstanceOf(InvalidParameterException.class);
    }

    @Test
    void openAdChest_throws_cooldown_when_under_one_hour() {
        UserEntity u = user(FIXED_NOW.minus(Duration.ofMinutes(30)), 1000);
        when(userPort.findById(USER_ID)).thenReturn(Optional.of(u));
        when(jwtDecoder.decode("tok")).thenReturn(validToken(USER_ID));

        assertThatThrownBy(() -> service.openAdChest(USER_ID, "tok"))
                .isInstanceOf(CooldownActiveException.class);
        verify(txPort, org.mockito.Mockito.never()).save(any());
    }

    @Test
    void openAdChest_succeeds_after_cooldown_elapsed() {
        UserEntity u = user(FIXED_NOW.minus(Duration.ofHours(1).plusSeconds(1)), 500);
        when(userPort.findById(USER_ID)).thenReturn(Optional.of(u));
        when(jwtDecoder.decode("tok")).thenReturn(validToken(USER_ID));
        when(userPort.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        OpenAdChestResult result = service.openAdChest(USER_ID, "tok");

        assertThat(result.newBalance()).isEqualTo(600);
        verify(txPort).save(any(Transaction.class));
        verify(userPort).save(eq(u));
    }
}
