package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.OpenAdChestUseCase;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.AdChestOpenRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.AdChestOpenResponse;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.AdChestStartResponse;
import com.lootopia.lootopia_app.infrastructure.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coins/ad-chest")
@RequiredArgsConstructor
public class AdChestController {

    private final OpenAdChestUseCase openAdChestUseCase;

    @Operation(summary = "Start an ad-chest session",
            description = "Returns a signed token the client must hand back to /open after watching the ad.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ad-watch token issued"),
            @ApiResponse(responseCode = "429", description = "Cooldown active"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/start")
    public ResponseEntity<AdChestStartResponse> start(@CurrentUserId Long userId) {
        OpenAdChestUseCase.StartAdChestResult result = openAdChestUseCase.startAdChest(userId);
        return ResponseEntity.ok(AdChestStartResponse.builder()
                .adWatchToken(result.adWatchToken())
                .minWatchSeconds(result.minWatchSeconds())
                .build());
    }

    @Operation(summary = "Open the ad-chest",
            description = "Validates the ad-watch token, credits a random amount of coins, returns the new balance.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chest opened"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired ad-watch token"),
            @ApiResponse(responseCode = "429", description = "Cooldown active"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/open")
    public ResponseEntity<AdChestOpenResponse> open(
            @CurrentUserId Long userId,
            @RequestBody @Valid AdChestOpenRequest request) {
        OpenAdChestUseCase.OpenAdChestResult result =
                openAdChestUseCase.openAdChest(userId, request.getAdWatchToken());
        return ResponseEntity.ok(AdChestOpenResponse.builder()
                .amount(result.amount())
                .newBalance(result.newBalance())
                .build());
    }
}
