package com.lootopia.lootopia_app.application.port.in;

public interface JwtServiceUseCase {

    String getUserIdFromToken(String accessToken);

    String decodeJwt(String accessToken);
}
