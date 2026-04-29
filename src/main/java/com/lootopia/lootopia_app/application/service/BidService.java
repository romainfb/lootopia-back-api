package com.lootopia.lootopia_app.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.application.port.in.PlaceBidUseCase;
import com.lootopia.lootopia_app.application.port.out.AuctionPersistencePort;
import com.lootopia.lootopia_app.application.port.out.BidPersistencePort;
import com.lootopia.lootopia_app.application.port.out.OutboxEventPort;
import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.application.port.out.WalletHoldPersistencePort;
import com.lootopia.lootopia_app.application.service.event.BidPlacedEvent;
import com.lootopia.lootopia_app.domain.AuctionStatus;
import com.lootopia.lootopia_app.domain.HoldStatus;
import com.lootopia.lootopia_app.domain.model.Auction;
import com.lootopia.lootopia_app.domain.model.Bid;
import com.lootopia.lootopia_app.domain.model.WalletHold;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import com.lootopia.lootopia_app.utils.ActionNotAllowedException;
import com.lootopia.lootopia_app.utils.InvalidParameterException;
import com.lootopia.lootopia_app.utils.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class BidService implements PlaceBidUseCase {

    private static final long SOFT_CLOSE_WINDOW_SECONDS = 30L;

    private final AuctionPersistencePort auctionPort;
    private final BidPersistencePort bidPort;
    private final WalletHoldPersistencePort holdPort;
    private final UserPersistencePort userPort;
    private final OutboxEventPort outboxPort;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Override
    public Bid placeBid(Long bidderId, Long auctionId, int amount) {
        // 1. Verrouiller l'enchère
        Auction auction = auctionPort.findByIdForUpdate(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", auctionId));

        // 2. Validations
        if (auction.getStatus()!=AuctionStatus.OPEN) {
            throw new ActionNotAllowedException("L'enchère n'est pas ouverte");
        }
        Instant now = Instant.now(clock);
        if (now.isAfter(auction.getEndsAt())) {
            throw new ActionNotAllowedException("L'enchère est terminée");
        }
        if (bidderId.equals(auction.getSellerId())) {
            throw new ActionNotAllowedException("Vous ne pouvez pas enchérir sur votre propre vente");
        }

        // 3. Montant minimal
        boolean hasBids = auction.getCurrentWinnerId()!=null;
        int minimumAmount = hasBids
                ? auction.getCurrentPrice() + auction.getMinIncrement()
                :auction.getStartPrice();
        if (amount < minimumAmount) {
            throw new InvalidParameterException("Montant insuffisant. Minimum requis : " + minimumAmount);
        }

        // 4. Vérifier solde dispo (balance - holds actifs, en excluant l'éventuel hold du bidder sur cette même enchère car il sera remplacé)
        UserEntity bidder = userPort.findById(bidderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", bidderId));
        Integer heldTotal = holdPort.sumHeldByUserId(bidderId);
        Optional<WalletHold> existingHold = holdPort.findActiveByUserAndAuction(bidderId, auctionId);
        int heldExcludingThisAuction = heldTotal - existingHold.map(WalletHold::getAmount).orElse(0);
        int available = bidder.getBalance() - heldExcludingThisAuction;
        if (available < amount) {
            throw new ActionNotAllowedException("Solde insuffisant");
        }

        // 5. Libérer le hold précédent du bidder s'il existe
        existingHold.ifPresent(h -> {
            h.setStatus(HoldStatus.RELEASED);
            holdPort.save(h);
        });

        // 6. Libérer le hold de l'ancien gagnant s'il existe et != bidder
        if (auction.getCurrentWinnerId()!=null && !auction.getCurrentWinnerId().equals(bidderId)) {
            holdPort.findActiveByUserAndAuction(auction.getCurrentWinnerId(), auctionId)
                    .ifPresent(h -> {
                        h.setStatus(HoldStatus.RELEASED);
                        holdPort.save(h);
                    });
        }

        // 7. Créer le nouveau hold
        WalletHold newHold = WalletHold.builder()
                .userId(bidderId)
                .auctionId(auctionId)
                .amount(amount)
                .status(HoldStatus.HELD)
                .createdAt(now)
                .build();
        holdPort.save(newHold);

        // 8. Insérer le bid
        Bid bid = Bid.builder()
                .auctionId(auctionId)
                .bidderId(bidderId)
                .amount(amount)
                .placedAt(now)
                .build();
        Bid savedBid = bidPort.save(bid);

        // 9. Mettre à jour l'enchère
        auction.setCurrentPrice(amount);
        auction.setCurrentWinnerId(bidderId);

        // 10. Soft close anti-sniping
        long secondsUntilEnd = Duration.between(now, auction.getEndsAt()).getSeconds();
        boolean extended = false;
        if (secondsUntilEnd < SOFT_CLOSE_WINDOW_SECONDS) {
            auction.setEndsAt(now.plusSeconds(SOFT_CLOSE_WINDOW_SECONDS));
            extended = true;
        }
        Auction savedAuction = auctionPort.save(auction);

        // 11. Outbox events
        outboxPort.append("AUCTION", auctionId, "BID_PLACED", toJson(Map.of(
                "bidId", savedBid.getId(),
                "bidderId", bidderId,
                "amount", amount,
                "newCurrentPrice", savedAuction.getCurrentPrice(),
                "newEndsAt", savedAuction.getEndsAt().toString()
        )));
        if (extended) {
            outboxPort.append("AUCTION", auctionId, "AUCTION_EXTENDED", toJson(Map.of(
                    "newEndsAt", savedAuction.getEndsAt().toString()
            )));
        }

        // 12. In-memory event pour SSE
        eventPublisher.publishEvent(new BidPlacedEvent(auctionId, savedBid, savedAuction, extended));

        return savedBid;
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot serialize event", e);
        }
    }
}
