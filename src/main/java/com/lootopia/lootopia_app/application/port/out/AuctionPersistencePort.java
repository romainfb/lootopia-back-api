package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.AuctionStatus;
import com.lootopia.lootopia_app.domain.model.Auction;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AuctionPersistencePort {
    Auction save(Auction auction);

    Optional<Auction> findById(Long id);

    Optional<Auction> findByIdForUpdate(Long id);

    List<Auction> findAll();

    List<Auction> findAllByStatus(AuctionStatus status);

    List<Auction> findOpenEndedBefore(Instant moment);

    boolean existsActiveByArtifactId(Long artifactId);
}
