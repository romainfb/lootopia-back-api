package com.lootopia.lootopia_app.infrastructure.in.rest.mapper;

import com.lootopia.lootopia_app.infrastructure.in.rest.dto.HuntRequest;
import com.lootopia.lootopia_app.domain.model.Hunt;
import org.springframework.stereotype.Component;

@Component
public class HuntMapper {

    public Hunt huntRequestToHunt(HuntRequest huntRequest) {

        if(huntRequest == null) {
            return null;
        }

        return Hunt.builder()
                .title(huntRequest.getTitle())
                .description(huntRequest.getDescription())
                .mode(huntRequest.getMode())
                .difficulty(huntRequest.getDifficulty())
                .participationFees(huntRequest.getParticipationFees())
                .chatEnabled(huntRequest.getChatEnabled())
                .organizerId(huntRequest.getOrganizerId())
                .build();
    }
}
