package com.classroomapp.classroombackend.model.hrmanagement;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity representing evidence files uploaded with violation explanations
 */
@Entity
@Table(name = "explanation_evidence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExplanationEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Giải trình không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "explanation_id", nullable = false)
    private ViolationExplanation explanation;

    @NotBlank(message = "Tên file không được để trống")
    @Column(name = "original_filename", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String originalFilename;

    @NotBlank(message = "Đường dẫn file không được để trống")
    @Column(name = "file_path", columnDefinition = "NVARCHAR(500)", nullable = false)
    private String filePath;

    @NotBlank(message = "URL file không được để trống")
    @Column(name = "file_url", columnDefinition = "NVARCHAR(1000)", nullable = false)
    private String fileUrl;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type", length = 100)
    private String fileType;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false)
    private EvidenceType evidenceType = EvidenceType.DOCUMENT;

    @Column(name = "description", columnDefinition = "NVARCHAR(500)")
    private String description;

    @Column(name = "is_verified", columnDefinition = "BIT DEFAULT 0")
    private Boolean isVerified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private Long verifiedBy;

    @Column(name = "upload_ip", length = 45)
    private String uploadIp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Enum for evidence types
     */
    public enum EvidenceType {
        DOCUMENT("Tài liệu"),
        IMAGE("Hình ảnh"),
        MEDICAL_CERTIFICATE("Giấy khám bệnh"),
        OFFICIAL_LETTER("Công văn"),
        RECEIPT("Hóa đơn/Biên lai"),
        OTHER("Khác");

        private final String description;

        EvidenceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Check if file is an image
     * @return true if image file
     */
    public boolean isImage() {
        if (mimeType == null) return false;
        return mimeType.startsWith("image/");
    }

    /**
     * Check if file is a PDF
     * @return true if PDF file
     */
    public boolean isPdf() {
        return "application/pdf".equals(mimeType);
    }

    /**
     * Check if file is a document
     * @return true if document file
     */
    public boolean isDocument() {
        if (mimeType == null) return false;
        return mimeType.startsWith("application/") || 
               mimeType.equals("text/plain") ||
               mimeType.startsWith("application/vnd.openxmlformats-officedocument");
    }

    /**
     * Get file size in human readable format
     * @return formatted file size
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
     * Get file extension from filename
     * @return file extension or empty string
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
     * Check if file type is allowed
     * @return true if allowed file type
     */
    public boolean isAllowedFileType() {
        String extension = getFileExtension();
        
        // Allowed extensions
        String[] allowedExtensions = {
            "jpg", "jpeg", "png", "gif", "bmp",  // Images
            "pdf",                                // PDF
            "doc", "docx",                       // Word documents
            "xls", "xlsx",                       // Excel documents
            "txt"                                // Text files
        };
        
        for (String allowed : allowedExtensions) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Verify the evidence file
     * @param verifiedBy user who verified
     */
    public void verify(Long verifiedBy) {
        this.isVerified = true;
        this.verifiedAt = LocalDateTime.now();
        this.verifiedBy = verifiedBy;
    }

    /**
     * Check if evidence is verified
     * @return true if verified
     */
    public boolean isVerified() {
        return Boolean.TRUE.equals(isVerified);
    }

    /**
     * Get display name for the file
     * @return display name
     */
    public String getDisplayName() {
        if (description != null && !description.trim().isEmpty()) {
            return description;
        }
        return originalFilename;
    }

    /**
     * Check if file size is within limit
     * @param maxSizeInMB maximum size in MB
     * @return true if within limit
     */
    public boolean isWithinSizeLimit(int maxSizeInMB) {
        if (fileSize == null) return false;
        long maxSizeInBytes = maxSizeInMB * 1024L * 1024L;
        return fileSize <= maxSizeInBytes;
    }

    /**
     * Get security info for the file
     * @return security info string
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
        
        if (isVerified()) {
            if (info.length() > 0) info.append(" | ");
            info.append("Đã xác minh");
        }
        
        return info.toString();
    }
}
