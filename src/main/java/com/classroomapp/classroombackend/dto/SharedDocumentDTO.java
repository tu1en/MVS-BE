package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.SharedDocument;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho SharedDocument
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedDocumentDTO {
    
    private Long id;
    private Long liveStreamId;
    private Long slotId;
    private String uploadedByName;
    private String fileName;
    private String originalFileName;
    private String filePath;
    private String fileUrl;
    private String thumbnailUrl;
    private String previewUrl;
    private String documentType;
    private Long fileSize;
    private String mimeType;
    private String documentStatus;
    private String accessLevel;
    private String description;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    private Integer downloadCount;
    private Integer viewCount;
    private Boolean isPinned;
    private Boolean isPublic;
    private Boolean passwordProtected;
    private String fileHash;
    private Integer version;
    private Long parentDocumentId;
    private Integer currentPage;
    private Integer totalPages;
    private Long presenterId;
    private String presenterName;
    private Boolean isCurrentlyPresenting;
    private Boolean autoAdvanceSlides;
    private Integer slideDurationSeconds;
    
    /**
     * Convert entity to DTO
     */
    public static SharedDocumentDTO fromEntity(SharedDocument entity) {
        if (entity == null) return null;
        
        SharedDocumentDTO dto = new SharedDocumentDTO();
        dto.setId(entity.getId());
        dto.setLiveStreamId(entity.getLiveStream() != null ? entity.getLiveStream().getId() : null);
        dto.setSlotId(entity.getSlotId());
        dto.setUploadedByName(entity.getUploadedBy() != null ? entity.getUploadedBy().getFullName() : null);
        dto.setFileName(entity.getFileName());
        dto.setOriginalFileName(entity.getOriginalFileName());
        dto.setFilePath(entity.getFilePath());
        dto.setFileUrl(entity.getFileUrl());
        dto.setThumbnailUrl(entity.getThumbnailUrl());
        dto.setPreviewUrl(entity.getPreviewUrl());
        dto.setDocumentType(entity.getDocumentType() != null ? entity.getDocumentType().name() : null);
        dto.setFileSize(entity.getFileSize());
        dto.setMimeType(entity.getMimeType());
        dto.setDocumentStatus(entity.getDocumentStatus() != null ? entity.getDocumentStatus().name() : null);
        dto.setAccessLevel(entity.getAccessLevel() != null ? entity.getAccessLevel().name() : null);
        dto.setDescription(entity.getDescription());
        dto.setUploadedAt(entity.getUploadedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setExpiresAt(entity.getExpiresAt());
        dto.setDownloadCount(entity.getDownloadCount());
        dto.setViewCount(entity.getViewCount());
        dto.setIsPinned(entity.getIsPinned());
        dto.setIsPublic(entity.getIsPublic());
        dto.setPasswordProtected(entity.getPasswordProtected());
        dto.setFileHash(entity.getFileHash());
        dto.setVersion(entity.getVersion());
        dto.setParentDocumentId(entity.getParentDocumentId());
        dto.setCurrentPage(entity.getCurrentPage());
        dto.setTotalPages(entity.getTotalPages());
        dto.setPresenterId(entity.getPresenterId());
        dto.setIsCurrentlyPresenting(entity.getIsCurrentlyPresenting());
        dto.setAutoAdvanceSlides(entity.getAutoAdvanceSlides());
        dto.setSlideDurationSeconds(entity.getSlideDurationSeconds());
        
        return dto;
    }
    
    /**
     * Convert DTO to entity (for updates) - chỉ update các field có thể thay đổi
     */
    public void updateEntity(SharedDocument entity) {
        if (entity == null) return;
        
        if (this.description != null) entity.setDescription(this.description);
        if (this.accessLevel != null) {
            entity.setAccessLevel(SharedDocument.AccessLevel.valueOf(this.accessLevel));
        }
        if (this.expiresAt != null) entity.setExpiresAt(this.expiresAt);
        if (this.isPinned != null) entity.setIsPinned(this.isPinned);
        if (this.isPublic != null) entity.setIsPublic(this.isPublic);
        if (this.passwordProtected != null) entity.setPasswordProtected(this.passwordProtected);
        if (this.autoAdvanceSlides != null) entity.setAutoAdvanceSlides(this.autoAdvanceSlides);
        if (this.slideDurationSeconds != null) entity.setSlideDurationSeconds(this.slideDurationSeconds);
        
        entity.setUpdatedAt(LocalDateTime.now());
    }
    
    /**
     * Get file size in human readable format
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "Unknown";
        
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        if (fileSize < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * Get file extension
     */
    public String getFileExtension() {
        if (originalFileName == null || !originalFileName.contains(".")) {
            return "";
        }
        return originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();
    }
    
    /**
     * Check if file is an image
     */
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }
    
    /**
     * Check if file is a PDF
     */
    public boolean isPdf() {
        return "application/pdf".equals(mimeType);
    }
    
    /**
     * Check if document can be presented (PDF, PPT, PPTX)
     */
    public boolean canBePresented() {
        String ext = getFileExtension();
        return "pdf".equals(ext) || "ppt".equals(ext) || "pptx".equals(ext);
    }
    
    /**
     * Check if document is accessible
     */
    public boolean isAccessible() {
        if (!"AVAILABLE".equals(documentStatus)) {
            return false;
        }
        
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now())) {
            return false;
        }
        
        return true;
    }
}