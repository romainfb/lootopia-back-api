package com.lootopia.lootopia_app.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Bid {
    private Long id;
    private Long auctionId;
    private Long bidderId;
    private Integer amount;
    private Instant placedAt;
}
