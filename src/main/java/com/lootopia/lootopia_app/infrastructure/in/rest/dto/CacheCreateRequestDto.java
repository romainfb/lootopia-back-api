package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CacheCreateRequestDto {
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;
    @NotNull
    private Long huntId;
}
