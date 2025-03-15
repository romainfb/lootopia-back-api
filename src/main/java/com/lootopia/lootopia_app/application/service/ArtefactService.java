package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.GetArtifactsUseCase;
import com.lootopia.lootopia_app.application.port.out.ArtifactPersistencePort;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtefactService implements GetArtifactsUseCase {

    private final ArtifactPersistencePort artefactPersistencePort;

    @Override
    public List<Artifact> getArtifactsByUserId(Long id_user) {
        if (id_user == null) throw new InvalidParameterException("User ID cannot be null");
        return artefactPersistencePort.findByUserId(id_user);
    }

}
