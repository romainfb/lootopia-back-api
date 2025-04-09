package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Step;

import java.util.List;

public interface FetchStepUseCase {
    List<Step> getStepsByHuntId(Long huntId);
    Step getStepDetail(Long huntId, Long stepId);
}
