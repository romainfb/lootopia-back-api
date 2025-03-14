package com.lootopia.lootopia_app.infrastructure.in.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LootopiaController {
    @GetMapping
    public Map<String, String> getApiStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("etat", "ok");
        return response;
    }
}
