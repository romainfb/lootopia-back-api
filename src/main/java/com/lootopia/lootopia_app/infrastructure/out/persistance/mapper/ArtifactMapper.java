package com.lootopia.lootopia_app.infrastructure.out.persistance.mapper;

import com.lootopia.lootopia_app.domain.ArtifactRarity;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.ArtifactEntity;

public class ArtifactMapper {
    public static Artifact toDomain(ArtifactEntity entity) {
        return Artifact.builder()
                .id(entity.getId())
                .title(entity.getNom())
                .rarity(ArtifactRarity.valueOf(entity.getRarete()))
                .description(entity.getDescription())
                .imageUrl(entity.getImage())
                .userId(entity.getUserId())
                .build();


    }

    public static ArtifactEntity toEntity(Artifact domain) {
        return ArtifactEntity.builder()
                .id(domain.getId())
                .nom(domain.getTitle())
                .rarete(domain.getRarity().name())
                .description(domain.getDescription())
                .image(domain.getImageUrl())
                .userId(domain.getUserId())
                .build();

    }
}
