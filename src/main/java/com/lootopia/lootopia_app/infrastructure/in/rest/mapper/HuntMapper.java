package com.lootopia.lootopia_app.infrastructure.in.rest.mapper;

import com.lootopia.lootopia_app.infrastructure.in.rest.dto.HuntRequest;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.HuntUpdateRequest;
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

    public Hunt huntUpdateRequestToHunt(HuntUpdateRequest updateRequest) {

        if(updateRequest == null) {
            return null;
        }

        return Hunt.builder()
                .title(updateRequest.getTitle())
                .description(updateRequest.getDescription())
                .mode(updateRequest.getMode())
                .difficulty(updateRequest.getDifficulty())
                .participationFees(updateRequest.getParticipationFees())
                .chatEnabled(updateRequest.getChatEnabled())
                .organizerId(updateRequest.getOrganizerId())
                .build();
    }
}
