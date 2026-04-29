package com.lootopia.lootopia_app.application.port.out;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStoragePort {
    /**
     * Upload a file to the file system
     *
     * @param file The file to upload
     * @param fileName The original name of the file
     * @return The unique name of the uploaded file
     * @throws IOException If there's an error uploading the file
     */
    String uploadFile(MultipartFile file, String fileName) throws IOException;

    /**
     * Delete a file from the file system
     *
     * @param fileName The name of the file to delete
     * @return true if the file was deleted, false otherwise
     */
    boolean deleteFile(String fileName);

    /**
     * Download a file from the file system
     *
     * @param fileName The name of the file to download
     * @return A resource representing the file
     */
    Resource downloadFile(String fileName);

    /**
     * Generate a URL for a file
     *
     * @param fileName The name of the file
     * @return The URL for the file, or null if the file doesn't exist
     */
    String generateUrl(String fileName);

    /**
     * Upload a reward file to the file system
     *
     * @param file The file to upload
     * @param fileName The original name of the file
     * @return The unique name of the uploaded file
     * @throws IOException If there's an error uploading the file
     */
    String uploadRewardFile(MultipartFile file, String fileName) throws IOException;

    /**
     * Delete a reward file from the file system
     *
     * @param fileName The name of the file to delete
     * @return true if the file was deleted, false otherwise
     */
    boolean deleteRewardFile(String fileName);

    /**
     * Download a reward file from the file system
     *
     * @param fileName The name of the file to download
     * @return A resource representing the file
     */
    Resource downloadRewardFile(String fileName);

    /**
     * Generate a URL for a reward file
     *
     * @param fileName The name of the file
     * @return The URL for the file, or null if the file doesn't exist
     */
    String generateRewardUrl(String fileName);
}
