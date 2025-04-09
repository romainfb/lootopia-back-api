package com.lootopia.lootopia_app.domain.model;

import com.lootopia.lootopia_app.domain.ArtifactRarity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Artifact {
    private Long id;
    private String title;
    private ArtifactRarity rarity;
    private Long cacheId;
    private String description;
    private String imageUrl;
    private Long userId;
}
