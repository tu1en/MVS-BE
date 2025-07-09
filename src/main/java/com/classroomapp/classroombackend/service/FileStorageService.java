package com.classroomapp.classroombackend.service;

import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.FileUploadResponse;

public interface FileStorageService {
    /**
     * Saves a file to the storage.
     * @param file the file to save
     * @return metadata about the saved file
     */
    default FileUploadResponse save(MultipartFile file) {
        return save(file, "uploads");
    }

    /**
     * Saves a file to a specific folder in the storage
     * @param file the file to save
     * @param folder the folder to save into
     * @return metadata about the saved file
     */
    FileUploadResponse save(MultipartFile file, String folder);

    /**
     * Deletes a file from the storage.
     * @param fileName the name of the file to delete
     */
    void delete(String fileName);
}