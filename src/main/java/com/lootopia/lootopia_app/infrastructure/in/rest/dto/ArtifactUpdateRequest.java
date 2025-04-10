package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import com.lootopia.lootopia_app.domain.ArtifactRarity;
import lombok.Data;

@Data
public class ArtifactUpdateRequest {
    private String title;
    private ArtifactRarity rarity;
    private String description;
    private String imageUrl;
    private Long cacheId;
    private Long userId;
}
