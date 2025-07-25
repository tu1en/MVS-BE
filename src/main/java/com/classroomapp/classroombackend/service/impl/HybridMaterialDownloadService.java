package com.classroomapp.classroombackend.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Hybrid service Ä‘á»ƒ handle cáº£ local files vÃ  Firebase Storage URLs
 * Supports backward compatibility vá»›i existing local files
 */
@Service
@Slf4j
public class HybridMaterialDownloadService {

    @Value("${firebase.bucket-name:}")
    private String firebaseBucket;

    /**
     * Download material content tá»« local file hoáº·c Firebase Storage
     * @param filePath path hoáº·c URL cá»§a file
     * @param materialId ID cá»§a material Ä‘á»ƒ logging
     * @return byte array cá»§a file content
     */
    public byte[] downloadMaterialContent(String filePath, Long materialId) {
        log.info("ðŸ”½ HYBRID: Starting download for material ID: {}, path: {}", materialId, filePath);

        if (isFirebaseUrl(filePath)) {
            return downloadFromFirebase(filePath, materialId);
        } else {
            return downloadFromLocalFileSystem(filePath, materialId);
        }
    }

    /**
     * Kiá»ƒm tra xem path cÃ³ pháº£i lÃ  Firebase Storage URL khÃ´ng
     */
    private boolean isFirebaseUrl(String filePath) {
        if (filePath == null) return false;
        
        return filePath.startsWith("https://storage.googleapis.com/") ||
               filePath.startsWith("https://firebasestorage.googleapis.com/") ||
               (firebaseBucket != null && filePath.contains(firebaseBucket));
    }

    /**
     * Download tá»« Firebase Storage URL
     */
    private byte[] downloadFromFirebase(String firebaseUrl, Long materialId) {
        try {
            log.info("ðŸ”½ HYBRID: Downloading from Firebase Storage: {}", firebaseUrl);
            
            // Download tá»« Firebase Storage URL
            URL url = new URL(firebaseUrl);
            byte[] content = url.openStream().readAllBytes();
            
            log.info("ðŸ”½ HYBRID: Successfully downloaded from Firebase, size: {} bytes", content.length);
            return content;
            
        } catch (IOException e) {
            log.error("ðŸ”½ HYBRID: Failed to download from Firebase URL: {}", firebaseUrl, e);
            throw new RuntimeException("KhÃ´ng thá»ƒ táº£i file tá»« Firebase Storage: " + e.getMessage(), e);
        }
    }

    /**
     * Download tá»« local file system (existing logic)
     */
    private byte[] downloadFromLocalFileSystem(String filePath, Long materialId) {
        try {
            log.info("ðŸ”½ HYBRID: Downloading from local file system: {}", filePath);
            
            // Remove leading "/" if present
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
                log.info("ðŸ”½ HYBRID: Removed leading slash, new path: {}", filePath);
            }

            // Build absolute path from project root
            Path projectRoot = Paths.get(System.getProperty("user.dir"));
            Path absolutePath;
            
            log.info("ðŸ”½ HYBRID: Project root directory: {}", projectRoot.toString());
            
            // Check if running from backend/doproject directory
            if (projectRoot.toString().endsWith("backend" + File.separator + "doproject")) {
                absolutePath = projectRoot.resolve(filePath);
                log.info("ðŸ”½ HYBRID: Running from backend/doproject, resolved path: {}", absolutePath.toString());
            } else {
                // If running from root project
                absolutePath = projectRoot.resolve("backend").resolve("doproject").resolve(filePath);
                log.info("ðŸ”½ HYBRID: Running from root project, resolved path: {}", absolutePath.toString());
            }

            log.info("ðŸ”½ HYBRID: Final absolute file path: {}", absolutePath.toString());

            // Check if file exists
            if (!Files.exists(absolutePath)) {
                log.error("ðŸ”½ HYBRID: File does not exist at primary path: {}", absolutePath.toString());
                
                // Try alternative paths
                Path alternativePath1 = Paths.get("uploads", filePath.replace("uploads/", ""));
                Path alternativePath2 = Paths.get("backend", "doproject", "uploads", filePath.replace("uploads/", ""));
                
                log.info("ðŸ”½ HYBRID: Trying alternative path 1: {}", alternativePath1.toString());
                log.info("ðŸ”½ HYBRID: Trying alternative path 2: {}", alternativePath2.toString());
                
                if (Files.exists(alternativePath1)) {
                    absolutePath = alternativePath1;
                    log.info("ðŸ”½ HYBRID: Using alternative path 1 - file found");
                } else if (Files.exists(alternativePath2)) {
                    absolutePath = alternativePath2;
                    log.info("ðŸ”½ HYBRID: Using alternative path 2 - file found");
                } else {
                    log.error("ðŸ”½ HYBRID: File not found at any local path for material ID: {}", materialId);
                    throw new RuntimeException("File tÃ i liá»‡u khÃ´ng tá»“n táº¡i: " + filePath);
                }
            } else {
                log.info("ðŸ”½ HYBRID: File exists at primary path: {}", absolutePath.toString());
            }

            // Read and return file content
            byte[] fileContent = Files.readAllBytes(absolutePath);
            log.info("ðŸ”½ HYBRID: Successfully read local file content, size: {} bytes", fileContent.length);
            
            return fileContent;
            
        } catch (IOException e) {
            log.error("ðŸ”½ HYBRID: I/O error reading local file for material ID {}: {}", materialId, e.getMessage());
            throw new RuntimeException("KhÃ´ng thá»ƒ Ä‘á»c file tÃ i liá»‡u: " + e.getMessage(), e);
        }
    }

    /**
     * Determine storage type for a given file path
     */
    public String getStorageType(String filePath) {
        return isFirebaseUrl(filePath) ? "FIREBASE" : "LOCAL";
    }
}
