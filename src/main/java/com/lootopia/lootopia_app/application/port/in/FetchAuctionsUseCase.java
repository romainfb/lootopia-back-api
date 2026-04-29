package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Auction;
import com.lootopia.lootopia_app.domain.model.Bid;

import java.util.List;

public interface FetchAuctionsUseCase {
    List<Auction> fetchOpen();

    Auction fetchById(Long id);

    List<Bid> fetchBids(Long auctionId);
}
