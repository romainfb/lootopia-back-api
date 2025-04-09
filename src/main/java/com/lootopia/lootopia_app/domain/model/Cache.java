package com.lootopia.lootopia_app.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Cache {
    private Long id;
    private Long huntId;
    private Coordinates coordinatesGps;
    private Long artefactId;
}
