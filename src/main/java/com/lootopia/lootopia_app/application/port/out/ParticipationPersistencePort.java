package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.model.Participation;

import java.util.List;
import java.util.Optional;

public interface ParticipationPersistencePort {
    Participation saveParticipation(Participation participation);
    void deleteParticipationById(Long id);
    List<Participation> findByHuntId(Integer huntId);
    Optional<Participation> findById(Long id);
    long countByHuntId(Long huntId);
    Optional<Participation> findByHuntIdAndUserId(Long huntId, Integer userId);
}
