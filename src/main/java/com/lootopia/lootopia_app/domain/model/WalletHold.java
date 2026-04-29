package com.lootopia.lootopia_app.domain.model;

import com.lootopia.lootopia_app.domain.HoldStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletHold {
    private Long id;
    private Long userId;
    private Long auctionId;
    private Integer amount;
    private HoldStatus status;
    private Instant createdAt;
}
