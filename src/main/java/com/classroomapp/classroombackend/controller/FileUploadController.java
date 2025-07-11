package com.classroomapp.classroombackend.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.service.FileStorageService;

import lombok.RequiredArgsConstructor;

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
            FileUploadResponse response = fileStorageService.save(file, folder);
            String fileUrl = response.getFileUrl();
            
            logger.info("File uploaded successfully: {}", fileUrl);
            
            Map<String, String> responseMap = new HashMap<>();
            responseMap.put("url", fileUrl);
            
            return ResponseEntity.ok(responseMap);
        } catch (Exception e) {
            logger.error("Error uploading file: {}", e.getMessage(), e);
            
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 