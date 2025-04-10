package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Cache;

public interface GetCacheUseCase {
    Cache getCacheId(Long treasureId);
}
