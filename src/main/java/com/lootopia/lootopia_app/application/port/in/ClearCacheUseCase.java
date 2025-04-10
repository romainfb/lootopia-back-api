package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Artifact;

public interface ClearCacheUseCase {
    Artifact clearTreasure(Long treasureId, Long userId);
}
