package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.model.Cache;

import java.util.List;
import java.util.Optional;

public interface CachePersistencePort {
    Cache save(Cache cache);
    Optional<Cache> findById(Long treasureId);
    boolean existsById(Long treasureId);
    void delete(Long treasureId);
    List<Cache> findAll();
}
