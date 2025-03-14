package com.lootopia.lootopia_app.infrastructure.out.persistance.mapper;

import com.lootopia.lootopia_app.domain.model.Reward;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.RewardEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;

public class RewardMapper {
    public static Reward toDomain(RewardEntity entity) {
        return Reward.builder()
                .id(entity.getId())
                .chasseId(entity.getChasseId())
                .utilisateurId(entity.getUtilisateurId())
                .type(entity.getType())
                .valeur(entity.getValeur())
                .description(entity.getDescription())
                .build();

    }

    public static RewardEntity toEntity(Reward domain) {
        return RewardEntity.builder()
                .id(domain.getId())
                .chasseId(domain.getChasseId())
                .utilisateurId(domain.getUtilisateurId())
                .type(domain.getType())
                .valeur(domain.getValeur())
                .description(domain.getDescription())
                .build();
    }
}
