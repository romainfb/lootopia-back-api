package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.GetArtifactsByUserIdUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserByIdUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserInventoryByIdUseCase;
import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements GetUserByIdUseCase, GetUserInventoryByIdUseCase {

    private final UserPersistencePort userPersistencePort;
    private final GetArtifactsByUserIdUseCase getArtifactsByUserIdUseCase;

    @Override
    public Optional<User> getUserById(Long id_user) {
        return userPersistencePort.findById(id_user);
    }

    @Override
    public Optional<User> getUserInventory(Long id_user) {
        Optional<User> user = userPersistencePort.findById(id_user);
        user.ifPresent(u -> u.setArtifacts(getArtifactsByUserIdUseCase.getArtifactsByUserId(id_user)));
        return user;
    }
}
