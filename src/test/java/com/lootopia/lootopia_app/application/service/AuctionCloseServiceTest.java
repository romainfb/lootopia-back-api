package com.lootopia.lootopia_app.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuctionCloseServiceTest {

    private final Long WINNER_ID = 1L;
    private final Long SELLER_ID = 2L;
    private final Long AUCTION_ID = 100L;
    private final Long ARTIFACT_ID = 200L;
    private final Instant NOW = Instant.parse("2025-01-01T12:00:00Z");
    @Mock
    private AuctionPersistencePort auctionPort;
    @Mock
    private WalletHoldPersistencePort holdPort;
    @Mock
    private UserPersistencePort userPort;
    @Mock
    private ArtifactPersistencePort artifactPort;
    @Mock
    private TransactionPersistencePort transactionPort;
    @Mock
    private ArtifactRepository artifactRepository;
    @Mock
    private OutboxEventPort outboxPort;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private Clock clock;
    @InjectMocks
    private AuctionCloseService auctionCloseService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(clock.instant()).thenReturn(NOW);
        when(objectMapper.writeValueAsString(any())).thenReturn("jsonPayload");
    }

    private Auction createTestAuction(Long winnerId, Integer winningAmount) {
        return Auction.builder()
                .id(AUCTION_ID)
                .artifactId(ARTIFACT_ID)
                .sellerId(SELLER_ID)
                .status(AuctionStatus.OPEN)
                .endsAt(NOW.minusSeconds(1))
                .currentWinnerId(winnerId)
                .currentPrice(winningAmount)
                .build();
    }

    private UserEntity createTestUser(Long id, int balance) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setBalance(balance);
        return user;
    }

    @Test
    void closeExpired_settlesAuctionWithWinner_transfersBalanceAndArtifact() {
        // Given
        Auction auction = createTestAuction(WINNER_ID, 150);
        UserEntity winner = createTestUser(WINNER_ID, 500);
        UserEntity seller = createTestUser(SELLER_ID, 1000);
        Artifact artifact = Artifact.builder().id(ARTIFACT_ID).userId(SELLER_ID).build();
        ArtifactEntity artifactEntity = new ArtifactEntity();
        artifactEntity.setId(ARTIFACT_ID);
        WalletHold winnerHold = WalletHold.builder().id(1L).userId(WINNER_ID).auctionId(AUCTION_ID).amount(150).status(HoldStatus.HELD).build();

        when(auctionPort.findOpenEndedBefore(NOW)).thenReturn(List.of(auction));
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(holdPort.findActiveByUserAndAuction(WINNER_ID, AUCTION_ID)).thenReturn(Optional.of(winnerHold));
        when(userPort.findById(WINNER_ID)).thenReturn(Optional.of(winner));
        when(userPort.findById(SELLER_ID)).thenReturn(Optional.of(seller));
        when(artifactPort.findById(ARTIFACT_ID)).thenReturn(Optional.of(artifact));
        when(artifactRepository.findById(ARTIFACT_ID)).thenReturn(Optional.of(artifactEntity));
        when(auctionPort.save(any(Auction.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        int closedCount = auctionCloseService.closeExpired();

        // Then
        assertEquals(1, closedCount);

        // 1. Capture hold
        verify(holdPort).save(argThat(h -> h.getStatus()==HoldStatus.CAPTURED));

        // 2. Balance transfer
        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userPort, times(2)).save(userCaptor.capture());
        List<UserEntity> savedUsers = userCaptor.getAllValues();
        assertEquals(350, savedUsers.stream().filter(u -> u.getId().equals(WINNER_ID)).findFirst().get().getBalance()); // 500 - 150
        assertEquals(1150, savedUsers.stream().filter(u -> u.getId().equals(SELLER_ID)).findFirst().get().getBalance()); // 1000 + 150

        // 3. Artifact transfer
        verify(artifactPort).save(argThat(a -> a.getUserId().equals(WINNER_ID)));

        // 4. Transaction history
        verify(transactionPort, times(2)).save(any(Transaction.class));

        // 5. Auction status
        verify(auctionPort).save(argThat(a -> a.getStatus()==AuctionStatus.SETTLED));

        // 6. Events
        verify(outboxPort).append(eq("AUCTION"), eq(AUCTION_ID), eq("AUCTION_CLOSED"), anyString());
        verify(eventPublisher).publishEvent(any(AuctionClosedEvent.class));
    }

    @Test
    void closeExpired_settlesAuctionWithoutWinner_noTransfer() {
        // Given
        Auction auction = createTestAuction(null, 100); // No winner
        when(auctionPort.findOpenEndedBefore(NOW)).thenReturn(List.of(auction));
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(auctionPort.save(any(Auction.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        int closedCount = auctionCloseService.closeExpired();

        // Then
        assertEquals(1, closedCount);
        verify(holdPort, never()).save(any());
        verify(userPort, never()).save(any());
        verify(artifactPort, never()).save(any());
        verify(transactionPort, never()).save(any());
        verify(auctionPort).save(argThat(a -> a.getStatus()==AuctionStatus.SETTLED));
        verify(outboxPort).append(eq("AUCTION"), eq(AUCTION_ID), eq("AUCTION_CLOSED"), anyString());
        verify(eventPublisher).publishEvent(any(AuctionClosedEvent.class));
    }

    @Test
    void closeExpired_idempotent_skipsAlreadyClosed() {
        // Given
        Auction auction = createTestAuction(WINNER_ID, 150);
        auction.setStatus(AuctionStatus.CLOSED); // Already closed
        when(auctionPort.findOpenEndedBefore(NOW)).thenReturn(List.of()); // The query shouldn't find it
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));

        // When
        auctionCloseService.settleSingle(AUCTION_ID); // Call settleSingle directly to test idempotency

        // Then
        verify(holdPort, never()).save(any());
        verify(userPort, never()).save(any());
        verify(outboxPort, never()).append(any(), any(), any(), any());
    }

    @Test
    void closeExpired_skipsIfEndsAtStillFuture() {
        // Given
        Auction auction = createTestAuction(WINNER_ID, 150);
        auction.setEndsAt(NOW.plusSeconds(60)); // Not ended yet
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));

        // When
        auctionCloseService.settleSingle(AUCTION_ID);

        // Then
        verify(holdPort, never()).save(any());
        verify(userPort, never()).save(any());
        verify(outboxPort, never()).append(any(), any(), any(), any());
    }

    @Test
    void closeExpired_continuesOnFailure() {
        // Given
        Auction auction1 = createTestAuction(WINNER_ID, 150);
        auction1.setId(101L);
        Auction auction2 = createTestAuction(null, 100);
        auction2.setId(102L);
        when(auctionPort.findOpenEndedBefore(NOW)).thenReturn(List.of(auction1, auction2));
        when(auctionPort.findByIdForUpdate(101L)).thenThrow(new RuntimeException("DB error on first auction"));
        when(auctionPort.findByIdForUpdate(102L)).thenReturn(Optional.of(auction2));
        when(auctionPort.save(any(Auction.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        int closedCount = auctionCloseService.closeExpired();

        // Then
        assertEquals(1, closedCount); // Only the second one succeeded
        verify(auctionPort).findByIdForUpdate(101L);
        verify(auctionPort).findByIdForUpdate(102L);
        verify(auctionPort).save(argThat(a -> a.getId().equals(102L) && a.getStatus()==AuctionStatus.SETTLED));
    }

    @Test
    void closeExpired_publishesAuctionClosedEvent() {
        // Given
        Auction auction = createTestAuction(WINNER_ID, 150);
        UserEntity winner = createTestUser(WINNER_ID, 500);
        UserEntity seller = createTestUser(SELLER_ID, 1000);
        Artifact artifact = Artifact.builder().id(ARTIFACT_ID).userId(SELLER_ID).build();
        ArtifactEntity artifactEntity = new ArtifactEntity();
        artifactEntity.setId(ARTIFACT_ID);
        WalletHold winnerHold = WalletHold.builder().id(1L).userId(WINNER_ID).auctionId(AUCTION_ID).amount(150).status(HoldStatus.HELD).build();

        when(auctionPort.findOpenEndedBefore(NOW)).thenReturn(List.of(auction));
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(holdPort.findActiveByUserAndAuction(WINNER_ID, AUCTION_ID)).thenReturn(Optional.of(winnerHold));
        when(userPort.findById(WINNER_ID)).thenReturn(Optional.of(winner));
        when(userPort.findById(SELLER_ID)).thenReturn(Optional.of(seller));
        when(artifactPort.findById(ARTIFACT_ID)).thenReturn(Optional.of(artifact));
        when(artifactRepository.findById(ARTIFACT_ID)).thenReturn(Optional.of(artifactEntity));
        when(auctionPort.save(any(Auction.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        auctionCloseService.closeExpired();

        // Then
        ArgumentCaptor<AuctionClosedEvent> eventCaptor = ArgumentCaptor.forClass(AuctionClosedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        AuctionClosedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(AUCTION_ID, capturedEvent.auctionId());
        assertEquals(WINNER_ID, capturedEvent.winnerId());
        assertEquals(150, capturedEvent.winningAmount());
        assertEquals(AuctionStatus.SETTLED, capturedEvent.auction().getStatus());
    }
}
