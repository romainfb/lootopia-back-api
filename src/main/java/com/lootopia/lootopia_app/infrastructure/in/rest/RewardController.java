package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.AssociateRewardWithPlayerUseCase;
import com.lootopia.lootopia_app.application.port.in.GetRewardsUseCase;
import com.lootopia.lootopia_app.domain.model.Reward;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.RewardResponse;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.RewardMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final GetRewardsUseCase getRewardsUseCase;
    private final AssociateRewardWithPlayerUseCase associateRewardWithPlayerUseCase;
    private final RewardMapper rewardMapper;

    @Operation(
            summary = "Get rewards by user ID",
            description = "Returns all rewards associated with the specified user ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rewards retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid user ID",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public List<RewardResponse> getRewardsByUserId(@PathVariable("userId") Long userId) {
        List<Reward> rewards = getRewardsUseCase.getRewardsByUserId(userId);
        return rewards.stream()
                .map(rewardMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(
            summary = "Associate a reward with a player",
            description = "Associates the specified reward with the specified player."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reward associated successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Reward not found",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PatchMapping("/{rewardId}/player/{playerId}")
    @ResponseStatus(HttpStatus.OK)
    public RewardResponse associateRewardWithPlayer(
            @PathVariable("rewardId") Long rewardId,
            @PathVariable("playerId") Long playerId) {
        Reward reward = associateRewardWithPlayerUseCase.associateRewardWithPlayer(rewardId, playerId);
        return rewardMapper.toResponse(reward);
    }
}
