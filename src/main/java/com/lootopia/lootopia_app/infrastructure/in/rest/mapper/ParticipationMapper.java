package com.lootopia.lootopia_app.infrastructure.in.rest.mapper;


import com.lootopia.lootopia_app.domain.model.Participation;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.ParticipationRequestDto;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class ParticipationMapper {

    public Participation ParticipationRequestToParticipation(ParticipationRequestDto participationRequest, Integer organizerId) {

        if (participationRequest == null) {
            return null;
        }

        return Participation.builder()
                .userId(participationRequest.getUserId())
                .huntId(participationRequest.getHuntId())
                .statut(participationRequest.getStatut())
                .organizerId(organizerId)
                .dateInscription(new Timestamp(System.currentTimeMillis()))
                .build();
    }
}
