package com.classroomapp.classroombackend.service;

import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.FileUploadResponse;

public interface FileStorageService {

    /**
     * Stores the file and returns the file URL as String
     */
    String store(MultipartFile file, String folder);

    /**
     * Saves a file to the storage and returns metadata
     */
    FileUploadResponse save(MultipartFile file, String folder);

    /**
     * Default save method (uploads to default folder "uploads")
     */
    default FileUploadResponse save(MultipartFile file) {
        return save(file, "uploads");
    }

    /**
     * Deletes a file by name
     */
    void delete(String fileName);
}
