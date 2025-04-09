package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Step;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.StepRequest;

public interface UpdateStepUseCase {
    Step updateStep(Long huntId, Long stepId, StepRequest request);
}