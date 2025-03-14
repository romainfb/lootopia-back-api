package com.lootopia.lootopia_app.infrastructure.out.persistance.mapper;

import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;

public class UserMapper {
    public static User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .username(entity.getUsername())
                .accountType(entity.getAccountType())
                .balance(entity.getBalance())
                .activityHistory(entity.getActivityHistory())
                .build();
    }

    public static UserEntity toEntity(User domain) {
        return UserEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .password(domain.getPassword())
                .username(domain.getUsername())
                .accountType(domain.getAccountType())
                .balance(domain.getBalance())
                .activityHistory(domain.getActivityHistory())
                .build();
    }
}
