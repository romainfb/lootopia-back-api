package com.lootopia.lootopia_app.application.port.in;

import org.springframework.security.oauth2.jwt.Jwt;

public interface DeleteUserUseCase {
    void deleteUser(Jwt jwt);
}
