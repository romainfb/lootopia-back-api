package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserToUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;
import org.springframework.security.oauth2.jwt.Jwt;

public interface UpdateUserUseCase {
    UserUpdatedDto updateUser(UserToUpdateDto userToUpdateDto, Jwt jwt);
    Boolean updatePassword(String password, Jwt jwt);
}
