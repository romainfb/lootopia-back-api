package com.lootopia.lootopia_app.domain.model;

import com.lootopia.lootopia_app.domain.AccountType;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private AccountType accountType;
    private Integer balance;
}
