package com.lootopia.lootopia_app.infrastructure.out.persistance.repository;

import com.lootopia.lootopia_app.domain.HoldStatus;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.WalletHoldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WalletHoldRepository extends JpaRepository<WalletHoldEntity, Long> {

    @Query("SELECT COALESCE(SUM(h.montant), 0) FROM WalletHoldEntity h WHERE h.utilisateurId = :userId AND h.statut = 'HELD'")
    Integer sumHeldByUserId(@Param("userId") Long userId);

    Optional<WalletHoldEntity> findByUtilisateurIdAndEnchereIdAndStatut(Long userId, Long auctionId, HoldStatus statut);

    List<WalletHoldEntity> findByEnchereIdAndStatut(Long auctionId, HoldStatus statut);
}
