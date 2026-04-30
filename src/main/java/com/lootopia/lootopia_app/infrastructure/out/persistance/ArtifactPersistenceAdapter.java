package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.application.port.out.ArtifactPersistencePort;
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
        List<ArtifactEntity> artifactEntities = artifactRepository.findByUserId(id);
        if (artifactEntities == null || artifactEntities.isEmpty()) {
            return new ArrayList<>();
        }
        return artifactEntities.stream()
                .map(ArtifactMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Artifact save(Artifact artefact) {
        ArtifactEntity entity = ArtifactMapper.toEntity(artefact);
        ArtifactEntity savedEntity = artifactRepository.save(entity);
        return ArtifactMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Artifact> findById(Long id) {
        return artifactRepository.findById(id)
                .map(ArtifactMapper::toDomain);
    }

    @Override
    public boolean existsById(Long id) {
        return artifactRepository.existsById(id);
    }

    @Override
    public void delete(Long id) {
        artifactRepository.deleteById(id);
    }
}
