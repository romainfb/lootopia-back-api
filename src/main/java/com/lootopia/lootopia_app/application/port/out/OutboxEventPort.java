package com.lootopia.lootopia_app.application.port.out;

public interface OutboxEventPort {
    void append(String aggregateType, Long aggregateId, String eventType, String jsonPayload);
}
