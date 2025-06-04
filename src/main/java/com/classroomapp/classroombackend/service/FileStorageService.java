package com.classroomapp.classroombackend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Service interface for file storage operations
 */
public interface FileStorageService {
    
    /**
     * Store a file and return its URL
     * @param file the file to store
     * @return the URL of the stored file
     * @throws IOException if an I/O error occurs
     */
    String storeFile(MultipartFile file) throws IOException;
    
    /**
     * Store multiple files and return their URLs
     * @param files the files to store
     * @return list of URLs of the stored files
     * @throws IOException if an I/O error occurs
     */
    List<String> storeFiles(List<MultipartFile> files) throws IOException;
    
    /**
     * Delete a file by its URL
     * @param fileUrl the URL of the file to delete
     * @return true if the file was deleted successfully
     */
    boolean deleteFile(String fileUrl);
}
