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
 * Lưu trữ thông tin về files đã upload
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
     * Tên file gốc do user upload
     */
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    /**
     * Tên file đã được sanitize và unique
     */
    @Column(name = "filename", nullable = false)
    private String filename;

    /**
     * Đường dẫn đầy đủ đến file trên server
     */
    @Column(name = "file_path", nullable = false)
    private String filePath;

    /**
     * Kích thước file (bytes)
     */
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    /**
     * MIME type của file
     */
    @Column(name = "mime_type")
    private String mimeType;

    /**
     * Danh mục file (image, document, video, etc.)
     */
    @Column(name = "category", nullable = false)
    private String category;

    /**
     * Mô tả file
     */
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    /**
     * Tags cho file (JSON array)
     */
    @Column(name = "tags", columnDefinition = "NVARCHAR(MAX)")
    private String tags;

    /**
     * Danh sách thumbnails (JSON array)
     */
    @ElementCollection
    @CollectionTable(name = "file_thumbnails", joinColumns = @JoinColumn(name = "file_id"))
    @Column(name = "thumbnail_path")
    private List<String> thumbnails;

    /**
     * Metadata bổ sung (JSON)
     */
    @Column(name = "metadata", columnDefinition = "NVARCHAR(MAX)")
    private String metadata;

    /**
     * Người upload
     */
    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    /**
     * Thời gian upload
     */
    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    /**
     * Thời gian cập nhật cuối
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Có bị xóa không (soft delete)
     */
    @Column(name = "deleted", nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    /**
     * Thời gian xóa
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Người xóa
     */
    @Column(name = "deleted_by")
    private String deletedBy;

    /**
     * Lý do xóa
     */
    @Column(name = "deletion_reason", columnDefinition = "NVARCHAR(MAX)")
    private String deletionReason;

    /**
     * Số lần download
     */
    @Column(name = "download_count")
    @Builder.Default
    private Long downloadCount = 0L;

    /**
     * Lần download cuối
     */
    @Column(name = "last_downloaded_at")
    private LocalDateTime lastDownloadedAt;

    /**
     * Người download cuối
     */
    @Column(name = "last_downloaded_by")
    private String lastDownloadedBy;

    /**
     * File có public không
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
     * Thời gian hết hạn access token
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
     * Thời gian scan virus
     */
    @Column(name = "virus_scanned_at")
    private LocalDateTime virusScannedAt;

    /**
     * File có bị quarantine không
     */
    @Column(name = "quarantined")
    @Builder.Default
    private Boolean quarantined = false;

    /**
     * Lý do quarantine
     */
    @Column(name = "quarantine_reason", columnDefinition = "NVARCHAR(MAX)")
    private String quarantineReason;

    /**
     * Checksum của file (để detect duplicates)
     */
    @Column(name = "checksum")
    private String checksum;

    /**
     * Version của file (cho versioning)
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
        PENDING,    // Chưa scan
        SCANNING,   // Đang scan
        CLEAN,      // Sạch
        INFECTED,   // Nhiễm virus
        ERROR       // Lỗi khi scan
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
