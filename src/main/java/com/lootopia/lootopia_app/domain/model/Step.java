package com.lootopia.lootopia_app.domain.model;

import com.lootopia.lootopia_app.domain.TypeClefValidation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Step {
    private Long id;
    private Long huntId;
    private String description;
    private Boolean isFinal;
    private TypeClefValidation validationType;
    private Set<Long> validatedUsersId;
}
