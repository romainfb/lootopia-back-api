package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.model.Reward;

import java.util.List;

public interface RewardPersistencePort {
    List<Reward> findByUserId(Long id);
}
