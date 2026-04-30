package com.lootopia.lootopia_app.application.service;


import com.lootopia.lootopia_app.application.port.in.DeleteUserUseCase;
import com.lootopia.lootopia_app.application.port.in.FetchAllUsersUseCase;
import com.lootopia.lootopia_app.application.port.in.GetArtifactsUseCase;
import com.lootopia.lootopia_app.application.port.in.GetUserUseCase;
import com.lootopia.lootopia_app.application.port.in.UpdateUserUseCase;
import com.lootopia.lootopia_app.application.port.out.FileStoragePort;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements GetUserUseCase, DeleteUserUseCase, UpdateUserUseCase, FetchAllUsersUseCase {
    private final UserPersistencePort userPersistencePort;
    private final GetArtifactsUseCase getArtifactsUseCase;
    private final FileStoragePort fileStoragePort;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> getUserById(Long id_user) {
        log.debug("Fetching user by id: {}", id_user);
        if (id_user == null) throw new InvalidParameterException("User ID cannot be null");
        UserEntity entity = userPersistencePort.findById(id_user)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id_user));
        return Optional.of(UserMapper.toDomain(refreshUrl(entity)));
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
        log.info("Deleting user with id: {}", id_user);
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
                userEntity.setImageUrl(fileStoragePort.uploadFile(imageFile, fileName));
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
        log.info("Updating password for user id: {}", userId);
        UserEntity userEntity = userPersistencePort.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        userEntity.setPasswordHash(passwordEncoder.encode(password));
        userPersistencePort.save(userEntity);
        return true;
    }

    @Override
    public List<User> getAllUsers() {
        return userPersistencePort.findAll().stream().map(UserMapper::toDomain).toList();
    }

    private UserEntity refreshUrl(UserEntity entity) {
        if (entity.getImageUrl()==null || entity.getImageUrl().isEmpty()) {
            return entity;
        }
        try {
            String imageUrl = entity.getImageUrl();
            String fileName = imageUrl.contains("?")
                    ? imageUrl.substring(imageUrl.lastIndexOf("/") + 1, imageUrl.indexOf("?"))
                    :imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            String url = fileStoragePort.generateUrl(fileName);
            if (url!=null) {
                entity.setImageUrl(url);
            }
        } catch (Exception e) {
            log.error("Error generating URL for user image", e);
        }
        return entity;
    }

    private void deleteExistingProfileImage(UserEntity userEntity) {
        if (userEntity.getImageUrl()==null || userEntity.getImageUrl().isEmpty()) {
            return;
        }
        String existingImageUrl = userEntity.getImageUrl();
        String existingFileName = existingImageUrl.contains("?")
                ? existingImageUrl.substring(existingImageUrl.lastIndexOf("/") + 1, existingImageUrl.indexOf("?"))
                :existingImageUrl.substring(existingImageUrl.lastIndexOf("/") + 1);
        fileStoragePort.deleteFile(existingFileName);
    }
}
