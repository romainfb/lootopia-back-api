package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.GetRewardsByUserIdUseCase;
import com.lootopia.lootopia_app.application.port.out.RewardPersistencePort;
import com.lootopia.lootopia_app.domain.model.Reward;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RewardService implements GetRewardsByUserIdUseCase {

    private final RewardPersistencePort rewardPersistencePort;

    @Override
    public List<Reward> getRewardsByUserId(Long id) {
        return rewardPersistencePort.findByUserId(id);
    }

}
