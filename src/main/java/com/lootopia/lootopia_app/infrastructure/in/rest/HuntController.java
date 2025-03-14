package com.lootopia.lootopia_app.infrastructure.in.rest;


import com.lootopia.lootopia_app.application.port.in.CreateHuntUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteHuntUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateHuntUseCase;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.HuntUpdateRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.HuntMapper;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.HuntRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
public class HuntController {

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
    @PostMapping()
    //TODO : Add admin role verification
    public Hunt createHunt(@RequestBody @Valid HuntRequest huntRequest) {
        log.info("Creating new hunt : {}", huntRequest);
        return createHuntUseCase.createHunt(huntMapper.huntRequestToHunt(huntRequest));
    }


    @Operation(summary = "Create a new hunt", description = "Endpoint to create a new hunt.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Bad Request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/{id}/update")
    public Hunt updateHunt(@PathVariable Long id,
                           @RequestBody @Valid HuntUpdateRequest updateRequest) {
        log.info("Updating hunt : {}", updateRequest);
        return updateHuntUseCase.updateHunt(id,huntMapper.huntUpdateRequestToHunt(updateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHunt(@PathVariable Long id) {
        deleteHuntUseCase.deleteHunt(id);
        return ResponseEntity.noContent().build();
    }

}
