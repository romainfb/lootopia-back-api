package com.lootopia.lootopia_app.infrastructure.in.rest;

import com.lootopia.lootopia_app.application.port.out.FileStoragePort;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Controller
@RequiredArgsConstructor
public class FileController {

    private final FileStoragePort fileStoragePort;

    @GetMapping("/upload/{filename:.+}")
    @ResponseBody
    public void serveFile(@PathVariable String filename, HttpServletResponse response) {
        try {
            Resource resource = fileStoragePort.downloadFile(filename);
            if (!resource.exists() || !resource.isReadable()) {
                log.error("File not found or not readable: {}", filename);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\": \"File not found\"}");
                return;
            }

            response.setContentType(resolveContentType(filename).toString());
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"");

            try (InputStream inputStream = resource.getInputStream()) {
                StreamUtils.copy(inputStream, response.getOutputStream());
                response.flushBuffer();
            }

        } catch (IOException e) {
            // This is expected if the client disconnects during the download
            log.warn("Error serving file {}: {}. Client likely disconnected.", filename, e.getMessage());
        } catch (RuntimeException e) {
            log.error("Could not serve file {}: {}", filename, e.getMessage());
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("{\"error\": \"Could not serve file\"}");
            } catch (IOException ex) {
                log.error("Error writing error response: {}", ex.getMessage());
            }
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
