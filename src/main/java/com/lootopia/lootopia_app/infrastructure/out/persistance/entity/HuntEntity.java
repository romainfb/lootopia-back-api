package com.lootopia.lootopia_app.infrastructure.out.persistance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Chasse")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HuntEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titre")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "mode")
    private String mode;

    @Column(name = "difficulte")
    private String difficulty;

    @Column(name = "frais_participation")
    private Integer participationFees;

    @Column(name = "chat_actif")
    private Boolean chatEnabled;

    @Column(name = "organisateur_id")
    private Integer organizerId;

    @Column(name = "monde")
    private String world;

    @Column(name = "duree")
    private Integer duration;

    @Column(name="nombre_participants")
    private Integer numberOfParticipants;
}

