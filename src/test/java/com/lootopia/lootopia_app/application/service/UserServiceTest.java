package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.GetArtifactsUseCase;
import com.lootopia.lootopia_app.application.port.out.KeycloakPort;
import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.domain.AccountType;
import com.lootopia.lootopia_app.domain.ArtifactRarity;
import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.domain.model.UserInventory;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserRegisterFromKeycloakDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserToUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private UserEntity userEntity;
    private UserRepresentation userRepresentation;
    private UserToUpdateDto userToUpdateDto;
    private final String KEYCLOAK_USER_ID = "keycloak-123";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testUser")
                .accountType(AccountType.USER)
                .balance(0)
                .build();
        userEntity = UserEntity.builder()
                .id(1L)
                .keycloakId(KEYCLOAK_USER_ID)
                .username("testUser")
                .accountType(AccountType.USER)
                .balance(0)
                .build();

        userToUpdateDto = UserToUpdateDto.builder()
                .email("test@gmail.com")
                .firstName("firstName")
                .lastName("lastName")
                .username("username")
                .build();

        userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("username");
        userRepresentation.setEmail("test@gmail.com");
        userRepresentation.setFirstName("firstName");
        userRepresentation.setLastName("lastName");
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
        UserRegisterFromKeycloakDto dto = new UserRegisterFromKeycloakDto(KEYCLOAK_USER_ID, "testUser");
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
    void getUserByKeycloakId_shouldThrowException_whenKeycloakIdIsNull() {
        assertThrows(InvalidParameterException.class, () -> userService.getUserByKeycloakId(null));
    }

    @Test
    void getUserByKeycloakId_shouldReturnUser_whenUserExists() {
        when(userPersistencePort.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.of(userEntity));

        Optional<User> result = userService.getUserByKeycloakId(KEYCLOAK_USER_ID);

        assertTrue(result.isPresent());
        assertEquals("testUser", result.get().getUsername());
        verify(userPersistencePort).findByKeycloakId(KEYCLOAK_USER_ID);
    }

    @Test
    void getUserInventory_shouldThrowException_whenUserIdIsNull() {
        assertThrows(InvalidParameterException.class, () -> userService.getUserInventory(null));
    }

    @Test
    void getUserInventory_shouldReturnUserWithArtifacts() {
        List<Artifact> artifacts = List.of(Artifact.builder()
                .title("bite")
                .cacheId(1L)
                .userId(1L)
                .description("suiii")
                .imageUrl("imagedefou.png")
                .rarity(ArtifactRarity.LEGENDAIRE)
                .build());
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
        when(userPersistencePort.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.of(userEntity));
        when(keycloakPort.deleteUser(KEYCLOAK_USER_ID)).thenReturn(true);

        userService.deleteUser(KEYCLOAK_USER_ID);

        verify(userPersistencePort).deleteById(1L);
        verify(keycloakPort).deleteUser(KEYCLOAK_USER_ID);
    }

    @Test
    void deleteUser_shouldThrowException_whenKeycloakDeletionFails() {
        when(userPersistencePort.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.of(userEntity));
        when(keycloakPort.deleteUser(KEYCLOAK_USER_ID)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.deleteUser(KEYCLOAK_USER_ID));
    }

    @Test
    void deleteUser_shouldThrowException_whenKeycloakIdIsNull() {
        userEntity.setKeycloakId(null);
        when(userPersistencePort.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.of(userEntity));
        assertThrows(IllegalStateException.class, () -> userService.deleteUser(KEYCLOAK_USER_ID));
    }

    @Test
    void deleteUser_shouldThrowException_whenKeycloakIdIsEmpty() {
        userEntity.setKeycloakId("");
        when(userPersistencePort.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.of(userEntity));
        assertThrows(IllegalStateException.class, () -> userService.deleteUser(KEYCLOAK_USER_ID));
    }

    @Test
    void deleteUser_shouldThrowException_whenUserNotFound() {
        when(userPersistencePort.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.empty());
        assertThrows(InvalidParameterException.class, () -> userService.deleteUser(KEYCLOAK_USER_ID));
    }

    @Test
    void updateUser_shouldThrowException_whenUserNotFound() {
        when(userPersistencePort.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(userToUpdateDto, KEYCLOAK_USER_ID));
    }

    @Test
    void updateUser_shouldUpdateUser_whenUserExists() {
        when(userPersistencePort.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.of(userEntity));
        when(keycloakPort.updateUser(any(UserUpdatedDto.class))).thenReturn(userRepresentation);

        UserUpdatedDto result = userService.updateUser(userToUpdateDto, KEYCLOAK_USER_ID);

        assertEquals("username", result.getUsername());
        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("firstName", result.getFirstName());
        assertEquals("lastName", result.getLastName());

        verify(userPersistencePort).save(userEntity);
        verify(keycloakPort).updateUser(any(UserUpdatedDto.class));
    }

    @Test
    void updateUser_shouldUpdateOnlyUsername_whenOtherFieldsAreNull() {
        UserToUpdateDto partialUpdate = UserToUpdateDto.builder()
                .username("newUsername")
                .build();
        userEntity.setUsername("oldUsername");

        when(userPersistencePort.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.of(userEntity));
        when(keycloakPort.updateUser(any(UserUpdatedDto.class))).thenReturn(userRepresentation);

        userService.updateUser(partialUpdate, KEYCLOAK_USER_ID);
        assertEquals("newUsername", userEntity.getUsername());
        verify(userPersistencePort).save(userEntity);
    }

    @Test
    void updatePassword_shouldThrowException_whenUserNotFound() {
        when(userPersistencePort.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updatePassword("newPassword", KEYCLOAK_USER_ID));
    }

    @Test
    void updatePassword_shouldUpdatePassword_whenUserExists() {
        when(userPersistencePort.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.of(userEntity));
        when(keycloakPort.updatePassword(KEYCLOAK_USER_ID, "newPassword")).thenReturn(true);

        Boolean result = userService.updatePassword("newPassword", KEYCLOAK_USER_ID);

        assertTrue(result);
        verify(keycloakPort).updatePassword(KEYCLOAK_USER_ID, "newPassword");
    }

    @Test
    void updatePassword_shouldThrowException_whenKeycloakIdIsNull() {
        userEntity.setKeycloakId(null);
        when(userPersistencePort.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.of(userEntity));
        assertThrows(IllegalStateException.class, () -> userService.updatePassword("newPassword", KEYCLOAK_USER_ID));
    }

    @Test
    void updatePassword_shouldThrowException_whenKeycloakIdIsEmpty() {
        userEntity.setKeycloakId("");
        when(userPersistencePort.findByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Optional.of(userEntity));
        assertThrows(IllegalStateException.class, () -> userService.updatePassword("newPassword", KEYCLOAK_USER_ID));
    }
}
