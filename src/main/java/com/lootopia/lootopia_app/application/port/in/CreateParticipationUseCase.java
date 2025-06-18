package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Participation;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.ParticipationRequestDto;

public interface CreateParticipationUseCase {
    Participation createParticipation(ParticipationRequestDto participationRequestDto);
}
