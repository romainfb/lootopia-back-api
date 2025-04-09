package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import com.lootopia.lootopia_app.domain.TypeClefValidation;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StepRequest {
    private String description;
    private boolean isFinal;
    private TypeClefValidation typeClefValidation;
}