package com.lootopia.lootopia_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.SecureRandom;
import java.time.Clock;
import java.util.Random;

@Configuration
public class AdChestConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public Random adChestRandom() {
        return new SecureRandom();
    }
}
