package com.lootopia.lootopia_app.infrastructure.in.rest.mapper;

import com.lootopia.lootopia_app.domain.model.Cache;
import com.lootopia.lootopia_app.domain.model.Coordinates;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.CacheCreateRequestDto;
import org.springframework.stereotype.Component;

@Component
public class CacheMapper {
    public Cache cacheCreateRequestToCache(CacheCreateRequestDto request){
        if(request == null) return null;
        return Cache.builder()
                .huntId(request.getHuntId())
                .coordinatesGps(new Coordinates(request.getLatitude(), request.getLongitude()))
                .artefactId(null)
                .build();
    }
}
