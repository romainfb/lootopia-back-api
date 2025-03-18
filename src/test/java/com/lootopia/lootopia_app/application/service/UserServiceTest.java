package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.JwtServiceUseCase;
import com.lootopia.lootopia_app.application.port.out.KeycloakPort;
import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.application.port.in.GetArtifactsUseCase;
import com.lootopia.lootopia_app.domain.AccountType;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.domain.model.UserInventory;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserRegisterFromKeycloakDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserToUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private KeycloakPort keycloakPort;

    @Mock
    private GetArtifactsUseCase getArtifactsUseCase;

    @Mock
    private JwtServiceUseCase jwtServiceUseCase;

    @InjectMocks
    private UserService userService;

    private User user;
    public UserEntity userEntity;
    private UserRepresentation userRepresentation;
    private UserToUpdateDto userToUpdateDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "testUser", AccountType.USER, 0);
        userEntity = UserEntity.builder()
                .id(1L)
                .keycloakId("keycloak-123")
                .username("testUser")
                .accountType(AccountType.USER)
                .balance(0)
                .build();

        userToUpdateDto = new UserToUpdateDto(
                "test@gmail.com", "firstName", "lastName", "username"
        );

        userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("username");
        userRepresentation.setEmail("test@gmail.com");
        userRepresentation.setFirstName("firstName");
        userRepresentation.setLastName("lastName");
    }

    Jwt fakeJwt = new Jwt(
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." +
                    "eyJzdWIiOiJrZXljbG9hay0xMjMiLCJlbWFpbCI6InRlc3RVc2VyQGV4YW1wbGUuY29tIiwicHJlZmVycmVkX3VzZXJuYW1lIjoidGVzdFVzZXIiLCJnaXZlbl9uYW1lIjoidGVzdCIsImZhbWlseV9uYW1lIjoidXNlciIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoidGVzdCB1c2VyIiwicGljdHVyZSI6InRlc3QucG5nIiwiZXhwIjoxNzEwNjQ2MDAwLCJpYXQiOjE3MTA2NDI0MDB9." +
                    "dGVzdC1zaWduYXR1cmU",
            Instant.ofEpochSecond(1710642400), // iat (timestamp en dur)
            Instant.ofEpochSecond(1710646000), // exp (timestamp en dur)
            Map.of("alg", "HS256", "typ", "JWT"),
            Map.of(
                    "sub", "keycloak-123",
                    "email", "testUser@example.com",
                    "preferred_username", "testUser",
                    "given_name", "test",
                    "family_name", "user",
                    "email_verified", true,
                    "name", "test user",
                    "picture", "test.png",
                    "iat", Instant.ofEpochSecond(1710642400),
                    "exp", Instant.ofEpochSecond(1710646000)
            )
    );

    @Test
    void createUser_shouldThrowException_whenUserDtoIsNull() {
        assertThrows(InvalidParameterException.class, () -> userService.createUser(null));
    }

    @Test
    void createUser_shouldThrowException_whenUserDtoHasNullFields() {
        UserRegisterFromKeycloakDto dto = new UserRegisterFromKeycloakDto(null, null);
        assertThrows(InvalidParameterException.class, () -> userService.createUser(dto));
    }

    @Test
    void createUser_shouldSaveUser_whenUserDtoIsValid() {
        UserRegisterFromKeycloakDto dto = new UserRegisterFromKeycloakDto("keycloak-123", "testUser");
        when(userPersistencePort.save(any(UserEntity.class))).thenReturn(user);

        User result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(userPersistencePort).save(any(UserEntity.class));
    }

    @Test
    void getUserById_shouldThrowException_whenUserIdIsNull() {
        assertThrows(InvalidParameterException.class, () -> userService.getUserById(null));
    }

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userEntity));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().getUsername());
        verify(userPersistencePort).findById(1L);
    }

    @Test
    void getUserById_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        Long userId = 999L;

        when(userPersistencePort.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
        verify(userPersistencePort, times(1)).findById(userId);
    }

    @Test
    void getUserInventory_shouldThrowException_whenUserIdIsNull() {
        assertThrows(InvalidParameterException.class, () -> userService.getUserInventory(null));
    }

    @Test
    void getUserInventory_shouldReturnUserWithArtifacts() {
        List<Artifact> artifacts = List.of(new Artifact(1L, "testArtifact", "100", "testArtifact.png", "1L", 1L));
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userEntity));
        when(getArtifactsUseCase.getArtifactsByUserId(1L)).thenReturn(artifacts);

        Optional<UserInventory> result = userService.getUserInventory(1L);

        assertTrue(result.isPresent());
        assertEquals(artifacts, result.get().getArtifacts());
        verify(userPersistencePort).findById(1L);
        verify(getArtifactsUseCase).getArtifactsByUserId(1L);
    }

    @Test
    void deleteUser_shouldDeleteUser_whenUserExists() {
        when(jwtServiceUseCase.getUserIdFromToken(fakeJwt.getTokenValue())).thenReturn("keycloak-user-id");
        when(userPersistencePort.findByKeycloakId("keycloak-user-id")).thenReturn(Optional.of(userEntity));
        when(keycloakPort.deleteUser(userEntity.getKeycloakId())).thenReturn(true);

        userService.deleteUser(fakeJwt);

        verify(userPersistencePort).deleteById(1L);
        verify(keycloakPort).deleteUser(userEntity.getKeycloakId());
    }

    @Test
    void deleteUser_shouldThrowException_whenKeycloakDeletionFails() {
        when(jwtServiceUseCase.getUserIdFromToken(fakeJwt.getTokenValue())).thenReturn("keycloak-user-id");
        when(userPersistencePort.findByKeycloakId("keycloak-user-id")).thenReturn(Optional.of(userEntity));
        when(keycloakPort.deleteUser(userEntity.getKeycloakId())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.deleteUser(fakeJwt));
    }

    @Test
    void deleteUser_shouldThrowException_whenKeycloakIdIsNull() {
        userEntity.setKeycloakId(null);
        when(jwtServiceUseCase.getUserIdFromToken(fakeJwt.getTokenValue())).thenReturn("keycloak-user-id");
        when(userPersistencePort.findByKeycloakId("keycloak-user-id")).thenReturn(Optional.of(userEntity));
        assertThrows(IllegalStateException.class, () -> userService.deleteUser(fakeJwt));
    }

    @Test
    void deleteUser_shouldThrowException_whenKeycloakIdIsEmpty() {
        userEntity.setKeycloakId("");
        when(jwtServiceUseCase.getUserIdFromToken(fakeJwt.getTokenValue())).thenReturn("keycloak-user-id");
        when(userPersistencePort.findByKeycloakId("keycloak-user-id")).thenReturn(Optional.of(userEntity));
        assertThrows(IllegalStateException.class, () -> userService.deleteUser(fakeJwt));
    }

    @Test
    void updateUser_shouldThrowException_whenUserNotFound() {
        when(jwtServiceUseCase.getUserIdFromToken(fakeJwt.getTokenValue())).thenReturn("keycloak-user-id");
        when(userPersistencePort.findByKeycloakId("keycloak-user-id")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(userToUpdateDto, fakeJwt));
    }

    @Test
    void updateUser_shouldUpdateUser_whenUserExists() {
        when(jwtServiceUseCase.getUserIdFromToken(fakeJwt.getTokenValue())).thenReturn("keycloak-user-id");
        when(userPersistencePort.findByKeycloakId("keycloak-user-id")).thenReturn(Optional.of(userEntity));
        when(keycloakPort.updateUser(any(UserUpdatedDto.class))).thenReturn(userRepresentation);

        // Utilisez l'instance initialisée dans setUp
        UserUpdatedDto result = userService.updateUser(userToUpdateDto, fakeJwt);

        assertEquals("username", result.getUsername());
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("firstName", result.getFirstName());
        assertEquals("lastName", result.getLastName());

        verify(userPersistencePort).save(userEntity);
        verify(keycloakPort).updateUser(any(UserUpdatedDto.class));
    }

    @Test
    void updateUser_shouldUpdateOnlyUsername_whenOtherFieldsAreNull() {
        UserToUpdateDto partialUpdate = new UserToUpdateDto(null, null, null, "newUsername");
        userEntity.setUsername("oldUsername");

        when(jwtServiceUseCase.getUserIdFromToken(fakeJwt.getTokenValue())).thenReturn("keycloak-user-id");
        when(userPersistencePort.findByKeycloakId("keycloak-user-id")).thenReturn(Optional.of(userEntity));
        when(keycloakPort.updateUser(any(UserUpdatedDto.class))).thenReturn(userRepresentation);

        userService.updateUser(partialUpdate, fakeJwt);
        assertEquals("newUsername", userEntity.getUsername());
        verify(userPersistencePort).save(userEntity);
    }

    @Test
    void updatePassword_shouldThrowException_whenUserNotFound() {
        when(jwtServiceUseCase.getUserIdFromToken(fakeJwt.getTokenValue())).thenReturn("keycloak-user-id");
        when(userPersistencePort.findByKeycloakId("keycloak-user-id")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updatePassword("newPassword", fakeJwt));
    }

    @Test
    void updatePassword_shouldUpdatePassword_whenUserExists() {
        when(jwtServiceUseCase.getUserIdFromToken(fakeJwt.getTokenValue())).thenReturn("keycloak-user-id");
        when(userPersistencePort.findByKeycloakId("keycloak-user-id")).thenReturn(Optional.of(userEntity));
        when(keycloakPort.updatePassword(userEntity.getKeycloakId(), "newPassword")).thenReturn(true);

        Boolean result = userService.updatePassword("newPassword", fakeJwt);

        assertTrue(result);
        verify(keycloakPort).updatePassword(userEntity.getKeycloakId(), "newPassword");
    }

    @Test
    void updatePassword_shouldThrowException_whenKeycloakIdIsNull() {
        userEntity.setKeycloakId(null);
        when(jwtServiceUseCase.getUserIdFromToken(fakeJwt.getTokenValue())).thenReturn("keycloak-user-id");
        when(userPersistencePort.findByKeycloakId("keycloak-user-id")).thenReturn(Optional.of(userEntity));
        assertThrows(IllegalStateException.class, () -> userService.updatePassword("newPassword", fakeJwt));
    }

    @Test
    void updatePassword_shouldThrowException_whenKeycloakIdIsEmpty() {
        userEntity.setKeycloakId("");
        when(jwtServiceUseCase.getUserIdFromToken(fakeJwt.getTokenValue())).thenReturn("keycloak-user-id");
        when(userPersistencePort.findByKeycloakId("keycloak-user-id")).thenReturn(Optional.of(userEntity));
        assertThrows(IllegalStateException.class, () -> userService.updatePassword("newPassword", fakeJwt));
    }
}
