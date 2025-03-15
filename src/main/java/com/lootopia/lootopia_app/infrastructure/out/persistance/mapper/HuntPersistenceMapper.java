package com.lootopia.lootopia_app.infrastructure.out.persistance.mapper;

import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.HuntEntity;

public class HuntPersistenceMapper {

    public static Hunt toModel(HuntEntity entity) {
        if (entity == null) {
            return null;
        }
        return Hunt.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .mode(entity.getMode())
                .difficulty(entity.getDifficulty())
                .participationFees(entity.getParticipationFees())
                .chatEnabled(entity.getChatEnabled())
                .organizerId(entity.getOrganizerId())
                .world(entity.getWorld())
                .duration(entity.getDuration())
                .numberOfParticipants(entity.getNumberOfParticipants())
                .build();
    }

    public static HuntEntity toEntity(Hunt model) {
        if (model == null) {
            return null;
        }
        return HuntEntity.builder()
                .id(model.getId())
                .title(model.getTitle())
                .description(model.getDescription())
                .mode(model.getMode())
                .difficulty(model.getDifficulty())
                .participationFees(model.getParticipationFees())
                .chatEnabled(model.getChatEnabled())
                .organizerId(model.getOrganizerId())
                .world(model.getWorld())
                .duration(model.getDuration())
                .numberOfParticipants(model.getNumberOfParticipants())
                .build();
    }
}
