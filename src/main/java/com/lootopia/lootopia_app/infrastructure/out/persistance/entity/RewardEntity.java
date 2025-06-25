package com.lootopia.lootopia_app.infrastructure.out.persistance.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "recompense")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RewardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chasse_id")
    private Long chasseId;

    @Column(name = "utilisateur_id", nullable = false)
    private Long utilisateurId;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "valeur")
    private BigDecimal valeur;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;
}
