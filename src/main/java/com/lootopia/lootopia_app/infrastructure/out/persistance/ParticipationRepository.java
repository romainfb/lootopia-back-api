package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.ParticipationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipationRepository extends JpaRepository<ParticipationEntity, Long> {
    long countByHuntId(Long huntId);
}
