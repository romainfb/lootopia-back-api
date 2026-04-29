package com.lootopia.lootopia_app.infrastructure.out.persistance;

import com.lootopia.lootopia_app.application.port.out.OutboxEventPort;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.OutboxEventEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OutboxEventAdapter implements OutboxEventPort {

    private final OutboxEventRepository repository;

    @Override
    public void append(String aggregateType, Long aggregateId, String eventType, String jsonPayload) {
        OutboxEventEntity entity = OutboxEventEntity.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(jsonPayload)
                .createdAt(Instant.now())
                .build();
        repository.save(entity);
    }
}
