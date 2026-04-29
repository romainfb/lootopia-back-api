package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.application.port.out.AuctionPersistencePort;
import com.lootopia.lootopia_app.domain.AuctionStatus;
import com.lootopia.lootopia_app.domain.model.Auction;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.AuctionEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.AuctionPersistenceMapper;
import com.lootopia.lootopia_app.infrastructure.out.persistance.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuctionPersistenceAdapter implements AuctionPersistencePort {

    private final AuctionRepository repository;

    @Override
    public Auction save(Auction auction) {
        AuctionEntity entity = AuctionPersistenceMapper.toEntity(auction);
        AuctionEntity saved = repository.save(entity);
        return AuctionPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Auction> findById(Long id) {
        return repository.findById(id).map(AuctionPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Auction> findByIdForUpdate(Long id) {
        return repository.findByIdForUpdate(id).map(AuctionPersistenceMapper::toDomain);
    }

    @Override
    public List<Auction> findAll() {
        return repository.findAll().stream()
                .map(AuctionPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Auction> findAllByStatus(AuctionStatus status) {
        return repository.findByStatut(status).stream()
                .map(AuctionPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Auction> findOpenEndedBefore(Instant moment) {
        return repository.findByStatutAndFinAtBefore(AuctionStatus.OPEN, moment).stream()
                .map(AuctionPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsActiveByArtifactId(Long artifactId) {
        return repository.existsActiveByArtefactId(artifactId);
    }
}
