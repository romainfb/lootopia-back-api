package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.model.Reward;

import java.util.List;
import java.util.Optional;

public interface RewardPersistencePort {
    List<Reward> findByUserId(Long id);

    Reward save(Reward reward);

    Optional<Reward> findById(Long id);

    List<Reward> findAll();

    List<Reward> findByChasseId(Long chasseId);
}
