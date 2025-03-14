package com.lootopia.lootopia_app.infrastructure.out.persistance.entity;

import com.lootopia.lootopia_app.domain.AccountType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "utilisateur")  // Noms de table en minuscules par convention
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_compte", nullable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "solde_couronnes", nullable = false)
    private Integer balance;
}
