package com.lootopia.lootopia_app.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Reward {
    private Long id;
    private Long chasseId;
    private Long utilisateurId;
    private String type;
    private BigDecimal valeur;
    private String description;
}
