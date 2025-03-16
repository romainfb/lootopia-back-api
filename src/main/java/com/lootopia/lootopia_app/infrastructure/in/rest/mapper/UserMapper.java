package com.lootopia.lootopia_app.infrastructure.in.rest.mapper;

import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserToUpdateDto;
import com.lootopia.lootopia_app.infrastructure.out.keycloak.UserKeycloak;

public class UserMapper {

    public static UserToUpdateDto toUserUpdateDto(User user) {
        return UserToUpdateDto.builder()
                .username(user.getUsername())
                .build();
    }

    public static UserKeycloak toKeycloakUser(UserToUpdateDto userToUpdateDto, Long userId) {
        return UserKeycloak.builder()
                .id(userId)
                .username(userToUpdateDto.getUsername())
                .email(userToUpdateDto.getEmail())
                .firstName(userToUpdateDto.getFirstName())
                .lastName(userToUpdateDto.getLastName())
                .build();
    }
}
