package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.domain.AccountType;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserToUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserPersistencePort userPersistencePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = UserEntity.builder()
                .id(1L)
                .username("testUser")
                .email("user@test.com")
                .accountType(AccountType.USER)
                .balance(0)
                .enabled(true)
                .build();
    }

    @Test
    void getUserById_throws_when_id_is_null() {
        assertThrows(InvalidParameterException.class, () -> userService.getUserById(null));
    }

    @Test
    void getUserById_returns_user_when_found() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userEntity));
        Optional<User> result = userService.getUserById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("testUser");
    }

    @Test
    void getUserById_throws_when_not_found() {
        when(userPersistencePort.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void deleteUser_deletes_when_user_exists() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userEntity));
        userService.deleteUser(1L);
        verify(userPersistencePort).deleteById(1L);
    }

    @Test
    void deleteUser_throws_when_not_found() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void updateUser_throws_when_not_found() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.empty());
        UserToUpdateDto dto = UserToUpdateDto.builder().username("new").build();
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(dto, 1L));
    }

    @Test
    void updateUser_updates_username() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userEntity));
        when(userPersistencePort.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        UserToUpdateDto dto = UserToUpdateDto.builder().username("newName").build();

        UserUpdatedDto result = userService.updateUser(dto, 1L);

        assertThat(result.getUsername()).isEqualTo("newName");
        assertThat(userEntity.getUsername()).isEqualTo("newName");
    }

    @Test
    void updatePassword_encodes_and_saves() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.encode("newPassword")).thenReturn("{bcrypt}HASH");
        when(userPersistencePort.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Boolean result = userService.updatePassword("newPassword", 1L);

        assertThat(result).isTrue();
        assertThat(userEntity.getPasswordHash()).isEqualTo("{bcrypt}HASH");
        verify(userPersistencePort).save(userEntity);
    }

    @Test
    void updatePassword_throws_when_user_not_found() {
        when(userPersistencePort.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updatePassword("p", 1L));
    }

    @Test
    void getAllUsers_maps_entities_to_domain() {
        when(userPersistencePort.findAll()).thenReturn(List.of(userEntity));
        List<User> result = userService.getAllUsers();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getUsername()).isEqualTo("testUser");
    }
}
