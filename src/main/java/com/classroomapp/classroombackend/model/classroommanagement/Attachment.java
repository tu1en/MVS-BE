package com.classroomapp.classroombackend.model.classroommanagement;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Attachment entity representing files uploaded to slots
 * Each attachment belongs to a slot and is uploaded by a user
 */
@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Original file name is required")
    @Size(max = 255, message = "Original file name cannot exceed 255 characters")
    @Column(name = "original_file_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String originalFileName;

    @NotBlank(message = "Stored file name is required")
    @Size(max = 255, message = "Stored file name cannot exceed 255 characters")
    @Column(name = "stored_file_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String storedFileName;

    @NotBlank(message = "File path is required")
    @Size(max = 500, message = "File path cannot exceed 500 characters")
    @Column(name = "file_path", nullable = false, columnDefinition = "NVARCHAR(500)")
    private String filePath;

    @NotNull(message = "File size is required")
    @Positive(message = "File size must be positive")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @NotBlank(message = "MIME type is required")
    @Size(max = 100, message = "MIME type cannot exceed 100 characters")
    @Column(name = "mime_type", nullable = false, columnDefinition = "NVARCHAR(100)")
    private String mimeType;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "attachments"})
    private Slot slot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "password"})
    private User uploadedBy;

    /**
     * Allowed MIME types for security
     */
    public static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
        "application/vnd.openxmlformats-officedocument.presentationml.presentation" // .pptx
    );

    /**
     * Maximum file size in bytes (10MB)
     */
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * Lifecycle callbacks
     */
    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
        validateFile();
    }

    /**
     * Business logic methods
     */
    
    /**
     * Validate file constraints
     */
    private void validateFile() {
        // Validate MIME type
        if (mimeType != null && !ALLOWED_MIME_TYPES.contains(mimeType)) {
            throw new IllegalArgumentException(
                String.format("MIME type '%s' is not allowed. Allowed types: %s", 
                    mimeType, String.join(", ", ALLOWED_MIME_TYPES))
            );
        }

        // Validate file size
        if (fileSize != null && fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                String.format("File size %d bytes exceeds maximum allowed size of %d bytes", 
                    fileSize, MAX_FILE_SIZE)
            );
        }

        // Validate file name for security (prevent path traversal)
        if (originalFileName != null && (originalFileName.contains("..") || 
            originalFileName.contains("/") || originalFileName.contains("\\"))) {
            throw new IllegalArgumentException("File name contains invalid characters");
        }
    }

    /**
     * Get file extension from original file name
     */
    public String getFileExtension() {
        if (originalFileName == null) return "";
        int lastDotIndex = originalFileName.lastIndexOf('.');
        return lastDotIndex > 0 ? originalFileName.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    /**
     * Get file type based on MIME type
     */
    public FileType getFileType() {
        if (mimeType == null) return FileType.UNKNOWN;
        
        switch (mimeType) {
            case "application/pdf":
                return FileType.PDF;
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return FileType.WORD;
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return FileType.POWERPOINT;
            default:
                return FileType.UNKNOWN;
        }
    }

    /**
     * Get formatted file size
     */
    public String getFormattedFileSize() {
        if (fileSize == null || fileSize == 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB"};
        int unitIndex = 0;
        double size = fileSize;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }

    /**
     * Check if file is a PDF
     */
    public boolean isPdf() {
        return getFileType() == FileType.PDF;
    }

    /**
     * Check if file is a Word document
     */
    public boolean isWordDocument() {
        return getFileType() == FileType.WORD;
    }

    /**
     * Check if file is a PowerPoint presentation
     */
    public boolean isPowerPointPresentation() {
        return getFileType() == FileType.POWERPOINT;
    }

    /**
     * Check if current user can delete this attachment
     */
    public boolean canBeDeletedBy(User user) {
        if (user == null) return false;
        
        // Owner can delete
        if (uploadedBy != null && uploadedBy.getId().equals(user.getId())) {
            return true;
        }
        
        // Manager can delete any attachment
        if (user.getRoleId() != null && user.getRoleId() == 3) { // MANAGER
            return true;
        }
        
        return false;
    }

    /**
     * Check if current user can download this attachment
     */
    public boolean canBeDownloadedBy(User user) {
        if (user == null) return false;
        
        // All authenticated users can download attachments
        // Additional business logic can be added here if needed
        return true;
    }

    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        return originalFileName != null ? originalFileName : storedFileName;
    }

    /**
     * Get uploader name for display
     */
    public String getUploaderName() {
        if (uploadedBy == null) return "Unknown";
        return uploadedBy.getFullName() != null ? uploadedBy.getFullName() : uploadedBy.getUsername();
    }

    /**
     * File type enumeration
     */
    public enum FileType {
        PDF("PDF Document", "pdf-icon"),
        WORD("Word Document", "word-icon"),
        POWERPOINT("PowerPoint Presentation", "powerpoint-icon"),
        UNKNOWN("Unknown File", "file-icon");

        private final String displayName;
        private final String iconClass;

        FileType(String displayName, String iconClass) {
            this.displayName = displayName;
            this.iconClass = iconClass;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getIconClass() {
            return iconClass;
        }
    }

    /**
     * Static utility methods
     */
    
    /**
     * Check if MIME type is allowed
     */
    public static boolean isAllowedMimeType(String mimeType) {
        return mimeType != null && ALLOWED_MIME_TYPES.contains(mimeType);
    }

    /**
     * Check if file size is within limits
     */
    public static boolean isValidFileSize(long fileSize) {
        return fileSize > 0 && fileSize <= MAX_FILE_SIZE;
    }

    /**
     * Generate secure file name
     */
    public static String generateSecureFileName(String originalFileName) {
        if (originalFileName == null) return "file_" + System.currentTimeMillis();
        
        String extension = "";
        int lastDotIndex = originalFileName.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFileName.substring(lastDotIndex);
        }
        
        return System.currentTimeMillis() + "_" + 
               originalFileName.replaceAll("[^a-zA-Z0-9._-]", "_") + extension;
    }

    @Override
    public String toString() {
        return String.format("Attachment{id=%d, fileName='%s', size=%s, type=%s}", 
            id, originalFileName, getFormattedFileSize(), getFileType());
    }
}
