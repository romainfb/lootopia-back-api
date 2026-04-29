package com.lootopia.lootopia_app.infrastructure.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "enchere_offre")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "enchere_id", nullable = false)
    private Long enchereId;

    @Column(name = "encherisseur_id", nullable = false)
    private Long encherisseurId;

    @Column(name = "montant", nullable = false)
    private Integer montant;

    @Column(name = "place_at", nullable = false, updatable = false)
    private Instant placeAt;

    @PrePersist
    protected void onCreate() {
        if (placeAt==null) {
            placeAt = Instant.now();
        }
    }
}
