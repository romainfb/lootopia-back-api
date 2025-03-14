package com.lootopia.lootopia_app.domain.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String email;
    private String password;
    private String username;
    private String accountType;
    private Integer balance;
    private String activityHistory;
}
