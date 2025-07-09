package com.classroomapp.classroombackend.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.exception.FileStorageException;
import com.classroomapp.classroombackend.service.FileStorageService;

@Service
@Primary
@Profile("local")
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorageServiceImpl.class);
    
    @Value("${file.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${server.port:8088}")
    private String serverPort;

    @Override
    public FileUploadResponse save(MultipartFile file, String folder) {
        try {
            // Get original filename
            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            logger.info("Received file to upload to folder {}: {}", folder, originalFilename);

            // Basic validation
            if (originalFilename.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + originalFilename);
            }

            // Generate a unique filename
            String uniqueFileName = UUID.randomUUID() + "_" + originalFilename;

            // Create directory structure
            Path folderPath = Paths.get(uploadDir).resolve(folder);
            Files.createDirectories(folderPath);

            // Save the file
            Path filePath = folderPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Construct download URL
            String downloadUrl = "http://localhost:" + serverPort + "/api/files/download/" + folder + "/" + uniqueFileName;
            
            logger.info("File saved successfully: {} -> {}", originalFilename, filePath.toString());
            logger.info("Download URL: {}", downloadUrl);

            // Return the response object
            return new FileUploadResponse(
                    uniqueFileName,
                    downloadUrl,
                    file.getContentType(),
                    file.getSize()
            );
        } catch (IOException e) {
            logger.error("Error saving file to local storage", e);
            throw new FileStorageException("Could not save file " + file.getOriginalFilename() + ". Please try again!", e);
        }
    }

    @Override
    public void delete(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            logger.warn("Attempted to delete a file with null or blank name.");
            return;
        }
        
        try {
            // Find and delete the file from any folder (simple implementation)
            Path uploadPath = Paths.get(uploadDir);
            if (Files.exists(uploadPath)) {
                Files.walk(uploadPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(fileName))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            logger.info("Successfully deleted file: {}", path.toString());
                        } catch (IOException e) {
                            logger.error("Error deleting file: {}", path.toString(), e);
                        }
                    });
            }
        } catch (IOException e) {
            logger.error("Error deleting file from local storage: {}", fileName, e);
        }
    }
}
