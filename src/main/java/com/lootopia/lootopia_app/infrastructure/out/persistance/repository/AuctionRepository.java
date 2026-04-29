package com.lootopia.lootopia_app.infrastructure.out.persistance.repository;

import com.lootopia.lootopia_app.domain.AuctionStatus;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.AuctionEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AuctionRepository extends JpaRepository<AuctionEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AuctionEntity a WHERE a.id = :id")
    Optional<AuctionEntity> findByIdForUpdate(@Param("id") Long id);

    List<AuctionEntity> findByStatutAndFinAtBefore(AuctionStatus statut, Instant finAt);

    List<AuctionEntity> findByStatut(AuctionStatus statut);

    @Query("SELECT COUNT(a) > 0 FROM AuctionEntity a WHERE a.artefactId = :artefactId AND a.statut IN ('SCHEDULED','OPEN')")
    boolean existsActiveByArtefactId(@Param("artefactId") Long artefactId);
}
