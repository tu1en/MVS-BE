package com.classroomapp.classroombackend.dto.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho kết quả upload file
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResult {

    /**
     * Upload có thành công không
     */
    private boolean success;

    /**
     * ID của file trong database
     */
    private Long fileId;

    /**
     * Tên file gốc
     */
    private String originalFilename;

    /**
     * Tên file đã được sanitize
     */
    private String filename;

    /**
     * Đường dẫn relative đến file
     */
    private String filePath;

    /**
     * URL public để access file
     */
    private String fileUrl;

    /**
     * Kích thước file (bytes)
     */
    private Long fileSize;

    /**
     * MIME type
     */
    private String mimeType;

    /**
     * Category của file
     */
    private String category;

    /**
     * Danh sách thumbnails (nếu là image)
     */
    private List<String> thumbnails;

    /**
     * Thời gian upload
     */
    private LocalDateTime uploadedAt;

    /**
     * Thông báo lỗi (nếu có)
     */
    private String error;

    /**
     * Thông tin bảo mật
     */
    private SecurityInfo securityInfo;

    /**
     * Thông tin virus scan
     */
    private VirusScanInfo virusScanInfo;

    /**
     * Metadata bổ sung
     */
    private FileMetadata metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityInfo {
        private boolean passed;
        private String threatLevel;
        private List<String> warnings;
        private boolean quarantined;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VirusScanInfo {
        private boolean scanned;
        private boolean clean;
        private String scanMethod;
        private String virusName;
        private LocalDateTime scanTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileMetadata {
        private ImageMetadata imageMetadata;
        private DocumentMetadata documentMetadata;
        private VideoMetadata videoMetadata;
        private AudioMetadata audioMetadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageMetadata {
        private int width;
        private int height;
        private String format;
        private boolean hasExif;
        private String colorSpace;
        private int bitDepth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentMetadata {
        private int pageCount;
        private String author;
        private String title;
        private String subject;
        private LocalDateTime createdDate;
        private LocalDateTime modifiedDate;
        private String application;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoMetadata {
        private int width;
        private int height;
        private long duration; // in seconds
        private String codec;
        private double frameRate;
        private long bitrate;
        private String format;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AudioMetadata {
        private long duration; // in seconds
        private String codec;
        private int sampleRate;
        private int bitrate;
        private int channels;
        private String format;
        private String artist;
        private String album;
        private String title;
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
     * Get thumbnail URL for specific size
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
     * Create success result
     */
    public static FileUploadResult success(Long fileId, String originalFilename, String filename, 
                                         String filePath, Long fileSize, String mimeType, String category) {
        return FileUploadResult.builder()
            .success(true)
            .fileId(fileId)
            .originalFilename(originalFilename)
            .filename(filename)
            .filePath(filePath)
            .fileSize(fileSize)
            .mimeType(mimeType)
            .category(category)
            .uploadedAt(LocalDateTime.now())
            .build();
    }

    /**
     * Create error result
     */
    public static FileUploadResult error(String originalFilename, String error) {
        return FileUploadResult.builder()
            .success(false)
            .originalFilename(originalFilename)
            .error(error)
            .build();
    }

    /**
     * Add security info
     */
    public FileUploadResult withSecurityInfo(boolean passed, String threatLevel, List<String> warnings) {
        this.securityInfo = SecurityInfo.builder()
            .passed(passed)
            .threatLevel(threatLevel)
            .warnings(warnings)
            .build();
        return this;
    }

    /**
     * Add virus scan info
     */
    public FileUploadResult withVirusScanInfo(boolean scanned, boolean clean, String scanMethod, String virusName) {
        this.virusScanInfo = VirusScanInfo.builder()
            .scanned(scanned)
            .clean(clean)
            .scanMethod(scanMethod)
            .virusName(virusName)
            .scanTime(LocalDateTime.now())
            .build();
        return this;
    }

    /**
     * Add image metadata
     */
    public FileUploadResult withImageMetadata(int width, int height, String format) {
        if (this.metadata == null) {
            this.metadata = new FileMetadata();
        }
        this.metadata.setImageMetadata(ImageMetadata.builder()
            .width(width)
            .height(height)
            .format(format)
            .build());
        return this;
    }
}
