package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserUpdatedDto {

    private Long id;

    @Nullable
    private String username;
}
