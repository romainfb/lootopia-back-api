package com.lootopia.lootopia_app.infrastructure.out.persistance.entity;

import com.lootopia.lootopia_app.domain.ParticipationStatut;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name = "Participation")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "statut")
    @Enumerated(EnumType.STRING)
    private ParticipationStatut statut;

    @Column(name = "date_inscription")
    private Timestamp dateInscription;

    @Column(name = "organisateur_id")
    private Integer organizerId;

    @Column(name = "utilisateur_id")
    private Integer userId;

    @Column(name = "chasse_id")
    private Integer huntId;

}

