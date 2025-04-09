package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CacheClearRequest {
    @NotNull
    private Long userId;
}
