package com.lootopia.lootopia_app.infrastructure.out.persistance.repository;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.HuntEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HuntRepository extends JpaRepository<HuntEntity, Long> {
}
