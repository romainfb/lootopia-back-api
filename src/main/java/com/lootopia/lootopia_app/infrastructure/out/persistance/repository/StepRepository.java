package com.lootopia.lootopia_app.infrastructure.out.persistance.repository;

import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.StepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StepRepository extends JpaRepository<StepEntity, Long> {
    List<StepEntity> findByHuntId(Long huntId);
    Optional<StepEntity> findByIdAndHuntId(Long stepId, Long huntId);
    boolean existsByIdAndHuntId(Long stepId, Long huntId);
}