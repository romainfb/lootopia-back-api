package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.domain.model.UserInventory;

import java.util.Optional;

public interface GetUserUseCase {
    Optional<User> getUserById(Long id_user);
    Optional<UserInventory> getUserInventory(Long id_user);
}
