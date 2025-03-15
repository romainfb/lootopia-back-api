package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.application.port.out.ParticipationPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ParticipationPersistenceAdapter implements ParticipationPersistencePort {

    private final ParticipationRepository participationRepository;

    @Override
    public long countByHuntId(Long huntId) {return participationRepository.countByHuntId(huntId); }
}