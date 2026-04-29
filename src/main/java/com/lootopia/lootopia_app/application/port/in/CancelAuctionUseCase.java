package com.lootopia.lootopia_app.application.port.in;

public interface CancelAuctionUseCase {
    void cancelAuction(Long sellerId, Long auctionId);
}
