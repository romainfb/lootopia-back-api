package com.lootopia.lootopia_app.infrastructure.in.rest.mapper;

import com.lootopia.lootopia_app.infrastructure.in.rest.dto.HuntRequestDto;
import com.lootopia.lootopia_app.domain.model.Hunt;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.HuntUpdateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class HuntMapper {

    public Hunt huntRequestToHunt(HuntRequestDto huntRequest) {

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

    public Hunt huntUpdateRequestToHunt(HuntUpdateRequestDto updateRequest) {

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
