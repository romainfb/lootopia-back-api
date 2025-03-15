package com.lootopia.lootopia_app.domain.model;

import com.lootopia.lootopia_app.domain.AccountType;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String username;
    private String keycloakId;
    private AccountType accountType;
    private Integer balance;
    private List<Artifact> artifacts;
}
