package com.lootopia.lootopia_app.application.service;


import com.lootopia.lootopia_app.application.port.in.CreateUserUseCase;
import com.lootopia.lootopia_app.application.port.in.DeleteUserUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchAllUsersUseCase;
import com.lootopia.lootopia_app.application.port.in.GetArtifactsUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateUserUseCase;
import com.lootopia.lootopia_app.application.port.out.AzureBlobStoragePort;
import com.lootopia.lootopia_app.application.port.out.KeycloakPort;
import com.lootopia.lootopia_app.application.port.out.UserPersistencePort;
import com.lootopia.lootopia_app.domain.AccountType;
import com.lootopia.lootopia_app.domain.model.User;
import com.lootopia.lootopia_app.domain.model.UserInventory;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserRegisterFromKeycloakDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserToUpdateDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.dto.UserUpdatedDto;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.InvalidParameterException;
import com.lootopia.lootopia_app.infrastructure.in.rest.exception.ResourceNotFoundException;
import com.lootopia.lootopia_app.infrastructure.out.persistance.entity.UserEntity;
import com.lootopia.lootopia_app.infrastructure.out.persistance.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements GetUserUseCase, CreateUserUseCase, DeleteUserUseCase, UpdateUserUseCase, FetchAllUsersUseCase {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserPersistencePort userPersistencePort;
    private final KeycloakPort keycloakPort;
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
        Optional<User> user = userPersistencePort.findById(id_user).map(UserMapper::toDomain);
        if (user.isPresent()) {
            UserInventory userInventory = UserInventory.builder()
                    .id(id_user)
                    .artifacts(getArtifactsUseCase.getArtifactsByUserId(id_user))
                    .build();
            return Optional.of(userInventory);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public User createUser(UserRegisterFromKeycloakDto user) {
        if (user==null || user.getId()==null || user.getUsername()==null) {
            throw new InvalidParameterException("User data cannot be null");
        }
        UserEntity newUser = UserEntity.builder()
                .keycloakId(user.getId())
                .username(user.getUsername())
                .accountType(AccountType.USER)
                .balance(0)
                .build();
        return userPersistencePort.save(newUser);
    }

    @Override
    public void deleteUser(String id_user) {
        UserEntity userEntity = userPersistencePort.findByKeycloakId(id_user)
                .orElseThrow(() -> new InvalidParameterException("User with ID " + id_user + " not found"));
        String keycloakId = userEntity.getKeycloakId();
        if (keycloakId == null || keycloakId.isEmpty()) {
            throw new IllegalStateException("KeycloakId is missing for User with ID " + id_user);
        }
        if (keycloakPort.deleteUser(keycloakId)) {
            userPersistencePort.deleteById(userEntity.getId());
        } else {
            throw new RuntimeException("Failed to delete user from Keycloak");
        }
    }

    @Override
    public UserUpdatedDto updateUser(UserToUpdateDto user, String id_user) {
        log.info("Updating user with id : {}", id_user);
        log.info("User data : {}", user);
        UserEntity userEntity = userPersistencePort.findByKeycloakId(id_user)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id_user));

        if (user.getUsername() != null) {
            userEntity.setUsername(user.getUsername());
        }

        // Handle profile image upload if provided
        if (user.getProfileImage()!=null && !user.getProfileImage().isEmpty()) {
            try {
                // Check if the user already has an image and delete it
                if (userEntity.getImageUrl()!=null && !userEntity.getImageUrl().isEmpty()) {
                    String existingImageUrl = userEntity.getImageUrl();
                    String existingFileName;

                    // Extract the filename from the URL
                    if (existingImageUrl.contains("?")) {
                        // Extract the filename from the URL (everything before the '?')
                        existingFileName = existingImageUrl.substring(existingImageUrl.lastIndexOf("/") + 1, existingImageUrl.indexOf("?"));
                    } else {
                        // Extract the filename from the URL (everything after the last '/')
                        existingFileName = existingImageUrl.substring(existingImageUrl.lastIndexOf("/") + 1);
                    }

                    log.info("Deleting existing profile image: {}", existingFileName);
                    boolean deleted = azureBlobStoragePort.deleteFile(existingFileName);
                    if (deleted) {
                        log.info("Existing profile image deleted successfully");
                    } else {
                        log.warn("Failed to delete existing profile image: {}", existingFileName);
                    }
                }

                MultipartFile imageFile = user.getProfileImage();
                String fileName = id_user + "_profile_" + imageFile.getOriginalFilename();

                // Upload the image to Azure Blob Storage
                String imageUrl = azureBlobStoragePort.uploadFile(imageFile, fileName);

                // Update the user entity with the image URL
                userEntity.setImageUrl(imageUrl);
                log.info("Profile image uploaded successfully. URL: {}", imageUrl);
            } catch (IOException e) {
                log.error("Error uploading profile image", e);
                throw new RuntimeException("Failed to upload profile image: " + e.getMessage(), e);
            }
        }

        // Save the updated user entity
        userPersistencePort.save(userEntity);

        UserUpdatedDto keycloakUpdateDto = UserUpdatedDto.builder()
                .id(userEntity.getKeycloakId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();

        UserRepresentation userFromKeycloak = keycloakPort.updateUser(keycloakUpdateDto);

        return UserUpdatedDto.builder()
                .id(String.valueOf(id_user))
                .username(userFromKeycloak.getUsername())
                .email(userFromKeycloak.getEmail())
                .firstName(userFromKeycloak.getFirstName())
                .lastName(userFromKeycloak.getLastName())
                .build();
    }

    @Override
    public Boolean updatePassword(String password, String userId) {
        UserEntity userEntity = userPersistencePort.findByKeycloakId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        String keycloakId = userEntity.getKeycloakId();
        if (keycloakId == null || keycloakId.isEmpty()) {
            throw new IllegalStateException("KeycloakId is missing for User with id " + userId);
        }
        return keycloakPort.updatePassword(keycloakId, password);
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
    }

}
