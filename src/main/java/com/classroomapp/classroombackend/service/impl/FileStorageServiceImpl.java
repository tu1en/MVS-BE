package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of FileStorageService that stores files on the local file system
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    
    @Value("${server.port:8088}")
    private String serverPort;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    public String storeFile(MultipartFile file) throws IOException {
        // Create the upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        
        // Generate a unique file name to prevent conflicts
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        if (originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        
        // Copy the file to the upload directory
        Path targetLocation = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        // Return the URL for accessing the file
        String fileUrl = String.format("http://localhost:%s%s/api/files/%s", 
                serverPort, 
                contextPath, 
                uniqueFileName);
        
        log.info("Stored file: {} with URL: {}", originalFileName, fileUrl);
        return fileUrl;
    }

    @Override
    public List<String> storeFiles(List<MultipartFile> files) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String fileUrl = storeFile(file);
                fileUrls.add(fileUrl);
            }
        }
        return fileUrls;
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // Extract the file name from the URL
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir).resolve(fileName).normalize();
            
            // Check if the file exists and delete it
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted file: {}", fileName);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("Error deleting file: {}", fileUrl, e);
            return false;
        }
    }
}
