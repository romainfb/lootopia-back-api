package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.model.Transaction;
import com.lootopia.lootopia_app.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface TransactionPersistencePort {
    List<Transaction> findByUserId(Long id);
}
