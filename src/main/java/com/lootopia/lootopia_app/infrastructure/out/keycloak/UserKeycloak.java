package com.lootopia.lootopia_app.infrastructure.out.keycloak;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserKeycloak {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
}
