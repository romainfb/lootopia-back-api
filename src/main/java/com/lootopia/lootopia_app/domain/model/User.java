package com.lootopia.lootopia_app.domain.model;

import com.lootopia.lootopia_app.domain.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String username;
    private AccountType accountType;
    private Integer balance;
    private String imageUrl;
}
