package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdChestOpenRequest {

    @NotBlank
    private String adWatchToken;
}
