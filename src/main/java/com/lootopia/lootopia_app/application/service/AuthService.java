package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.AuthentificationUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.out.KeycloakPort;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserInfoDto;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthentificationUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final KeycloakPort keycloakPort;
    private final GetUserUseCase getUserUseCase;

    @Override
    public AccessTokenResponse exchangeCodeForToken(String code) {
      return keycloakPort.getAccessToken(code);
    }

    @Override
    public Boolean logout(String accessToken) {
        log.info("Logging out user with access token: {}", accessToken);
        return keycloakPort.logout(accessToken);
    }

    @Override
    public UserInfoDto getUserInfo(String userId) {
        log.info("Getting user info for user with ID: {}", userId);

        UserRepresentation userKeycloak = keycloakPort.getUserById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found in Keycloak"));

        User userFromDb = getUserUseCase.getUserByKeycloakId(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found in database"));

        return UserInfoDto.builder()
                .id(userFromDb.getId())
                .keycloakId(userKeycloak.getId())
                .firstName(userKeycloak.getFirstName())
                .emailVerified(userKeycloak.isEmailVerified())
                .lastName(userKeycloak.getLastName())
                .username(userKeycloak.getUsername())
                .email(userKeycloak.getEmail())
                .build();
    }

    @Override
    public AccessTokenResponse loginUser(String username, String password) {
        return keycloakPort.loginUser(username, password);
    }






}
