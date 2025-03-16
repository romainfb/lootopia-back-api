package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserKeycloakUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdateDto;

public interface UpdateUserUseCase {
    UserKeycloakUpdateDto updateUser(UserUpdateDto userUpdateDto, Long userId);
}
