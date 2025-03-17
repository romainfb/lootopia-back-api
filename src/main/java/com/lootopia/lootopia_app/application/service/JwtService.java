package com.lootopia.lootopia_app.application.service;

import com.lootopia.lootopia_app.application.port.in.JwtServiceUseCase;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
@Service
public class JwtService implements JwtServiceUseCase {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Override
    public String getUserIdFromToken(String accessToken) {
        log.info("Decoding JWT token");
        return decodeJwt(accessToken);
    }

    public String decodeJwt(String accessToken) {
        try {
            // Split JWT into its three parts
            String[] parts = accessToken.split("\\.");

            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT token format");
            }

            // Decode the payload (second part of JWT)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

            // Parse the JSON payload
            JSONObject json = new JSONObject(payload);

            // Extract the user ID ('sub' field)

            return json.getString("sub");
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode the JWT token", e);
        }
    }
}
