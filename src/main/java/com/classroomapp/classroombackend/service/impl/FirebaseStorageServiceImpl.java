package com.classroomapp.classroombackend.service.impl;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.service.FileStorageService;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;

@Service
@Primary
@Profile("firebase")
public class FirebaseStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseStorageServiceImpl.class);

    @Value("${firebase.bucket-name}")
    private String bucketName;

    @Override
    public FileUploadResponse save(MultipartFile file, String folder) {
        try {
            logger.info("Starting file upload to Firebase Storage");
            logger.info("Bucket name: {}", bucketName);
            logger.info("File name: {}, size: {}, content type: {}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType());

            Storage storage = StorageClient.getInstance().bucket().getStorage();

            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String fullPath = folder + "/" + fileName;
            logger.info("Generated path for upload: {}", fullPath);

            BlobId blobId = BlobId.of(bucketName, fullPath);

            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            logger.info("Uploading file to Firebase Storage...");
            Blob blob = storage.create(blobInfo, file.getBytes());

            if (blob == null || !blob.exists()) {
                throw new Exception("Failed to upload file to Firebase Storage - blob creation failed");
            }

            String downloadUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, fullPath);
            logger.info("Upload successful. Download URL: {}", downloadUrl);
            return new FileUploadResponse(fileName, downloadUrl, file.getContentType(), file.getSize());
        } catch (IOException e) {
            logger.error("Error uploading file to Firebase Storage", e);
            throw new RuntimeException("Failed to upload file to Firebase Storage: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error during file upload", e);
            throw new RuntimeException("A general error occurred during file upload.", e);
        }
    }
    
    @Override
    public void delete(String fileName) {
        // This is a simplified delete, assuming a default folder if none is provided.
        // A better implementation might require the full path or a more robust lookup.
        this.delete(fileName, "uploads");
    }

    public void delete(String fileName, String folder) {
        try {
            String fullPath = folder + "/" + fileName;
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of(bucketName, fullPath);

            boolean deleted = storage.delete(blobId);
            if (!deleted) {
                logger.warn("File not found or already deleted: {}", fullPath);
            } else {
                logger.info("Successfully deleted file: {}", fullPath);
            }
        } catch (Exception e) {
            logger.error("Error deleting file from Firebase Storage", e);
        }
    }
    
    /**
     * Generate a unique filename to prevent duplicates
     */
    private String generateUniqueFileName(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueName = UUID.randomUUID().toString() + extension;
        logger.info("Generated unique filename: {}", uniqueName);
        return uniqueName;
    }
}