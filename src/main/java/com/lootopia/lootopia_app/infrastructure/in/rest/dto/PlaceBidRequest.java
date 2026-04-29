package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PlaceBidRequest {
    @NotNull
    @Positive
    private Integer amount;
}
