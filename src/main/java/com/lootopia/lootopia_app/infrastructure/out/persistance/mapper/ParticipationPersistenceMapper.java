package com.lootopia.lootopia_app.infrastructure.out.persistance.mapper;

import com.lootopia.lootopia_app.domain.model.Participation;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.ParticipationEntity;

public class ParticipationPersistenceMapper {

    public static Participation toModel(ParticipationEntity entity) {
        if (entity == null) {
            return null;
        }
        return Participation.builder()
                .id(entity.getId())
                .statut(entity.getStatut())
                .dateInscription(entity.getDateInscription())
                .organizerId(entity.getOrganizerId())
                .userId(entity.getUserId())
                .huntId(entity.getHuntId())
                .build();
    }

    public static ParticipationEntity toEntity(Participation model) {
        if (model == null) {
            return null;
        }
        return ParticipationEntity.builder()
                .id(model.getId())
                .statut(model.getStatut())
                .dateInscription(model.getDateInscription())
                .organizerId(model.getOrganizerId())
                .userId(model.getUserId())
                .huntId(model.getHuntId())
                .build();
    }
}
