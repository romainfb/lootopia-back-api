package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.application.port.out.RewardPersistencePort;
import com.lootopia.lootopia_app.domain.model.Reward;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.RewardEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.RewardMapper;
import com.lootopia.lootopia_app.infrastructure.out.persistance.repository.RewardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.RewardMapper.toDomain;
import static com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.RewardMapper.toEntity;

@Component
@RequiredArgsConstructor
public class RewardPersistenceAdapter implements RewardPersistencePort {

    private final RewardRepository rewardRepository;

    @Override
    public List<Reward> findByUserId(Long id) {
        List<RewardEntity> rewardEntities = rewardRepository.findByUtilisateurId(id);
        if (rewardEntities == null || rewardEntities.isEmpty()) {
            return new ArrayList<>();
        }
        return rewardEntities.stream()
                .map(RewardMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Reward save(Reward reward) {
        RewardEntity rewardEntity = toEntity(reward);
        RewardEntity savedEntity = rewardRepository.save(rewardEntity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<Reward> findById(Long id) {
        return rewardRepository.findById(id)
                .map(RewardMapper::toDomain);
    }

    @Override
    public List<Reward> findAll() {
        List<RewardEntity> rewardEntities = rewardRepository.findAll();
        if (rewardEntities==null || rewardEntities.isEmpty()) {
            return new ArrayList<>();
        }
        return rewardEntities.stream()
                .map(RewardMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reward> findByChasseId(Long chasseId) {
        List<RewardEntity> rewardEntities = rewardRepository.findByChasseId(chasseId);
        if (rewardEntities==null || rewardEntities.isEmpty()) {
            return new ArrayList<>();
        }
        return rewardEntities.stream()
                .map(RewardMapper::toDomain)
                .collect(Collectors.toList());
    }
}
