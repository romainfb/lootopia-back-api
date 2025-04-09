package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.CreateStepUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateStepUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteStepUseCase;
import com.lootopia.lootopia_app.domain.model.Step;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.StepRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/hunts/{huntId}/steps")
@RequiredArgsConstructor
public class StepAdminController {

    private final CreateStepUseCase createStepUseCase;
    private final UpdateStepUseCase updateStepUseCase;
    private final DeleteStepUseCase deleteStepUseCase;

    @PostMapping("/create")
    public Step createStep(
            @PathVariable Long huntId,
            @RequestBody StepRequest request
    ) {
        log.info("babab");
        return createStepUseCase.createStep(huntId, request);
    }

    @PatchMapping("/{stepId}/update")
    public Step updateStep(
            @PathVariable Long huntId,
            @PathVariable Long stepId,
            @RequestBody StepRequest request
    ) {
        return updateStepUseCase.updateStep(huntId, stepId, request);
    }

    @DeleteMapping("/{stepId}/delete")
    public void deleteStep(
            @PathVariable Long huntId,
            @PathVariable Long stepId
    ) {
        deleteStepUseCase.deleteStep(huntId, stepId);
    }
}
