package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.out.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileStoragePort fileStoragePort;

    @GetMapping("/upload/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Resource resource = fileStoragePort.downloadFile(filename);
            return ResponseEntity.ok()
                    .contentType(resolveContentType(filename))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(new InputStreamResource(resource.getInputStream()));
        } catch (IOException e) {
            log.error("Error serving file {}: {}", filename, e.getMessage());
            // Client likely disconnected, so we just log and let the connection close.
            // No need to return an error response to a disconnected client.
            return ResponseEntity.status(500).build(); // Or just return null/empty response if preferred
        }
    }

    private MediaType resolveContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".glb")) return MediaType.parseMediaType("model/gltf-binary");
        if (lower.endsWith(".gltf")) return MediaType.parseMediaType("model/gltf+json");
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
