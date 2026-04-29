package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lootopia.lootopia_app.application.port.in.CancelAuctionUseCase;
import com.lootopia.lootopia_app.application.port.in.CreateAuctionUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchAuctionsUseCase;
import com.lootopia.lootopia_app.application.port.in.PlaceBidUseCase;
import com.lootopia.lootopia_app.domain.AuctionStatus;
import com.lootopia.lootopia_app.domain.model.Auction;
import com.lootopia.lootopia_app.domain.model.Bid;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.AuctionResponse;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.BidResponse;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.CreateAuctionRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.PlaceBidRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.AuctionRestMapper;
import com.lootopia.lootopia_app.infrastructure.in.rest.sse.AuctionSseRegistry;
import com.lootopia.lootopia_app.utils.ActionNotAllowedException;
import com.lootopia.lootopia_app.utils.ResourceNotFoundException;
import com.lootopia.lootopia_app.utils.UnauthorizedAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuctionControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CreateAuctionUseCase createUC;
    @Mock
    private CancelAuctionUseCase cancelUC;
    @Mock
    private PlaceBidUseCase placeBidUC;
    @Mock
    private FetchAuctionsUseCase fetchUC;
    @Mock
    private AuctionSseRegistry sseRegistry;
    @Mock
    private AuctionRestMapper mapper;

    private Jwt mockJwt;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        createUC = mock(CreateAuctionUseCase.class);
        cancelUC = mock(CancelAuctionUseCase.class);
        placeBidUC = mock(PlaceBidUseCase.class);
        fetchUC = mock(FetchAuctionsUseCase.class);
        sseRegistry = mock(AuctionSseRegistry.class);
        mapper = mock(AuctionRestMapper.class);

        // Create controller instance with mocked dependencies
        AuctionController auctionController = new AuctionController(createUC, cancelUC, placeBidUC, fetchUC, sseRegistry, mapper);

        // Build MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(auctionController)
                .build();

        // Mock JWT for authenticated user
        mockJwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .claim("sub", "1")
                .build();

        // Configure ObjectMapper to handle Instant
        objectMapper.findAndRegisterModules();
    }

    // --- Create Auction Tests ---

    @Test
    void create_returns201_andAuctionResponse() throws Exception {
        CreateAuctionRequest request = new CreateAuctionRequest();
        request.setArtifactId(1L);
        request.setStartPrice(100);
        request.setMinIncrement(10);
        request.setStartsAt(Instant.now().minusSeconds(60));
        request.setEndsAt(Instant.now().plusSeconds(3600));

        Auction createdAuction = Auction.builder().id(1L).status(AuctionStatus.OPEN).build();
        AuctionResponse response = AuctionResponse.builder().id(1L).status("OPEN").build();

        when(mapper.toCommand(any(CreateAuctionRequest.class))).thenReturn(new CreateAuctionUseCase.CreateAuctionCommand(
                request.getArtifactId(), request.getStartPrice(), request.getMinIncrement(), request.getStartsAt(), request.getEndsAt()
        ));
        when(createUC.createAuction(anyLong(), any(CreateAuctionUseCase.CreateAuctionCommand.class))).thenReturn(createdAuction);
        when(mapper.toResponse(any(Auction.class))).thenReturn(response);

        mockMvc.perform(post("/api/auctions")
                        .with(request -> {
                            request.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(createUC).createAuction(eq(1L), any(CreateAuctionUseCase.CreateAuctionCommand.class));
        verify(mapper).toResponse(createdAuction);
    }

    @Test
    void create_returns400_whenInvalidBody() throws Exception {
        CreateAuctionRequest request = new CreateAuctionRequest(); // Missing artifactId, startPrice, etc.

        mockMvc.perform(post("/api/auctions")
                        .with(request1 -> {
                            request1.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request1;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(createUC, never()).createAuction(anyLong(), any(CreateAuctionUseCase.CreateAuctionCommand.class));
    }

    // --- List Open Auctions Tests ---

    @Test
    void listOpen_returnsListOfOpenAuctions() throws Exception {
        Auction auction1 = Auction.builder().id(1L).status(AuctionStatus.OPEN).build();
        Auction auction2 = Auction.builder().id(2L).status(AuctionStatus.OPEN).build();
        List<Auction> auctions = List.of(auction1, auction2);

        AuctionResponse response1 = AuctionResponse.builder().id(1L).status("OPEN").build();
        AuctionResponse response2 = AuctionResponse.builder().id(2L).status("OPEN").build();
        List<AuctionResponse> responses = List.of(response1, response2);

        when(fetchUC.fetchOpen()).thenReturn(auctions);
        when(mapper.toResponse(auction1)).thenReturn(response1);
        when(mapper.toResponse(auction2)).thenReturn(response2);

        mockMvc.perform(get("/api/auctions")
                        .with(request -> {
                            request.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(fetchUC).fetchOpen();
        verify(mapper, times(2)).toResponse(any(Auction.class));
    }

    // --- Get One Auction Tests ---

    @Test
    void getOne_returnsDetail_whenFound() throws Exception {
        Auction auction = Auction.builder().id(1L).status(AuctionStatus.OPEN).build();
        AuctionResponse response = AuctionResponse.builder().id(1L).status("OPEN").build();

        when(fetchUC.fetchById(1L)).thenReturn(auction);
        when(mapper.toResponse(auction)).thenReturn(response);

        mockMvc.perform(get("/api/auctions/{id}", 1L)
                        .with(request -> {
                            request.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(fetchUC).fetchById(1L);
        verify(mapper).toResponse(auction);
    }

    @Test
    void getOne_returns404_whenNotFound() throws Exception {
        when(fetchUC.fetchById(anyLong())).thenThrow(new ResourceNotFoundException("Auction", "id", 99L));

        mockMvc.perform(get("/api/auctions/{id}", 99L)
                        .with(request -> {
                            request.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(fetchUC).fetchById(99L);
    }

    // --- List Bids Tests ---

    @Test
    void listBids_returnsListOfBids() throws Exception {
        Bid bid1 = Bid.builder().id(1L).auctionId(1L).amount(100).build();
        Bid bid2 = Bid.builder().id(2L).auctionId(1L).amount(110).build();
        List<Bid> bids = List.of(bid1, bid2);

        BidResponse response1 = BidResponse.builder().id(1L).amount(100).build();
        BidResponse response2 = BidResponse.builder().id(2L).amount(110).build();
        List<BidResponse> responses = List.of(response1, response2);

        when(fetchUC.fetchBids(1L)).thenReturn(bids);
        when(mapper.toBidResponse(bid1)).thenReturn(response1);
        when(mapper.toBidResponse(bid2)).thenReturn(response2);

        mockMvc.perform(get("/api/auctions/{id}/bids", 1L)
                        .with(request -> {
                            request.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(fetchUC).fetchBids(1L);
        verify(mapper, times(2)).toBidResponse(any(Bid.class));
    }

    @Test
    void listBids_returns404_whenAuctionNotFound() throws Exception {
        when(fetchUC.fetchBids(anyLong())).thenThrow(new ResourceNotFoundException("Auction", "id", 99L));

        mockMvc.perform(get("/api/auctions/{id}/bids", 99L)
                        .with(request -> {
                            request.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(fetchUC).fetchBids(99L);
    }

    // --- Place Bid Tests ---

    @Test
    void placeBid_returns201_andBidResponse() throws Exception {
        PlaceBidRequest request = new PlaceBidRequest();
        request.setAmount(150);

        Bid placedBid = Bid.builder().id(1L).auctionId(1L).amount(150).build();
        BidResponse response = BidResponse.builder().id(1L).auctionId(1L).amount(150).build();

        when(placeBidUC.placeBid(anyLong(), anyLong(), anyInt())).thenReturn(placedBid);
        when(mapper.toBidResponse(any(Bid.class))).thenReturn(response);

        mockMvc.perform(post("/api/auctions/{id}/bids", 1L)
                        .with(request1 -> {
                            request1.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request1;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.amount").value(150));

        verify(placeBidUC).placeBid(eq(1L), eq(1L), eq(150));
        verify(mapper).toBidResponse(placedBid);
    }

    @Test
    void placeBid_returns400_whenInvalidAmount() throws Exception {
        PlaceBidRequest request = new PlaceBidRequest();
        request.setAmount(0); // Invalid amount

        mockMvc.perform(post("/api/auctions/{id}/bids", 1L)
                        .with(request1 -> {
                            request1.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request1;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(placeBidUC, never()).placeBid(anyLong(), anyLong(), anyInt());
    }

    @Test
    void placeBid_returns400_whenActionNotAllowed() throws Exception {
        PlaceBidRequest request = new PlaceBidRequest();
        request.setAmount(150);

        when(placeBidUC.placeBid(anyLong(), anyLong(), anyInt())).thenThrow(new ActionNotAllowedException("Not allowed"));

        mockMvc.perform(post("/api/auctions/{id}/bids", 1L)
                        .with(request1 -> {
                            request1.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request1;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // ActionNotAllowedException maps to 400 by default

        verify(placeBidUC).placeBid(eq(1L), eq(1L), eq(150));
    }

    @Test
    void placeBid_returns404_whenAuctionNotFound() throws Exception {
        PlaceBidRequest request = new PlaceBidRequest();
        request.setAmount(150);

        when(placeBidUC.placeBid(anyLong(), anyLong(), anyInt())).thenThrow(new ResourceNotFoundException("Auction", "id", 99L));

        mockMvc.perform(post("/api/auctions/{id}/bids", 99L)
                        .with(request1 -> {
                            request1.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request1;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(placeBidUC).placeBid(eq(1L), eq(99L), eq(150));
    }

    // --- Cancel Auction Tests ---

    @Test
    void cancel_returns204() throws Exception {
        doNothing().when(cancelUC).cancelAuction(anyLong(), anyLong());

        mockMvc.perform(delete("/api/auctions/{id}", 1L)
                        .with(request -> {
                            request.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request;
                        }))
                .andExpect(status().isNoContent());

        verify(cancelUC).cancelAuction(eq(1L), eq(1L));
    }

    @Test
    void cancel_returns403_whenUnauthorized() throws Exception {
        doThrow(new UnauthorizedAccessException("Not seller")).when(cancelUC).cancelAuction(anyLong(), anyLong());

        mockMvc.perform(delete("/api/auctions/{id}", 1L)
                        .with(request -> {
                            request.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request;
                        }))
                .andExpect(status().isForbidden()); // UnauthorizedAccessException maps to 403

        verify(cancelUC).cancelAuction(eq(1L), eq(1L));
    }

    @Test
    void cancel_returns400_whenActionNotAllowed() throws Exception {
        doThrow(new ActionNotAllowedException("Has bids")).when(cancelUC).cancelAuction(anyLong(), anyLong());

        mockMvc.perform(delete("/api/auctions/{id}", 1L)
                        .with(request -> {
                            request.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request;
                        }))
                .andExpect(status().isBadRequest()); // ActionNotAllowedException maps to 400

        verify(cancelUC).cancelAuction(eq(1L), eq(1L));
    }

    // --- SSE Stream Tests ---

    @Test
    void stream_returnsSseContentType() throws Exception {
        Auction auction = Auction.builder().id(1L).status(AuctionStatus.OPEN).build();
        SseEmitter sseEmitter = new SseEmitter();

        when(fetchUC.fetchById(1L)).thenReturn(auction);
        when(sseRegistry.register(1L)).thenReturn(sseEmitter);

        mockMvc.perform(get("/api/auctions/{id}/stream", 1L)
                        .with(request -> {
                            request.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8"));

        verify(fetchUC).fetchById(1L);
        verify(sseRegistry).register(1L);
    }

    @Test
    void stream_returns404_whenAuctionNotFound() throws Exception {
        when(fetchUC.fetchById(anyLong())).thenThrow(new ResourceNotFoundException("Auction", "id", 99L));

        mockMvc.perform(get("/api/auctions/{id}/stream", 99L)
                        .with(request -> {
                            request.setAttribute("org.springframework.security.core.context.SecurityContext.CONTEXT", mockJwt);
                            return request;
                        }))
                .andExpect(status().isNotFound());

        verify(fetchUC).fetchById(99L);
        verify(sseRegistry, never()).register(anyLong());
    }
}
