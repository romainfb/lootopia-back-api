package com.lootopia.lootopia_app.infrastructure.out.persistance.entity;

import com.lootopia.lootopia_app.domain.AuctionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "enchere")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "artefact_id", nullable = false)
    private Long artefactId;

    @Column(name = "vendeur_id", nullable = false)
    private Long vendeurId;

    @Column(name = "prix_depart", nullable = false)
    private Integer prixDepart;

    @Column(name = "prix_actuel", nullable = false)
    private Integer prixActuel;

    @Column(name = "increment_min", nullable = false)
    private Integer incrementMin;

    @Column(name = "gagnant_actuel_id")
    private Long gagnantActuelId;

    @Column(name = "debut_at", nullable = false)
    private Instant debutAt;

    @Column(name = "fin_at", nullable = false)
    private Instant finAt;

    @Column(name = "statut", nullable = false)
    @Enumerated(EnumType.STRING)
    private AuctionStatus statut;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt==null) {
            createdAt = Instant.now();
        }
    }
}
