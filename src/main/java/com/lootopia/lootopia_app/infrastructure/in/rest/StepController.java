package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.ClearStepUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchStepUseCase;
import com.lootopia.lootopia_app.domain.model.Step;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.ClearStepRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hunts/{huntId}/steps")
@RequiredArgsConstructor
public class StepController {

    private final FetchStepUseCase fetchStepUseCase;
    private final ClearStepUseCase clearStepUseCase;

    @Operation(
            summary = "Retrieve all steps",
            description = "Récupère toutes les étapes d'une chasse identifiée par son ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Étapes récupérées avec succès", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Chasse non trouvée", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Step> getAllSteps(@PathVariable Long huntId) {
        return fetchStepUseCase.getStepsByHuntId(huntId);
    }

    @Operation(
            summary = "Retrieve step detail",
            description = "Récupère le détail d'une étape spécifique d'une chasse."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Détail de l'étape récupéré avec succès", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Étape non trouvée", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{stepId}")
    @ResponseStatus(HttpStatus.OK)
    public Step getStepDetail(@PathVariable Long huntId, @PathVariable Long stepId) {
        return fetchStepUseCase.getStepDetail(huntId, stepId);
    }

    @Operation(
            summary = "Clear a step",
            description = "Valide une étape en ajoutant l'ID de l'utilisateur dans le set des validations de l'étape."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Étape validée avec succès", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Étape non trouvée", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Erreur interne", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/{stepId}/clear")
    @ResponseStatus(HttpStatus.OK)
    public Step clearStep(@PathVariable Long huntId,
                          @PathVariable Long stepId,
                          @RequestBody ClearStepRequest request) {
        return clearStepUseCase.clearStep(huntId, stepId, request.getUserId());
    }
}
