package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Bid;

public interface PlaceBidUseCase {
    Bid placeBid(Long bidderId, Long auctionId, int amount);
}
