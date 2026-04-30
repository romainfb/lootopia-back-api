package com.lootopia.lootopia_app.infrastructure.in.web.dto;

import com.lootopia.lootopia_app.domain.ArtifactRarity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArtifactWebFormRequest {

    @NotNull
    private String title;

    @NotNull
    private ArtifactRarity rarity;

    private String description;

    @NotNull
    private Long cacheId;

    @NotNull
    private Long userId;
}
