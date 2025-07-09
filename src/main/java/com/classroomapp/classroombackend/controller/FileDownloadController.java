package com.classroomapp.classroombackend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileDownloadController {

    private static final Logger logger = LoggerFactory.getLogger(FileDownloadController.class);

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/download/{folder}/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String folder, @PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(folder).resolve(filename);
            
            if (!Files.exists(filePath)) {
                logger.warn("File not found: {}", filePath.toString());
                return ResponseEntity.notFound().build();
            }

            byte[] data = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(data);

            // Try to determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // Extract original filename from UUID-prefixed filename
            String originalFilename = filename;
            if (filename.contains("_")) {
                originalFilename = filename.substring(filename.indexOf("_") + 1);
            }

            logger.info("Serving file: {} -> {}", filePath.toString(), originalFilename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"")
                    .body(resource);

        } catch (IOException e) {
            logger.error("Error reading file: {}/{}", folder, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
