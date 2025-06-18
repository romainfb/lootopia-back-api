package com.lootopia.lootopia_app.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Hunt {
    private Long id;
    private String title;
    private String description;
    private String mode;
    private String difficulty;
    private Integer participationFees;
    private Boolean chatEnabled;
    private Integer organizerId;
    private String world;
    private Integer duration;
    private Integer numberOfParticipants;

}
