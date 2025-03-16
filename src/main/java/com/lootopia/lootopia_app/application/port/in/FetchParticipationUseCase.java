package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Participation;

import java.util.List;

public interface FetchParticipationUseCase {
    List<Participation> findByHuntId(Integer huntId);
}
