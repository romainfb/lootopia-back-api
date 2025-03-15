package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.CreateUserUseCase;
import com.lootopia.lootopia_app.application.port.in.GetArtifactsUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.domain.AccountType;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserRegisterFromKeycloakDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements GetUserUseCase, CreateUserUseCase {

    private final UserPersistencePort userPersistencePort;
    private final GetArtifactsUseCase getArtifactsUseCase;

    @Override
    public Optional<User> getUserById(Long id_user) {
        if (id_user==null) throw new InvalidParameterException("User ID cannot be null");
        return userPersistencePort.findById(id_user);
    }

    @Override
    public Optional<User> getUserInventory(Long id_user) {
        if (id_user==null) throw new InvalidParameterException("User ID cannot be null");
        Optional<User> user = userPersistencePort.findById(id_user);
        user.ifPresent(u -> u.setArtifacts(getArtifactsUseCase.getArtifactsByUserId(id_user)));
        return user;
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
}
