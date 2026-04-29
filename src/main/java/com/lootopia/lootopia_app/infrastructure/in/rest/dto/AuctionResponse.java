package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AuctionResponse {
    private Long id;
    private Long artifactId;
    private Long sellerId;
    private String sellerUsername;
    private Integer startPrice;
    private Integer currentPrice;
    private Integer minIncrement;
    private Long currentWinnerId;
    private String currentWinnerUsername;
    private Instant startsAt;
    private Instant endsAt;
    private String status;
}
