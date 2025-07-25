package com.classroomapp.classroombackend.model.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * UploadedFile Entity
 * LÆ°u trá»¯ thÃ´ng tin vá» files Ä‘Ã£ upload
 */
@Entity
@Table(name = "uploaded_files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UploadedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * TÃªn file gá»‘c do user upload
     */
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    /**
     * TÃªn file Ä‘Ã£ Ä‘Æ°á»£c sanitize vÃ  unique
     */
    @Column(name = "filename", nullable = false)
    private String filename;

    /**
     * ÄÆ°á»ng dáº«n Ä‘áº§y Ä‘á»§ Ä‘áº¿n file trÃªn server
     */
    @Column(name = "file_path", nullable = false)
    private String filePath;

    /**
     * KÃ­ch thÆ°á»›c file (bytes)
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * MIME type cá»§a file
     */
    @Column(name = "mime_type")
    private String mimeType;

    /**
     * Danh má»¥c file (image, document, video, etc.)
     */
    @Column(name = "category", nullable = false)
    private String category;

    /**
     * MÃ´ táº£ file
     */
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    /**
     * Tags cho file (JSON array)
     */
    @Column(name = "tags", columnDefinition = "NVARCHAR(MAX)")
    private String tags;

    /**
     * Danh sÃ¡ch thumbnails (JSON array)
     */
    @ElementCollection
    @CollectionTable(name = "file_thumbnails", joinColumns = @JoinColumn(name = "file_id"))
    @Column(name = "thumbnail_path")
    private List<String> thumbnails;

    /**
     * Metadata bá»• sung (JSON)
     */
    @Column(name = "metadata", columnDefinition = "NVARCHAR(MAX)")
    private String metadata;

    /**
     * NgÆ°á»i upload
     */
    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    /**
     * Thá»i gian upload
     */
    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    /**
     * Thá»i gian cáº­p nháº­t cuá»‘i
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * CÃ³ bá»‹ xÃ³a khÃ´ng (soft delete)
     */
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * Thá»i gian xÃ³a
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * NgÆ°á»i xÃ³a
     */
    @Column(name = "deleted_by")
    private String deletedBy;

    /**
     * LÃ½ do xÃ³a
     */
    @Column(name = "deletion_reason", columnDefinition = "NVARCHAR(MAX)")
    private String deletionReason;

    /**
     * Sá»‘ láº§n download
     */
    @Column(name = "download_count")
    @Builder.Default
    private Long downloadCount = 0L;

    /**
     * Láº§n download cuá»‘i
     */
    @Column(name = "last_downloaded_at")
    private LocalDateTime lastDownloadedAt;

    /**
     * NgÆ°á»i download cuá»‘i
     */
    @Column(name = "last_downloaded_by")
    private String lastDownloadedBy;

    /**
     * File cÃ³ public khÃ´ng
     */
    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = false;

    /**
     * Access token cho file private
     */
    @Column(name = "access_token")
    @JsonIgnore
    private String accessToken;

    /**
     * Thá»i gian háº¿t háº¡n access token
     */
    @Column(name = "access_token_expires_at")
    private LocalDateTime accessTokenExpiresAt;

    /**
     * Virus scan status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "virus_scan_status")
    @Builder.Default
    private VirusScanStatus virusScanStatus = VirusScanStatus.PENDING;

    /**
     * Virus scan result
     */
    @Column(name = "virus_scan_result", columnDefinition = "NVARCHAR(MAX)")
    private String virusScanResult;

    /**
     * Thá»i gian scan virus
     */
    @Column(name = "virus_scanned_at")
    private LocalDateTime virusScannedAt;

    /**
     * File cÃ³ bá»‹ quarantine khÃ´ng
     */
    @Column(name = "quarantined")
    @Builder.Default
    private Boolean quarantined = false;

    /**
     * LÃ½ do quarantine
     */
    @Column(name = "quarantine_reason", columnDefinition = "NVARCHAR(MAX)")
    private String quarantineReason;

    /**
     * Checksum cá»§a file (Ä‘á»ƒ detect duplicates)
     */
    @Column(name = "checksum")
    private String checksum;

    /**
     * Version cá»§a file (cho versioning)
     */
    @Column(name = "version")
    @Builder.Default
    private Integer version = 1;

    /**
     * Parent file ID (cho versioning)
     */
    @Column(name = "parent_file_id")
    private Long parentFileId;

    /**
     * Enum cho virus scan status
     */
    public enum VirusScanStatus {
        PENDING,    // ChÆ°a scan
        SCANNING,   // Äang scan
        CLEAN,      // Sáº¡ch
        INFECTED,   // Nhiá»…m virus
        ERROR       // Lá»—i khi scan
    }

    /**
     * Get file URL for public access
     */
    public String getPublicUrl() {
        if (deleted || quarantined) {
            return null;
        }
        return "/uploads/" + category + "/" + filename;
    }

    /**
     * Get thumbnail URL
     */
    public String getThumbnailUrl(String size) {
        if (thumbnails == null || thumbnails.isEmpty()) {
            return null;
        }
        
        for (String thumbnail : thumbnails) {
            if (thumbnail.contains(size)) {
                return "/uploads/" + thumbnail;
            }
        }
        
        // Return first thumbnail if specific size not found
        return "/uploads/" + thumbnails.get(0);
    }

    /**
     * Check if file is image
     */
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * Check if file is document
     */
    public boolean isDocument() {
        return mimeType != null && (
            mimeType.startsWith("application/") || 
            mimeType.equals("text/plain")
        );
    }

    /**
     * Check if file is video
     */
    public boolean isVideo() {
        return mimeType != null && mimeType.startsWith("video/");
    }

    /**
     * Check if file is audio
     */
    public boolean isAudio() {
        return mimeType != null && mimeType.startsWith("audio/");
    }

    /**
     * Get human readable file size
     */
    public String getHumanReadableSize() {
        if (fileSize == null) {
            return "0 B";
        }
        
        long size = fileSize;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", (double) size, units[unitIndex]);
    }

    /**
     * Check if access token is valid
     */
    public boolean isAccessTokenValid() {
        return accessToken != null && 
               accessTokenExpiresAt != null && 
               accessTokenExpiresAt.isAfter(LocalDateTime.now());
    }

    /**
     * Increment download count
     */
    public void incrementDownloadCount(String downloadedBy) {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
        this.lastDownloadedAt = LocalDateTime.now();
        this.lastDownloadedBy = downloadedBy;
    }

    /**
     * Mark as deleted (soft delete)
     */
    public void markAsDeleted(String deletedBy, String reason) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
        this.deletionReason = reason;
    }

    /**
     * Mark as quarantined
     */
    public void quarantine(String reason) {
        this.quarantined = true;
        this.quarantineReason = reason;
        this.virusScanStatus = VirusScanStatus.INFECTED;
    }

    /**
     * Update virus scan result
     */
    public void updateVirusScanResult(VirusScanStatus status, String result) {
        this.virusScanStatus = status;
        this.virusScanResult = result;
        this.virusScannedAt = LocalDateTime.now();
    }
}
