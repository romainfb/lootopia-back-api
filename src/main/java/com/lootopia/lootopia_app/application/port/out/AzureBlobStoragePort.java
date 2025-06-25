package com.lootopia.lootopia_app.application.port.out;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface AzureBlobStoragePort {
    /**
     * Upload a file to Azure Blob Storage
     *
     * @param file The file to upload
     * @param fileName The original name of the file
     * @return The unique name of the uploaded file in Azure Blob Storage
     * @throws IOException If there's an error uploading the file
     */
    String uploadFile(MultipartFile file, String fileName) throws IOException;

    /**
     * Delete a file from Azure Blob Storage
     *
     * @param fileName The name of the file to delete
     * @return true if the file was deleted, false otherwise
     */
    boolean deleteFile(String fileName);

    /**
     * Download a file from Azure Blob Storage
     *
     * @param fileName The name of the file to download
     * @return A resource representing the file
     */
    Resource downloadFile(String fileName);

    /**
     * Generate a SAS URL for a blob
     *
     * @param fileName The name of the blob
     * @return The SAS URL for the blob, or null if the blob doesn't exist
     */
    String generateSasUrl(String fileName);

    /**
     * Upload a file to the rewards container in Azure Blob Storage
     *
     * @param file The file to upload
     * @param fileName The original name of the file
     * @return The unique name of the uploaded file in Azure Blob Storage
     * @throws IOException If there's an error uploading the file
     */
    String uploadRewardFile(MultipartFile file, String fileName) throws IOException;

    /**
     * Delete a file from the rewards container in Azure Blob Storage
     *
     * @param fileName The name of the file to delete
     * @return true if the file was deleted, false otherwise
     */
    boolean deleteRewardFile(String fileName);

    /**
     * Download a file from the rewards container in Azure Blob Storage
     *
     * @param fileName The name of the file to download
     * @return A resource representing the file
     */
    Resource downloadRewardFile(String fileName);

    /**
     * Generate a SAS URL for a blob in the rewards container
     *
     * @param fileName The name of the blob
     * @return The SAS URL for the blob, or null if the blob doesn't exist
     */
    String generateRewardSasUrl(String fileName);
}
