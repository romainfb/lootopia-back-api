package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.ClearStepUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchStepUseCase;
import com.lootopia.lootopia_app.application.port.out.StepPersistencePort;
import com.lootopia.lootopia_app.domain.model.Step;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class StepService implements FetchStepUseCase, ClearStepUseCase {
    private final StepPersistencePort stepPersistencePort;

    @Override
    public List<Step> getStepsByHuntId(Long huntId) {
        return stepPersistencePort.findByHuntId(huntId);
    }

    @Override
    public Step getStepDetail(Long huntId, Long stepId) {
        return stepPersistencePort.findByIdAndHuntId(stepId, huntId)
                .orElseThrow(() -> new ResourceNotFoundException("Step", "stepid", stepId));
    }

    @Override
    public Step clearStep(Long huntId, Long stepId, Long userId) {
        Step step = stepPersistencePort.findByIdAndHuntId(stepId, huntId)
                .orElseThrow(() -> new ResourceNotFoundException("Step", "Hunt", huntId));

        if (!step.getValidatedUsersId().contains(userId)) {
            step.getValidatedUsersId().add(userId);
            step = stepPersistencePort.save(step);
        }
        return step;
    }
}
