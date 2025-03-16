package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.out.KeycloakPort;
import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.application.port.in.GetArtifactsUseCase;
import com.lootopia.lootopia_app.domain.AccountType;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.domain.model.UserInventory;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserKeycloakUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserRegisterFromKeycloakDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
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

    @InjectMocks
    private UserService userService;

    private User user;
    public UserEntity userEntity;
    @BeforeEach
    void setUp() {
        user = new User(1L, "testUser", "fqsdfqsdfq1234", AccountType.USER, 0);
        userEntity = UserEntity.builder()
                .id(1L)
                .keycloakId("keycloak-123")
                .username("testUser")
                .accountType(AccountType.USER)
                .balance(0)
                .build();
    }

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
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().getUsername());
        verify(userPersistencePort).findById(1L);
    }

    @Test
    void getUserById_shouldReturnEmpty_whenUserDoesNotExist() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isEmpty());
        verify(userPersistencePort).findById(1L);
    }

    @Test
    void getUserInventory_shouldThrowException_whenUserIdIsNull() {
        assertThrows(InvalidParameterException.class, () -> userService.getUserInventory(null));
    }

    @Test
    void getUserInventory_shouldReturnUserWithArtifacts() {
        List<Artifact> artifacts = List.of(new Artifact(1L, "testArtifact", "100", "testArtifact.png", "1L", 1L));
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        when(getArtifactsUseCase.getArtifactsByUserId(1L)).thenReturn(artifacts);

        Optional<UserInventory> result = userService.getUserInventory(1L);

        assertTrue(result.isPresent());
        assertEquals(artifacts, result.get().getArtifacts());
        verify(userPersistencePort).findById(1L);
        verify(getArtifactsUseCase).getArtifactsByUserId(1L);
    }

    @Test
    void deleteUser_shouldThrowException_whenUserIdIsNull() {
        assertThrows(InvalidParameterException.class, () -> userService.deleteUser(null));
    }

    @Test
    void deleteUser_shouldThrowException_whenUserNotFound() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.empty());
        assertThrows(InvalidParameterException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void deleteUser_shouldDeleteUser_whenUserExists() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        when(keycloakPort.deleteUser(user.getKeycloakId())).thenReturn(true);

        userService.deleteUser(1L);

        verify(userPersistencePort).deleteById(1L);
        verify(keycloakPort).deleteUser(user.getKeycloakId());
    }

    @Test
    void deleteUser_shouldThrowException_whenKeycloakDeletionFails() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        when(keycloakPort.deleteUser(user.getKeycloakId())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void updateUser_shouldThrowException_whenUserIdIsNull() {
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(new UserUpdateDto(
                "test@gmail.com", "firstName", "lastName", "username"
        ), 12341234L));
    }

    @Test
    void updateUser_shouldThrowException_whenUserNotFound() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(new UserUpdateDto(
                "test@gmail.com", "firstName", "lastName", "username"
        ), 1L));
    }

    @Test
    void updateUser_shouldUpdateUser_whenUserExists() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        UserUpdateDto updateDto = new UserUpdateDto(
                "newEmail@example.com", "newFirstName", "newLastName", "newUsername"
        );

        userService.updateUser(updateDto, 1L);

        verify(userPersistencePort).save(any(UserEntity.class));
        verify(keycloakPort).updateUser(any(UserKeycloakUpdateDto.class));
    }

    @Test
    void updatePassword_shouldThrowException_whenUserIdIsNull() {
        assertThrows(ResourceNotFoundException.class, () -> userService.updatePassword("newPassword", null));
    }

    @Test
    void updatePassword_shouldThrowException_whenUserNotFound() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updatePassword("newPassword", 1L));
    }

    @Test
    void updatePassword_shouldUpdatePassword_whenUserExists() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(user));
        when(keycloakPort.updatePassword(user.getKeycloakId(), "newPassword")).thenReturn(true);

        Boolean result = userService.updatePassword("newPassword", 1L);

        assertTrue(result);
        verify(keycloakPort).updatePassword(user.getKeycloakId(), "newPassword");
    }
}
