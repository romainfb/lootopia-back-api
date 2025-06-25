package com.lootopia.lootopia_app.infrastructure.out.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.lootopia.lootopia_app.application.port.out.AzureBlobStoragePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Service
public class AzureBlobStorageAdapter implements AzureBlobStoragePort {

    private final BlobContainerClient blobContainerClient;
    private final BlobContainerClient rewardsBlobContainerClient;

    public AzureBlobStorageAdapter(
            BlobContainerClient blobContainerClient,
            @Qualifier("rewardsBlobContainerClient") BlobContainerClient rewardsBlobContainerClient) {
        this.blobContainerClient = blobContainerClient;
        this.rewardsBlobContainerClient = rewardsBlobContainerClient;
    }

    @Override
    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        log.info("Uploading file {} to Azure Blob Storage", fileName);

        if (file==null || file.isEmpty()) {
            log.error("File is null or empty");
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        try {
            // Generate a unique file name to avoid collisions
            String uniqueFileName = UUID.randomUUID() + "_" + fileName;

            // Get a reference to a blob
            BlobClient blobClient = blobContainerClient.getBlobClient(uniqueFileName);

            // Upload the file
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            // Generate a SAS token for the blob
            BlobSasPermission blobSasPermission = new BlobSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(1); // SAS token valid for 1 hour

            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, blobSasPermission)
                    .setStartTime(OffsetDateTime.now().minusMinutes(5)); // Start time 5 minutes ago to avoid clock skew issues

            String sasToken = blobClient.generateSas(sasValues);
            String sasUrl = blobClient.getBlobUrl() + "?" + sasToken;

            log.info("File uploaded successfully. SAS URL: {}", sasUrl);

            return sasUrl;
        } catch (Exception e) {
            log.error("Error uploading file to Azure Blob Storage", e);
            throw new IOException("Failed to upload file to Azure Blob Storage: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteFile(String fileName) {
        log.info("Deleting file {} from Azure Blob Storage", fileName);

        // Get a reference to a blob
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);

        // Check if the blob exists
        if (blobClient.exists()) {
            // Delete the blob
            blobClient.delete();
            log.info("File deleted successfully");
            return true;
        } else {
            log.warn("File {} does not exist in Azure Blob Storage", fileName);
            return false;
        }
    }

    @Override
    public Resource downloadFile(String fileName) {
        log.info("Downloading file {} from Azure Blob Storage", fileName);
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
            if (blobClient.exists()) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                blobClient.downloadStream(outputStream);
                final byte[] bytes = outputStream.toByteArray();
                return new ByteArrayResource(bytes);
            } else {
                log.warn("File {} does not exist in Azure Blob Storage", fileName);
                return null;
            }
        } catch (Exception e) {
            log.error("Error downloading file from Azure Blob Storage", e);
            return null;
        }
    }

    /**
     * Generate a SAS URL for a blob
     *
     * @param fileName The name of the blob
     * @return The SAS URL for the blob, or null if the blob doesn't exist
     */
    @Override
    public String generateSasUrl(String fileName) {
        log.info("Generating SAS URL for file {} in Azure Blob Storage", fileName);
        try {
            BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
            if (blobClient.exists()) {
                // Generate a SAS token for the blob
                BlobSasPermission blobSasPermission = new BlobSasPermission().setReadPermission(true);
                OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(24); // SAS token valid for 24 hours

                BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, blobSasPermission)
                        .setStartTime(OffsetDateTime.now().minusMinutes(5)); // Start time 5 minutes ago to avoid clock skew issues

                String sasToken = blobClient.generateSas(sasValues);
                String sasUrl = blobClient.getBlobUrl() + "?" + sasToken;

                log.info("SAS URL generated successfully: {}", sasUrl);

                return sasUrl;
            } else {
                log.warn("File {} does not exist in Azure Blob Storage", fileName);
                return null;
            }
        } catch (Exception e) {
            log.error("Error generating SAS URL for file in Azure Blob Storage", e);
            return null;
        }
    }

    @Override
    public String uploadRewardFile(MultipartFile file, String fileName) throws IOException {
        log.info("Uploading file {} to Azure Blob Storage rewards container", fileName);

        if (file==null || file.isEmpty()) {
            log.error("File is null or empty");
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        try {
            // Generate a unique file name to avoid collisions
            String uniqueFileName = UUID.randomUUID() + "_" + fileName;

            // Get a reference to a blob
            BlobClient blobClient = rewardsBlobContainerClient.getBlobClient(uniqueFileName);

            // Upload the file
            blobClient.upload(file.getInputStream(), file.getSize(), true);

            // Generate a SAS token for the blob
            BlobSasPermission blobSasPermission = new BlobSasPermission().setReadPermission(true);
            OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(1); // SAS token valid for 1 hour

            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, blobSasPermission)
                    .setStartTime(OffsetDateTime.now().minusMinutes(5)); // Start time 5 minutes ago to avoid clock skew issues

            String sasToken = blobClient.generateSas(sasValues);
            String sasUrl = blobClient.getBlobUrl() + "?" + sasToken;

            log.info("File uploaded successfully to rewards container. SAS URL: {}", sasUrl);

            return sasUrl;
        } catch (Exception e) {
            log.error("Error uploading file to Azure Blob Storage rewards container", e);
            throw new IOException("Failed to upload file to Azure Blob Storage rewards container: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteRewardFile(String fileName) {
        log.info("Deleting file {} from Azure Blob Storage rewards container", fileName);

        // Get a reference to a blob
        BlobClient blobClient = rewardsBlobContainerClient.getBlobClient(fileName);

        // Check if the blob exists
        if (blobClient.exists()) {
            // Delete the blob
            blobClient.delete();
            log.info("File deleted successfully from rewards container");
            return true;
        } else {
            log.warn("File {} does not exist in Azure Blob Storage rewards container", fileName);
            return false;
        }
    }

    @Override
    public Resource downloadRewardFile(String fileName) {
        log.info("Downloading file {} from Azure Blob Storage rewards container", fileName);
        try {
            BlobClient blobClient = rewardsBlobContainerClient.getBlobClient(fileName);
            if (blobClient.exists()) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                blobClient.downloadStream(outputStream);
                final byte[] bytes = outputStream.toByteArray();
                return new ByteArrayResource(bytes);
            } else {
                log.warn("File {} does not exist in Azure Blob Storage rewards container", fileName);
                return null;
            }
        } catch (Exception e) {
            log.error("Error downloading file from Azure Blob Storage rewards container", e);
            return null;
        }
    }

    @Override
    public String generateRewardSasUrl(String fileName) {
        log.info("Generating SAS URL for file {} in Azure Blob Storage rewards container", fileName);
        try {
            BlobClient blobClient = rewardsBlobContainerClient.getBlobClient(fileName);
            if (blobClient.exists()) {
                // Generate a SAS token for the blob
                BlobSasPermission blobSasPermission = new BlobSasPermission().setReadPermission(true);
                OffsetDateTime expiryTime = OffsetDateTime.now().plusHours(24); // SAS token valid for 24 hours

                BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, blobSasPermission)
                        .setStartTime(OffsetDateTime.now().minusMinutes(5)); // Start time 5 minutes ago to avoid clock skew issues

                String sasToken = blobClient.generateSas(sasValues);
                String sasUrl = blobClient.getBlobUrl() + "?" + sasToken;

                log.info("SAS URL generated successfully for rewards container: {}", sasUrl);

                return sasUrl;
            } else {
                log.warn("File {} does not exist in Azure Blob Storage rewards container", fileName);
                return null;
            }
        } catch (Exception e) {
            log.error("Error generating SAS URL for file in Azure Blob Storage rewards container", e);
            return null;
        }
    }
}
