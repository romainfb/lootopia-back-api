package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.CreateCacheUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteCacheUseCase;
import com.lootopia.lootopia_app.application.port.in.GetAllCacheUseCase;

import com.lootopia.lootopia_app.domain.model.Cache;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.CacheCreateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/treasure")
@RequiredArgsConstructor
public class CacheAdminController {

    private final CreateCacheUseCase createCacheUseCase;
    private final DeleteCacheUseCase deleteCacheUseCase;
    private final GetAllCacheUseCase getAllCacheUseCase;

    @Operation(
            summary = "Get all treasures",
            description = "Retrieves all treasures across all hunts."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treasures retrieved successfully", content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Cache> getTreasures() {
        return getAllCacheUseCase.getAllCache();
    }

    @Operation(
            summary = "Create a treasure",
            description = "Creates a new treasure for a specific hunt based on the provided details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Treasure created successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public Cache createTreasure(@RequestBody CacheCreateRequestDto request) {
        return createCacheUseCase.createCache(request);
    }

    @Operation(
            summary = "Delete a treasure",
            description = "Deletes an existing treasure identified by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Treasure deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Treasure not found", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{treasureId}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTreasure(@PathVariable Long treasureId) {
        deleteCacheUseCase.deleteCache(treasureId);
    }
}

