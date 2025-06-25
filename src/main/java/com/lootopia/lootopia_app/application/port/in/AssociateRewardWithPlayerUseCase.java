package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Reward;

public interface AssociateRewardWithPlayerUseCase {
    Reward associateRewardWithPlayer(Long rewardId, Long playerId);
}
