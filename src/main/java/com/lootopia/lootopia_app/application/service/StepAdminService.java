package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.CreateStepUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateStepUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteStepUseCase;
import com.lootopia.lootopia_app.application.port.out.StepPersistencePort;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import com.lootopia.lootopia_app.domain.model.Step;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.StepRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StepAdminService implements CreateStepUseCase, UpdateStepUseCase, DeleteStepUseCase {

    private final StepPersistencePort stepPersistencePort;

    @Override
    public Step createStep(Step step) {
        return stepPersistencePort.save(step);
    }

    @Override
    public Step updateStep(Long huntId, Long stepId, StepRequest request) {
        Step step = stepPersistencePort.findByIdAndHuntId(stepId, huntId)
                .orElseThrow(() -> new ResourceNotFoundException("Step","Hunt",huntId));

        step.setDescription(request.getDescription());

        return stepPersistencePort.save(step);
    }

    @Override
    public void deleteStep(Long huntId, Long stepId) {
        Optional<Step> stepOptional = stepPersistencePort.findByIdAndHuntId(stepId, huntId);
        if (stepOptional.isEmpty()) {
            throw new ResourceNotFoundException("Step", "Hunt", huntId);
        }
        stepPersistencePort.delete(stepId);
    }
}
