package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.CreateStepUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteStepUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateStepUseCase;
import com.lootopia.lootopia_app.domain.model.Step;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.StepRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.StepMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/admin/hunts/{huntId}/steps")
@RequiredArgsConstructor
public class StepAdminController {

    private final CreateStepUseCase createStepUseCase;
    private final UpdateStepUseCase updateStepUseCase;
    private final DeleteStepUseCase deleteStepUseCase;
    private final StepMapper stepMapper;

    @Operation(
            summary = "Create a new step",
            description = "Creates a new step for the specified hunt with the provided details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Step created successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Step createStep(
            @PathVariable Long huntId,
            @RequestBody StepRequest request
    ) {
        log.info("Creating a new step for huntId: {}", huntId);
        return createStepUseCase.createStep(stepMapper.StepRequestToStep(request, huntId));
    }

    @Operation(
            summary = "Update an existing step",
            description = "Updates the details of a specific step for the specified hunt."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Step updated successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Step not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/{stepId}/update")
    @ResponseStatus(HttpStatus.OK)
    public Step updateStep(
            @PathVariable Long huntId,
            @PathVariable Long stepId,
            @RequestBody StepRequest request
    ) {
        return updateStepUseCase.updateStep(huntId, stepId, request);
    }

    @Operation(
            summary = "Delete an existing step",
            description = "Deletes a specific step for the specified hunt."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Step deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Step not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{stepId}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteStep(
            @PathVariable Long huntId,
            @PathVariable Long stepId
    ) {
        deleteStepUseCase.deleteStep(huntId, stepId);
    }
}
