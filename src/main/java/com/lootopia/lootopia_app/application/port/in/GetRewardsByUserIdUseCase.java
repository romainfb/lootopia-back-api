package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Reward;

import java.util.List;

public interface GetRewardsByUserIdUseCase {

    List<Reward> getRewardsByUserId(Long id);
}
