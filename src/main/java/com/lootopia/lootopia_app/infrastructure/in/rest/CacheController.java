package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.ClearCacheUseCase;
import com.lootopia.lootopia_app.application.port.in.GetCacheUseCase;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.domain.model.Cache;
import com.lootopia.lootopia_app.infrastructure.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/treasure")
@RequiredArgsConstructor
public class CacheController {

    private final GetCacheUseCase getTreasureUseCase;
    private final ClearCacheUseCase clearTreasureUseCase;

    @Operation(
            summary = "Get treasure coordinates",
            description = "Returns the coordinates of a treasure (cache) by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Treasure found successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Treasure not found", content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{treasureId}")
    @ResponseStatus(HttpStatus.OK)
    public Cache getTreasure(@PathVariable Long treasureId) {
        return getTreasureUseCase.getCacheId(treasureId);
    }

    @Operation(
            summary = "Clear a treasure",
            description = "Clears a treasure by assigning its associated artifact to the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Artifact assigned successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Treasure or artifact not found", content = @Content(mediaType = "application/json"))
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{treasureId}/clear")
    @ResponseStatus(HttpStatus.OK)
    public Artifact clearTreasure(@PathVariable Long treasureId,
                                  @CurrentUserId Long userId) {
        return clearTreasureUseCase.clearTreasure(treasureId, userId);
    }
}
