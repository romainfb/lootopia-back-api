package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;

import java.util.Optional;

public interface UserPersistencePort {
    Optional<User> findById(Long id);
    User save(UserEntity user);
    void deleteById(Long id);
}
