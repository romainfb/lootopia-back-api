package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.AssociateRewardWithPlayerUseCase;
import com.lootopia.lootopia_app.application.port.in.CreateRewardUseCase;
import com.lootopia.lootopia_app.application.port.in.GetRewardsUseCase;
import com.lootopia.lootopia_app.application.port.out.RewardPersistencePort;
import com.lootopia.lootopia_app.domain.model.Reward;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService implements GetRewardsUseCase, CreateRewardUseCase, AssociateRewardWithPlayerUseCase {

    private final RewardPersistencePort rewardPersistencePort;

    @Override
    public List<Reward> getRewardsByUserId(Long id) {
        if (id == null) throw new InvalidParameterException("User ID cannot be null");
        return rewardPersistencePort.findByUserId(id);
    }

    @Override
    public List<Reward> getAllRewards() {
        return rewardPersistencePort.findAll();
    }

    @Override
    public List<Reward> getRewardsByHuntId(Long huntId) {
        if (huntId==null) throw new InvalidParameterException("Hunt ID cannot be null");
        return rewardPersistencePort.findByChasseId(huntId);
    }

    @Override
    public Reward createReward(Reward reward) {
        if (reward==null) throw new InvalidParameterException("Reward cannot be null");
        return rewardPersistencePort.save(reward);
    }

    @Override
    public Reward associateRewardWithPlayer(Long rewardId, Long playerId) {
        if (rewardId==null) throw new InvalidParameterException("Reward ID cannot be null");
        if (playerId==null) throw new InvalidParameterException("Player ID cannot be null");

        Reward reward = rewardPersistencePort.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward", "id", rewardId));

        reward.setUtilisateurId(playerId);
        return rewardPersistencePort.save(reward);
    }
}
