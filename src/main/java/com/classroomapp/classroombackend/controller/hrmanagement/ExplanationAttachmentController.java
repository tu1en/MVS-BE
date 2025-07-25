package com.classroomapp.classroombackend.controller.hrmanagement;

import java.io.IOException;
import java.util.List;

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

import com.classroomapp.classroombackend.model.hrmanagement.ExplanationAttachment;
import com.classroomapp.classroombackend.service.hrmanagement.ExplanationFileUploadService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for handling explanation attachment operations
 */
@RestController
@RequestMapping("/api/hr/explanation-attachments")
@RequiredArgsConstructor
@Slf4j
public class ExplanationAttachmentController {
    
    private final ExplanationFileUploadService fileUploadService;
    
    /**
     * Upload a single file for an explanation
     */
    @PostMapping("/upload/{explanationId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> uploadFile(
            @PathVariable Long explanationId,
            @RequestParam("file") MultipartFile file) {
        
        try {
            log.info("Uploading file for explanation ID: {}", explanationId);
            
            ExplanationAttachment attachment = fileUploadService.uploadFile(file, explanationId);
            
            return ResponseEntity.ok(new FileUploadResponse(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getFormattedFileSize(),
                attachment.getMimeType(),
                "File uploaded successfully"
            ));
            
        } catch (IllegalArgumentException e) {
            log.warn("File upload validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage()));
            
        } catch (IOException e) {
            log.error("File upload failed for explanation {}", explanationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("UPLOAD_ERROR", "Failed to upload file"));
        }
    }
    
    /**
     * Upload multiple files for an explanation
     */
    @PostMapping("/upload-multiple/{explanationId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> uploadMultipleFiles(
            @PathVariable Long explanationId,
            @RequestParam("files") MultipartFile[] files) {
        
        try {
            log.info("Uploading {} files for explanation ID: {}", files.length, explanationId);
            
            List<ExplanationAttachment> attachments = fileUploadService.uploadFiles(files, explanationId);
            
            List<FileUploadResponse> responses = attachments.stream()
                .map(attachment -> new FileUploadResponse(
                    attachment.getId(),
                    attachment.getFileName(),
                    attachment.getFormattedFileSize(),
                    attachment.getMimeType(),
                    "File uploaded successfully"
                ))
                .toList();
            
            return ResponseEntity.ok(new MultipleFileUploadResponse(
                responses.size(),
                "All files uploaded successfully",
                responses
            ));
            
        } catch (IllegalArgumentException e) {
            log.warn("Multiple file upload validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("VALIDATION_ERROR", e.getMessage()));
            
        } catch (IOException e) {
            log.error("Multiple file upload failed for explanation {}", explanationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("UPLOAD_ERROR", "Failed to upload one or more files"));
        }
    }
    
    /**
     * Get all attachments for an explanation
     */
    @GetMapping("/explanation/{explanationId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<AttachmentInfo>> getAttachmentsByExplanation(@PathVariable Long explanationId) {
        
        try {
            List<ExplanationAttachment> attachments = fileUploadService.getAttachmentsByExplanation(explanationId);
            
            List<AttachmentInfo> attachmentInfos = attachments.stream()
                .map(attachment -> new AttachmentInfo(
                    attachment.getId(),
                    attachment.getFileName(),
                    attachment.getFormattedFileSize(),
                    attachment.getMimeType(),
                    attachment.getUploadedAt(),
                    attachment.isImage(),
                    attachment.isDocument()
                ))
                .toList();
            
            return ResponseEntity.ok(attachmentInfos);
            
        } catch (Exception e) {
            log.error("Failed to get attachments for explanation {}", explanationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Download a file
     */
    @GetMapping("/download/{attachmentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long attachmentId) {
        
        try {
            ExplanationAttachment attachment = fileUploadService.getAttachment(attachmentId);
            byte[] fileContent = fileUploadService.getFileContent(attachmentId);
            
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
                
        } catch (IllegalArgumentException e) {
            log.warn("File download failed - attachment not found: {}", attachmentId);
            return ResponseEntity.notFound().build();
            
        } catch (IOException e) {
            log.error("File download failed for attachment {}", attachmentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * View/Preview a file (for images and documents)
     */
    @GetMapping("/view/{attachmentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Resource> viewFile(@PathVariable Long attachmentId) {
        
        try {
            ExplanationAttachment attachment = fileUploadService.getAttachment(attachmentId);
            byte[] fileContent = fileUploadService.getFileContent(attachmentId);
            
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(attachment.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(resource);
                
        } catch (IllegalArgumentException e) {
            log.warn("File view failed - attachment not found: {}", attachmentId);
            return ResponseEntity.notFound().build();
            
        } catch (IOException e) {
            log.error("File view failed for attachment {}", attachmentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete an attachment
     */
    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long attachmentId) {
        
        try {
            fileUploadService.deleteAttachment(attachmentId);
            
            return ResponseEntity.ok(new SuccessResponse("File deleted successfully"));
            
        } catch (IllegalArgumentException e) {
            log.warn("File deletion failed - attachment not found: {}", attachmentId);
            return ResponseEntity.notFound().build();
            
        } catch (IOException e) {
            log.error("File deletion failed for attachment {}", attachmentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("DELETE_ERROR", "Failed to delete file"));
        }
    }
    
    /**
     * Get storage statistics for an explanation
     */
    @GetMapping("/storage/{explanationId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<StorageInfo> getStorageInfo(@PathVariable Long explanationId) {
        
        try {
            Long totalSize = fileUploadService.getTotalStorageUsed(explanationId);
            List<ExplanationAttachment> attachments = fileUploadService.getAttachmentsByExplanation(explanationId);
            
            return ResponseEntity.ok(new StorageInfo(
                attachments.size(),
                totalSize,
                formatFileSize(totalSize),
                ExplanationFileUploadService.FileUploadConfig.MAX_FILES_PER_EXPLANATION - attachments.size()
            ));
            
        } catch (Exception e) {
            log.error("Failed to get storage info for explanation {}", explanationId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Helper method to format file size
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) {
            return "0 B";
        }
        
        double size = bytes.doubleValue();
        if (size < 1024) {
            return String.format("%.0f B", size);
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024 * 1024));
        } else {
            return String.format("%.1f GB", size / (1024 * 1024 * 1024));
        }
    }
    
    // Response DTOs
    public record FileUploadResponse(
        Long id,
        String fileName,
        String fileSize,
        String mimeType,
        String message
    ) {}
    
    public record MultipleFileUploadResponse(
        int uploadedCount,
        String message,
        List<FileUploadResponse> files
    ) {}
    
    public record AttachmentInfo(
        Long id,
        String fileName,
        String fileSize,
        String mimeType,
        java.time.LocalDateTime uploadedAt,
        boolean isImage,
        boolean isDocument
    ) {}
    
    public record StorageInfo(
        int fileCount,
        Long totalSizeBytes,
        String totalSizeFormatted,
        int remainingSlots
    ) {}
    
    public record ErrorResponse(
        String errorCode,
        String message
    ) {}
    
    public record SuccessResponse(
        String message
    ) {}
}