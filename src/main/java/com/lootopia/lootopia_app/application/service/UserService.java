package com.lootopia.lootopia_app.application.service;


import com.lootopia.lootopia_app.application.port.in.CreateUserUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteUserUseCase;
import com.lootopia.lootopia_app.application.port.in.GetArtifactsUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateUserUseCase;
import com.lootopia.lootopia_app.application.port.out.KeycloakPort;
import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.domain.AccountType;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.domain.model.UserInventory;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserKeycloakUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserRegisterFromKeycloakDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements GetUserUseCase, CreateUserUseCase, DeleteUserUseCase, UpdateUserUseCase {

    private final UserPersistencePort userPersistencePort;
    private final KeycloakPort keycloakPort;
    private final GetArtifactsUseCase getArtifactsUseCase;

    @Override
    public Optional<User> getUserById(Long id_user) {
        if (id_user==null) throw new InvalidParameterException("User ID cannot be null");
        return userPersistencePort.findById(id_user);
    }

    @Override
    public Optional<UserInventory> getUserInventory(Long id_user) {
        if (id_user==null) throw new InvalidParameterException("User ID cannot be null");
        Optional<User> user = userPersistencePort.findById(id_user);
        if (user.isPresent()) {
            UserInventory userInventory = UserInventory.builder()
                    .id(id_user)
                    .artifacts(getArtifactsUseCase.getArtifactsByUserId(id_user))
                    .build();
            return Optional.of(userInventory);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public User createUser(UserRegisterFromKeycloakDto user) {
        if (user==null || user.getId()==null || user.getUsername()==null) {
            throw new InvalidParameterException("User data cannot be null");
        }
        UserEntity newUser = UserEntity.builder()
                .keycloakId(user.getId())
                .username(user.getUsername())
                .accountType(AccountType.USER)
                .balance(0)
                .build();
        return userPersistencePort.save(newUser);
    }

    @Override
    public void deleteUser(Long id_user) {
        if (id_user == null) throw new InvalidParameterException("User ID cannot be null");
        User user = userPersistencePort.findById(id_user)
                .orElseThrow(() -> new InvalidParameterException("User with ID " + id_user + " not found"));
        if (keycloakPort.deleteUser(user.getKeycloakId())) {
            userPersistencePort.deleteById(id_user);
        } else {
            throw new RuntimeException("Failed to delete user from Keycloak");
        }
    }

    @Override
    public UserKeycloakUpdateDto updateUser(UserUpdateDto user, Long id_user) {
        User existingUser = userPersistencePort.findById(id_user)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id_user));

        if (user.getUsername() != null) {
            existingUser.setUsername(user.getUsername());
            userPersistencePort.save(UserMapper.toEntity(existingUser));
        }

        UserKeycloakUpdateDto keycloakUpdateDto = UserKeycloakUpdateDto.builder()
                .id(existingUser.getKeycloakId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();

        keycloakPort.updateUser(keycloakUpdateDto);
        return keycloakUpdateDto;
    }

    @Override
    public Boolean updatePassword(String password, Long userId) {
        User user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return keycloakPort.updatePassword(user.getKeycloakId(), password);
    }


}
