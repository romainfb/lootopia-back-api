package com.lootopia.lootopia_app.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Artifact {
    private Long id;
    private String nom;
    private String rarete;
    private String description;
    private String image;
    private Long userId;
}
