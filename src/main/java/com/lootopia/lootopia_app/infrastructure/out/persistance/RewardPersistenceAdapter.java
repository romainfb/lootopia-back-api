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
}
