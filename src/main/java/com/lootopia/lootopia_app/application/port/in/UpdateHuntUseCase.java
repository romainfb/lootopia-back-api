package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Hunt;

public interface UpdateHuntUseCase {
    Hunt updateHunt(Long id, Hunt hunt);
}
