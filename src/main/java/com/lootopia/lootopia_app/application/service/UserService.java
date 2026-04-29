package com.lootopia.lootopia_app.application.service;


import com.lootopia.lootopia_app.application.port.in.DeleteUserUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchAllUsersUseCase;
import com.lootopia.lootopia_app.application.port.in.GetArtifactsUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateUserUseCase;
import com.lootopia.lootopia_app.application.port.out.AzureBlobStoragePort;
import com.lootopia.lootopia_app.application.port.out.KeycloakPort;
import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.domain.model.UserInventory;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserToUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements GetUserUseCase, DeleteUserUseCase, UpdateUserUseCase, FetchAllUsersUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserPersistencePort userPersistencePort;
    private final GetArtifactsUseCase getArtifactsUseCase;
    private final AzureBlobStoragePort azureBlobStoragePort;

    @Override
    public Optional<User> getUserById(Long id_user) {
        if (id_user == null) throw new InvalidParameterException("User ID cannot be null");
        return userPersistencePort.findById(id_user)
            .map(UserMapper::toDomain)
            .or(() -> {
                throw new ResourceNotFoundException("User", "id", id_user);
            });
    }

    @Override
    public Optional<User> getUserByKeycloakId(String keycloakId) {
        if (keycloakId==null) throw new InvalidParameterException("Keycloak ID cannot be null");
        return userPersistencePort.findByKeycloakId(keycloakId).map(userEntity -> {
            User user = UserMapper.toDomain(userEntity);

            // If the user has an imageUrl, generate a fresh SAS URL
            if (user.getImageUrl()!=null && !user.getImageUrl().isEmpty()) {
                try {
                    // Extract the filename from the URL
                    String imageUrl = user.getImageUrl();
                    String fileName;

                    // Check if the URL contains a SAS token (indicated by a '?' character)
                    if (imageUrl.contains("?")) {
                        // Extract the filename from the URL (everything before the '?')
                        fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.indexOf("?"));
                    } else {
                        // Extract the filename from the URL (everything after the last '/')
                        fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                    }

                    log.info("Generating fresh SAS URL for user image with filename: {}", fileName);

                    // Generate a fresh SAS URL for the image
                    String sasUrl = azureBlobStoragePort.generateSasUrl(fileName);
                    if (sasUrl!=null) {
                        // Update the user's imageUrl with the fresh SAS URL
                        user.setImageUrl(sasUrl);
                        log.info("Updated user image URL with fresh SAS URL: {}", sasUrl);
                    } else {
                        log.warn("Failed to generate SAS URL for image: {}", fileName);
                    }
                } catch (Exception e) {
                    log.error("Error generating SAS URL for user image: {}", e.getMessage());
                    // If there's an error, we still return the user with the original imageUrl
                    // The client will handle the case where the image can't be loaded
                }
            }

            return user;
        });
    }

    @Override
    public Optional<UserInventory> getUserInventory(Long id_user) {
        if (id_user==null) throw new InvalidParameterException("User ID cannot be null");
        return userPersistencePort.findById(id_user).map(entity -> UserInventory.builder()
                .id(id_user)
                .artifacts(getArtifactsUseCase.getArtifactsByUserId(id_user))
                .build());
    }

    @Override
    public void deleteUser(Long id_user) {
        if (userPersistencePort.findById(id_user).isEmpty()) {
            throw new ResourceNotFoundException("User", "id", id_user);
        }
        userPersistencePort.deleteById(id_user);
    }

    @Override
    public UserUpdatedDto updateUser(UserToUpdateDto user, Long id_user) {
        log.info("Updating user with id : {}", id_user);
        UserEntity userEntity = userPersistencePort.findById(id_user)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id_user));

        if (user.getUsername() != null) {
            userEntity.setUsername(user.getUsername());
        }

        if (user.getProfileImage()!=null && !user.getProfileImage().isEmpty()) {
            try {
                deleteExistingProfileImage(userEntity);
                MultipartFile imageFile = user.getProfileImage();
                String fileName = id_user + "_profile_" + imageFile.getOriginalFilename();

                // Upload the image to Azure Blob Storage
                String imageUrl = azureBlobStoragePort.uploadFile(imageFile, fileName);

                // Update the user entity with the image URL
                userEntity.setImageUrl(imageUrl);
                log.info("Profile image uploaded successfully. URL: {}", imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload profile image: " + e.getMessage(), e);
            }
        }

        UserEntity saved = userPersistencePort.save(userEntity);
        return UserUpdatedDto.builder()
                .id(saved.getId())
                .username(saved.getUsername())
                .build();
    }

    @Override
    public Boolean updatePassword(String password, Long userId) {
        UserEntity userEntity = userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        userEntity.setPasswordHash(passwordEncoder.encode(password));
        userPersistencePort.save(userEntity);
        return true;
    }

    @Override
    public List<UserRepresentation> getAllUsers() {
        log.info("Fetching all users from Keycloak");
        try {
            List<UserRepresentation> users = keycloakPort.getAllUsers();
            log.info("Successfully fetched {} users from Keycloak", users.size());
            return users;
        } catch (Exception e) {
            log.error("Error fetching all users from Keycloak", e);
            throw new RuntimeException("Failed to fetch users from Keycloak: " + e.getMessage(), e);
        }
        return entity;
    }

}
