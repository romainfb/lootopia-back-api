package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.FetchStepUseCase;
import com.lootopia.lootopia_app.application.port.in.ClearStepUseCase;
import com.lootopia.lootopia_app.domain.model.Step;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.ClearStepRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hunts")
@RequiredArgsConstructor
public class StepController {

    private final FetchStepUseCase fetchStepUseCase;
    private final ClearStepUseCase clearStepUseCase;

    @GetMapping("/{huntId}/steps")
    public List<Step> getAllSteps(@PathVariable Long huntId) {
        return fetchStepUseCase.getStepsByHuntId(huntId);
    }

    @GetMapping("{huntId}/steps/{stepId}")
    public Step getStepDetail(@PathVariable Long huntId, @PathVariable Long stepId) {
        return fetchStepUseCase.getStepDetail(huntId, stepId);
    }

    @PostMapping("{huntId}/steps/{stepId}/clear")
    public Step clearStep(@PathVariable Long huntId,
                          @PathVariable Long stepId,
                          @RequestBody ClearStepRequest request) {
        return clearStepUseCase.clearStep(huntId, stepId, request.getUserId());
    }
}
