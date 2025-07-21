package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity để quản lý documents được chia sẻ trong live sessions
 */
@Entity
@Table(name = "shared_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharedDocument {
    
    public enum DocumentType {
        PDF,
        DOC,
        DOCX,
        PPT,
        PPTX,
        XLS,
        XLSX,
        IMAGE,
        VIDEO,
        AUDIO,
        TEXT,
        OTHER
    }
    
    public enum DocumentStatus {
        UPLOADING,
        AVAILABLE,
        PROCESSING,
        ERROR,
        DELETED,
        EXPIRED
    }
    
    public enum AccessLevel {
        VIEW_ONLY,
        DOWNLOAD_ONLY,
        VIEW_AND_DOWNLOAD,
        EDIT_ALLOWED,
        FULL_ACCESS
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "live_stream_id", nullable = false)
    private LiveStream liveStream;
    
    @Column(name = "slot_id")
    private Long slotId;
    
    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;
    
    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;
    
    @Column(name = "original_file_name", length = 255)
    private String originalFileName;
    
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @Column(name = "file_url", length = 500)
    private String fileUrl;
    
    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;
    
    @Column(name = "preview_url", length = 500)
    private String previewUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private DocumentType documentType;
    
    @Column(name = "file_size", nullable = false)
    private Long fileSize;
    
    @Column(name = "mime_type", length = 100)
    private String mimeType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "document_status", nullable = false)
    @Builder.Default
    private DocumentStatus documentStatus = DocumentStatus.UPLOADING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", nullable = false)
    @Builder.Default
    private AccessLevel accessLevel = AccessLevel.VIEW_AND_DOWNLOAD;
    
    @Column(name = "description", columnDefinition = "NTEXT")
    private String description;
    
    @Column(name = "uploaded_at", nullable = false)
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "download_count")
    @Builder.Default
    private Integer downloadCount = 0;
    
    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;
    
    @Column(name = "is_pinned")
    @Builder.Default
    private Boolean isPinned = false;
    
    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = true; // True means all participants can access
    
    @Column(name = "password_protected")
    @Builder.Default
    private Boolean passwordProtected = false;
    
    @Column(name = "access_password", length = 255)
    private String accessPassword;
    
    @Column(name = "file_hash", length = 64)
    private String fileHash; // SHA-256 hash for integrity check
    
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;
    
    @Column(name = "parent_document_id")
    private Long parentDocumentId; // For document versions
    
    @Column(name = "current_page")
    @Builder.Default
    private Integer currentPage = 1; // For presentation sync
    
    @Column(name = "total_pages")
    private Integer totalPages;
    
    @Column(name = "presenter_id")
    private Long presenterId; // Who is currently presenting this document
    
    @Column(name = "is_currently_presenting")
    @Builder.Default
    private Boolean isCurrentlyPresenting = false;
    
    @Column(name = "auto_advance_slides")
    @Builder.Default
    private Boolean autoAdvanceSlides = false;
    
    @Column(name = "slide_duration_seconds")
    private Integer slideDurationSeconds;
    
    /**
     * Check if document can be presented (PDF, PPT, PPTX)
     */
    public boolean canBePresented() {
        String extension = getFileExtension();
        return "pdf".equals(extension) || "ppt".equals(extension) || "pptx".equals(extension);
    }
    
    /**
     * Check if document is accessible
     */
    public boolean isAccessible() {
        if (documentStatus != DocumentStatus.AVAILABLE) {
            return false;
        }
        
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if user can access this document
     */
    public boolean canBeAccessedBy(Long userId, String userRole) {
        if (!isAccessible()) {
            return false;
        }
        
        // Uploader can always access
        if (uploadedBy.getId().equals(userId)) {
            return true;
        }
        
        // Teachers can access all documents
        if ("TEACHER".equals(userRole)) {
            return true;
        }
        
        // Check if document is public
        return isPublic;
    }
    
    /**
     * Check if user can edit this document
     */
    public boolean canBeEditedBy(Long userId, String userRole) {
        if (!isAccessible()) {
            return false;
        }
        
        // Only uploader or teacher can edit
        if (uploadedBy.getId().equals(userId) || "TEACHER".equals(userRole)) {
            return accessLevel == AccessLevel.EDIT_ALLOWED || accessLevel == AccessLevel.FULL_ACCESS;
        }
        
        return false;
    }
    
    /**
     * Check if user can download this document
     */
    public boolean canBeDownloadedBy(Long userId, String userRole) {
        if (!canBeAccessedBy(userId, userRole)) {
            return false;
        }
        
        return accessLevel == AccessLevel.DOWNLOAD_ONLY || 
               accessLevel == AccessLevel.VIEW_AND_DOWNLOAD ||
               accessLevel == AccessLevel.FULL_ACCESS;
    }
    
    /**
     * Increment view count
     */
    public void incrementViewCount() {
        this.viewCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Increment download count
     */
    public void incrementDownloadCount() {
        this.downloadCount++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Start presentation
     */
    public void startPresentation(Long presenterId) {
        this.presenterId = presenterId;
        this.isCurrentlyPresenting = true;
        this.currentPage = 1;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Stop presentation
     */
    public void stopPresentation() {
        this.presenterId = null;
        this.isCurrentlyPresenting = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Change slide/page
     */
    public void changePage(int newPage) {
        if (newPage > 0 && (totalPages == null || newPage <= totalPages)) {
            this.currentPage = newPage;
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    /**
     * Get file extension
     */
    public String getFileExtension() {
        if (originalFileName != null && originalFileName.contains(".")) {
            return originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }
    
    /**
     * Get human readable file size
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
    
    /**
     * Create document from file info
     */
    public static SharedDocument createFromFile(LiveStream liveStream, User uploader, 
                                              String fileName, String filePath, long fileSize, String mimeType) {
        DocumentType docType = determineDocumentType(fileName, mimeType);
        
        return SharedDocument.builder()
                .liveStream(liveStream)
                .uploadedBy(uploader)
                .fileName(fileName)
                .originalFileName(fileName)
                .filePath(filePath)
                .documentType(docType)
                .fileSize(fileSize)
                .mimeType(mimeType)
                .documentStatus(DocumentStatus.AVAILABLE)
                .accessLevel(AccessLevel.VIEW_AND_DOWNLOAD)
                .isPublic(true)
                .build();
    }
    
    /**
     * Determine document type from file name and MIME type
     */
    private static DocumentType determineDocumentType(String fileName, String mimeType) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        
        switch (extension) {
            case "pdf":
                return DocumentType.PDF;
            case "doc":
                return DocumentType.DOC;
            case "docx":
                return DocumentType.DOCX;
            case "ppt":
                return DocumentType.PPT;
            case "pptx":
                return DocumentType.PPTX;
            case "xls":
                return DocumentType.XLS;
            case "xlsx":
                return DocumentType.XLSX;
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
                return DocumentType.IMAGE;
            case "mp4":
            case "avi":
            case "mov":
            case "wmv":
                return DocumentType.VIDEO;
            case "mp3":
            case "wav":
            case "flac":
                return DocumentType.AUDIO;
            case "txt":
            case "rtf":
                return DocumentType.TEXT;
            default:
                return DocumentType.OTHER;
        }
    }
    
    /**
     * Convert to JSON for real-time sync
     */
    public String toJson() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":").append(id).append(",");
        json.append("\"fileName\":\"").append(fileName).append("\",");
        json.append("\"fileUrl\":\"").append(fileUrl != null ? fileUrl : "").append("\",");
        json.append("\"thumbnailUrl\":\"").append(thumbnailUrl != null ? thumbnailUrl : "").append("\",");
        json.append("\"documentType\":\"").append(documentType).append("\",");
        json.append("\"fileSize\":").append(fileSize).append(",");
        json.append("\"formattedSize\":\"").append(getFormattedFileSize()).append("\",");
        json.append("\"uploadedBy\":\"").append(uploadedBy.getFullName()).append("\",");
        json.append("\"uploadedAt\":\"").append(uploadedAt).append("\",");
        json.append("\"isCurrentlyPresenting\":").append(isCurrentlyPresenting).append(",");
        json.append("\"currentPage\":").append(currentPage).append(",");
        json.append("\"totalPages\":").append(totalPages != null ? totalPages : 0).append(",");
        json.append("\"viewCount\":").append(viewCount).append(",");
        json.append("\"downloadCount\":").append(downloadCount).append(",");
        json.append("\"isPinned\":").append(isPinned).append(",");
        json.append("\"accessLevel\":\"").append(accessLevel).append("\"");
        json.append("}");
        
        return json.toString();
    }
}