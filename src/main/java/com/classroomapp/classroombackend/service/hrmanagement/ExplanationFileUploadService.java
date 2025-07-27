package com.classroomapp.classroombackend.service.hrmanagement;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.model.hrmanagement.ExplanationAttachment;

/**
 * Service interface for handling file uploads for attendance explanations
 */
public interface ExplanationFileUploadService {
    
    /**
     * Upload a single file for an explanation
     * @param file the multipart file
     * @param explanationId the explanation ID
     * @return the saved attachment
     * @throws IOException if file upload fails
     */
    ExplanationAttachment uploadFile(MultipartFile file, Long explanationId) throws IOException;
    
    /**
     * Upload multiple files for an explanation
     * @param files array of multipart files
     * @param explanationId the explanation ID
     * @return list of saved attachments
     * @throws IOException if any file upload fails
     */
    List<ExplanationAttachment> uploadFiles(MultipartFile[] files, Long explanationId) throws IOException;
    
    /**
     * Delete an attachment
     * @param attachmentId the attachment ID
     * @throws IOException if file deletion fails
     */
    void deleteAttachment(Long attachmentId) throws IOException;
    
    /**
     * Get attachment by ID
     * @param attachmentId the attachment ID
     * @return the attachment
     */
    ExplanationAttachment getAttachment(Long attachmentId);
    
    /**
     * Get all attachments for an explanation
     * @param explanationId the explanation ID
     * @return list of attachments
     */
    List<ExplanationAttachment> getAttachmentsByExplanation(Long explanationId);
    
    /**
     * Get file content for download
     * @param attachmentId the attachment ID
     * @return file content as byte array
     * @throws IOException if file reading fails
     */
    byte[] getFileContent(Long attachmentId) throws IOException;
    
    /**
     * Validate file before upload
     * @param file the multipart file
     * @return true if valid
     * @throws IllegalArgumentException if file is invalid
     */
    boolean validateFile(MultipartFile file) throws IllegalArgumentException;
    
    /**
     * Get total storage used by an explanation
     * @param explanationId the explanation ID
     * @return total size in bytes
     */
    Long getTotalStorageUsed(Long explanationId);
    
    /**
     * Clean up orphaned files (files without explanation)
     * @return number of files cleaned up
     */
    int cleanupOrphanedFiles();
    
    /**
     * Archive old files (move to archive storage)
     * @param daysSince number of days since upload
     * @return number of files archived
     */
    int archiveOldFiles(int daysSince);
    
    /**
     * Configuration for file upload restrictions
     */
    class FileUploadConfig {
        public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
        public static final int MAX_FILES_PER_EXPLANATION = 5;
        public static final String[] ALLOWED_EXTENSIONS = {
            "jpg", "jpeg", "png", "gif", "bmp", // Images
            "pdf", "doc", "docx", "txt", "rtf", // Documents
            "xls", "xlsx", "csv" // Spreadsheets
        };
        public static final String[] ALLOWED_MIME_TYPES = {
            "image/jpeg", "image/png", "image/gif", "image/bmp",
            "application/pdf", "application/msword", 
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain", "text/rtf",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/csv"
        };
    }
}