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
} 