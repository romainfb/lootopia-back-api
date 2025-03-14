package com.lootopia.lootopia_app.infrastructure.out.persistance.mapper;

import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.domain.model.Reward;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.ArtifactEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.RewardEntity;

public class ArtifactMapper {
    public static Artifact toDomain(ArtifactEntity entity) {
        return Artifact.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .rarete(entity.getRarete())
                .description(entity.getDescription())
                .image(entity.getImage())
                .user(UserMapper.toDomain(entity.getUser()))
                .build();


    }

    public static ArtifactEntity toEntity(Artifact domain) {
        return ArtifactEntity.builder()
                .id(domain.getId())
                .nom(domain.getNom())
                .rarete(domain.getRarete())
                .description(domain.getDescription())
                .image(domain.getImage())
                .user(UserMapper.toEntity(domain.getUser()))
                .build();

    }
}
