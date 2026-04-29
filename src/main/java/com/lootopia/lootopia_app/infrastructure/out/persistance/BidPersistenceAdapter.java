package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.application.port.out.BidPersistencePort;
import com.lootopia.lootopia_app.domain.model.Bid;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.BidEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.BidPersistenceMapper;
import com.lootopia.lootopia_app.infrastructure.out.persistance.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BidPersistenceAdapter implements BidPersistencePort {

    private final BidRepository repository;

    @Override
    public Bid save(Bid bid) {
        BidEntity entity = BidPersistenceMapper.toEntity(bid);
        BidEntity saved = repository.save(entity);
        return BidPersistenceMapper.toDomain(saved);
    }

    @Override
    public List<Bid> findByAuctionIdOrderByPlacedAtDesc(Long auctionId) {
        return repository.findByEnchereIdOrderByPlaceAtDesc(auctionId).stream()
                .map(BidPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}
