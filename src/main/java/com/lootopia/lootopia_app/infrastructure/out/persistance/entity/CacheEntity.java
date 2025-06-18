package com.lootopia.lootopia_app.infrastructure.out.persistance.entity;

import com.lootopia.lootopia_app.domain.model.Coordinates;
import com.lootopia.lootopia_app.utils.CoordinatesConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cache")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CacheEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chasse_id", nullable = false)
    private Long huntId;

    @Column(name = "coordonnees_gps", nullable = false)
    @Convert(converter = CoordinatesConverter.class)
    private Coordinates coordinatesGps;

    @Column(name = "artefact_id")
    private Long artefactId;
}

