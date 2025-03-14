package com.lootopia.lootopia_app.infrastructure.in.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/keycloak-event/")
public class KeycloakController {
    @PostMapping("/register")
    public Map<String, String> getApiStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("keycloackevent", "ok");
        return response;
    }
}
