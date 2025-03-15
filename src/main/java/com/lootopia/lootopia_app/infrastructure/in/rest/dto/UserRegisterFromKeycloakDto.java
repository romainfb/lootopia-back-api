package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserRegisterFromKeycloakDto {
    private String id;
    private String username;
}
