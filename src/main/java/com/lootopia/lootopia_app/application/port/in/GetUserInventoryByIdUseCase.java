package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.User;

import java.util.Optional;

public interface GetUserInventoryByIdUseCase {
    Optional<User> getUserInventory(Long id_user);
}
