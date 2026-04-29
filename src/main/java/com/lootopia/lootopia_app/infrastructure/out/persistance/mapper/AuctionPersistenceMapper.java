package com.lootopia.lootopia_app.infrastructure.out.persistance.mapper;

import com.lootopia.lootopia_app.domain.model.Auction;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.AuctionEntity;

public class AuctionPersistenceMapper {

    public static Auction toDomain(AuctionEntity entity) {
        if (entity==null) {
            return null;
        }

        return Auction.builder()
                .id(entity.getId())
                .artifactId(entity.getArtefactId())
                .sellerId(entity.getVendeurId())
                .startPrice(entity.getPrixDepart())
                .currentPrice(entity.getPrixActuel())
                .minIncrement(entity.getIncrementMin())
                .currentWinnerId(entity.getGagnantActuelId())
                .startsAt(entity.getDebutAt())
                .endsAt(entity.getFinAt())
                .status(entity.getStatut())
                .build();
    }

    public static AuctionEntity toEntity(Auction domain) {
        if (domain==null) {
            return null;
        }

        AuctionEntity entity = new AuctionEntity();
        entity.setId(domain.getId());
        entity.setArtefactId(domain.getArtifactId());
        entity.setVendeurId(domain.getSellerId());
        entity.setPrixDepart(domain.getStartPrice());
        entity.setPrixActuel(domain.getCurrentPrice());
        entity.setIncrementMin(domain.getMinIncrement());
        entity.setGagnantActuelId(domain.getCurrentWinnerId());
        entity.setDebutAt(domain.getStartsAt());
        entity.setFinAt(domain.getEndsAt());
        entity.setStatut(domain.getStatus());
        // version et createdAt sont gérés par JPA / Hibernate
        return entity;
    }
}
