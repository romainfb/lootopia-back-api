package com.lootopia.lootopia_app.application.port.in;

import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

/**
 * Use case for fetching all users from Keycloak
 */
public interface FetchAllUsersUseCase {
    /**
     * Retrieves all users from Keycloak
     *
     * @return List of UserRepresentation objects representing all users in Keycloak
     */
    List<UserRepresentation> getAllUsers();
}
