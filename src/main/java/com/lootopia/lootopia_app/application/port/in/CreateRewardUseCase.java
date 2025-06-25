package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Reward;

public interface CreateRewardUseCase {
    Reward createReward(Reward reward);
}
