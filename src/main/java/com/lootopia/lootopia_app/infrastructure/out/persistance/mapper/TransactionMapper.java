package com.lootopia.lootopia_app.infrastructure.out.persistance.mapper;

import com.lootopia.lootopia_app.domain.model.Transaction;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.TransactionEntity;

public class TransactionMapper {
    public static Transaction toDomain(TransactionEntity entity) {
        return Transaction.builder()
                .id(entity.getId())
                .utilisateur(entity.getUtilisateur())
                .montant(entity.getMontant())
                .typeTransaction(entity.getTypeTransaction())
                .date(entity.getDate())
                .artefact(entity.getArtefact())
                .build();
    }

    public static TransactionEntity toEntity(Transaction domain) {
        return TransactionEntity.builder()
                .id(domain.getId())
                .utilisateur(domain.getUtilisateur())
                .montant(domain.getMontant())
                .typeTransaction(domain.getTypeTransaction())
                .date(domain.getDate())
                .artefact(domain.getArtefact())
                .build();
    }
}
