package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.User;

import java.util.Optional;

public interface GetUserUseCase {
    Optional<User> getUserById(Long id_user);
    Optional<User> getUserInventory(Long id_user);
}
