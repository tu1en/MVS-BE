package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.service.FileStorageService;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class FirebaseStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseStorageServiceImpl.class);

    @Value("${firebase.bucket-name}")
    private String bucketName;

    @Override
    public String uploadFile(MultipartFile file, String folder) throws Exception {
        try {
            logger.info("Starting file upload to Firebase Storage");
            logger.info("Bucket name: {}", bucketName);
            logger.info("File name: {}, size: {}, content type: {}", 
                    file.getOriginalFilename(), file.getSize(), file.getContentType());
            
            // Get Firebase Storage instance
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            
            // Generate a unique file name
            String fileName = generateUniqueFileName(file.getOriginalFilename());
            String fullPath = folder + "/" + fileName;
            logger.info("Generated path for upload: {}", fullPath);
            
            // Create a blob ID with bucket and file name
            BlobId blobId = BlobId.of(bucketName, fullPath);
            
            // Create Blob info with appropriate content type
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            
            // Upload the file to Firebase Storage
            logger.info("Uploading file to Firebase Storage...");
            Blob blob = storage.create(blobInfo, file.getBytes());
            
            // Return the download URL
            String downloadUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, fullPath);
            logger.info("Upload successful. Download URL: {}", downloadUrl);
            return downloadUrl;
        } catch (IOException e) {
            logger.error("Error uploading file to Firebase Storage", e);
            throw new Exception("Failed to upload file to Firebase Storage: " + e.getMessage(), e);
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