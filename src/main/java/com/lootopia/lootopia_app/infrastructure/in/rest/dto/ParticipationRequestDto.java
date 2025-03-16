package com.lootopia.lootopia_app.infrastructure.in.rest.dto;


import com.lootopia.lootopia_app.domain.ParticipationStatut;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParticipationRequestDto {
    private Integer huntId;
    private Integer userId;
    private ParticipationStatut statut;
}
