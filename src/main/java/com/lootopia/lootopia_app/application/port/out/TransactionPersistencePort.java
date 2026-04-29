package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.model.Transaction;

import java.util.List;

public interface TransactionPersistencePort {
    List<Transaction> findByUserId(Long id);

    Transaction save(Transaction transaction);
}
