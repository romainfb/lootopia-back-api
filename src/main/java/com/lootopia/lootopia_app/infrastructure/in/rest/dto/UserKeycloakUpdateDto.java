package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserKeycloakUpdateDto {

    private String id;

    @Nullable
    private String email;

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    @Nullable
    private String username;
}
