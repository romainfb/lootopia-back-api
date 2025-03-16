package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserKeycloakUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdateDto;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Optional;

public interface KeycloakPort {
    Optional<UserRepresentation> getUserById(String userId);

    boolean deleteUser(String userId);

    UserRepresentation updateUser(UserKeycloakUpdateDto user);

    boolean updatePassword(String userId, String newPassword);
}
