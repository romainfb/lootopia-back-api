package com.lootopia.lootopia_app.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Participation {
    private Long id;
    private String statut;
    private Timestamp dateInscription;
    private Integer organizerId;
    private Integer userId;
    private Integer huntId;

}
