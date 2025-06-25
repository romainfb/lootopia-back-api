package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Reward;

import java.util.List;

public interface GetRewardsUseCase {

    List<Reward> getRewardsByUserId(Long id);

    List<Reward> getAllRewards();

    List<Reward> getRewardsByHuntId(Long huntId);
}
