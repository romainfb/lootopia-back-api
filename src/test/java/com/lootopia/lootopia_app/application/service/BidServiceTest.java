package com.lootopia.lootopia_app.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BidServiceTest {

    private final Long BIDDER_ID = 1L;
    private final Long SELLER_ID = 2L;
    private final Long AUCTION_ID = 100L;
    private final Instant NOW = Instant.parse("2025-01-01T12:00:00Z");
    private final Instant ENDS_AT_FAR_FUTURE = NOW.plusSeconds(3600); // 1 hour from now
    private final Instant ENDS_AT_SOFT_CLOSE = NOW.plusSeconds(10); // within 30s soft close window
    @Mock
    private AuctionPersistencePort auctionPort;
    @Mock
    private BidPersistencePort bidPort;
    @Mock
    private WalletHoldPersistencePort holdPort;
    @Mock
    private UserPersistencePort userPort;
    @Mock
    private OutboxEventPort outboxPort;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private Clock clock;
    @InjectMocks
    private BidService bidService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(clock.instant()).thenReturn(NOW);
        when(objectMapper.writeValueAsString(any())).thenReturn("jsonPayload"); // Mock JSON serialization
    }

    private Auction createTestAuction(AuctionStatus status, Integer currentPrice, Long currentWinnerId, Instant endsAt) {
        return Auction.builder()
                .id(AUCTION_ID)
                .sellerId(SELLER_ID)
                .startPrice(100)
                .currentPrice(currentPrice)
                .minIncrement(10)
                .status(status)
                .endsAt(endsAt)
                .currentWinnerId(currentWinnerId)
                .build();
    }

    private UserEntity createTestUser(Long id, int balance) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setBalance(balance);
        return user;
    }

    private WalletHold createTestWalletHold(Long id, Long userId, Long auctionId, int amount, HoldStatus status) {
        return WalletHold.builder()
                .id(id)
                .userId(userId)
                .auctionId(auctionId)
                .amount(amount)
                .status(status)
                .build();
    }

    @Test
    void placeBid_happyPath_firstBid() {
        // Given
        Auction auction = createTestAuction(AuctionStatus.OPEN, 100, null, ENDS_AT_FAR_FUTURE);
        UserEntity bidder = createTestUser(BIDDER_ID, 500);
        Bid expectedBid = Bid.builder().id(1L).auctionId(AUCTION_ID).bidderId(BIDDER_ID).amount(110).placedAt(NOW).build();

        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(userPort.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));
        when(holdPort.sumHeldByUserId(BIDDER_ID)).thenReturn(0);
        when(holdPort.findActiveByUserAndAuction(BIDDER_ID, AUCTION_ID)).thenReturn(Optional.empty());
        when(holdPort.save(any(WalletHold.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bidPort.save(any(Bid.class))).thenReturn(expectedBid);
        when(auctionPort.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Bid result = bidService.placeBid(BIDDER_ID, AUCTION_ID, 110);

        // Then
        assertNotNull(result);
        assertEquals(110, result.getAmount());
        assertEquals(BIDDER_ID, result.getBidderId());

        verify(holdPort).save(argThat(hold ->
                hold.getUserId().equals(BIDDER_ID) &&
                        hold.getAuctionId().equals(AUCTION_ID) &&
                        hold.getAmount().equals(110) &&
                        hold.getStatus().equals(HoldStatus.HELD)
        ));
        verify(auctionPort).save(argThat(a ->
                a.getCurrentPrice().equals(110) &&
                        a.getCurrentWinnerId().equals(BIDDER_ID)
        ));
        verify(outboxPort).append(eq("AUCTION"), eq(AUCTION_ID), eq("BID_PLACED"), anyString());
        verify(eventPublisher).publishEvent(any(BidPlacedEvent.class));
    }

    @Test
    void placeBid_happyPath_secondBid_releasesPreviousWinnerHold() {
        // Given
        Long PREVIOUS_WINNER_ID = 3L;
        Auction auction = createTestAuction(AuctionStatus.OPEN, 110, PREVIOUS_WINNER_ID, ENDS_AT_FAR_FUTURE);
        UserEntity bidder = createTestUser(BIDDER_ID, 500);
        WalletHold previousWinnerHold = createTestWalletHold(2L, PREVIOUS_WINNER_ID, AUCTION_ID, 110, HoldStatus.HELD);
        Bid expectedBid = Bid.builder().id(1L).auctionId(AUCTION_ID).bidderId(BIDDER_ID).amount(120).placedAt(NOW).build();

        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(userPort.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));
        when(holdPort.sumHeldByUserId(BIDDER_ID)).thenReturn(0);
        when(holdPort.findActiveByUserAndAuction(BIDDER_ID, AUCTION_ID)).thenReturn(Optional.empty());
        when(holdPort.findActiveByUserAndAuction(PREVIOUS_WINNER_ID, AUCTION_ID)).thenReturn(Optional.of(previousWinnerHold));
        when(holdPort.save(any(WalletHold.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bidPort.save(any(Bid.class))).thenReturn(expectedBid);
        when(auctionPort.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bidService.placeBid(BIDDER_ID, AUCTION_ID, 120);

        // Then
        verify(holdPort).save(argThat(hold ->
                hold.getUserId().equals(PREVIOUS_WINNER_ID) &&
                        hold.getStatus().equals(HoldStatus.RELEASED)
        ));
        verify(holdPort).save(argThat(hold ->
                hold.getUserId().equals(BIDDER_ID) &&
                        hold.getStatus().equals(HoldStatus.HELD)
        ));
        verify(auctionPort).save(argThat(a ->
                a.getCurrentPrice().equals(120) &&
                        a.getCurrentWinnerId().equals(BIDDER_ID)
        ));
    }

    @Test
    void placeBid_sameUserRebids_releasesOwnPreviousHold() {
        // Given
        Auction auction = createTestAuction(AuctionStatus.OPEN, 110, BIDDER_ID, ENDS_AT_FAR_FUTURE);
        UserEntity bidder = createTestUser(BIDDER_ID, 500);
        WalletHold previousBidderHold = createTestWalletHold(2L, BIDDER_ID, AUCTION_ID, 110, HoldStatus.HELD);
        Bid expectedBid = Bid.builder().id(1L).auctionId(AUCTION_ID).bidderId(BIDDER_ID).amount(120).placedAt(NOW).build();

        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(userPort.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));
        when(holdPort.sumHeldByUserId(BIDDER_ID)).thenReturn(110); // Total held includes previous bid on this auction
        when(holdPort.findActiveByUserAndAuction(BIDDER_ID, AUCTION_ID)).thenReturn(Optional.of(previousBidderHold));
        when(holdPort.save(any(WalletHold.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bidPort.save(any(Bid.class))).thenReturn(expectedBid);
        when(auctionPort.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bidService.placeBid(BIDDER_ID, AUCTION_ID, 120);

        // Then
        verify(holdPort).save(argThat(hold ->
                hold.getUserId().equals(BIDDER_ID) &&
                        hold.getAuctionId().equals(AUCTION_ID) &&
                        hold.getAmount().equals(110) &&
                        hold.getStatus().equals(HoldStatus.RELEASED)
        ));
        verify(holdPort).save(argThat(hold ->
                hold.getUserId().equals(BIDDER_ID) &&
                        hold.getAuctionId().equals(AUCTION_ID) &&
                        hold.getAmount().equals(120) &&
                        hold.getStatus().equals(HoldStatus.HELD)
        ));
        verify(auctionPort).save(argThat(a ->
                a.getCurrentPrice().equals(120) &&
                        a.getCurrentWinnerId().equals(BIDDER_ID)
        ));
    }

    @Test
    void placeBid_extendsEndsAt_whenWithin30sOfEnd() {
        // Given
        Auction auction = createTestAuction(AuctionStatus.OPEN, 100, null, ENDS_AT_SOFT_CLOSE);
        UserEntity bidder = createTestUser(BIDDER_ID, 500);
        Bid expectedBid = Bid.builder().id(1L).auctionId(AUCTION_ID).bidderId(BIDDER_ID).amount(110).placedAt(NOW).build();

        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(userPort.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));
        when(holdPort.sumHeldByUserId(BIDDER_ID)).thenReturn(0);
        when(holdPort.findActiveByUserAndAuction(BIDDER_ID, AUCTION_ID)).thenReturn(Optional.empty());
        when(holdPort.save(any(WalletHold.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bidPort.save(any(Bid.class))).thenReturn(expectedBid);
        when(auctionPort.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bidService.placeBid(BIDDER_ID, AUCTION_ID, 110);

        // Then
        verify(auctionPort).save(argThat(a ->
                a.getEndsAt().isAfter(ENDS_AT_SOFT_CLOSE) &&
                        a.getEndsAt().isAfter(NOW.plusSeconds(29)) // Check it's extended by at least 30s from NOW
        ));
        verify(outboxPort).append(eq("AUCTION"), eq(AUCTION_ID), eq("AUCTION_EXTENDED"), anyString());

        ArgumentCaptor<BidPlacedEvent> eventCaptor = ArgumentCaptor.forClass(BidPlacedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertTrue(eventCaptor.getValue().extended());
    }

    @Test
    void placeBid_doesNotExtend_whenOutsideSoftCloseWindow() {
        // Given
        Auction auction = createTestAuction(AuctionStatus.OPEN, 100, null, ENDS_AT_FAR_FUTURE);
        UserEntity bidder = createTestUser(BIDDER_ID, 500);
        Bid expectedBid = Bid.builder().id(1L).auctionId(AUCTION_ID).bidderId(BIDDER_ID).amount(110).placedAt(NOW).build();

        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(userPort.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));
        when(holdPort.sumHeldByUserId(BIDDER_ID)).thenReturn(0);
        when(holdPort.findActiveByUserAndAuction(BIDDER_ID, AUCTION_ID)).thenReturn(Optional.empty());
        when(holdPort.save(any(WalletHold.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bidPort.save(any(Bid.class))).thenReturn(expectedBid);
        when(auctionPort.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bidService.placeBid(BIDDER_ID, AUCTION_ID, 110);

        // Then
        verify(auctionPort).save(argThat(a -> a.getEndsAt().equals(ENDS_AT_FAR_FUTURE))); // Should not be extended
        verify(outboxPort, never()).append(eq("AUCTION"), eq(AUCTION_ID), eq("AUCTION_EXTENDED"), anyString());

        ArgumentCaptor<BidPlacedEvent> eventCaptor = ArgumentCaptor.forClass(BidPlacedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertFalse(eventCaptor.getValue().extended());
    }

    @Test
    void placeBid_throws_whenAuctionNotFound() {
        // Given
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> bidService.placeBid(BIDDER_ID, AUCTION_ID, 110));
        verify(bidPort, never()).save(any(Bid.class));
    }

    @Test
    void placeBid_throws_whenAuctionNotOpen() {
        // Given
        Auction auction = createTestAuction(AuctionStatus.CLOSED, 100, null, ENDS_AT_FAR_FUTURE);
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));

        // When & Then
        assertThrows(ActionNotAllowedException.class, () -> bidService.placeBid(BIDDER_ID, AUCTION_ID, 110));
        verify(bidPort, never()).save(any(Bid.class));
    }

    @Test
    void placeBid_throws_whenAuctionEnded() {
        // Given
        Auction auction = createTestAuction(AuctionStatus.OPEN, 100, null, NOW.minusSeconds(10)); // Auction ended
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));

        // When & Then
        assertThrows(ActionNotAllowedException.class, () -> bidService.placeBid(BIDDER_ID, AUCTION_ID, 110));
        verify(bidPort, never()).save(any(Bid.class));
    }

    @Test
    void placeBid_throws_whenBidderIsSeller() {
        // Given
        Auction auction = createTestAuction(AuctionStatus.OPEN, 100, null, ENDS_AT_FAR_FUTURE);
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));

        // When & Then
        assertThrows(ActionNotAllowedException.class, () -> bidService.placeBid(SELLER_ID, AUCTION_ID, 110));
        verify(bidPort, never()).save(any(Bid.class));
    }

    @Test
    void placeBid_throws_whenAmountBelowStartPrice() {
        // Given
        Auction auction = createTestAuction(AuctionStatus.OPEN, 100, null, ENDS_AT_FAR_FUTURE);
        UserEntity bidder = createTestUser(BIDDER_ID, 500);

        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(userPort.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));
        when(holdPort.sumHeldByUserId(BIDDER_ID)).thenReturn(0);
        when(holdPort.findActiveByUserAndAuction(BIDDER_ID, AUCTION_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidParameterException.class, () -> bidService.placeBid(BIDDER_ID, AUCTION_ID, 90)); // Below startPrice
        verify(bidPort, never()).save(any(Bid.class));
    }

    @Test
    void placeBid_throws_whenAmountBelowCurrentPlusIncrement() {
        // Given
        Auction auction = createTestAuction(AuctionStatus.OPEN, 110, 3L, ENDS_AT_FAR_FUTURE); // Current price 110
        UserEntity bidder = createTestUser(BIDDER_ID, 500);

        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(userPort.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));
        when(holdPort.sumHeldByUserId(BIDDER_ID)).thenReturn(0);
        when(holdPort.findActiveByUserAndAuction(BIDDER_ID, AUCTION_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InvalidParameterException.class, () -> bidService.placeBid(BIDDER_ID, AUCTION_ID, 115)); // Below 110 + 10 = 120
        verify(bidPort, never()).save(any(Bid.class));
    }

    @Test
    void placeBid_throws_whenInsufficientBalance() {
        // Given
        Auction auction = createTestAuction(AuctionStatus.OPEN, 100, null, ENDS_AT_FAR_FUTURE);
        UserEntity bidder = createTestUser(BIDDER_ID, 50); // Balance too low
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(userPort.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));
        when(holdPort.sumHeldByUserId(BIDDER_ID)).thenReturn(0);
        when(holdPort.findActiveByUserAndAuction(BIDDER_ID, AUCTION_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ActionNotAllowedException.class, () -> bidService.placeBid(BIDDER_ID, AUCTION_ID, 110));
        verify(bidPort, never()).save(any(Bid.class));
    }

    @Test
    void placeBid_throws_whenInsufficientAvailable_dueToHoldsOnOtherAuctions() {
        // Given
        Auction auction = createTestAuction(AuctionStatus.OPEN, 100, null, ENDS_AT_FAR_FUTURE);
        UserEntity bidder = createTestUser(BIDDER_ID, 200); // Balance 200
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(userPort.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));
        when(holdPort.sumHeldByUserId(BIDDER_ID)).thenReturn(150); // 150 held on other auctions
        when(holdPort.findActiveByUserAndAuction(BIDDER_ID, AUCTION_ID)).thenReturn(Optional.empty());

        // When & Then
        // Available = 200 - 150 = 50. Bid amount 110 is too high.
        assertThrows(ActionNotAllowedException.class, () -> bidService.placeBid(BIDDER_ID, AUCTION_ID, 110));
        verify(bidPort, never()).save(any(Bid.class));
    }

    @Test
    void placeBid_publishesBidPlacedEvent() {
        // Given
        Auction auction = createTestAuction(AuctionStatus.OPEN, 100, null, ENDS_AT_FAR_FUTURE);
        UserEntity bidder = createTestUser(BIDDER_ID, 500);
        Bid expectedBid = Bid.builder().id(1L).auctionId(AUCTION_ID).bidderId(BIDDER_ID).amount(110).placedAt(NOW).build();

        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(userPort.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));
        when(holdPort.sumHeldByUserId(BIDDER_ID)).thenReturn(0);
        when(holdPort.findActiveByUserAndAuction(BIDDER_ID, AUCTION_ID)).thenReturn(Optional.empty());
        when(holdPort.save(any(WalletHold.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bidPort.save(any(Bid.class))).thenReturn(expectedBid);
        when(auctionPort.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bidService.placeBid(BIDDER_ID, AUCTION_ID, 110);

        // Then
        ArgumentCaptor<BidPlacedEvent> eventCaptor = ArgumentCaptor.forClass(BidPlacedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        BidPlacedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(AUCTION_ID, capturedEvent.auctionId());
        assertEquals(expectedBid, capturedEvent.bid());
        assertFalse(capturedEvent.extended());
    }

    @Test
    void placeBid_publishesAuctionExtendedEvent_whenSoftClosed() {
        // Given
        Auction auction = createTestAuction(AuctionStatus.OPEN, 100, null, ENDS_AT_SOFT_CLOSE);
        UserEntity bidder = createTestUser(BIDDER_ID, 500);
        Bid expectedBid = Bid.builder().id(1L).auctionId(AUCTION_ID).bidderId(BIDDER_ID).amount(110).placedAt(NOW).build();

        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(userPort.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));
        when(holdPort.sumHeldByUserId(BIDDER_ID)).thenReturn(0);
        when(holdPort.findActiveByUserAndAuction(BIDDER_ID, AUCTION_ID)).thenReturn(Optional.empty());
        when(holdPort.save(any(WalletHold.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bidPort.save(any(Bid.class))).thenReturn(expectedBid);
        when(auctionPort.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bidService.placeBid(BIDDER_ID, AUCTION_ID, 110);

        // Then
        verify(outboxPort).append(eq("AUCTION"), eq(AUCTION_ID), eq("AUCTION_EXTENDED"), anyString());
    }

    @Test
    void placeBid_appendsOutboxEvents() {
        // Given
        Auction auction = createTestAuction(AuctionStatus.OPEN, 100, null, ENDS_AT_FAR_FUTURE);
        UserEntity bidder = createTestUser(BIDDER_ID, 500);
        Bid expectedBid = Bid.builder().id(1L).auctionId(AUCTION_ID).bidderId(BIDDER_ID).amount(110).placedAt(NOW).build();

        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(userPort.findById(BIDDER_ID)).thenReturn(Optional.of(bidder));
        when(holdPort.sumHeldByUserId(BIDDER_ID)).thenReturn(0);
        when(holdPort.findActiveByUserAndAuction(BIDDER_ID, AUCTION_ID)).thenReturn(Optional.empty());
        when(holdPort.save(any(WalletHold.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bidPort.save(any(Bid.class))).thenReturn(expectedBid);
        when(auctionPort.save(any(Auction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        bidService.placeBid(BIDDER_ID, AUCTION_ID, 110);

        // Then
        verify(outboxPort).append(eq("AUCTION"), eq(AUCTION_ID), eq("BID_PLACED"), anyString());
        verify(outboxPort, never()).append(eq("AUCTION"), eq(AUCTION_ID), eq("AUCTION_EXTENDED"), anyString());
    }
}
