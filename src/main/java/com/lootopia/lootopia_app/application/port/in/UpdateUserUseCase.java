package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserToUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;

public interface UpdateUserUseCase {
    UserUpdatedDto updateUser(UserToUpdateDto userToUpdateDto, Long userId);
    Boolean updatePassword(String password, Long userId);
}
