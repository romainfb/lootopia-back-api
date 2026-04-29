package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.application.port.out.TransactionPersistencePort;
import com.lootopia.lootopia_app.domain.model.Transaction;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.TransactionEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.TransactionMapper;
import com.lootopia.lootopia_app.infrastructure.out.persistance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionPersistenceAdapter implements TransactionPersistencePort {

    private final TransactionRepository transactionRepository;

    @Override
    public List<Transaction> findByUserId(Long id) {
        List<TransactionEntity> transactionEntities = transactionRepository.findByUtilisateurId(id);
        if (transactionEntities == null || transactionEntities.isEmpty()) {
            return new ArrayList<>();
        }
        return transactionEntities.stream()
                .map(TransactionMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity = TransactionMapper.toEntity(transaction);
        TransactionEntity saved = transactionRepository.save(entity);
        return TransactionMapper.toDomain(saved);
    }
}
