package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.User;

import java.util.List;

public interface FetchAllUsersUseCase {
    List<User> getAllUsers();
}
