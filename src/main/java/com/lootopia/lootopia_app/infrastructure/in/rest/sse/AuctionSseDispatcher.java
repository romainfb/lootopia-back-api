package com.lootopia.lootopia_app.infrastructure.in.rest.sse;

import com.lootopia.lootopia_app.application.service.event.AuctionCancelledEvent;
import com.lootopia.lootopia_app.application.service.event.AuctionClosedEvent;
import com.lootopia.lootopia_app.application.service.event.BidPlacedEvent;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.AuctionRestMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionSseDispatcher {

    private final AuctionSseRegistry registry;
    private final AuctionRestMapper mapper; // Will be created in Commit 9

    @EventListener
    public void onBidPlaced(BidPlacedEvent ev) {
        registry.broadcast(ev.auctionId(), "bid_placed", Map.of(
                "bid", mapper.toBidResponse(ev.bid()),
                "auction", mapper.toResponse(ev.auction()),
                "extended", ev.extended()
        ));
    }

    @EventListener
    public void onAuctionClosed(AuctionClosedEvent ev) {
        registry.broadcast(ev.auctionId(), "auction_closed", Map.of(
                "auction", mapper.toResponse(ev.auction()),
                "winnerId", ev.winnerId()!=null ? ev.winnerId():"null",
                "winningAmount", ev.winningAmount()!=null ? ev.winningAmount():0
        ));
    }

    @EventListener
    public void onAuctionCancelled(AuctionCancelledEvent ev) {
        registry.broadcast(ev.auctionId(), "auction_cancelled", Map.of("auctionId", ev.auctionId()));
    }
}
