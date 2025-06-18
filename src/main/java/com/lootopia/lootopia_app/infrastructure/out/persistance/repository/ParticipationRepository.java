package com.lootopia.lootopia_app.infrastructure.out.persistance.repository;

import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.ParticipationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipationRepository extends JpaRepository<ParticipationEntity, Long> {
    List<ParticipationEntity> findByHuntId(Integer huntId);
    long countByHuntId(Long huntId);
    Optional<ParticipationEntity> findByHuntIdAndUserId(Long huntId, Integer userId);
}