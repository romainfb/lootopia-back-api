package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.CreateArtefactUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteArtefactUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateArtefactUseCase;
import com.lootopia.lootopia_app.application.port.out.ArtifactPersistencePort;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.ArtifactRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtefactAdminService implements CreateArtefactUseCase, UpdateArtefactUseCase, DeleteArtefactUseCase {

    private final ArtifactPersistencePort artefactPersistencePort;

    @Override
    public Artifact createArtefact(Artifact artifact) {
        return artefactPersistencePort.save(artifact);
    }

    @Override
    public Artifact updateArtefact(Artifact artifactUpdated) {
        return artefactPersistencePort.save(artifactUpdated);
    }


    @Override
    public void deleteArtefact(Long id) {
        if (!artefactPersistencePort.existsById(id)) {
            throw new ResourceNotFoundException("Artefact","artefact", id);
        }
        artefactPersistencePort.delete(id);
    }
}
