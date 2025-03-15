package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.GetTransactionsUseCase;
import com.lootopia.lootopia_app.domain.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService implements GetTransactionsUseCase {

    @Override
    public List<Transaction> getTransactionsByUserId(Long id) {
        return List.of();
    }
}
