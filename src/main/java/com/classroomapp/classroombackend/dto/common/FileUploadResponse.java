package com.classroomapp.classroombackend.dto.common;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO hợp nhất cho kết quả upload file
 * Kết hợp functionality từ FileUploadResponse và FileUploadResult
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

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
     * Tên file đã được sanitize (alias cho fileName để tương thích)
     */
    private String filename;

    /**
     * Alias cho filename để tương thích với version cũ
     */
    public String getFileName() {
        return filename;
    }

    public void setFileName(String fileName) {
        this.filename = fileName;
    }

    /**
     * Đường dẫn relative đến file
     */
    private String filePath;

    /**
     * URL public để access file (alias cho fileUrl để tương thích)
     */
    private String fileUrl;

    /**
     * Kích thước file (bytes)
     */
    private Long fileSize;

    /**
     * Alias cho fileSize để tương thích với version cũ
     */
    public long getSize() {
        return fileSize != null ? fileSize : 0L;
    }

    public void setSize(long size) {
        this.fileSize = size;
    }

    /**
     * MIME type của file (alias cho fileType để tương thích)
     */
    private String mimeType;

    /**
     * Alias cho mimeType để tương thích với version cũ
     */
    public String getFileType() {
        return mimeType;
    }

    public void setFileType(String fileType) {
        this.mimeType = fileType;
    }

    /**
     * Thời gian upload
     */
    private LocalDateTime uploadedAt;

    /**
     * Thông tin lỗi nếu có
     */
    private String error;

    /**
     * Danh mục file
     */
    private String category;

    /**
     * Thông tin bảo mật
     */
    private SecurityInfo securityInfo;

    /**
     * Thông tin quét virus
     */
    private VirusScanInfo virusScanInfo;

    /**
     * Metadata của file
     */
    private FileMetadata metadata;

    /**
     * Danh sách thumbnails (cho image files)
     */
    private List<String> thumbnails;

    /**
     * Nested classes for complex data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SecurityInfo {
        private boolean passed;
        private String threatLevel;
        private List<String> warnings;
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
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileMetadata {
        private ImageMetadata imageMetadata;
        private DocumentMetadata documentMetadata;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageMetadata {
        private int width;
        private int height;
        private String format;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentMetadata {
        private int pageCount;
        private String author;
        private String title;
    }

    /**
     * Kiểm tra xem file có phải là image không
     */
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * Kiểm tra xem file có phải là document không
     */
    public boolean isDocument() {
        return mimeType != null && (
            mimeType.equals("application/pdf") ||
            mimeType.startsWith("application/vnd.openxmlformats-officedocument") ||
            mimeType.startsWith("application/msword")
        );
    }

    /**
     * Lấy URL của thumbnail với kích thước cụ thể
     */
    public String getThumbnailUrl(String size) {
        if (thumbnails == null || thumbnails.isEmpty()) {
            return null;
        }

        for (String thumbnail : thumbnails) {
            if (thumbnail.contains("_" + size + ".")) {
                return "/uploads/" + thumbnail;
            }
        }

        return "/uploads/" + thumbnails.get(0);
    }

    /**
     * Create success result
     */
    public static FileUploadResponse success(Long fileId, String originalFilename, String filename,
                                             String filePath, Long fileSize, String mimeType, String category) {
        return FileUploadResponse.builder()
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
     * Create simple success result (tương thích với version cũ)
     */
    public static FileUploadResponse success(String fileName, String fileUrl, String fileType, long size) {
        return FileUploadResponse.builder()
            .success(true)
            .filename(fileName)
            .fileUrl(fileUrl)
            .mimeType(fileType)
            .fileSize(size)
            .uploadedAt(LocalDateTime.now())
            .build();
    }

    /**
     * Create error result
     */
    public static FileUploadResponse error(String originalFilename, String error) {
        return FileUploadResponse.builder()
            .success(false)
            .originalFilename(originalFilename)
            .error(error)
            .build();
    }

    /**
     * Add security info
     */
    public FileUploadResponse withSecurityInfo(boolean passed, String threatLevel, List<String> warnings) {
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
    public FileUploadResponse withVirusScanInfo(boolean scanned, boolean clean, String scanMethod, String virusName) {
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
    public FileUploadResponse withImageMetadata(int width, int height, String format) {
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

    // ✅ Constructor tương thích với code cũ
    public FileUploadResponse(String fileName, String fileUrl, String fileType, long size) {
        this.filename = fileName;
        this.fileUrl = fileUrl;
        this.mimeType = fileType;
        this.fileSize = size;
        this.success = true;
        this.uploadedAt = LocalDateTime.now();
    }

    // ✅ Custom builder methods để hỗ trợ code cũ
    public static class FileUploadResponseBuilder {
        public FileUploadResponseBuilder fileType(String fileType) {
            this.mimeType = fileType;
            return this;
        }

        public FileUploadResponseBuilder size(long size) {
            this.fileSize = size;
            return this;
        }
    }
}
