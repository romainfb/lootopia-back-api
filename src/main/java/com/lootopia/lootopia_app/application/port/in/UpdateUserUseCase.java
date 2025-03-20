package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserToUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;

public interface UpdateUserUseCase {
    UserUpdatedDto updateUser(UserToUpdateDto userToUpdateDto, String id_user);
    Boolean updatePassword(String password, String id_user);
}
