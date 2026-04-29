package com.lootopia.lootopia_app.infrastructure.out.persistance.mapper;

import com.lootopia.lootopia_app.domain.model.Bid;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.BidEntity;

public class BidPersistenceMapper {

    public static Bid toDomain(BidEntity entity) {
        if (entity==null) {
            return null;
        }

        return Bid.builder()
                .id(entity.getId())
                .auctionId(entity.getEnchereId())
                .bidderId(entity.getEncherisseurId())
                .amount(entity.getMontant())
                .placedAt(entity.getPlaceAt())
                .build();
    }

    public static BidEntity toEntity(Bid domain) {
        if (domain==null) {
            return null;
        }

        BidEntity entity = new BidEntity();
        entity.setId(domain.getId());
        entity.setEnchereId(domain.getAuctionId());
        entity.setEncherisseurId(domain.getBidderId());
        entity.setMontant(domain.getAmount());
        entity.setPlaceAt(domain.getPlacedAt());
        return entity;
    }
}
