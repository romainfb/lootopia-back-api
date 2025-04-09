package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.CreateCacheUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteCacheUseCase;
import com.lootopia.lootopia_app.application.port.in.GetAllCacheUseCase;
import com.lootopia.lootopia_app.application.port.out.CachePersistencePort;
import com.lootopia.lootopia_app.domain.model.Cache;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.CacheCreateRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import com.lootopia.lootopia_app.infrastructure.in.rest.mapper.CacheMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CacheAdminService implements CreateCacheUseCase, DeleteCacheUseCase, GetAllCacheUseCase {
    private final CachePersistencePort cachePersistencePort;
    private final CacheMapper cacheMapper;

    @Override
    public Cache createCache(CacheCreateRequestDto request) {
        Cache treasure = cacheMapper.cacheCreateRequestToCache(request);
        return cachePersistencePort.save(treasure);
    }

    @Override
    public void deleteCache(Long treasureId) {
        if (!cachePersistencePort.existsById(treasureId)) {
            throw new ResourceNotFoundException("Treasure","id", treasureId);
        }
        cachePersistencePort.delete(treasureId);
    }

    @Override
    public List<Cache> getAllCache() {
        return cachePersistencePort.findAll();
    }
}
