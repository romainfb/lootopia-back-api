package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Data
@Builder
public class UpdatePasswordRequestDto {
    private String password;
}
