package com.lootopia.lootopia_app.infrastructure.in.rest.mapper;

import com.lootopia.lootopia_app.application.port.out.ArtifactPersistencePort;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.ArtifactRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.ArtifactUpdateRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ArtifactMapper {

    private final ArtifactPersistencePort artifactPersistencePort;

    public Artifact ArtifactRequestoToArtifact(ArtifactRequest request) {
        if(request == null) return null;
        return Artifact.builder()
                .title(request.getTitle())
                .rarity(request.getRarity())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .cacheId(request.getCacheId())
                .userId(request.getUserId())
                .build();
    }

    public Artifact updateFromRequest(Long id, ArtifactUpdateRequest request) {

        final var artefact = artifactPersistencePort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Artefact","id", id));

        if (request.getTitle() != null) {
            artefact.setTitle(request.getTitle());
        }
        if (request.getRarity() != null) {
            artefact.setRarity(request.getRarity());
        }
        if (request.getDescription() != null) {
            artefact.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            artefact.setImageUrl(request.getImageUrl());
        }
        if (request.getCacheId() != null) {
            artefact.setCacheId(request.getCacheId());
        }
        if (request.getUserId() != null) {
            artefact.setUserId(request.getUserId());
        }
        return artefact;
    }
}
