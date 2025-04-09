package com.lootopia.lootopia_app.infrastructure.out.persistance.entity;

import com.lootopia.lootopia_app.domain.TypeClefValidation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "etape")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StepEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chasse_id")
    private Long huntId;

    @Column(name = "description")
    private String description;

    @Column(name = "est_finale")
    private Boolean isFinal;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_clef_validation")
    private TypeClefValidation typeClefValidation;

    @ElementCollection
    @CollectionTable(
            name = "etape_utilisateur_validation",
            joinColumns = @JoinColumn(name = "etape_id")
    )
    @Column(name = "utilisateur_id")
    private Set<Long> validatedUserIds = new HashSet<>();

}
