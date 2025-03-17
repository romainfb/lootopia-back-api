package com.lootopia.lootopia_app.infrastructure.out.keycloak;

import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceAdapterTest {


    @InjectMocks
    private KeycloakServiceAdapter keycloakServiceAdapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void getUserById_ShouldReturnEmpty_WhenUserIdIsNull() {
        // Act
        Optional<UserRepresentation> result = keycloakServiceAdapter.getUserById(null);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getUserById_ShouldReturnEmpty_WhenUserIdIsBlank() {
        // Act
        Optional<UserRepresentation> result = keycloakServiceAdapter.getUserById("");

        // Assert
        assertFalse(result.isPresent());
    }


    @Test
    void deleteUser_ShouldReturnFalse_WhenUserIdIsNull() {
        // Act
        boolean result = keycloakServiceAdapter.deleteUser(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void deleteUser_ShouldReturnFalse_WhenUserIdIsBlank() {
        // Act
        boolean result = keycloakServiceAdapter.deleteUser("");

        // Assert
        assertFalse(result);
    }


    @Test
    void updateUser_ShouldThrowException_WhenUserDtoIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> keycloakServiceAdapter.updateUser(null));
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserIdIsNull() {
        // Arrange
        UserUpdatedDto userDto = new UserUpdatedDto(null, "test-username", "test@example.com", "Test", "User");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> keycloakServiceAdapter.updateUser(userDto));
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserIdIsBlank() {
        // Arrange
        UserUpdatedDto userDto = new UserUpdatedDto("", "test-username", "test@example.com", "Test", "User");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> keycloakServiceAdapter.updateUser(userDto));
    }



    @Test
    void updatePassword_ShouldReturnFalse_WhenUserIdIsNull() {
        // Act
        boolean result = keycloakServiceAdapter.updatePassword(null, "new-password");

        // Assert
        assertFalse(result);
    }

    @Test
    void updatePassword_ShouldReturnFalse_WhenUserIdIsBlank() {
        // Act
        boolean result = keycloakServiceAdapter.updatePassword("", "new-password");

        // Assert
        assertFalse(result);
    }

    @Test
    void updatePassword_ShouldReturnFalse_WhenPasswordIsNull() {
        // Act
        boolean result = keycloakServiceAdapter.updatePassword("test-user-id", null);

        // Assert
        assertFalse(result);
    }

    @Test
    void updatePassword_ShouldReturnFalse_WhenPasswordIsBlank() {
        // Act
        boolean result = keycloakServiceAdapter.updatePassword("test-user-id", "");

        // Assert
        assertFalse(result);
    }


    @Test
    void getAccessToken_ShouldThrowException_WhenCodeIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> keycloakServiceAdapter.getAccessToken(null));
    }

    @Test
    void getAccessToken_ShouldThrowException_WhenCodeIsBlank() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> keycloakServiceAdapter.getAccessToken(""));
    }


    @Test
    void logout_ShouldReturnFalse_WhenAccessTokenIsNull() {
        // Act
        boolean result = keycloakServiceAdapter.logout(null);

        // Assert
        assertFalse(result);
    }

    @Test
    void logout_ShouldReturnFalse_WhenAccessTokenIsBlank() {
        // Act
        boolean result = keycloakServiceAdapter.logout("");

        // Assert
        assertFalse(result);
    }

}
