package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserToUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;

public interface UpdateUserUseCase {
    UserUpdatedDto updateUser(UserToUpdateDto userToUpdateDto, Long id_user);

    Boolean updatePassword(String password, Long id_user);
}
