package com.lootopia.lootopia_app.infrastructure.in.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.Duration;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class CooldownActiveException extends RuntimeException {

    private final Duration retryAfter;

    public CooldownActiveException(String message, Duration retryAfter) {
        super(message);
        this.retryAfter = retryAfter;
    }

    public Duration getRetryAfter() {
        return retryAfter;
    }
}
