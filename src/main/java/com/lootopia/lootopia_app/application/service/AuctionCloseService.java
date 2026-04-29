package com.lootopia.lootopia_app.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.application.port.in.CloseExpiredAuctionsUseCase;
import com.lootopia.lootopia_app.application.port.out.ArtifactPersistencePort;
import com.lootopia.lootopia_app.application.port.out.AuctionPersistencePort;
import com.lootopia.lootopia_app.application.port.out.OutboxEventPort;
import com.lootopia.lootopia_app.application.port.out.TransactionPersistencePort;
import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.application.port.out.WalletHoldPersistencePort;
import com.lootopia.lootopia_app.application.service.event.AuctionClosedEvent;
import com.lootopia.lootopia_app.domain.AuctionStatus;
import com.lootopia.lootopia_app.domain.HoldStatus;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.domain.model.Auction;
import com.lootopia.lootopia_app.domain.model.Transaction;
import com.lootopia.lootopia_app.domain.model.WalletHold;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.ArtifactEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.repository.ArtifactRepository;
import com.lootopia.lootopia_app.utils.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionCloseService implements CloseExpiredAuctionsUseCase {

    private final AuctionPersistencePort auctionPort;
    private final WalletHoldPersistencePort holdPort;
    private final UserPersistencePort userPort;
    private final ArtifactPersistencePort artifactPort;
    private final TransactionPersistencePort transactionPort;
    private final ArtifactRepository artifactRepository; // Direct JPA repo for Transaction entity mapping
    private final OutboxEventPort outboxPort;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Override
    public int closeExpired() {
        Instant now = Instant.now(clock);
        List<Auction> toClose = auctionPort.findOpenEndedBefore(now);
        int closed = 0;
        for (Auction a : toClose) {
            try {
                settleSingle(a.getId());
                closed++;
            } catch (Exception e) {
                log.error("Failed to close auction {}", a.getId(), e);
            }
        }
        return closed;
    }

    @Transactional
    public void settleSingle(Long auctionId) {
        Auction auction = auctionPort.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", auctionId));
        // Idempotence : si déjà clos par un autre thread/instance, on sort sans erreur
        if (auction.getStatus()!=AuctionStatus.OPEN) return;
        if (Instant.now(clock).isBefore(auction.getEndsAt())) return; // safety net

        auction.setStatus(AuctionStatus.CLOSED);

        Long winnerId = auction.getCurrentWinnerId();
        Integer winningAmount = auction.getCurrentPrice();

        if (winnerId!=null) {
            // 1. Capture hold winner
            WalletHold winnerHold = holdPort.findActiveByUserAndAuction(winnerId, auctionId)
                    .orElseThrow(() -> new IllegalStateException("Hold actif manquant pour winner " + winnerId));
            winnerHold.setStatus(HoldStatus.CAPTURED);
            holdPort.save(winnerHold);

            // 2. Débiter winner — UserEntity direct (cf section 0)
            UserEntity winner = userPort.findById(winnerId).orElseThrow(() -> new ResourceNotFoundException("User", "id", winnerId));
            winner.setBalance(winner.getBalance() - winningAmount);
            userPort.save(winner);

            // 3. Créditer seller
            UserEntity seller = userPort.findById(auction.getSellerId()).orElseThrow(() -> new ResourceNotFoundException("User", "id", auction.getSellerId()));
            seller.setBalance(seller.getBalance() + winningAmount);
            userPort.save(seller);

            // 4. Transfert artefact (port retourne Artifact domain)
            Artifact artifact = artifactPort.findById(auction.getArtifactId()).orElseThrow(() -> new ResourceNotFoundException("Artifact", "id", auction.getArtifactId()));
            artifact.setUserId(winnerId);
            artifactPort.save(artifact);

            // 5. Transactions historiques.
            ArtifactEntity artifactEntity = artifactRepository.findById(auction.getArtifactId()).orElseThrow(() -> new ResourceNotFoundException("ArtifactEntity", "id", auction.getArtifactId()));
            Timestamp nowTs = Timestamp.from(Instant.now(clock));

            Transaction debitTx = Transaction.builder()
                    .utilisateur(winner)
                    .artefact(artifactEntity)
                    .montant(BigDecimal.valueOf(winningAmount).negate())
                    .typeTransaction("AUCTION_BUY")
                    .date(nowTs)
                    .build();
            transactionPort.save(debitTx);

            Transaction creditTx = Transaction.builder()
                    .utilisateur(seller)
                    .artefact(artifactEntity)
                    .montant(BigDecimal.valueOf(winningAmount))
                    .typeTransaction("AUCTION_SELL")
                    .date(nowTs)
                    .build();
            transactionPort.save(creditTx);

            auction.setStatus(AuctionStatus.SETTLED);
        } else {
            // Pas de winner : direct SETTLED, rien à transférer
            auction.setStatus(AuctionStatus.SETTLED);
        }

        Auction savedAuction = auctionPort.save(auction);

        outboxPort.append("AUCTION", auctionId, "AUCTION_CLOSED", toJson(Map.of(
                "winnerId", winnerId!=null ? String.valueOf(winnerId):"null",
                "winningAmount", winningAmount!=null ? winningAmount:0
        )));
        eventPublisher.publishEvent(new AuctionClosedEvent(auctionId, savedAuction, winnerId, winningAmount));
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize event", e);
        }
    }
}
