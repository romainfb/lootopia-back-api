package com.lootopia.lootopia_app.infrastructure.in.rest.mapper;

import com.lootopia.lootopia_app.application.port.out.AzureBlobStoragePort;
import com.lootopia.lootopia_app.domain.model.Reward;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.RewardRequest;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.RewardResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RewardMapper {

    private final AzureBlobStoragePort azureBlobStoragePort;

    public Reward toReward(RewardRequest request) {
        String imageUrl = request.getImageUrl();

        // Handle image upload if provided
        MultipartFile image = request.getImage();
        if (image!=null && !image.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
                imageUrl = azureBlobStoragePort.uploadRewardFile(image, fileName);
                log.info("Uploaded reward image: {}", imageUrl);
            } catch (IOException e) {
                log.error("Failed to upload reward image", e);
                // Keep the existing imageUrl if upload fails
            }
        }

        return Reward.builder()
                .chasseId(request.getChasseId())
                .type(request.getType())
                .valeur(request.getValeur())
                .description(request.getDescription())
                .imageUrl(imageUrl)
                .rarity(request.getRarity())
                .build();
    }

    public RewardResponse toResponse(Reward reward) {
        return RewardResponse.builder()
                .id(reward.getId())
                .chasseId(reward.getChasseId())
                .utilisateurId(reward.getUtilisateurId())
                .type(reward.getType())
                .valeur(reward.getValeur())
                .description(reward.getDescription())
                .imageUrl(reward.getImageUrl())
                .rarity(reward.getRarity())
                .build();
    }
}
