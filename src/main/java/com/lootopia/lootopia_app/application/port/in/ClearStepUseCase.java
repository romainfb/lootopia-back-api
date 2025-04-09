package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Step;

public interface ClearStepUseCase {
    Step clearStep(Long huntId, Long stepId, Long userId);
}
