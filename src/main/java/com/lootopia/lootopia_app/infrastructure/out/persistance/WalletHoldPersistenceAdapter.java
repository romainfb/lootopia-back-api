package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.application.port.out.WalletHoldPersistencePort;
import com.lootopia.lootopia_app.domain.HoldStatus;
import com.lootopia.lootopia_app.domain.model.WalletHold;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.WalletHoldEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.WalletHoldPersistenceMapper;
import com.lootopia.lootopia_app.infrastructure.out.persistance.repository.WalletHoldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WalletHoldPersistenceAdapter implements WalletHoldPersistencePort {

    private final WalletHoldRepository repository;

    @Override
    public WalletHold save(WalletHold hold) {
        WalletHoldEntity entity = WalletHoldPersistenceMapper.toEntity(hold);
        WalletHoldEntity saved = repository.save(entity);
        return WalletHoldPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<WalletHold> findActiveByUserAndAuction(Long userId, Long auctionId) {
        return repository.findByUtilisateurIdAndEnchereIdAndStatut(userId, auctionId, HoldStatus.HELD)
                .map(WalletHoldPersistenceMapper::toDomain);
    }

    @Override
    public Integer sumHeldByUserId(Long userId) {
        return repository.sumHeldByUserId(userId);
    }

    @Override
    public List<WalletHold> findActiveByAuctionId(Long auctionId) {
        return repository.findByEnchereIdAndStatut(auctionId, HoldStatus.HELD).stream()
                .map(WalletHoldPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}
