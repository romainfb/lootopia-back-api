package com.lootopia.lootopia_app.application.port.in;

import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserInfoDto;
import org.keycloak.representations.AccessTokenResponse;

public interface AuthentificationUseCase {

    AccessTokenResponse exchangeCodeForToken(String code);

    Boolean logout(String accessToken);

    UserInfoDto getUserInfo(String userId);

    AccessTokenResponse loginUser(String username, String password);

}
