package com.classroomapp.classroombackend.controller.hrmanagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.common.FileUploadResponse;
import com.classroomapp.classroombackend.service.FileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 🎯 FIXED EXPLANATION ATTACHMENT CONTROLLER
 * ✅ Sử dụng ResponseEntity thay vì ApiResponse để tránh lỗi import
 * ✅ Sử dụng FileStorageService đơn giản
 * ✅ Proper error handling
 * ✅ Security validation
 */
@RestController
@RequestMapping("/api/hr/explanation-attachments")
@RequiredArgsConstructor
@Slf4j
public class ExplanationAttachmentController {
    
    private final FileStorageService fileStorageService;
    
    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    // Configuration constants
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_FILES_PER_EXPLANATION = 5;
    private static final String[] ALLOWED_EXTENSIONS = {
        "jpg", "jpeg", "png", "gif", "bmp", // Images
        "pdf", "doc", "docx", "txt", "rtf", // Documents
        "xls", "xlsx", "csv" // Spreadsheets
    };

    /**
     * 📤 Upload a single file for an explanation
     */
    @PostMapping("/upload/{explanationId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @PathVariable Long explanationId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            log.info("📁 Uploading file for explanation ID: {}", explanationId);
            
            // ✅ Validate file
            validateFile(file);
            
            // ✅ Check file count limit (simple implementation)
            int currentFileCount = getCurrentFileCount(explanationId);
            if (currentFileCount >= MAX_FILES_PER_EXPLANATION) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("FILE_LIMIT_EXCEEDED", 
                        "Maximum " + MAX_FILES_PER_EXPLANATION + " files allowed per explanation"));
            }
            
            // ✅ Save file using FileStorageService
            String folder = "explanation-attachments/" + explanationId;
            FileUploadResponse uploadResult = fileStorageService.save(file, folder);
            
            log.info("✅ File uploaded successfully for explanation {}: {}", explanationId, uploadResult.getFileName());
            
            return ResponseEntity.ok(createSuccessResponse("File uploaded successfully", uploadResult));
            
        } catch (IllegalArgumentException e) {
            log.warn("❌ File upload validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(createErrorResponse("VALIDATION_ERROR", e.getMessage()));
            
        } catch (Exception e) {
            log.error("💥 File upload failed for explanation {}: {}", explanationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("UPLOAD_ERROR", "Failed to upload file"));
        }
    }
    
    /**
     * 📤 Upload multiple files for an explanation
     */
    @PostMapping("/upload-multiple/{explanationId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(
            @PathVariable Long explanationId,
            @RequestParam("files") MultipartFile[] files) {
        
        try {
            log.info("📁 Uploading {} files for explanation ID: {}", files.length, explanationId);
            
            // ✅ Check total file count
            int currentFileCount = getCurrentFileCount(explanationId);
            if (currentFileCount + files.length > MAX_FILES_PER_EXPLANATION) {
                return ResponseEntity.badRequest()
                    .body(createErrorResponse("FILE_LIMIT_EXCEEDED", 
                        "Total files would exceed limit of " + MAX_FILES_PER_EXPLANATION));
            }
            
            List<FileUploadResponse> uploadResults = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            String folder = "explanation-attachments/" + explanationId;
            
            // ✅ Upload each file
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        validateFile(file);
                        FileUploadResponse result = fileStorageService.save(file, folder);
                        uploadResults.add(result);
                    } catch (Exception e) {
                        log.error("💥 Failed to upload file: {}", file.getOriginalFilename(), e);
                        errors.add(file.getOriginalFilename() + ": " + e.getMessage());
                    }
                }
            }
            
            MultipleFileUploadResponse response = new MultipleFileUploadResponse(
                uploadResults.size(),
                errors.isEmpty() ? "All files uploaded successfully" : 
                    uploadResults.size() + " files uploaded, " + errors.size() + " failed",
                uploadResults,
                errors
            );
            
            return ResponseEntity.ok(createSuccessResponse("Multiple files processed", response));
            
        } catch (Exception e) {
            log.error("💥 Multiple file upload failed for explanation {}: {}", explanationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("UPLOAD_ERROR", "Failed to upload files"));
        }
    }
    
    /**
     * 📥 Download a file
     */
    @GetMapping("/download/{explanationId}/{filename:.+}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long explanationId, @PathVariable String filename) {
        
        try {
            log.info("📥 Download request for explanation {}: {}", explanationId, filename);
            
            // ✅ Construct file path
            Path filePath = Paths.get(uploadDir, "explanation-attachments", explanationId.toString(), filename);
            
            // ✅ Security check
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path resolvedPath = filePath.toAbsolutePath().normalize();
            
            if (!resolvedPath.startsWith(uploadPath)) {
                log.warn("⚠️ Path traversal attempt detected: {}", filename);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            
            // ✅ Check if file exists
            if (!Files.exists(filePath)) {
                log.warn("📄 File not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // ✅ Read file
            byte[] fileContent = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            // ✅ Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            // ✅ Extract original filename
            String originalFilename = extractOriginalFilename(filename);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + originalFilename + "\"")
                .body(resource);
                
        } catch (IOException e) {
            log.error("💥 File download failed for explanation {}: {}", explanationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 👀 View/Preview a file (for images and documents)
     */
    @GetMapping("/view/{explanationId}/{filename:.+}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Resource> viewFile(@PathVariable Long explanationId, @PathVariable String filename) {
        
        try {
            Path filePath = Paths.get(uploadDir, "explanation-attachments", explanationId.toString(), filename);
            
            // Security check
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path resolvedPath = filePath.toAbsolutePath().normalize();
            
            if (!resolvedPath.startsWith(uploadPath) || !Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] fileContent = Files.readAllBytes(filePath);
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
                
        } catch (IOException e) {
            log.error("💥 File view failed for explanation {}: {}", explanationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 📋 Get all attachments for an explanation
     */
    @GetMapping("/explanation/{explanationId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getAttachmentsByExplanation(@PathVariable Long explanationId) {
        
        try {
            Path explanationDir = Paths.get(uploadDir, "explanation-attachments", explanationId.toString());
            List<AttachmentInfo> attachments = new ArrayList<>();
            
            if (Files.exists(explanationDir) && Files.isDirectory(explanationDir)) {
                Files.list(explanationDir)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        try {
                            String filename = filePath.getFileName().toString();
                            long size = Files.size(filePath);
                            String contentType = Files.probeContentType(filePath);
                            
                            AttachmentInfo info = new AttachmentInfo(
                                filename,
                                extractOriginalFilename(filename),
                                formatFileSize(size),
                                contentType,
                                LocalDateTime.now(), // Simplified - could get actual timestamp
                                isImageFile(contentType),
                                isDocumentFile(contentType)
                            );
                            
                            attachments.add(info);
                        } catch (IOException e) {
                            log.warn("Error reading file info: {}", filePath, e);
                        }
                    });
            }
            
            return ResponseEntity.ok(createSuccessResponse("Attachments retrieved successfully", attachments));
            
        } catch (IOException e) {
            log.error("💥 Failed to get attachments for explanation {}: {}", explanationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("READ_ERROR", "Failed to read attachments"));
        }
    }
    
    /**
     * 🗑️ Delete an attachment
     */
    @DeleteMapping("/{explanationId}/{filename:.+}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> deleteAttachment(@PathVariable Long explanationId, @PathVariable String filename) {
        
        try {
            Path filePath = Paths.get(uploadDir, "explanation-attachments", explanationId.toString(), filename);
            
            // Security check
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path resolvedPath = filePath.toAbsolutePath().normalize();
            
            if (!resolvedPath.startsWith(uploadPath)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse("FORBIDDEN", "Access denied"));
            }
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("🗑️ Deleted file: {}", filePath);
                return ResponseEntity.ok(createSuccessResponse("File deleted successfully", null));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (IOException e) {
            log.error("💥 File deletion failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("DELETE_ERROR", "Failed to delete file"));
        }
    }
    
    /**
     * 📊 Get storage statistics for an explanation
     */
    @GetMapping("/storage/{explanationId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Map<String, Object>> getStorageInfo(@PathVariable Long explanationId) {
        
        try {
            Path explanationDir = Paths.get(uploadDir, "explanation-attachments", explanationId.toString());
            
            int fileCount = 0;
            long totalSize = 0;
            
            if (Files.exists(explanationDir) && Files.isDirectory(explanationDir)) {
                fileCount = (int) Files.list(explanationDir).filter(Files::isRegularFile).count();
                totalSize = Files.list(explanationDir)
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
            }
            
            StorageInfo storageInfo = new StorageInfo(
                fileCount,
                totalSize,
                formatFileSize(totalSize),
                MAX_FILES_PER_EXPLANATION - fileCount
            );
            
            return ResponseEntity.ok(createSuccessResponse("Storage info retrieved", storageInfo));
            
        } catch (IOException e) {
            log.error("💥 Failed to get storage info for explanation {}: {}", explanationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("STORAGE_ERROR", "Failed to get storage info"));
        }
    }
    
    // ===== UTILITY METHODS =====
    
    /**
     * ✅ Create success response map
     */
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    /**
     * ❌ Create error response map
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("errorCode", errorCode);
        response.put("message", message);
        response.put("timestamp", LocalDateTime.now());
        return response;
    }
    
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File trống");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File quá lớn. Kích thước tối đa: " + (MAX_FILE_SIZE / (1024 * 1024)) + "MB");
        }
        
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Tên file là bắt buộc");
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        boolean allowed = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(extension)) {
                allowed = true;
                break;
            }
        }
        
        if (!allowed) {
            throw new IllegalArgumentException("Loại file không được phép: " + extension);
        }
    }
    
    private int getCurrentFileCount(Long explanationId) {
        try {
            Path explanationDir = Paths.get(uploadDir, "explanation-attachments", explanationId.toString());
            if (Files.exists(explanationDir) && Files.isDirectory(explanationDir)) {
                return (int) Files.list(explanationDir).filter(Files::isRegularFile).count();
            }
            return 0;
        } catch (IOException e) {
            log.warn("Error counting files for explanation {}: {}", explanationId, e.getMessage());
            return 0;
        }
    }
    
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
    
    private String extractOriginalFilename(String filename) {
        // Same logic as FileDownloadController
        if (filename == null || !filename.contains("_")) {
            return filename;
        }
        
        String[] parts = filename.split("_");
        if (parts.length >= 3) {
            return parts[parts.length - 1]; // Simplified extraction
        }
        return filename;
    }
    
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) {
            return "0 B";
        }
        
        double size = bytes.doubleValue();
        if (size < 1024) return String.format("%.0f B", size);
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024 * 1024));
        return String.format("%.1f GB", size / (1024 * 1024 * 1024));
    }
    
    private boolean isImageFile(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
    
    private boolean isDocumentFile(String contentType) {
        return contentType != null && (contentType.startsWith("application/") || contentType.equals("text/plain"));
    }
    
    // ===== RESPONSE DTOs =====
    
    public record MultipleFileUploadResponse(
        int uploadedCount,
        String message,
        List<FileUploadResponse> files,
        List<String> errors
    ) {}
    
    public record AttachmentInfo(
        String filename,
        String originalFilename,
        String fileSize,
        String mimeType,
        LocalDateTime uploadedAt,
        boolean isImage,
        boolean isDocument
    ) {}
    
    public record StorageInfo(
        int fileCount,
        Long totalSizeBytes,
        String totalSizeFormatted,
        int remainingSlots
    ) {}
}