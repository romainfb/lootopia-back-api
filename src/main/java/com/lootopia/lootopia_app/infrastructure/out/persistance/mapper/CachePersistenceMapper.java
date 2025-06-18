package com.lootopia.lootopia_app.infrastructure.out.persistance.mapper;

import com.lootopia.lootopia_app.domain.model.Cache;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.CacheEntity;

public class CachePersistenceMapper {
    public static Cache toDomain(CacheEntity entity) {
        if (entity == null) return null;
        return Cache.builder()
                .id(entity.getId())
                .huntId(entity.getHuntId())
                .coordinatesGps(entity.getCoordinatesGps())
                .artefactId(entity.getArtefactId())
                .build();
    }

    public static CacheEntity toEntity(Cache cache) {
        if (cache == null) return null;
        return CacheEntity.builder()
                .id(cache.getId())
                .huntId(cache.getHuntId())
                .coordinatesGps(cache.getCoordinatesGps())
                .artefactId(cache.getArtefactId())
                .build();
    }
}
