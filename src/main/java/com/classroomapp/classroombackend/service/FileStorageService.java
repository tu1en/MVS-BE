package com.classroomapp.classroombackend.service;

import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.common.FileUploadResponse;

/**
 * 🎯 MERGED FILE STORAGE SERVICE
 * Merged từ: FileStorageService + LocalFileStorageServiceImpl + DummyFileStorageServiceImpl
 * 
 * ✅ Simplified interface cho file storage
 * ✅ Support cả local và cloud storage
 * ✅ Compatible với existing assignment system
 */
public interface FileStorageService {
    
    /**
     * Save file to storage với default folder
     */
    default FileUploadResponse save(MultipartFile file) {
        return save(file, "uploads");
    }

    /**
     * Save file to specific folder
     */
    FileUploadResponse save(MultipartFile file, String folder);

    /**
     * Delete file from storage
     */
    void delete(String fileName);

    /**
     * Check if file exists
     */
    default boolean exists(String fileName) {
        return false; // Default implementation
    }

    /**
     * Get file URL
     */
    default String getFileUrl(String fileName, String folder) {
        return "/api/files/download/" + folder + "/" + fileName;
    }
}