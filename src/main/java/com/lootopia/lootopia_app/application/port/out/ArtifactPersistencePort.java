package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.domain.model.Reward;

import java.util.List;

public interface ArtifactPersistencePort {
    List<Artifact> findByUserId(Long id);
}
