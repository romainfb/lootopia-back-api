package com.lootopia.lootopia_app.application.service.event;

import com.lootopia.lootopia_app.domain.model.Auction;
import com.lootopia.lootopia_app.domain.model.Bid;

public record BidPlacedEvent(Long auctionId, Bid bid, Auction auction, boolean extended) {
}
