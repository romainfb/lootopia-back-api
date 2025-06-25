package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.in.CreateRewardUseCase;
import com.lootopia.lootopia_app.application.port.in.GetRewardsUseCase;
import com.lootopia.lootopia_app.domain.model.Reward;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.RewardRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.RewardResponse;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.RewardMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/rewards")
@RequiredArgsConstructor
public class RewardAdminController {

    private final CreateRewardUseCase createRewardUseCase;
    private final GetRewardsUseCase getRewardsUseCase;
    private final RewardMapper rewardMapper;

    @Operation(
            summary = "Create a reward",
            description = "Creates a new reward with the provided details."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reward created successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RewardResponse createReward(@ModelAttribute RewardRequest request) {
        Reward reward = rewardMapper.toReward(request);
        Reward createdReward = createRewardUseCase.createReward(reward);
        return rewardMapper.toResponse(createdReward);
    }

    @Operation(
            summary = "Get all rewards",
            description = "Retrieves all rewards in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rewards retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    public List<RewardResponse> getAllRewards() {
        List<Reward> rewards = getRewardsUseCase.getAllRewards();
        return rewards.stream()
                .map(rewardMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Operation(
            summary = "Get rewards by hunt ID",
            description = "Retrieves all rewards for a specific hunt."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rewards retrieved successfully",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Invalid hunt ID",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/hunt/{huntId}")
    public List<RewardResponse> getRewardsByHuntId(@PathVariable("huntId") Long huntId) {
        List<Reward> rewards = getRewardsUseCase.getRewardsByHuntId(huntId);
        return rewards.stream()
                .map(rewardMapper::toResponse)
                .collect(Collectors.toList());
    }
}
