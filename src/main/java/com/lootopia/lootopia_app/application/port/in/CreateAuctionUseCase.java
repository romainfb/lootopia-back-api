package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Auction;

import java.time.Instant;

public interface CreateAuctionUseCase {
    Auction createAuction(Long sellerId, CreateAuctionCommand cmd);

    record CreateAuctionCommand(
            Long artifactId,
            int startPrice,
            int minIncrement,
            Instant startsAt,
            Instant endsAt
    ) {
    }
}
