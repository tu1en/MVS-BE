package com.classroomapp.classroombackend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Simple file upload controller that bypasses complex file storage services
 * Specifically for explanation request image uploads
 */
@RestController
@RequestMapping("/api/simple-upload")
@CrossOrigin(originPatterns = {"http://localhost:3000", "http://localhost:5173"}, maxAge = 3600, allowCredentials = "true")
public class SimpleFileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(SimpleFileUploadController.class);
    private static final String UPLOAD_DIR = "uploads";

    @PostMapping("/explanation")
    public ResponseEntity<Map<String, String>> uploadExplanationFile(@RequestParam("file") MultipartFile file) {
        logger.info("Simple upload request received for file: {}", file.getOriginalFilename());
        
        Map<String, String> response = new HashMap<>();
        
        try {
            // Validate file
            if (file.isEmpty()) {
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create upload directory if it doesn't exist
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                logger.info("Upload directory created: {}", created);
            }
            
            // Create explanations subdirectory
            File explanationsDir = new File(uploadDir, "explanations");
            if (!explanationsDir.exists()) {
                boolean created = explanationsDir.mkdirs();
                logger.info("Explanations directory created: {}", created);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path filePath = Paths.get(explanationsDir.getAbsolutePath(), uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Generate URL
            String fileUrl = "http://localhost:8088/api/simple-upload/download/explanations/" + uniqueFilename;
            
            logger.info("File saved successfully: {} -> {}", originalFilename, filePath.toString());
            logger.info("Download URL: {}", fileUrl);
            
            response.put("url", fileUrl);
            response.put("filename", uniqueFilename);
            response.put("originalName", originalFilename);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            logger.error("Error saving file: {}", e.getMessage(), e);
            response.put("error", "Failed to save file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            response.put("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @GetMapping("/download/explanations/{filename}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR, "explanations", filename);
            
            if (!Files.exists(filePath)) {
                logger.warn("File not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = Files.readAllBytes(filePath);
            
            return ResponseEntity.ok()
                    .header("Content-Type", "application/octet-stream")
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .body(fileContent);
                    
        } catch (IOException e) {
            logger.error("Error reading file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
