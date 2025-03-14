package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
