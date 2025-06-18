package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.application.port.out.StepPersistencePort;
import com.lootopia.lootopia_app.domain.model.Step;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.StepEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.StepMapper;
import com.lootopia.lootopia_app.infrastructure.out.persistance.repository.StepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StepPersistenceAdapter implements StepPersistencePort {

    private final StepRepository stepRepository;
    private final StepMapper stepMapper;

    @Override
    public List<Step> findByHuntId(Long huntId) {
        return stepRepository.findByHuntId(huntId)
                .stream()
                .map(stepMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Step> findByIdAndHuntId(Long stepId, Long huntId) {
        return stepRepository.findByIdAndHuntId(stepId, huntId)
                .map(stepMapper::toDomain);
    }

    @Override
    public Step save(Step step) {
        StepEntity entity = stepMapper.toEntity(step);
        StepEntity saved = stepRepository.save(entity);
        return stepMapper.toDomain(saved);
    }

    @Override
    public boolean existsByIdAndHuntId(Long stepId, Long huntId) {
        return stepRepository.existsByIdAndHuntId(stepId, huntId);
    }

    @Override
    public void delete(Long stepId) {
        stepRepository.deleteById(stepId);
    }
}
