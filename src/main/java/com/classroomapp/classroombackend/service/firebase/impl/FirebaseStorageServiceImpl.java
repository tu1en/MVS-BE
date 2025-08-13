package com.classroomapp.classroombackend.service.firebase.impl;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.common.FileUploadResponse;
import com.classroomapp.classroombackend.service.firebase.FirebaseStorageService;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;

import lombok.extern.slf4j.Slf4j;

@Service("firebaseStorageService")
// @Profile("firebase")
@Slf4j
public class FirebaseStorageServiceImpl implements FirebaseStorageService {

    @Value("${firebase.bucket-name:default-bucket}")
    private String bucketName;

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, String folder) {
        try {
            String fileName = generateFileName(file.getOriginalFilename());
            String filePath = folder + "/" + fileName;
            
            var firebaseApp = FirebaseApp.getInstance("classroom-management");
            var bucket = StorageClient.getInstance(firebaseApp).bucket();
            Storage storage = bucket.getStorage();
            // Prefer the bucket name provided by the initialized FirebaseApp to avoid mismatch
            String effectiveBucketName = bucket.getName();
            BlobId blobId = BlobId.of(effectiveBucketName, filePath);
            // Set Firebase download token in metadata to enable tokenized downloads without ACLs
            String downloadToken = java.util.UUID.randomUUID().toString();
            java.util.Map<String, String> metadata = new java.util.HashMap<>();
            metadata.put("firebaseStorageDownloadTokens", downloadToken);

            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .setMetadata(metadata)
                    .build();

            storage.create(blobInfo, file.getBytes());

            String downloadUrl = String.format(
                    "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media&token=%s",
                    effectiveBucketName,
                    java.net.URLEncoder.encode(filePath, "UTF-8"),
                    downloadToken
            );
            
            return FileUploadResponse.builder()
                    .filename(fileName)
                    .fileUrl(downloadUrl)
                    .mimeType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();
                    
        } catch (IOException e) {
            log.error("Lỗi khi tải lên file lên Firebase Storage: {}", e.getMessage());
            throw new RuntimeException("Tải lên file thất bại", e);
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            Storage storage = StorageClient.getInstance(FirebaseApp.getInstance("classroom-management")).bucket().getStorage();
            BlobId blobId = BlobId.of(bucketName, filePath);
            return storage.delete(blobId);
        } catch (Exception e) {
            log.error("Lỗi khi xóa file khỏi Firebase Storage: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getDownloadUrl(String filePath) {
        try {
            Storage storage = StorageClient.getInstance(FirebaseApp.getInstance("classroom-management")).bucket().getStorage();
            BlobId blobId = BlobId.of(bucketName, filePath);
            Blob blob = storage.get(blobId);
            
            if (blob != null) {
                return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucketName, java.net.URLEncoder.encode(filePath, "UTF-8"));
            }
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi lấy URL tải xuống: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean fileExists(String filePath) {
        try {
            Storage storage = StorageClient.getInstance(FirebaseApp.getInstance("classroom-management")).bucket().getStorage();
            BlobId blobId = BlobId.of(bucketName, filePath);
            Blob blob = storage.get(blobId);
            return blob != null && blob.exists();
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra sự tồn tại của file: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String generateSignedUrl(String filePath, int expirationMinutes) {
        try {
            Storage storage = StorageClient.getInstance(FirebaseApp.getInstance("classroom-management")).bucket().getStorage();
            BlobId blobId = BlobId.of(bucketName, filePath);
            Blob blob = storage.get(blobId);

            if (blob != null && blob.exists()) {
                // For simplicity, return the public URL
                // In production, you would generate a proper signed URL
                return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucketName, java.net.URLEncoder.encode(filePath, "UTF-8"));
            }
            return null;
        } catch (Exception e) {
            log.error("Lỗi khi tạo signed URL: {}", e.getMessage());
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
