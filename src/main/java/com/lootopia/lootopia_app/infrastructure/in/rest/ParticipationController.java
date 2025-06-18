package com.lootopia.lootopia_app.infrastructure.in.rest;


import com.lootopia.lootopia_app.application.port.in.CreateParticipationUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteParticipationUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchParticipationUseCase;
import com.lootopia.lootopia_app.domain.model.Participation;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.ParticipationRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.ParticipationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/participation")
@RequiredArgsConstructor
public class ParticipationController {

    private final CreateParticipationUseCase createParticipationUseCase;
    private final DeleteParticipationUseCase deleteParticipationUseCase;
    private final FetchParticipationUseCase fetchParticipationUseCase;
    private final ParticipationMapper participationMapper;

    @Operation(
            summary = "Create a new participation",
            description = "Creates a new participation for a specified hunt and user. " +
                    "Verifies if the hunt exists, if the user is already registered, " +
                    "and if the hunt's maximum capacity has not been exceeded. " +
                    "Returns the created participation."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Participation created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input or hunt not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "User already registered or hunt at full capacity",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Participation createParticipation(@RequestBody @Valid ParticipationRequestDto participationRequest) {
        log.info("create a new participation : {}", participationRequest);
        return createParticipationUseCase.createParticipation(participationRequest);
    }

    @Operation(
            summary = "Delete a participation",
            description = "Deletes an existing participation by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Participation deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Participation not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHunt(@PathVariable Long id) {
        log.info("delete participation with id : {}", id);
        deleteParticipationUseCase.deleteParticipation(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get participations for a hunt",
            description = "Retrieves all participations for a specific hunt identified by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Participations fetched successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Hunt not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/hunt/{huntId}")
    public List<Participation> getParticipationsByHunt(@PathVariable("huntId") Integer huntId) {
        log.info("Fetching participations for hunt with id {}", huntId);
        return fetchParticipationUseCase.findByHuntId(huntId);
    }

}
