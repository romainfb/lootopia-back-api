package com.lootopia.lootopia_app.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.application.port.in.CreateAuctionUseCase.CreateAuctionCommand;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuctionServiceTest {

    private final Long SELLER_ID = 1L;
    private final Long ARTIFACT_ID = 100L;
    private final Long AUCTION_ID = 200L;
    private final Instant NOW = Instant.parse("2025-01-01T12:00:00Z");
    @Mock
    private AuctionPersistencePort auctionPort;
    @Mock
    private BidPersistencePort bidPort;
    @Mock
    private ArtifactPersistencePort artifactPort;
    @Mock
    private OutboxEventPort outboxPort;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private Clock clock;
    @InjectMocks
    private AuctionService auctionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(clock.instant()).thenReturn(NOW);
        when(objectMapper.writeValueAsString(any())).thenReturn("jsonPayload");
    }

    // --- Create Auction Tests ---

    @Test
    void createAuction_happyPath_returnsOpenAuction() {
        // Given
        Artifact artifact = Artifact.builder().id(ARTIFACT_ID).userId(SELLER_ID).build();
        CreateAuctionCommand command = new CreateAuctionCommand(
                ARTIFACT_ID, 100, 10, NOW.minusSeconds(60), NOW.plusSeconds(3600)
        );
        Auction expectedAuction = Auction.builder()
                .artifactId(ARTIFACT_ID).sellerId(SELLER_ID).startPrice(100).currentPrice(100)
                .minIncrement(10).startsAt(command.startsAt()).endsAt(command.endsAt())
                .status(AuctionStatus.OPEN).build();
        expectedAuction.setId(AUCTION_ID); // Simulate ID generation

        when(artifactPort.findById(ARTIFACT_ID)).thenReturn(Optional.of(artifact));
        when(auctionPort.existsActiveByArtifactId(ARTIFACT_ID)).thenReturn(false);
        when(auctionPort.save(any(Auction.class))).thenReturn(expectedAuction);

        // When
        Auction result = auctionService.createAuction(SELLER_ID, command);

        // Then
        assertNotNull(result);
        assertEquals(AUCTION_ID, result.getId());
        assertEquals(AuctionStatus.OPEN, result.getStatus());
        verify(auctionPort).save(argThat(a ->
                a.getArtifactId().equals(ARTIFACT_ID) &&
                        a.getSellerId().equals(SELLER_ID) &&
                        a.getStartPrice().equals(100) &&
                        a.getCurrentPrice().equals(100) &&
                        a.getMinIncrement().equals(10) &&
                        a.getStartsAt().equals(command.startsAt()) &&
                        a.getEndsAt().equals(command.endsAt()) &&
                        a.getStatus().equals(AuctionStatus.OPEN)
        ));
        verify(outboxPort).append(eq("AUCTION"), eq(AUCTION_ID), eq("AUCTION_CREATED"), anyString());
    }

    @Test
    void createAuction_scheduled_whenStartsInFuture() {
        // Given
        Artifact artifact = Artifact.builder().id(ARTIFACT_ID).userId(SELLER_ID).build();
        CreateAuctionCommand command = new CreateAuctionCommand(
                ARTIFACT_ID, 100, 10, NOW.plusSeconds(60), NOW.plusSeconds(3600)
        );
        Auction expectedAuction = Auction.builder()
                .artifactId(ARTIFACT_ID).sellerId(SELLER_ID).startPrice(100).currentPrice(100)
                .minIncrement(10).startsAt(command.startsAt()).endsAt(command.endsAt())
                .status(AuctionStatus.SCHEDULED).build();
        expectedAuction.setId(AUCTION_ID);

        when(artifactPort.findById(ARTIFACT_ID)).thenReturn(Optional.of(artifact));
        when(auctionPort.existsActiveByArtifactId(ARTIFACT_ID)).thenReturn(false);
        when(auctionPort.save(any(Auction.class))).thenReturn(expectedAuction);

        // When
        Auction result = auctionService.createAuction(SELLER_ID, command);

        // Then
        assertNotNull(result);
        assertEquals(AuctionStatus.SCHEDULED, result.getStatus());
    }

    @Test
    void createAuction_throws_whenArtifactNotFound() {
        // Given
        CreateAuctionCommand command = new CreateAuctionCommand(
                ARTIFACT_ID, 100, 10, NOW.minusSeconds(60), NOW.plusSeconds(3600)
        );
        when(artifactPort.findById(ARTIFACT_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> auctionService.createAuction(SELLER_ID, command));
        verify(auctionPort, never()).save(any(Auction.class));
    }

    @Test
    void createAuction_throws_whenNotOwner() {
        // Given
        Artifact artifact = Artifact.builder().id(ARTIFACT_ID).userId(SELLER_ID + 1).build();
        CreateAuctionCommand command = new CreateAuctionCommand(
                ARTIFACT_ID, 100, 10, NOW.minusSeconds(60), NOW.plusSeconds(3600)
        );
        when(artifactPort.findById(ARTIFACT_ID)).thenReturn(Optional.of(artifact));

        // When & Then
        assertThrows(UnauthorizedAccessException.class, () -> auctionService.createAuction(SELLER_ID, command));
        verify(auctionPort, never()).save(any(Auction.class));
    }

    @Test
    void createAuction_throws_whenArtifactAlreadyOnSale() {
        // Given
        Artifact artifact = Artifact.builder().id(ARTIFACT_ID).userId(SELLER_ID).build();
        CreateAuctionCommand command = new CreateAuctionCommand(
                ARTIFACT_ID, 100, 10, NOW.minusSeconds(60), NOW.plusSeconds(3600)
        );
        when(artifactPort.findById(ARTIFACT_ID)).thenReturn(Optional.of(artifact));
        when(auctionPort.existsActiveByArtifactId(ARTIFACT_ID)).thenReturn(true);

        // When & Then
        assertThrows(ActionNotAllowedException.class, () -> auctionService.createAuction(SELLER_ID, command));
        verify(auctionPort, never()).save(any(Auction.class));
    }

    @Test
    void createAuction_throws_whenStartPriceZeroOrNegative() {
        // Given
        Artifact artifact = Artifact.builder().id(ARTIFACT_ID).userId(SELLER_ID).build();
        CreateAuctionCommand command = new CreateAuctionCommand(
                ARTIFACT_ID, 0, 10, NOW.minusSeconds(60), NOW.plusSeconds(3600)
        );
        when(artifactPort.findById(ARTIFACT_ID)).thenReturn(Optional.of(artifact));

        // When & Then
        assertThrows(InvalidParameterException.class, () -> auctionService.createAuction(SELLER_ID, command));
        verify(auctionPort, never()).save(any(Auction.class));
    }

    @Test
    void createAuction_throws_whenEndsBeforeStarts() {
        // Given
        Artifact artifact = Artifact.builder().id(ARTIFACT_ID).userId(SELLER_ID).build();
        CreateAuctionCommand command = new CreateAuctionCommand(
                ARTIFACT_ID, 100, 10, NOW.plusSeconds(3600), NOW.minusSeconds(60)
        );
        when(artifactPort.findById(ARTIFACT_ID)).thenReturn(Optional.of(artifact));

        // When & Then
        assertThrows(InvalidParameterException.class, () -> auctionService.createAuction(SELLER_ID, command));
        verify(auctionPort, never()).save(any(Auction.class));
    }

    @Test
    void createAuction_throws_whenEndsInPast() {
        // Given
        Artifact artifact = Artifact.builder().id(ARTIFACT_ID).userId(SELLER_ID).build();
        CreateAuctionCommand command = new CreateAuctionCommand(
                ARTIFACT_ID, 100, 10, NOW.minusSeconds(3600), NOW.minusSeconds(60)
        );
        when(artifactPort.findById(ARTIFACT_ID)).thenReturn(Optional.of(artifact));

        // When & Then
        assertThrows(InvalidParameterException.class, () -> auctionService.createAuction(SELLER_ID, command));
        verify(auctionPort, never()).save(any(Auction.class));
    }

    // --- Cancel Auction Tests ---

    @Test
    void cancelAuction_happyPath_setsStatusCancelled() {
        // Given
        Auction auction = Auction.builder().id(AUCTION_ID).sellerId(SELLER_ID)
                .status(AuctionStatus.SCHEDULED).currentWinnerId(null).build();
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(auctionPort.save(any(Auction.class))).thenReturn(auction);

        // When
        auctionService.cancelAuction(SELLER_ID, AUCTION_ID);

        // Then
        verify(auctionPort).save(argThat(a -> a.getStatus().equals(AuctionStatus.CANCELLED)));
        verify(outboxPort).append(eq("AUCTION"), eq(AUCTION_ID), eq("AUCTION_CANCELLED"), anyString());
    }

    @Test
    void cancelAuction_throws_whenNotFound() {
        // Given
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> auctionService.cancelAuction(SELLER_ID, AUCTION_ID));
        verify(auctionPort, never()).save(any(Auction.class));
    }

    @Test
    void cancelAuction_throws_whenNotSeller() {
        // Given
        Auction auction = Auction.builder().id(AUCTION_ID).sellerId(SELLER_ID + 1)
                .status(AuctionStatus.SCHEDULED).currentWinnerId(null).build();
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));

        // When & Then
        assertThrows(UnauthorizedAccessException.class, () -> auctionService.cancelAuction(SELLER_ID, AUCTION_ID));
        verify(auctionPort, never()).save(any(Auction.class));
    }

    @Test
    void cancelAuction_throws_whenAlreadyClosed() {
        // Given
        Auction auction = Auction.builder().id(AUCTION_ID).sellerId(SELLER_ID)
                .status(AuctionStatus.CLOSED).currentWinnerId(null).build();
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));

        // When & Then
        assertThrows(ActionNotAllowedException.class, () -> auctionService.cancelAuction(SELLER_ID, AUCTION_ID));
        verify(auctionPort, never()).save(any(Auction.class));
    }

    @Test
    void cancelAuction_throws_whenHasBids() {
        // Given
        Auction auction = Auction.builder().id(AUCTION_ID).sellerId(SELLER_ID)
                .status(AuctionStatus.OPEN).currentWinnerId(2L).build();
        when(auctionPort.findByIdForUpdate(AUCTION_ID)).thenReturn(Optional.of(auction));

        // When & Then
        assertThrows(ActionNotAllowedException.class, () -> auctionService.cancelAuction(SELLER_ID, AUCTION_ID));
        verify(auctionPort, never()).save(any(Auction.class));
    }

    // --- Fetch Auctions Tests ---

    @Test
    void fetchOpen_returnsListOfOpenAuctions() {
        // Given
        Auction auction1 = Auction.builder().id(AUCTION_ID).status(AuctionStatus.OPEN).build();
        Auction auction2 = Auction.builder().id(AUCTION_ID + 1).status(AuctionStatus.OPEN).build();
        when(auctionPort.findAllByStatus(AuctionStatus.OPEN)).thenReturn(List.of(auction1, auction2));

        // When
        List<Auction> result = auctionService.fetchOpen();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(auction1));
        assertTrue(result.contains(auction2));
    }

    @Test
    void fetchById_returnsAuction_whenFound() {
        // Given
        Auction auction = Auction.builder().id(AUCTION_ID).build();
        when(auctionPort.findById(AUCTION_ID)).thenReturn(Optional.of(auction));

        // When
        Auction result = auctionService.fetchById(AUCTION_ID);

        // Then
        assertNotNull(result);
        assertEquals(AUCTION_ID, result.getId());
    }

    @Test
    void fetchById_throws_whenNotFound() {
        // Given
        when(auctionPort.findById(AUCTION_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> auctionService.fetchById(AUCTION_ID));
    }

    @Test
    void fetchBids_returnsListOfBids_whenAuctionExists() {
        // Given
        Auction auction = Auction.builder().id(AUCTION_ID).build();
        Bid bid1 = Bid.builder().id(1L).auctionId(AUCTION_ID).build();
        Bid bid2 = Bid.builder().id(2L).auctionId(AUCTION_ID).build();
        when(auctionPort.findById(AUCTION_ID)).thenReturn(Optional.of(auction));
        when(bidPort.findByAuctionIdOrderByPlacedAtDesc(AUCTION_ID)).thenReturn(List.of(bid1, bid2));

        // When
        List<Bid> result = auctionService.fetchBids(AUCTION_ID);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(bid1));
        assertTrue(result.contains(bid2));
    }

    @Test
    void fetchBids_throws_whenAuctionNotFound() {
        // Given
        when(auctionPort.findById(AUCTION_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> auctionService.fetchBids(AUCTION_ID));
        verify(bidPort, never()).findByAuctionIdOrderByPlacedAtDesc(anyLong());
    }
}
