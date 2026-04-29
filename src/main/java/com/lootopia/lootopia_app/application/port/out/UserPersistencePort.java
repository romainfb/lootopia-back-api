package com.lootopia.lootopia_app.application.port.out;

import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;

import java.util.Optional;

public interface UserPersistencePort {
    Optional<UserEntity> findById(Long id);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    UserEntity save(UserEntity user);
    void deleteById(Long id);

    java.util.List<UserEntity> findAll();
}
