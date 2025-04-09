package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.domain.model.Reward;

import java.util.List;
import java.util.Optional;

public interface ArtifactPersistencePort {
    List<Artifact> findByUserId(Long id);

    Artifact save(Artifact artefact);

    Optional<Artifact> findById(Long id);
    boolean existsById(Long id);
    void delete(Long id);

}
