package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Cache;

import java.util.List;

public interface FetchCacheUseCase {
    List<Cache> getAllTreasures();
    Cache getCacheId(Long treasureId);
}
