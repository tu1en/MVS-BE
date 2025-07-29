package com.classroomapp.classroombackend.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.common.FileUploadResponse;
import com.classroomapp.classroombackend.exception.FileStorageException;
import com.classroomapp.classroombackend.service.FileStorageService;

import lombok.extern.slf4j.Slf4j;

/**
 * 🎯 MERGED SIMPLE FILE STORAGE SERVICE
 * Merged từ: LocalFileStorageServiceImpl + DummyFileStorageServiceImpl
 * 
 * ✅ Simplified implementation
 * ✅ Local storage với proper structure
 * ✅ Easy to extend cho Firebase/Cloud storage
 * ✅ Compatible với existing assignment system
 */
@Service
@Primary
@Slf4j
public class SimpleFileStorageServiceImpl implements FileStorageService {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${server.port:8088}")
    private String serverPort;

    @Value("${app.file.storage.mode:local}") // local, firebase, aws
    private String storageMode;

    @Override
    public FileUploadResponse save(MultipartFile file, String folder) {
        try {
            // ✅ 1. Validate input
            String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            log.info("📁 Saving file: {} to folder: {} (mode: {})", originalFilename, folder, storageMode);

            // ✅ 2. Basic security validation
            if (originalFilename.contains("..")) {
                throw new FileStorageException("Invalid filename path sequence: " + originalFilename);
            }

            // ✅ 3. Generate unique filename với timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uniqueFileName = String.format("%s_%s_%s", 
                timestamp, 
                UUID.randomUUID().toString().substring(0, 8), 
                originalFilename);

            // ✅ 4. Choose storage method based on mode
            switch (storageMode.toLowerCase()) {
                case "firebase":
                    return saveToFirebase(file, folder, uniqueFileName, originalFilename);
                case "aws":
                    return saveToAWS(file, folder, uniqueFileName, originalFilename);
                default:
                    return saveToLocal(file, folder, uniqueFileName, originalFilename);
            }

        } catch (Exception e) {
            log.error("💥 Error saving file: {}", e.getMessage(), e);
            throw new FileStorageException("Could not save file " + file.getOriginalFilename(), e);
        }
    }

    /**
     * 💾 Save to local storage
     */
    private FileUploadResponse saveToLocal(MultipartFile file, String folder, 
                                          String uniqueFileName, String originalFilename) throws IOException {
        
        // ✅ Create directory structure: uploads/folder/yyyy/MM/
        Path folderPath = Paths.get(uploadDir, folder);
        Path datePath = folderPath.resolve(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM")));
        Files.createDirectories(datePath);

        // ✅ Save file
        Path filePath = datePath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // ✅ Generate download URL
        String relativePath = String.format("%s/%s/%s", 
            folder, 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM")), 
            uniqueFileName);
        String downloadUrl = "http://localhost:" + serverPort + "/api/files/download/" + relativePath;
        
        log.info("✅ File saved locally: {} -> {}", originalFilename, downloadUrl);

        return FileUploadResponse.builder()
            .success(true)
            .filename(uniqueFileName)
            .originalFilename(originalFilename)
            .fileUrl(downloadUrl)
            .filePath(relativePath)
            .mimeType(file.getContentType())
            .fileSize(file.getSize())
            .category(folder)
            .uploadedAt(LocalDateTime.now())
            .build();
    }

    /**
     * 🔥 Save to Firebase (placeholder - có thể implement sau)
     */
    private FileUploadResponse saveToFirebase(MultipartFile file, String folder, 
                                             String uniqueFileName, String originalFilename) {
        
        log.info("🔥 Firebase storage mode - Creating mock response");
        
        // TODO: Implement Firebase Storage integration
        String firebaseUrl = "https://firebasestorage.googleapis.com/v0/b/your-project/o/" + 
                           folder + "%2F" + uniqueFileName + "?alt=media";

        return FileUploadResponse.builder()
            .success(true)
            .filename(uniqueFileName)
            .originalFilename(originalFilename)
            .fileUrl(firebaseUrl)
            .filePath(folder + "/" + uniqueFileName)
            .mimeType(file.getContentType())
            .fileSize(file.getSize())
            .category(folder)
            .uploadedAt(LocalDateTime.now())
            .build();
    }

    /**
     * ☁️ Save to AWS S3 (placeholder - có thể implement sau)
     */
    private FileUploadResponse saveToAWS(MultipartFile file, String folder, 
                                        String uniqueFileName, String originalFilename) {
        
        log.info("☁️ AWS S3 storage mode - Creating mock response");
        
        // TODO: Implement AWS S3 integration
        String s3Url = "https://your-bucket.s3.amazonaws.com/" + folder + "/" + uniqueFileName;

        return FileUploadResponse.builder()
            .success(true)
            .filename(uniqueFileName)
            .originalFilename(originalFilename)
            .fileUrl(s3Url)
            .filePath(folder + "/" + uniqueFileName)
            .mimeType(file.getContentType())
            .fileSize(file.getSize())
            .category(folder)
            .uploadedAt(LocalDateTime.now())
            .build();
    }

    @Override
    public void delete(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            log.warn("⚠️ Attempted to delete file with null/blank name");
            return;
        }
        
        try {
            switch (storageMode.toLowerCase()) {
                case "firebase":
                    deleteFromFirebase(fileName);
                    break;
                case "aws":
                    deleteFromAWS(fileName);
                    break;
                default:
                    deleteFromLocal(fileName);
            }
        } catch (Exception e) {
            log.error("💥 Error deleting file {}: {}", fileName, e.getMessage(), e);
        }
    }

    /**
     * 🗑️ Delete from local storage
     */
    private void deleteFromLocal(String fileName) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (Files.exists(uploadPath)) {
            Files.walk(uploadPath)
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().equals(fileName))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                        log.info("🗑️ Deleted local file: {}", path);
                    } catch (IOException e) {
                        log.error("💥 Error deleting local file: {}", path, e);
                    }
                });
        }
    }

    /**
     * 🔥 Delete from Firebase (placeholder)
     */
    private void deleteFromFirebase(String fileName) {
        log.info("🔥 Firebase delete: {}", fileName);
        // TODO: Implement Firebase Storage deletion
    }

    /**
     * ☁️ Delete from AWS S3 (placeholder)
     */
    private void deleteFromAWS(String fileName) {
        log.info("☁️ AWS S3 delete: {}", fileName);
        // TODO: Implement AWS S3 deletion
    }

    @Override
    public boolean exists(String fileName) {
        try {
            switch (storageMode.toLowerCase()) {
                case "firebase":
                    return existsInFirebase(fileName);
                case "aws":
                    return existsInAWS(fileName);
                default:
                    return existsInLocal(fileName);
            }
        } catch (Exception e) {
            log.error("💥 Error checking file existence: {}", e.getMessage());
            return false;
        }
    }

    private boolean existsInLocal(String fileName) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            return false;
        }
        
        return Files.walk(uploadPath)
            .filter(Files::isRegularFile)
            .anyMatch(path -> path.getFileName().toString().equals(fileName));
    }

    private boolean existsInFirebase(String fileName) {
        // TODO: Implement Firebase Storage existence check
        return false;
    }

    private boolean existsInAWS(String fileName) {
        // TODO: Implement AWS S3 existence check
        return false;
    }

    @Override
    public String getFileUrl(String fileName, String folder) {
        switch (storageMode.toLowerCase()) {
            case "firebase":
                return "https://firebasestorage.googleapis.com/v0/b/your-project/o/" + 
                       folder + "%2F" + fileName + "?alt=media";
            case "aws":
                return "https://your-bucket.s3.amazonaws.com/" + folder + "/" + fileName;
            default:
                return "http://localhost:" + serverPort + "/api/files/download/" + folder + "/" + fileName;
        }
    }
}