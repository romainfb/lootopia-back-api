package com.lootopia.lootopia_app.infrastructure.out.persistance.repository;

import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.ArtifactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtifactRepository extends JpaRepository<ArtifactEntity, Long> {

    Optional<ArtifactEntity> findByUserId(Long utilisateurId);

}
