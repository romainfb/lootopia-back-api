package com.lootopia.lootopia_app.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.application.port.in.CancelAuctionUseCase;
import com.lootopia.lootopia_app.application.port.in.CreateAuctionUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchAuctionsUseCase;
import com.lootopia.lootopia_app.application.port.out.ArtifactPersistencePort;
import com.lootopia.lootopia_app.application.port.out.AuctionPersistencePort;
import com.lootopia.lootopia_app.application.port.out.BidPersistencePort;
import com.lootopia.lootopia_app.application.port.out.OutboxEventPort;
import com.lootopia.lootopia_app.domain.AuctionStatus;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.domain.model.Auction;
import com.lootopia.lootopia_app.domain.model.Bid;
import com.lootopia.lootopia_app.utils.ActionNotAllowedException;
import com.lootopia.lootopia_app.utils.InvalidParameterException;
import com.lootopia.lootopia_app.utils.ResourceNotFoundException;
import com.lootopia.lootopia_app.utils.UnauthorizedAccessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AuctionService implements CreateAuctionUseCase, CancelAuctionUseCase, FetchAuctionsUseCase {

    private final AuctionPersistencePort auctionPort;
    private final BidPersistencePort bidPort; // Injected but not used in this commit, will be used later
    private final ArtifactPersistencePort artifactPort;
    private final OutboxEventPort outboxPort;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public Auction createAuction(Long sellerId, CreateAuctionCommand cmd) {
        // 1. Charger l'artefact, vérifier existence
        Artifact artifact = artifactPort.findById(cmd.artifactId())
                .orElseThrow(() -> new ResourceNotFoundException("Artifact", "id", cmd.artifactId()));
        // 2. Vérifier ownership
        if (!sellerId.equals(artifact.getUserId())) {
            throw new UnauthorizedAccessException("Vous n'êtes pas le propriétaire de cet artefact");
        }
        // 3. Vérifier qu'aucune enchère active n'existe déjà pour cet artefact
        if (auctionPort.existsActiveByArtifactId(cmd.artifactId())) {
            throw new ActionNotAllowedException("Cet artefact est déjà mis en vente");
        }
        // 4. Validations
        if (cmd.startPrice() <= 0) throw new InvalidParameterException("Le prix de départ doit être positif");
        if (cmd.minIncrement() <= 0) throw new InvalidParameterException("L'incrément minimum doit être positif");
        if (cmd.endsAt().isBefore(cmd.startsAt()))
            throw new InvalidParameterException("La fin doit être après le début");
        Instant now = Instant.now(clock); // Clock injecté
        if (cmd.endsAt().isBefore(now)) throw new InvalidParameterException("La fin doit être dans le futur");
        // 5. Statut initial
        AuctionStatus status = cmd.startsAt().isAfter(now) ? AuctionStatus.SCHEDULED:AuctionStatus.OPEN;
        // 6. Build + save
        Auction auction = Auction.builder()
                .artifactId(cmd.artifactId())
                .sellerId(sellerId)
                .startPrice(cmd.startPrice())
                .currentPrice(cmd.startPrice())
                .minIncrement(cmd.minIncrement())
                .startsAt(cmd.startsAt())
                .endsAt(cmd.endsAt())
                .status(status)
                .build();
        Auction saved = auctionPort.save(auction);
        // 7. Outbox event
        outboxPort.append("AUCTION", saved.getId(), "AUCTION_CREATED", toJson(saved));
        return saved;
    }

    @Override
    public void cancelAuction(Long sellerId, Long auctionId) {
        Auction auction = auctionPort.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", auctionId));
        if (!sellerId.equals(auction.getSellerId())) {
            throw new UnauthorizedAccessException("Vous n'êtes pas le vendeur");
        }
        if (auction.getStatus()!=AuctionStatus.SCHEDULED && auction.getStatus()!=AuctionStatus.OPEN) {
            throw new ActionNotAllowedException("Cette enchère n'est plus active");
        }
        if (auction.getCurrentWinnerId()!=null) {
            throw new ActionNotAllowedException("Impossible d'annuler : des offres ont déjà été placées");
        }
        auction.setStatus(AuctionStatus.CANCELLED);
        Auction saved = auctionPort.save(auction);
        outboxPort.append("AUCTION", auctionId, "AUCTION_CANCELLED", toJson(saved));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Auction> fetchOpen() {
        return auctionPort.findAllByStatus(AuctionStatus.OPEN);
    }

    @Override
    @Transactional(readOnly = true)
    public Auction fetchById(Long id) {
        return auctionPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bid> fetchBids(Long auctionId) {
        if (auctionPort.findById(auctionId).isEmpty()) {
            throw new ResourceNotFoundException("Auction", "id", auctionId);
        }
        return bidPort.findByAuctionIdOrderByPlacedAtDesc(auctionId);
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize event", e);
        }
    }
}
