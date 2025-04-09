package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Cache;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.CacheCreateRequestDto;

public interface CreateCacheUseCase {
    Cache createCache(CacheCreateRequestDto request);
}
