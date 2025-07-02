package com.classroomapp.classroombackend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * Upload a file to storage
     * 
     * @param file The file to upload
     * @param folder The destination folder in storage
     * @return The URL to access the uploaded file
     */
    String uploadFile(MultipartFile file, String folder) throws Exception;
    
    /**
     * Store a file and return the file name
     * 
     * @param file The file to store
     * @return The stored file name
     */
    String storeFile(MultipartFile file) throws Exception;
    
    /**
     * Get the file storage location path
     * 
     * @return The file storage location
     */
    String getFileStorageLocation();
    
    /**
     * Get file content as byte array
     * 
     * @param filePath The path to the file
     * @return The file content as byte array
     */
    byte[] getFileContent(String filePath) throws Exception;
    
    /**
     * Delete a file from storage
     * 
     * @param filePath The path to the file to delete
     * @return true if deleted successfully
     */
    boolean deleteFile(String filePath);
}