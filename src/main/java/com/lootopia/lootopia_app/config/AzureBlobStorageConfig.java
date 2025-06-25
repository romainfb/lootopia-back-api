package com.lootopia.lootopia_app.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AzureBlobStorageConfig {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    @Value("${azure.storage.rewards-container-name:rewardsimg}")
    private String rewardsContainerName;

    @Bean
    public BlobServiceClient blobServiceClient() {
        log.info("Initializing Azure Blob Service Client");
        try {
            return new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        } catch (Exception e) {
            log.error("Failed to initialize Azure Blob Service Client", e);
            throw e;
        }
    }

    @Bean
    public BlobContainerClient blobContainerClient(BlobServiceClient blobServiceClient) {
        log.info("Initializing Azure Blob Container Client for container: {}", containerName);
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            // Verify the container exists
            if (!containerClient.exists()) {
                log.warn("Container '{}' does not exist. Creating it...", containerName);
                containerClient.create();
            }

            return containerClient;
        } catch (Exception e) {
            log.error("Failed to initialize Azure Blob Container Client", e);
            throw e;
        }
    }

    @Bean
    public BlobContainerClient rewardsBlobContainerClient(BlobServiceClient blobServiceClient) {
        log.info("Initializing Azure Blob Container Client for rewards container: {}", rewardsContainerName);
        try {
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(rewardsContainerName);

            // Verify the container exists
            if (!containerClient.exists()) {
                log.warn("Container '{}' does not exist. Creating it...", rewardsContainerName);
                containerClient.create();
            }

            return containerClient;
        } catch (Exception e) {
            log.error("Failed to initialize Azure Blob Container Client for rewards", e);
            throw e;
        }
    }
}
