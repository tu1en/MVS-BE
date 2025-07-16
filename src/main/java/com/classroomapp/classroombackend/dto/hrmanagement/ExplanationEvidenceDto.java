package com.classroomapp.classroombackend.dto.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.ExplanationEvidence;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for ExplanationEvidence entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationEvidenceDto {
    
    private Long id;
    
    private Long explanationId;
    
    private String originalFilename;
    private String filePath;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private String mimeType;
    
    private ExplanationEvidence.EvidenceType evidenceType;
    private String evidenceTypeDescription;
    
    private String description;
    
    private Boolean isVerified;
    private LocalDateTime verifiedAt;
    private Long verifiedBy;
    private String verifiedByName;
    
    private String uploadIp;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    private String formattedFileSize;
    private String fileExtension;
    private Boolean isImage;
    private Boolean isPdf;
    private Boolean isDocument;
    private String displayName;
    private String securityInfo;
    
    /**
     * Get evidence type description
     */
    public String getEvidenceTypeDescription() {
        return evidenceType != null ? evidenceType.getDescription() : null;
    }
    
    /**
     * Get formatted file size
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";
        
        if (fileSize < 1024) {
            return fileSize + " B";
        } else if (fileSize < 1024 * 1024) {
            return String.format("%.1f KB", fileSize / 1024.0);
        } else {
            return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        }
    }
    
    /**
     * Get file extension
     */
    public String getFileExtension() {
        if (originalFilename == null) return "";
        
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot > 0 && lastDot < originalFilename.length() - 1) {
            return originalFilename.substring(lastDot + 1).toLowerCase();
        }
        
        return "";
    }
    
    /**
     * Check if file is an image
     */
    public Boolean getIsImage() {
        if (mimeType == null) return false;
        return mimeType.startsWith("image/");
    }
    
    /**
     * Check if file is a PDF
     */
    public Boolean getIsPdf() {
        return "application/pdf".equals(mimeType);
    }
    
    /**
     * Check if file is a document
     */
    public Boolean getIsDocument() {
        if (mimeType == null) return false;
        return mimeType.startsWith("application/") || 
               mimeType.equals("text/plain") ||
               mimeType.startsWith("application/vnd.openxmlformats-officedocument");
    }
    
    /**
     * Get display name for the file
     */
    public String getDisplayName() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        return originalFilename;
    }
    
    /**
     * Get security info for the file
     */
    public String getSecurityInfo() {
        StringBuilder info = new StringBuilder();
        
        if (uploadIp != null) {
            info.append("IP: ").append(uploadIp);
        }
        
        if (createdAt != null) {
            if (info.length() > 0) info.append(" | ");
            info.append("Tải lên: ").append(createdAt.toLocalDate());
        }
        
        if (Boolean.TRUE.equals(isVerified)) {
            if (info.length() > 0) info.append(" | ");
            info.append("Đã xác minh");
        }
        
        return info.toString();
    }
    
    /**
     * Check if file is verified
     */
    public Boolean getIsVerified() {
        return Boolean.TRUE.equals(isVerified);
    }
    
    /**
     * Get file type icon for UI
     */
    public String getFileTypeIcon() {
        if (getIsImage()) {
            return "file-image";
        } else if (getIsPdf()) {
            return "file-pdf";
        } else if (getIsDocument()) {
            return "file-text";
        } else {
            return "file";
        }
    }
    
    /**
     * Get file type color for UI
     */
    public String getFileTypeColor() {
        if (getIsImage()) {
            return "green";
        } else if (getIsPdf()) {
            return "red";
        } else if (getIsDocument()) {
            return "blue";
        } else {
            return "default";
        }
    }
    
    /**
     * Check if file size is within limit
     */
    public Boolean isWithinSizeLimit(int maxSizeInMB) {
        if (fileSize == null) return false;
        long maxSizeInBytes = maxSizeInMB * 1024L * 1024L;
        return fileSize <= maxSizeInBytes;
    }
    
    /**
     * Get verification status text
     */
    public String getVerificationStatus() {
        if (Boolean.TRUE.equals(isVerified)) {
            return "Đã xác minh";
        } else {
            return "Chưa xác minh";
        }
    }
    
    /**
     * Get verification status color
     */
    public String getVerificationStatusColor() {
        if (Boolean.TRUE.equals(isVerified)) {
            return "green";
        } else {
            return "orange";
        }
    }
    
    /**
     * Get file summary for display
     */
    public String getFileSummary() {
        StringBuilder summary = new StringBuilder();
        
        summary.append(getDisplayName());
        
        if (fileSize != null) {
            summary.append(" (").append(getFormattedFileSize()).append(")");
        }
        
        if (evidenceType != null) {
            summary.append(" - ").append(evidenceType.getDescription());
        }
        
        return summary.toString();
    }
}
