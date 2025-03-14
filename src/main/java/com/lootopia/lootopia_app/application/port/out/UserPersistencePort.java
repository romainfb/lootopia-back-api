package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.model.User;
import java.util.Optional;

public interface UserPersistencePort {
    Optional<User> findById(Long id);
}
