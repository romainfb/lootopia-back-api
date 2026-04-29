package com.lootopia.lootopia_app.application.service.event;

import com.lootopia.lootopia_app.domain.model.Auction;

public record AuctionClosedEvent(Long auctionId, Auction auction, Long winnerId, Integer winningAmount) {
}
