package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserInfoDto {

    private Long id;

    private String keycloakId;

    private String email;

    private String firstName;

    private String lastName;

    private String username;

    private Boolean emailVerified;
}
