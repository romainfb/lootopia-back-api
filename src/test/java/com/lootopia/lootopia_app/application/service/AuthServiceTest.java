package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.out.KeycloakPort;
import com.lootopia.lootopia_app.domain.AccountType;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private KeycloakPort keycloakPort;

    @Mock
    private GetUserUseCase getUserUseCase;

    @Spy
    @InjectMocks
    private AuthService authService;

    private final String USER_ID = "user123";

    @BeforeEach
    void setUp() {
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
    void getUserInfo_ShouldReturnUserInfo_WhenValidUserIdIsProvided() {
        User userFromDb = User.builder()
                .id(2L)
                .username("John")
                .accountType(AccountType.USER)
                .balance(0)
                .build();
        UserRepresentation userKeycloak = new UserRepresentation();
        userKeycloak.setId(USER_ID);
        userKeycloak.setFirstName("John");
        userKeycloak.setLastName("Doe");
        userKeycloak.setEmail("john.doe@example.com");
        userKeycloak.setUsername("john.doe");
        userKeycloak.setEmailVerified(true);

        when(keycloakPort.getUserById(USER_ID)).thenReturn(Optional.of(userKeycloak));
        when(getUserUseCase.getUserByKeycloakId(USER_ID)).thenReturn(Optional.of(userFromDb));

        UserInfoDto result = authService.getUserInfo(USER_ID);

        assertNotNull(result);
        assertEquals(userFromDb.getId(), result.getId());
        assertEquals(userKeycloak.getFirstName(), result.getFirstName());
        assertEquals(userKeycloak.getLastName(), result.getLastName());
        assertEquals(userKeycloak.getEmail(), result.getEmail());
        assertEquals(USER_ID, result.getKeycloakId());
        verify(keycloakPort, times(1)).getUserById(USER_ID);
        verify(getUserUseCase, times(1)).getUserByKeycloakId(USER_ID);
    }

    @Test
    void getUserInfo_ShouldThrowException_WhenUserNotFoundInKeycloak() {
        when(keycloakPort.getUserById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> authService.getUserInfo(USER_ID));

        verify(keycloakPort, times(1)).getUserById(USER_ID);
    }

    @Test
    void getUserInfo_ShouldThrowException_WhenUserNotFoundInDatabase() {
        UserRepresentation userKeycloak = new UserRepresentation();
        userKeycloak.setId(USER_ID);

        when(keycloakPort.getUserById(USER_ID)).thenReturn(Optional.of(userKeycloak));
        when(getUserUseCase.getUserByKeycloakId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> authService.getUserInfo(USER_ID));

        verify(keycloakPort, times(1)).getUserById(USER_ID);
        verify(getUserUseCase, times(1)).getUserByKeycloakId(USER_ID);
    }

    @Test
    void logout_ShouldReturnTrue_WhenSuccessful() {
        String accessToken = "valid_token";
        when(keycloakPort.logout(accessToken)).thenReturn(true);

        Boolean result = authService.logout(accessToken);

        assertTrue(result);
        verify(keycloakPort, times(1)).logout(accessToken);
    }

    @Test
    void loginUser_ShouldReturnAccessToken_WhenValidCredentialsAreProvided() {
        String username = "john.doe";
        String password = "password123";
        AccessTokenResponse tokenResponse = new AccessTokenResponse();
        tokenResponse.setToken("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI8I3...");

        when(keycloakPort.loginUser(username, password)).thenReturn(tokenResponse);

        AccessTokenResponse result = authService.loginUser(username, password);

        assertNotNull(result);
        assertEquals(tokenResponse.getToken(), result.getToken());
        verify(keycloakPort, times(1)).loginUser(username, password);
    }
}
