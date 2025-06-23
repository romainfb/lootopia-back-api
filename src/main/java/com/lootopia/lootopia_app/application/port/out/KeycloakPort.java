package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Optional;

public interface KeycloakPort {
    Optional<UserRepresentation> getUserById(String userId);

    boolean deleteUser(String userId);

    UserRepresentation updateUser(UserUpdatedDto user);

    boolean updatePassword(String userId, String newPassword);

    AccessTokenResponse getAccessToken(String code);

    Boolean logout(String accessToken);

    AccessTokenResponse loginUser(String username, String password);

    List<UserRepresentation> getAllUsers();
}
