package com.lootopia.lootopia_app.infrastructure.out.persistance.repository;

import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.CacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CacheRepository extends JpaRepository<CacheEntity, Long> {
    List<CacheEntity> findByHuntId(Long huntId);
}
