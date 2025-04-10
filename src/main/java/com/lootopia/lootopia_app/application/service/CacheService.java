package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.ClearCacheUseCase;
import com.lootopia.lootopia_app.application.port.in.GetCacheUseCase;
import com.lootopia.lootopia_app.application.port.out.ArtifactPersistencePort;
import com.lootopia.lootopia_app.application.port.out.CachePersistencePort;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.domain.model.Cache;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CacheService implements GetCacheUseCase, ClearCacheUseCase {

    private final CachePersistencePort cachePersistencePort;
    private final ArtifactPersistencePort artifactPersistencePort;

    @Override
    public Cache getCacheId(Long treasureId) {
        return cachePersistencePort.findById(treasureId)
                .orElseThrow(() -> new ResourceNotFoundException("Cache", "id", treasureId));
    }

    @Override
    public Artifact clearTreasure(Long treasureId, Long userId) {
        Cache treasure = getCacheId(treasureId);
        if (treasure.getArtefactId() == null) {
            throw new ResourceNotFoundException("Artefact in cache", "id", treasureId);
        }
        Artifact artifact = artifactPersistencePort.findById(treasure.getArtefactId())
                .orElseThrow(() -> new ResourceNotFoundException("Artifact", "ArtefactId", treasure.getArtefactId()));
        artifact.setUserId(userId);
        return artifactPersistencePort.save(artifact);
    }
}