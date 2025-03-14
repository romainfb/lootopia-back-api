package com.lootopia.lootopia_app.infrastructure.out.persistance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Utilisateur")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String password;

    @Column(name = "pseudo", unique = true, nullable = false)
    private String username;

    @Column(name = "type_compte", nullable = false)
    private String accountType;

    @Column(name = "solde_couronnes", nullable = false)
    private Integer balance;

    @Column(name = "historique_activites")
    private String activityHistory;
}
