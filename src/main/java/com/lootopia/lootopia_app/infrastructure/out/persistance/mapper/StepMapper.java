package com.lootopia.lootopia_app.infrastructure.out.persistance.mapper;

import com.lootopia.lootopia_app.domain.model.Step;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.StepEntity;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Component
public class StepMapper {

    public Step toDomain(StepEntity entity) {
        if (entity == null) return null;
        return Step.builder()
                .id(entity.getId())
                .huntId(entity.getHuntId())
                .description(entity.getDescription())
                .isFinal(entity.getIsFinal())
                .validationType(entity.getTypeClefValidation())
                .validatedUsersId(entity.getValidatedUserIds() != null ? entity.getValidatedUserIds() : new HashSet<>())
                .build();
    }

    public StepEntity toEntity(Step step) {
        if (step == null) return null;
        return StepEntity.builder()
                .id(step.getId())
                .huntId(step.getHuntId())
                .description(step.getDescription())
                .isFinal(step.getIsFinal())
                .typeClefValidation(step.getValidationType())
                .validatedUserIds(step.getValidatedUsersId() != null ? step.getValidatedUsersId() : new HashSet<>())
                .build();
    }
}
