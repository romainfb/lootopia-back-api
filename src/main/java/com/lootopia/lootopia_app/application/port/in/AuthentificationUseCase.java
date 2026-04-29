package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.infrastructure.in.rest.dto.RegisterRequestDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.TokenResponseDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserInfoDto;

public interface AuthentificationUseCase {

    TokenResponseDto register(RegisterRequestDto request);

    TokenResponseDto login(String email, String password);

    TokenResponseDto refresh(String refreshToken);

    UserInfoDto getUserInfo(Long userId);
}
