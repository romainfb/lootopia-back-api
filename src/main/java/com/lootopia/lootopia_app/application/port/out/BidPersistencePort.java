package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.model.Bid;

import java.util.List;

public interface BidPersistencePort {
    Bid save(Bid bid);

    List<Bid> findByAuctionIdOrderByPlacedAtDesc(Long auctionId);
}
