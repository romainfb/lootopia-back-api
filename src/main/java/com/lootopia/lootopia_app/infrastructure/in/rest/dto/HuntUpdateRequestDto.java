package com.lootopia.lootopia_app.infrastructure.in.rest.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HuntUpdateRequestDto {
    private String title;
    private String description;
    private String mode;
    private String difficulty;
    private Integer participationFees;
    private Boolean chatEnabled;
    private Integer organizerId;
    private String world;
    private Integer duration;
}
