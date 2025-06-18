package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.Transaction;

import java.util.List;

public interface GetTransactionsUseCase {

    List<Transaction> getTransactionsByUserId(Long id);
}
