package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import com.lootopia.lootopia_app.application.validation.ValidEmail;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserUpdateDto {

    @ValidEmail(nullable = true)
    private String email;

    @Nullable
    private String firstName;

    @Nullable
    private String lastName;

    @Nullable
    private String username;
}
