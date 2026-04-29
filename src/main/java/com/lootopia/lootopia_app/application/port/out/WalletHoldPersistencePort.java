package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.model.WalletHold;

import java.util.List;
import java.util.Optional;

public interface WalletHoldPersistencePort {
    WalletHold save(WalletHold hold);

    Optional<WalletHold> findActiveByUserAndAuction(Long userId, Long auctionId);

    Integer sumHeldByUserId(Long userId);

    List<WalletHold> findActiveByAuctionId(Long auctionId);
}
