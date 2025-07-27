package com.classroomapp.classroombackend.service.impl.hrmanagement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.model.AttendanceExplanation;
import com.classroomapp.classroombackend.model.hrmanagement.ExplanationAttachment;
import com.classroomapp.classroombackend.repository.AttendanceExplanationRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.ExplanationAttachmentRepository;
import com.classroomapp.classroombackend.service.hrmanagement.ExplanationFileUploadService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ExplanationFileUploadService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ExplanationFileUploadServiceImpl implements ExplanationFileUploadService {
    
    private final ExplanationAttachmentRepository attachmentRepository;
    private final AttendanceExplanationRepository explanationRepository;
    
    @Value("${app.file-upload.explanation-attachments.path:uploads/explanation-attachments}")
    private String uploadPath;
    
    @Value("${app.file-upload.create-directories:true}")
    private boolean createDirectories;
    
    @Override
    public ExplanationAttachment uploadFile(MultipartFile file, Long explanationId) throws IOException {
        log.info("Uploading file {} for explanation {}", file.getOriginalFilename(), explanationId);
        
        // Validate file
        validateFile(file);
        
        // Check if explanation exists
        AttendanceExplanation explanation = explanationRepository.findById(explanationId)
            .orElseThrow(() -> new IllegalArgumentException("Explanation not found with ID: " + explanationId));
        
        // Check file count limit
        long currentFileCount = attachmentRepository.countByExplanationId(explanationId);
        if (currentFileCount >= FileUploadConfig.MAX_FILES_PER_EXPLANATION) {
            throw new IllegalArgumentException("Maximum number of files exceeded for this explanation");
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
        
        // Create upload directory if it doesn't exist
        Path uploadDir = Paths.get(uploadPath);
        if (createDirectories && !Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        
        // Save file to disk
        Path filePath = uploadDir.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Create attachment record
        ExplanationAttachment attachment = new ExplanationAttachment();
        attachment.setExplanation(explanation);
        attachment.setFileName(originalFilename);
        attachment.setFilePath(filePath.toString());
        attachment.setFileSize(file.getSize());
        attachment.setMimeType(file.getContentType());
        attachment.setUploadedAt(LocalDateTime.now());
        
        ExplanationAttachment savedAttachment = attachmentRepository.save(attachment);
        
        log.info("File uploaded successfully: {} -> {}", originalFilename, uniqueFilename);
        return savedAttachment;
    }
    
    @Override
    public List<ExplanationAttachment> uploadFiles(MultipartFile[] files, Long explanationId) throws IOException {
        log.info("Uploading {} files for explanation {}", files.length, explanationId);
        
        List<ExplanationAttachment> attachments = new ArrayList<>();
        
        // Check total file count
        long currentFileCount = attachmentRepository.countByExplanationId(explanationId);
        if (currentFileCount + files.length > FileUploadConfig.MAX_FILES_PER_EXPLANATION) {
            throw new IllegalArgumentException("Total number of files would exceed the limit");
        }
        
        // Upload each file
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    ExplanationAttachment attachment = uploadFile(file, explanationId);
                    attachments.add(attachment);
                } catch (Exception e) {
                    log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
                    // Clean up already uploaded files if any upload fails
                    for (ExplanationAttachment uploaded : attachments) {
                        try {
                            deleteAttachment(uploaded.getId());
                        } catch (Exception cleanupException) {
                            log.error("Failed to cleanup file during rollback: {}", uploaded.getFileName(), cleanupException);
                        }
                    }
                    throw new IOException("Failed to upload file: " + file.getOriginalFilename(), e);
                }
            }
        }
        
        log.info("Successfully uploaded {} files for explanation {}", attachments.size(), explanationId);
        return attachments;
    }
    
    @Override
    public void deleteAttachment(Long attachmentId) throws IOException {
        log.info("Deleting attachment with ID: {}", attachmentId);
        
        ExplanationAttachment attachment = attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new IllegalArgumentException("Attachment not found with ID: " + attachmentId));
        
        // Delete physical file
        try {
            Path filePath = Paths.get(attachment.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.debug("Deleted physical file: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("Failed to delete physical file: {}", attachment.getFilePath(), e);
            // Continue with database deletion even if physical file deletion fails
        }
        
        // Delete database record
        attachmentRepository.delete(attachment);
        
        log.info("Attachment deleted successfully: {}", attachment.getFileName());
    }
    
    @Override
    public ExplanationAttachment getAttachment(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
            .orElseThrow(() -> new IllegalArgumentException("Attachment not found with ID: " + attachmentId));
    }
    
    @Override
    public List<ExplanationAttachment> getAttachmentsByExplanation(Long explanationId) {
        return attachmentRepository.findByExplanationIdOrderByUploadedAtDesc(explanationId);
    }
    
    @Override
    public byte[] getFileContent(Long attachmentId) throws IOException {
        ExplanationAttachment attachment = getAttachment(attachmentId);
        Path filePath = Paths.get(attachment.getFilePath());
        
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + attachment.getFileName());
        }
        
        return Files.readAllBytes(filePath);
    }
    
    @Override
    public boolean validateFile(MultipartFile file) throws IllegalArgumentException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Check file size
        if (file.getSize() > FileUploadConfig.MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of " + 
                (FileUploadConfig.MAX_FILE_SIZE / (1024 * 1024)) + "MB");
        }
        
        // Check file extension
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Filename is required");
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        if (!Arrays.asList(FileUploadConfig.ALLOWED_EXTENSIONS).contains(extension)) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: " + 
                Arrays.toString(FileUploadConfig.ALLOWED_EXTENSIONS));
        }
        
        // Check MIME type
        String mimeType = file.getContentType();
        if (mimeType != null && !Arrays.asList(FileUploadConfig.ALLOWED_MIME_TYPES).contains(mimeType)) {
            log.warn("MIME type validation failed for file: {} (MIME: {})", filename, mimeType);
            // Don't throw exception for MIME type, just warn as browsers can send different MIME types
        }
        
        return true;
    }
    
    @Override
    public Long getTotalStorageUsed(Long explanationId) {
        return attachmentRepository.getStorageUsedByExplanation(explanationId);
    }
    
    @Override
    public int cleanupOrphanedFiles() {
        log.info("Starting cleanup of orphaned attachment files");
        
        List<ExplanationAttachment> orphanedAttachments = attachmentRepository.findOrphanedAttachments();
        int cleanedCount = 0;
        
        for (ExplanationAttachment attachment : orphanedAttachments) {
            try {
                deleteAttachment(attachment.getId());
                cleanedCount++;
            } catch (Exception e) {
                log.error("Failed to cleanup orphaned attachment: {}", attachment.getFileName(), e);
            }
        }
        
        log.info("Cleaned up {} orphaned attachment files", cleanedCount);
        return cleanedCount;
    }
    
    @Override
    public int archiveOldFiles(int daysSince) {
        log.info("Starting archival of files older than {} days", daysSince);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysSince);
        List<ExplanationAttachment> oldAttachments = attachmentRepository.findByUploadedAtBeforeOrderByUploadedAtAsc(cutoffDate);
        
        int archivedCount = 0;
        
        for (ExplanationAttachment attachment : oldAttachments) {
            try {
                // For now, just log the files that would be archived
                // In a full implementation, you might move files to an archive storage
                log.info("Would archive file: {} (uploaded: {})", attachment.getFileName(), attachment.getUploadedAt());
                archivedCount++;
            } catch (Exception e) {
                log.error("Failed to archive attachment: {}", attachment.getFileName(), e);
            }
        }
        
        log.info("Processed {} files for archival", archivedCount);
        return archivedCount;
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}