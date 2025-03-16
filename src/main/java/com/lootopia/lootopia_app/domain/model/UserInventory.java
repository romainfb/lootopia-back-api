package com.lootopia.lootopia_app.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInventory {
    private Long id;
    private Integer balance;
    private List<Artifact> artifacts;
}
