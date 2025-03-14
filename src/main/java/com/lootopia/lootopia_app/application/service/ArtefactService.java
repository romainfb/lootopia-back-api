package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.GetArtifactsByUserIdUseCase;
import com.lootopia.lootopia_app.application.port.out.ArtifactPersistencePort;
import com.lootopia.lootopia_app.domain.model.Artifact;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtefactService implements GetArtifactsByUserIdUseCase {

    private final ArtifactPersistencePort artefactPersistencePort;

    @Override
    public List<Artifact> getArtifactsByUserId(Long id_user) {
        return artefactPersistencePort.findByUserId(id_user);
    }

}
