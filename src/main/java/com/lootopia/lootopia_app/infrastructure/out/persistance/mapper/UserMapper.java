package com.lootopia.lootopia_app.infrastructure.out.persistance.mapper;

import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;

public class UserMapper {
    public static User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .accountType(entity.getAccountType())
                .username(entity.getUsername())
                .balance(entity.getBalance())
                .build();
    }

    public static UserEntity toEntity(User domain) {
        return UserEntity.builder()
                .id(domain.getId())
                .username(domain.getUsername())
                .accountType(domain.getAccountType())
                .balance(domain.getBalance())
                .build();
    }
}
