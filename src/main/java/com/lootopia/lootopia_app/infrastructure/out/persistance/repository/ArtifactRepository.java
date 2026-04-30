package com.lootopia.lootopia_app.infrastructure.out.persistance.repository;

import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.ArtifactEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtifactRepository extends JpaRepository<ArtifactEntity, Long> {

    List<ArtifactEntity> findByUserId(Long utilisateurId);

}
