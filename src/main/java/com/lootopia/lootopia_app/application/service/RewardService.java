package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.GetRewardsUseCase;
import com.lootopia.lootopia_app.application.port.out.RewardPersistencePort;
import com.lootopia.lootopia_app.domain.model.Reward;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService implements GetRewardsUseCase {

    private final RewardPersistencePort rewardPersistencePort;

    @Override
    public List<Reward> getRewardsByUserId(Long id) {
        if (id == null) throw new InvalidParameterException("User ID cannot be null");
        return rewardPersistencePort.findByUserId(id);
    }
}
