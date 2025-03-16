package com.lootopia.lootopia_app.application.port.out;

import org.keycloak.representations.idm.UserRepresentation;

import java.util.Optional;

public interface KeycloakPort {
    Optional<UserRepresentation> getUserById(String userId);

    boolean deleteUser(String userId);

    UserRepresentation updateUser(String userId, String firstName, String lastName, String email);

    boolean updatePassword(String userId, String newPassword);

    UserRepresentation setUserEnabled(String userId, boolean enabled);
}
