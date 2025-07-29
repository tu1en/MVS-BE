package com.classroomapp.classroombackend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * 🎯 FIXED FILE DOWNLOAD CONTROLLER
 * ✅ Sửa lỗi anonymous class syntax
 * ✅ Support multiple URL formats
 * ✅ Security protection
 * ✅ Proper content type detection
 * ✅ Compatible với existing URLs
 */
@RestController
@RequestMapping("/api/file-download")
@Slf4j
public class FileDownloadController {

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    /**
     * 📥 Download file - Original format
     * URL: /api/files/download/{folder}/{filename}
     */
    @GetMapping("/download/{folder}/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String folder, @PathVariable String filename) {
        return handleFileDownload(folder, filename, null, null);
    }

    /**
     * 📥 Download file - New structured format  
     * URL: /api/files/download/{folder}/{year}/{month}/{filename}
     */
    @GetMapping("/download/{folder}/{year}/{month}/{filename:.+}")
    public ResponseEntity<Resource> downloadFileStructured(
            @PathVariable String folder, 
            @PathVariable String year, 
            @PathVariable String month, 
            @PathVariable String filename) {
        return handleFileDownload(folder, filename, year, month);
    }

    /**
     * 🔧 Common download handler
     */
    private ResponseEntity<Resource> handleFileDownload(String folder, String filename, String year, String month) {
        try {
            log.info("📥 Download request: folder={}, file={}, year={}, month={}", folder, filename, year, month);

            // ✅ 1. Construct file path
            Path filePath;
            if (year != null && month != null) {
                // New structured format: uploads/folder/year/month/filename
                filePath = Paths.get(uploadDir, folder, year, month, filename);
            } else {
                // Legacy format: uploads/folder/filename
                filePath = Paths.get(uploadDir, folder, filename);
            }

            // ✅ 2. Security check: ensure file is within upload directory
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path resolvedPath = filePath.toAbsolutePath().normalize();
            
            if (!resolvedPath.startsWith(uploadPath)) {
                log.warn("⚠️ Path traversal attempt detected: {}", filename);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // ✅ 3. Check if file exists
            if (!Files.exists(filePath)) {
                log.warn("📄 File not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // ✅ 4. Read file data
            byte[] data = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(data);

            // ✅ 5. Determine content type
            String contentType = null;
            try {
                contentType = Files.probeContentType(filePath);
            } catch (IOException ex) {
                log.debug("Could not determine file type for: {}", filename);
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // ✅ 6. Extract original filename (remove UUID prefix if present)
            String originalFilename = extractOriginalFilename(filename);

            log.info("✅ Serving file: {} as {} ({})", filePath, originalFilename, contentType);

            // ✅ 7. Return file with proper headers
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // 1 hour cache
                    .body(resource);

        } catch (IOException e) {
            log.error("💥 Error reading file: {}/{}", folder, filename, e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("💥 Unexpected error serving file: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 🔧 Extract original filename from UUID-prefixed filename
     * 
     * Handles formats like:
     * - user_123_username_20250101_120000_uuid8chars_originalfile.pdf
     * - 20250101_120000_uuid8chars_originalfile.pdf
     * - originalfile.pdf
     */
    private String extractOriginalFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "download";
        }

        // If filename doesn't contain underscore, return as-is
        if (!filename.contains("_")) {
            return filename;
        }

        try {
            // Split by underscore
            String[] parts = filename.split("_");
            
            if (parts.length >= 3) {
                // Check if we have a pattern like: prefix_timestamp_uuid_originalname.ext
                
                // Find the last part that looks like a UUID (8 chars) + extension
                for (int i = parts.length - 2; i >= 0; i--) {
                    if (parts[i].length() == 8 && isUUIDPart(parts[i])) {
                        // Everything after the UUID is the original filename
                        if (i + 1 < parts.length) {
                            StringBuilder originalName = new StringBuilder();
                            for (int j = i + 1; j < parts.length; j++) {
                                if (j > i + 1) {
                                    originalName.append("_");
                                }
                                originalName.append(parts[j]);
                            }
                            return originalName.toString();
                        }
                    }
                }
                
                // If no UUID pattern found, assume last part is original filename
                return parts[parts.length - 1];
            }
            
            return filename;
            
        } catch (Exception e) {
            log.debug("Could not extract original filename from: {}, using as-is", filename);
            return filename;
        }
    }

    /**
     * 🔧 Check if string looks like a UUID part (8 hex characters)
     */
    private boolean isUUIDPart(String part) {
        if (part == null || part.length() != 8) {
            return false;
        }
        
        return part.matches("[a-fA-F0-9]{8}");
    }

    /**
     * 📊 File info endpoint (optional)
     */
    @GetMapping("/info/{folder}/{filename:.+}")
    public ResponseEntity<FileInfoResponse> getFileInfo(@PathVariable String folder, @PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir, folder, filename);
            
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            // Security check
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path resolvedPath = filePath.toAbsolutePath().normalize();
            
            if (!resolvedPath.startsWith(uploadPath)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // ✅ FIXED: Use proper DTO class instead of anonymous class
            long size = Files.size(filePath);
            String contentType = Files.probeContentType(filePath);
            String originalFilename = extractOriginalFilename(filename);

            FileInfoResponse fileInfo = new FileInfoResponse(
                originalFilename,
                folder + "/" + filename,
                size,
                contentType,
                true
            );

            return ResponseEntity.ok(fileInfo);

        } catch (IOException e) {
            log.error("Error getting file info: {}/{}", folder, filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * ✅ FIXED: Proper DTO class for file info response
     */
    public static class FileInfoResponse {
        public final String filename;
        public final String path;
        public final long size;
        public final String contentType;
        public final boolean exists;

        public FileInfoResponse(String filename, String path, long size, String contentType, boolean exists) {
            this.filename = filename;
            this.path = path;
            this.size = size;
            this.contentType = contentType;
            this.exists = exists;
        }
    }
}