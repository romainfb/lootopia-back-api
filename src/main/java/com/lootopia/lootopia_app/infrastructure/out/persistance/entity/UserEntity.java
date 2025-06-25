package com.lootopia.lootopia_app.infrastructure.out.persistance.entity;

import com.lootopia.lootopia_app.domain.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "utilisateur")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = true)
    private String username;

    @Column(name = "keycloak_id", nullable = true)
    private String keycloakId;

    @Column(name = "type_compte", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "solde_couronnes", nullable = false)
    private Integer balance;

    @Column(name = "image_url", nullable = true, length = 1000)
    private String imageUrl;
}
