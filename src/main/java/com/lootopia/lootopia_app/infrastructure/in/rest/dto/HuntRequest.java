package com.lootopia.lootopia_app.infrastructure.in.rest.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class HuntRequest {
    @NotNull(message = "Le titre est obligatoire")
    private String title;
    private String description;

    @NotNull(message = "Le mode est obligatoire")
    private String mode;

    @NotNull(message = "La difficulté est obligatoire")
    private String difficulty;

    @PositiveOrZero(message = "Les frais de participation doivent être positifs ou 0")
    private Integer participationFees;
    private Boolean chatEnabled;

    @NotNull(message = "La monde est obligatoire")
    private String world;

    @NotNull(message = "La durée est obligatoire")
    private Integer duration;

    @NotNull(message = "L'organisateur est obligatoire")
    private Integer organizerId;
}
