package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.application.port.out.CachePersistencePort;
import com.lootopia.lootopia_app.domain.model.Cache;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.CacheEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.CachePersistenceMapper;
import com.lootopia.lootopia_app.infrastructure.out.persistance.repository.CacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CachePersistenceAdapter implements CachePersistencePort {

    private final CacheRepository cacheRepository;

    @Override
    public Cache save(Cache cache) {
        CacheEntity entity = CachePersistenceMapper.toEntity(cache);
        CacheEntity savedEntity = cacheRepository.save(entity);
        return CachePersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Cache> findById(Long treasureId) {
        return cacheRepository.findById(treasureId)
                .map(CachePersistenceMapper::toDomain);
    }

    @Override
    public boolean existsById(Long treasureId) {
        return cacheRepository.existsById(treasureId);
    }

    @Override
    public void delete(Long treasureId) {
        cacheRepository.deleteById(treasureId);
    }

    @Override
    public List<Cache> findAll() {
        return cacheRepository.findAll().stream()
                .map(CachePersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }
}
