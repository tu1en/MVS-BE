package com.classroomapp.classroombackend.service.firebase;

import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.FileUploadResponse;

/**
 * Service interface for Firebase Storage operations
 */
public interface FirebaseStorageService {
    
    /**
     * Upload file to Firebase Storage
     * 
     * @param file the file to upload
     * @param folder the folder path in storage
     * @return FileUploadResponse containing file information
     */
    FileUploadResponse uploadFile(MultipartFile file, String folder);
    
    /**
     * Delete file from Firebase Storage
     * 
     * @param filePath the path of file to delete
     * @return true if deletion successful, false otherwise
     */
    boolean deleteFile(String filePath);
    
    /**
     * Get download URL for a file
     * 
     * @param filePath the path of the file
     * @return download URL string
     */
    String getDownloadUrl(String filePath);
    
    /**
     * Check if file exists in Firebase Storage
     *
     * @param filePath the path of the file
     * @return true if file exists, false otherwise
     */
    boolean fileExists(String filePath);

    /**
     * Generate signed URL for file access
     *
     * @param filePath the path of the file
     * @param expirationMinutes expiration time in minutes
     * @return signed URL string
     */
    String generateSignedUrl(String filePath, int expirationMinutes);
}
