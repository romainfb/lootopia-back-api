package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateAuctionRequest {
    @NotNull
    private Long artifactId;
    @NotNull
    @Positive
    private Integer startPrice;
    @NotNull
    @Positive
    private Integer minIncrement;
    @NotNull
    private Instant startsAt;
    @NotNull
    @Future
    private Instant endsAt;
}
