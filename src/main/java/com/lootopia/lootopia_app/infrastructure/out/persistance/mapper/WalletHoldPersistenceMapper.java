package com.lootopia.lootopia_app.infrastructure.out.persistance.mapper;

import com.lootopia.lootopia_app.domain.model.WalletHold;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.WalletHoldEntity;

public class WalletHoldPersistenceMapper {

    public static WalletHold toDomain(WalletHoldEntity entity) {
        if (entity==null) {
            return null;
        }

        return WalletHold.builder()
                .id(entity.getId())
                .userId(entity.getUtilisateurId())
                .auctionId(entity.getEnchereId())
                .amount(entity.getMontant())
                .status(entity.getStatut())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static WalletHoldEntity toEntity(WalletHold domain) {
        if (domain==null) {
            return null;
        }

        WalletHoldEntity entity = new WalletHoldEntity();
        entity.setId(domain.getId());
        entity.setUtilisateurId(domain.getUserId());
        entity.setEnchereId(domain.getAuctionId());
        entity.setMontant(domain.getAmount());
        entity.setStatut(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
