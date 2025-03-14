package com.lootopia.lootopia_app.infrastructure.out.persistance.repository;

import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.RewardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RewardRepository extends JpaRepository<RewardEntity, Long> {

    List<RewardEntity> findByUtilisateurId(Long id);
}
