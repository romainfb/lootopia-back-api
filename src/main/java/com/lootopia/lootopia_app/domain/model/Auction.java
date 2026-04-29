package com.lootopia.lootopia_app.domain.model;

import com.lootopia.lootopia_app.domain.AuctionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Auction {
    private Long id;
    private Long artifactId;
    private Long sellerId;
    private Integer startPrice;
    private Integer currentPrice;
    private Integer minIncrement;
    private Long currentWinnerId;
    private Instant startsAt;
    private Instant endsAt;
    private AuctionStatus status;
}
