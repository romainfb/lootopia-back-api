package com.lootopia.lootopia_app.infrastructure.in.rest.mapper;

import com.lootopia.lootopia_app.domain.model.Artifact;
import com.lootopia.lootopia_app.domain.model.Step;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.ArtifactRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.StepRequest;
import org.springframework.stereotype.Component;


@Component("restStepMapper")
public class StepMapper {

    public Step StepRequestToStep(StepRequest request, Long huntId){

        if(request == null) return null;

        return Step.builder()
                .huntId(huntId)
                .isFinal(request.isFinal())
                .description(request.getDescription())
                .validationType(request.getTypeClefValidation())
                .build();

    }
}
