package com.lootopia.lootopia_app.infrastructure.in.rest.sse;

import com.lootopia.lootopia_app.application.service.event.AuctionCancelledEvent;
import com.lootopia.lootopia_app.application.service.event.AuctionClosedEvent;
import com.lootopia.lootopia_app.application.service.event.BidPlacedEvent;
import com.lootopia.lootopia_app.domain.AuctionStatus;
import com.lootopia.lootopia_app.domain.model.Auction;
import com.lootopia.lootopia_app.domain.model.Bid;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.AuctionResponse;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.BidResponse;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.AuctionRestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuctionSseDispatcherTest {

    private final Long AUCTION_ID = 1L;
    private final Long BIDDER_ID = 2L;
    private final Long SELLER_ID = 3L;
    @Mock
    private AuctionSseRegistry registry;
    @Mock
    private AuctionRestMapper mapper; // Will be created in Commit 9
    @InjectMocks
    private AuctionSseDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void onBidPlaced_broadcastsBidPlacedEvent() {
        // Given
        Bid bid = Bid.builder().id(10L).auctionId(AUCTION_ID).bidderId(BIDDER_ID).amount(150).placedAt(Instant.now()).build();
        Auction auction = Auction.builder().id(AUCTION_ID).sellerId(SELLER_ID).currentPrice(150).status(AuctionStatus.OPEN).build();
        boolean extended = false;
        BidPlacedEvent event = new BidPlacedEvent(AUCTION_ID, bid, auction, extended);

        BidResponse bidResponse = BidResponse.builder().id(10L).amount(150).build();
        AuctionResponse auctionResponse = AuctionResponse.builder().id(AUCTION_ID).currentPrice(150).build();

        when(mapper.toBidResponse(bid)).thenReturn(bidResponse);
        when(mapper.toResponse(auction)).thenReturn(auctionResponse);

        // When
        dispatcher.onBidPlaced(event);

        // Then
        verify(registry).broadcast(eq(AUCTION_ID), eq("bid_placed"), eq(Map.of(
                "bid", bidResponse,
                "auction", auctionResponse,
                "extended", extended
        )));
    }

    @Test
    void onAuctionClosed_broadcastsAuctionClosedEvent() {
        // Given
        Auction auction = Auction.builder().id(AUCTION_ID).sellerId(SELLER_ID).currentPrice(200).status(AuctionStatus.SETTLED).build();
        Long winnerId = BIDDER_ID;
        Integer winningAmount = 200;
        AuctionClosedEvent event = new AuctionClosedEvent(AUCTION_ID, auction, winnerId, winningAmount);

        AuctionResponse auctionResponse = AuctionResponse.builder().id(AUCTION_ID).currentPrice(200).status("SETTLED").build();
        when(mapper.toResponse(auction)).thenReturn(auctionResponse);

        // When
        dispatcher.onAuctionClosed(event);

        // Then
        verify(registry).broadcast(eq(AUCTION_ID), eq("auction_closed"), eq(Map.of(
                "auction", auctionResponse,
                "winnerId", String.valueOf(winnerId),
                "winningAmount", winningAmount
        )));
    }

    @Test
    void onAuctionClosed_broadcastsAuctionClosedEvent_noWinner() {
        // Given
        Auction auction = Auction.builder().id(AUCTION_ID).sellerId(SELLER_ID).currentPrice(0).status(AuctionStatus.SETTLED).build();
        AuctionClosedEvent event = new AuctionClosedEvent(AUCTION_ID, auction, null, 0);

        AuctionResponse auctionResponse = AuctionResponse.builder().id(AUCTION_ID).currentPrice(0).status("SETTLED").build();
        when(mapper.toResponse(auction)).thenReturn(auctionResponse);

        // When
        dispatcher.onAuctionClosed(event);

        // Then
        verify(registry).broadcast(eq(AUCTION_ID), eq("auction_closed"), eq(Map.of(
                "auction", auctionResponse,
                "winnerId", "null",
                "winningAmount", 0
        )));
    }

    @Test
    void onAuctionCancelled_broadcastsAuctionCancelledEvent() {
        // Given
        AuctionCancelledEvent event = new AuctionCancelledEvent(AUCTION_ID);

        // When
        dispatcher.onAuctionCancelled(event);

        // Then
        verify(registry).broadcast(eq(AUCTION_ID), eq("auction_cancelled"), eq(Map.of("auctionId", AUCTION_ID)));
    }
}
