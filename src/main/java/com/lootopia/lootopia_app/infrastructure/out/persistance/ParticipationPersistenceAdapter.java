package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.application.port.out.ParticipationPersistencePort;
import com.lootopia.lootopia_app.domain.model.Participation;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.ParticipationEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.ParticipationPersistenceMapper;
import com.lootopia.lootopia_app.infrastructure.out.persistance.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ParticipationPersistenceAdapter implements ParticipationPersistencePort {

    private final ParticipationRepository participationRepository;

    @Override
    public Participation saveParticipation(Participation participation) {
        ParticipationEntity entity = ParticipationPersistenceMapper.toEntity(participation);
        ParticipationEntity savedEntity = participationRepository.save(entity);
        return ParticipationPersistenceMapper.toModel(savedEntity);
    }

    @Override
    public void deleteParticipationById(Long id) {
        participationRepository.deleteById(id);
    }

    @Override
    public List<Participation> findByHuntId(Integer huntId) {
        List<ParticipationEntity> entities = participationRepository.findByHuntId(huntId);
        return entities.stream()
                .map(ParticipationPersistenceMapper::toModel)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Participation> findById(Long id) {
        return participationRepository.findById(id)
                .map(ParticipationPersistenceMapper::toModel);
    }

    @Override
    public long countByHuntId(Long huntId) {
        return participationRepository.countByHuntId(huntId);
    }

    @Override
    public Optional<Participation> findByHuntIdAndUserId(Long huntId, Integer userId) {
        return participationRepository.findByHuntIdAndUserId(huntId, userId)
                .map(ParticipationPersistenceMapper::toModel);
    }
}
