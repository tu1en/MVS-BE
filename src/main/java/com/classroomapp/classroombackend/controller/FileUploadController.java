package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(originPatterns = {"http://localhost:3000", "http://localhost:5173"}, maxAge = 3600, allowCredentials = "true")
@RequiredArgsConstructor
public class FileUploadController {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    private final FileStorageService fileStorageService;
    
    /**
     * Handle OPTIONS preflight requests
     */
    @RequestMapping(method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }
    
    /**
     * Upload a file to the specified folder
     * 
     * @param file The file to upload
     * @param folder The folder to store the file in
     * @return The URL to access the uploaded file
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "uploads") String folder) {
        
        logger.info("Received file upload request: filename={}, contentType={}, size={}",
                file.getOriginalFilename(), file.getContentType(), file.getSize());
        
        try {
            String fileUrl = fileStorageService.uploadFile(file, folder);
            
            logger.info("File uploaded successfully: {}", fileUrl);
            
            Map<String, String> response = new HashMap<>();
            response.put("url", fileUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error uploading file: {}", e.getMessage(), e);
            
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 