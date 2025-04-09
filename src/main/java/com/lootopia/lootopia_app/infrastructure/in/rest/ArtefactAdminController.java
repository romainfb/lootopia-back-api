package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.CreateArtefactUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteArtefactUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateArtefactUseCase;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.ArtifactRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.ArtifactUpdateRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.ArtifactMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/artefacts")
@RequiredArgsConstructor
public class ArtefactAdminController {

    private final CreateArtefactUseCase createArtefactUseCase;
    private final UpdateArtefactUseCase updateArtefactUseCase;
    private final DeleteArtefactUseCase deleteArtefactUseCase;
    private final ArtifactMapper artifactMapper;

    @Operation(
            summary = "Create an artefact",
            description = "Creates a new artefact with the provided details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Artefact created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Artifact createArtefact(@RequestBody ArtifactRequest request) {
        return createArtefactUseCase.createArtefact(artifactMapper.ArtifactRequestoToArtifact(request));
    }

    @Operation(
            summary = "Update an artefact",
            description = "Updates an existing artefact identified by its ID with the provided details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artefact updated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Artefact not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/{id}/update")
    @ResponseStatus(HttpStatus.OK)
    public Artifact updateArtefact(@PathVariable("id") Long id,
                                   @RequestBody ArtifactUpdateRequest request) {
        return updateArtefactUseCase.updateArtefact(artifactMapper.updateFromRequest(id, request));
    }

    @Operation(
            summary = "Delete an artefact",
            description = "Deletes an existing artefact identified by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Artefact deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Artefact not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArtefact(@PathVariable("id") Long id) {
        deleteArtefactUseCase.deleteArtefact(id);
    }
}
