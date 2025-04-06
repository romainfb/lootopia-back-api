package com.lootopia.lootopia_app.infrastructure.out.persistance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "etapes")
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

    @Column(name = "titre")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "validee")
    private Boolean validated;

}
