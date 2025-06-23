package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.CreateStepUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteStepUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchStepUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateStepUseCase;
import com.lootopia.lootopia_app.domain.model.Step;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.StepRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.StepMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin/hunts/{huntId}/steps")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Step Admin API",
        description = "API pour la gestion administrative des étapes")
public class StepAdminController {

    private final CreateStepUseCase createStepUseCase;
    private final UpdateStepUseCase updateStepUseCase;
    private final DeleteStepUseCase deleteStepUseCase;
    private final FetchStepUseCase fetchStepUseCase;
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

    @Operation(
            summary = "Récupérer toutes les étapes d'une chasse",
            description = "Récupère la liste de toutes les étapes avec leurs détails pour une chasse spécifique."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Étapes récupérées avec succès",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Chasse non trouvée",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Step> getAllStepsByHuntId(@PathVariable Long huntId) {
        log.info("Récupération de toutes les étapes pour la chasse avec l'ID: {}", huntId);
        return fetchStepUseCase.getStepsByHuntId(huntId);
    }

    @Operation(
            summary = "Récupérer le détail d'une étape",
            description = "Récupère les détails complets d'une étape spécifique pour une chasse donnée."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Détails de l'étape récupérés avec succès",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Étape ou chasse non trouvée",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne du serveur",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{stepId}")
    @ResponseStatus(HttpStatus.OK)
    public Step getStepDetail(
            @PathVariable Long huntId,
            @PathVariable Long stepId
    ) {
        log.info("Récupération des détails de l'étape {} pour la chasse {}", stepId, huntId);
        return fetchStepUseCase.getStepDetail(huntId, stepId);
    }
}
