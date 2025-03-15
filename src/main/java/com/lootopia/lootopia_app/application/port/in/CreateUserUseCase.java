package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserRegisterFromKeycloakDto;
import org.springframework.stereotype.Component;

@Component
public interface CreateUserUseCase {
    User createUser(UserRegisterFromKeycloakDto user);
}
