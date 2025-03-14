package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.application.port.out.ArtifactPersistencePort;
import com.lootopia.lootopia_app.application.port.out.RewardPersistencePort;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.ArtifactEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.ArtifactMapper;
import com.lootopia.lootopia_app.infrastructure.out.persistance.repository.ArtifactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ArtifactPersistenceAdapter implements ArtifactPersistencePort {

    private final ArtifactRepository artifactRepository;

    @Override
    public List<Artifact> findByUserId(Long id) {
        Optional<ArtifactEntity> artifactEntities = artifactRepository.findByUser_Id(id);
        if (artifactEntities == null || artifactEntities.isEmpty()) {
            return new ArrayList<>();
        }
        return artifactEntities.stream()
                .map(ArtifactMapper::toDomain)
                .collect(Collectors.toList());
    }
}
