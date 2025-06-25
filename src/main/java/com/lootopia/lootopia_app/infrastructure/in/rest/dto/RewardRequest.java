package com.lootopia.lootopia_app.infrastructure.in.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RewardRequest {
    private Long chasseId;
    private String type;
    private BigDecimal valeur;
    private String description;
    private String imageUrl;
    private String rarity;
    private MultipartFile image;
}
