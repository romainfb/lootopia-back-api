package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Artifact;

import java.util.List;
public interface GetArtifactsUseCase {
    List<Artifact> getArtifactsByUserId(Long id_user);
}
