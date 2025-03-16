package com.lootopia.lootopia_app.infrastructure.in.rest.mapper;

import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdateDto;
import com.lootopia.lootopia_app.infrastructure.out.keycloak.UserKeycloak;

public class UserMapper {

    public static UserUpdateDto toUserUpdateDto(User user) {
        return UserUpdateDto.builder()
                .username(user.getUsername())
                .build();
    }

    public static UserKeycloak toKeycloakUser(UserUpdateDto userUpdateDto, Long userId) {
        return UserKeycloak.builder()
                .id(userId)
                .username(userUpdateDto.getUsername())
                .email(userUpdateDto.getEmail())
                .firstName(userUpdateDto.getFirstName())
                .lastName(userUpdateDto.getLastName())
                .build();
    }
}
