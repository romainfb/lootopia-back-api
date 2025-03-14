package com.lootopia.lootopia_app.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "Chasse")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Hunt {

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
}
