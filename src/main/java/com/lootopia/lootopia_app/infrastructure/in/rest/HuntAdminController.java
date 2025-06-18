package com.lootopia.lootopia_app.infrastructure.in.rest;


import com.lootopia.lootopia_app.application.port.in.CreateHuntUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteHuntUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateHuntUseCase;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.HuntUpdateRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.HuntMapper;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.HuntRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/admin/hunts")
@RequiredArgsConstructor
public class HuntAdminController {

    private final CreateHuntUseCase createHuntUseCase;
    private final UpdateHuntUseCase updateHuntUseCase;
    private final DeleteHuntUseCase deleteHuntUseCase;
    private final HuntMapper huntMapper;


    @Operation(summary = "Create a new hunt", description = "Endpoint to create a new hunt.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    //TODO : Add admin role verification
    public Hunt createHunt(@RequestBody @Valid HuntRequestDto huntRequest) {
        log.info("Creating new hunt : {}", huntRequest);
        return createHuntUseCase.createHunt(huntMapper.huntRequestToHunt(huntRequest));
    }


    @Operation(summary = "Update a hunt by id", description = "Endpoint to update an existing hunt by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hunt updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Hunt.class))),
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Hunt not found",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })

    @PatchMapping("/{id}/update")
    public Hunt updateHunt(@PathVariable Long id,
                           @RequestBody @Valid HuntUpdateRequestDto updateRequest) {
        log.info("Updating hunt : {}", updateRequest);
        return updateHuntUseCase.updateHunt(id,huntMapper.huntUpdateRequestToHunt(updateRequest));
    }

    @Operation(
            summary = "Delete a hunt by id",
            description = "Endpoint to delete an existing hunt by id."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Hunt deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request - Invalid input",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Hunt not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(mediaType = "application/json")
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHunt(@PathVariable Long id) {
        deleteHuntUseCase.deleteHunt(id);
        return ResponseEntity.noContent().build();
    }

}
