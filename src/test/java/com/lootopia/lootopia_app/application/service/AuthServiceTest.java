package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.in.JwtServiceUseCase;
import com.lootopia.lootopia_app.application.port.out.KeycloakPort;
import com.lootopia.lootopia_app.domain.AccountType;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private KeycloakPort keycloakPort;

    @Mock
    private GetUserUseCase getUserUseCase;

    @Spy
    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtServiceUseCase jwtServiceUseCase;

    @BeforeEach
    void setUp() {
        // Initialisation des mocks si nécessaire
    }

    @Test
    void exchangeCodeForToken_ShouldReturnAccessToken_WhenValidCodeIsProvided() {
        String code = "valid_code";
        String accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI8I3...";
        AccessTokenResponse tokenResponse = new AccessTokenResponse();
        tokenResponse.setToken(accessToken);

        when(keycloakPort.getAccessToken(code)).thenReturn(tokenResponse);

        AccessTokenResponse result = authService.exchangeCodeForToken(code);

        assertNotNull(result);
        assertEquals(accessToken, result.getToken());
        verify(keycloakPort, times(1)).getAccessToken(code);
    }

    @Test
    void getUserInfo_ShouldReturnUserInfo_WhenValidTokenIsProvided() {
        String accessToken = "valid_token";
        String userId = "user123";
        User userFromDb = new User(2L, "John", AccountType.USER, 0);
        UserRepresentation userKeycloak = new UserRepresentation();
        userKeycloak.setId(userId);
        userKeycloak.setFirstName("John");
        userKeycloak.setLastName("Doe");
        userKeycloak.setEmail("john.doe@example.com");
        userKeycloak.setUsername("john.doe");
        userKeycloak.setEmailVerified(true);

        doReturn(userId).when(authService).jwtServiceUseCase.decodeJwt(accessToken);
        when(keycloakPort.getUserById(userId)).thenReturn(Optional.of(userKeycloak));
        when(getUserUseCase.getUserByKeycloakId(userId)).thenReturn(Optional.of(userFromDb));

        UserInfoDto result = authService.getUserInfo(accessToken);

        assertNotNull(result);
        assertEquals(userFromDb.getId(), result.getId());
        assertEquals(userKeycloak.getFirstName(), result.getFirstName());
        assertEquals(userKeycloak.getLastName(), result.getLastName());
        assertEquals(userKeycloak.getEmail(), result.getEmail());
        verify(keycloakPort, times(1)).getUserById(userId);
        verify(getUserUseCase, times(1)).getUserByKeycloakId(userId);
    }

    @Test
    void getUserInfo_ShouldThrowException_WhenUserNotFoundInKeycloak() {
        String accessToken = "valid_token";
        String userId = "user123";

        doReturn(userId).when(authService).jwtServiceUseCase.decodeJwt(accessToken);
        when(keycloakPort.getUserById(userId)).thenReturn(Optional.empty());
        // Pas besoin de mocker getUserByKeycloakId car il ne sera jamais appelé

        assertThrows(NoSuchElementException.class, () -> authService.getUserInfo(accessToken));

        verify(keycloakPort, times(1)).getUserById(userId);
        // Ne pas vérifier l'appel à getUserByKeycloakId car il ne devrait pas être appelé
    }

    @Test
    void getUserInfo_ShouldThrowException_WhenUserNotFoundInDatabase() {
        String accessToken = "valid_token";
        String userId = "user123";
        UserRepresentation userKeycloak = new UserRepresentation();
        userKeycloak.setId(userId);

        doReturn(userId).when(authService).jwtServiceUseCase.decodeJwt(accessToken);
        when(keycloakPort.getUserById(userId)).thenReturn(Optional.of(userKeycloak));
        when(getUserUseCase.getUserByKeycloakId(userId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> authService.getUserInfo(accessToken));

        verify(keycloakPort, times(1)).getUserById(userId);
        verify(getUserUseCase, times(1)).getUserByKeycloakId(userId);
    }

    @Test
    void decodeJwt_ShouldReturnUserId_WhenValidTokenIsProvided() {
        String accessToken = "header.eyJzdWIiOiAidXNlcjEyMyJ9.signature";
        String expectedUserId = "user123";

        // Ne pas mocker la méthode que nous testons
        String userId = authService.jwtServiceUseCase.decodeJwt(accessToken);

        assertEquals(expectedUserId, userId);
    }

    @Test
    void decodeJwt_ShouldThrowException_WhenInvalidTokenFormat() {
        String accessToken = "invalid_token";

        assertThrows(RuntimeException.class, () -> authService.jwtServiceUseCase.decodeJwt(accessToken));
    }
}
