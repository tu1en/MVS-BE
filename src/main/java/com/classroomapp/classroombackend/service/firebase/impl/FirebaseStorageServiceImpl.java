package com.classroomapp.classroombackend.service.firebase.impl;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.FileUploadResponse;
import com.classroomapp.classroombackend.service.firebase.FirebaseStorageService;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.cloud.StorageClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FirebaseStorageServiceImpl implements FirebaseStorageService {

    @Value("${firebase.bucket-name:default-bucket}")
    private String bucketName;

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String folder) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String filePath = folder + "/" + fileName;
            
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of(bucketName, filePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            
            Blob blob = storage.create(blobInfo, file.getBytes());
            String downloadUrl = getDownloadUrl(filePath);
            
            return FileUploadResponse.builder()
                    .fileName(fileName)
                    .fileUrl(downloadUrl)
                    .fileType(file.getContentType())
                    .size(file.getSize())
                    .build();
                    
        } catch (IOException e) {
            log.error("Error uploading file to Firebase Storage: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of(bucketName, filePath);
            return storage.delete(blobId);
        } catch (Exception e) {
            log.error("Error deleting file from Firebase Storage: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getDownloadUrl(String filePath) {
        try {
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of(bucketName, filePath);
            Blob blob = storage.get(blobId);
            
            if (blob != null) {
                return String.format("https://storage.googleapis.com/%s/%s", bucketName, filePath);
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting download URL: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        try {
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of(bucketName, filePath);
            Blob blob = storage.get(blobId);
            return blob != null && blob.exists();
        } catch (Exception e) {
            log.error("Error checking file existence: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String generateSignedUrl(String filePath, int expirationMinutes) {
        try {
            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of(bucketName, filePath);
            Blob blob = storage.get(blobId);

            if (blob != null && blob.exists()) {
                // For simplicity, return the public URL
                // In production, you would generate a proper signed URL
                return String.format("https://storage.googleapis.com/%s/%s", bucketName, filePath);
            }
            return null;
        } catch (Exception e) {
            log.error("Error generating signed URL: {}", e.getMessage());
            return null;
        }
    }

    private String generateFileName(String originalFileName) {
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}
