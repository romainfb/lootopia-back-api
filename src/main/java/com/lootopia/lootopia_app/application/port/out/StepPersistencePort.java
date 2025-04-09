package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.model.Step;

import java.util.List;
import java.util.Optional;

public interface StepPersistencePort {
    List<Step> findByHuntId(Long huntId);
    Optional<Step> findByIdAndHuntId(Long stepId, Long huntId);
    Step save(Step step);
    boolean existsByIdAndHuntId(Long stepId, Long huntId);
    void delete(Long stepId);
}
