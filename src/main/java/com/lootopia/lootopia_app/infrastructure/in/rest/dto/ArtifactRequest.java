package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import com.lootopia.lootopia_app.domain.ArtifactRarity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ArtifactRequest {

    @NotNull
    private String title;

    @NotNull
    private ArtifactRarity rarity;

    private String description;

    @NotNull
    private String imageUrl;

    @NotNull
    private Long cacheId;

    @NotNull
    private Long userId;
}
