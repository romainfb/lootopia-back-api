package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.CancelAuctionUseCase;
import com.lootopia.lootopia_app.application.port.in.CreateAuctionUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchAuctionsUseCase;
import com.lootopia.lootopia_app.application.port.in.PlaceBidUseCase;
import com.lootopia.lootopia_app.domain.model.Auction;
import com.lootopia.lootopia_app.domain.model.Bid;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.AuctionResponse;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.BidResponse;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.CreateAuctionRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.PlaceBidRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.AuctionRestMapper;
import com.lootopia.lootopia_app.infrastructure.in.rest.sse.AuctionSseRegistry;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuctionController {

    private final CreateAuctionUseCase createUC;
    private final CancelAuctionUseCase cancelUC;
    private final PlaceBidUseCase placeBidUC;
    private final FetchAuctionsUseCase fetchUC;
    private final AuctionSseRegistry sseRegistry;
    private final AuctionRestMapper mapper;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public AuctionResponse create(@AuthenticationPrincipal Jwt jwt,
                                  @RequestBody @Valid CreateAuctionRequest req) {
        Long sellerId = Long.valueOf(jwt.getSubject());
        Auction created = createUC.createAuction(sellerId, mapper.toCommand(req));
        return mapper.toResponse(created);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public List<AuctionResponse> listOpen() {
        return fetchUC.fetchOpen().stream().map(mapper::toResponse).toList();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public AuctionResponse getOne(@PathVariable Long id) {
        return mapper.toResponse(fetchUC.fetchById(id));
    }

    @GetMapping(value = "/{id}/bids", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    public List<BidResponse> listBids(@PathVariable Long id) {
        return fetchUC.fetchBids(id).stream().map(mapper::toBidResponse).toList();
    }

    @PostMapping(value = "/{id}/bids", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    public BidResponse placeBid(@AuthenticationPrincipal Jwt jwt,
                                @PathVariable Long id,
                                @RequestBody @Valid PlaceBidRequest req) {
        Long bidderId = Long.valueOf(jwt.getSubject());
        Bid bid = placeBidUC.placeBid(bidderId, id, req.getAmount());
        return mapper.toBidResponse(bid);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        Long sellerId = Long.valueOf(jwt.getSubject());
        cancelUC.cancelAuction(sellerId, id);
    }

    @GetMapping(value = "/{id}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter stream(@PathVariable Long id) {
        // 404 si l'enchère n'existe pas
        fetchUC.fetchById(id);
        return sseRegistry.register(id);
    }
}
