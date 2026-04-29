package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BidResponse {
    private Long id;
    private Long auctionId;
    private Long bidderId;
    private String bidderUsername;
    private Integer amount;
    private Instant placedAt;
}
