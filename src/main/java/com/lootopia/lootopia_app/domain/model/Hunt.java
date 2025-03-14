package com.lootopia.lootopia_app.domain.model;

import jakarta.persistence.*;
import lombok.*;

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
