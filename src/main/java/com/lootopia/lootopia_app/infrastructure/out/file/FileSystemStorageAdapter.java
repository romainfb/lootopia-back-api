package com.lootopia.lootopia_app.infrastructure.out.file;

import com.lootopia.lootopia_app.application.port.out.FileStoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class FileSystemStorageAdapter implements FileStoragePort {

    private final Path rootLocation;
    private final Path rewardsRootLocation;

    public FileSystemStorageAdapter(
            @Value("${app.storage.upload-dir:upload}") String uploadDir,
            @Value("${app.storage.rewards-dir:upload/rewards}") String rewardsDir) {
        this.rootLocation = Paths.get(uploadDir);
        this.rewardsRootLocation = Paths.get(rewardsDir);
        try {
            Files.createDirectories(rootLocation);
            Files.createDirectories(rewardsRootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    @Override
    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        log.info("Uploading file {} to file system", fileName);

        if (file==null || file.isEmpty()) {
            log.error("File is null or empty");
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        try {
            String uniqueFileName = UUID.randomUUID() + "_" + fileName;
            Path destinationFile = this.rootLocation.resolve(
                            Paths.get(uniqueFileName))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // This is a security check
                throw new RuntimeException(
                        "Cannot store file outside current directory.");
            }
            file.transferTo(destinationFile);
            log.info("File uploaded successfully. Path: {}", destinationFile);
            return destinationFile.toString();
        } catch (IOException e) {
            log.error("Error uploading file to file system", e);
            throw new IOException("Failed to upload file to file system: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String fileName) {
        log.info("Deleting file {} from file system", fileName);
        try {
            Path file = rootLocation.resolve(fileName);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("Error deleting file from file system", e);
            return false;
        }
    }

    @Override
    public Resource downloadFile(String fileName) {
        log.info("Downloading file {} from file system", fileName);
        try {
            Path file = rootLocation.resolve(fileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException(
                        "Could not read file: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + fileName, e);
        }
    }

    @Override
    public String generateUrl(String fileName) {
        log.info("Generating URL for file {} in file system", fileName);
        return "/upload/" + fileName;
    }

    @Override
    public String uploadRewardFile(MultipartFile file, String fileName) throws IOException {
        log.info("Uploading reward file {} to file system", fileName);

        if (file==null || file.isEmpty()) {
            log.error("File is null or empty");
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        try {
            String uniqueFileName = UUID.randomUUID() + "_" + fileName;
            Path destinationFile = this.rewardsRootLocation.resolve(
                            Paths.get(uniqueFileName))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rewardsRootLocation.toAbsolutePath())) {
                // This is a security check
                throw new RuntimeException(
                        "Cannot store file outside current directory.");
            }
            file.transferTo(destinationFile);
            log.info("Reward file uploaded successfully. Path: {}", destinationFile);
            return destinationFile.toString();
        } catch (IOException e) {
            log.error("Error uploading reward file to file system", e);
            throw new IOException("Failed to upload reward file to file system: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteRewardFile(String fileName) {
        log.info("Deleting reward file {} from file system", fileName);
        try {
            Path file = rewardsRootLocation.resolve(fileName);
            return Files.deleteIfExists(file);
        } catch (IOException e) {
            log.error("Error deleting reward file from file system", e);
            return false;
        }
    }

    @Override
    public Resource downloadRewardFile(String fileName) {
        log.info("Downloading reward file {} from file system", fileName);
        try {
            Path file = rewardsRootLocation.resolve(fileName);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException(
                        "Could not read file: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + fileName, e);
        }
    }

    @Override
    public String generateRewardUrl(String fileName) {
        log.info("Generating URL for reward file {} in file system", fileName);
        return "/upload/rewards/" + fileName;
    }
}
