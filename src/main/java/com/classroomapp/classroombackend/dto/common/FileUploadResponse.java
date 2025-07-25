package com.classroomapp.classroombackend.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO há»£p nháº¥t cho káº¿t quáº£ upload file
 * Káº¿t há»£p functionality tá»« FileUploadResponse vÃ  FileUploadResult
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {

    /**
     * Upload cÃ³ thÃ nh cÃ´ng khÃ´ng
     */
    private boolean success;

    /**
     * ID cá»§a file trong database
     */
    private Long fileId;

    /**
     * TÃªn file gá»‘c
     */
    private String originalFilename;

    /**
     * TÃªn file Ä‘Ã£ Ä‘Æ°á»£c sanitize (alias cho fileName Ä‘á»ƒ tÆ°Æ¡ng thÃ­ch)
     */
    private String filename;
    
    /**
     * Alias cho filename Ä‘á»ƒ tÆ°Æ¡ng thÃ­ch vá»›i version cÅ©
     */
    public String getFileName() {
        return filename;
    }
    
    public void setFileName(String fileName) {
        this.filename = fileName;
    }

    /**
     * ÄÆ°á»ng dáº«n relative Ä‘áº¿n file
     */
    private String filePath;

    /**
     * URL public Ä‘á»ƒ access file (alias cho fileUrl Ä‘á»ƒ tÆ°Æ¡ng thÃ­ch)
     */
    private String fileUrl;

    /**
     * KÃ­ch thÆ°á»›c file (bytes)
     */
    private Long fileSize;
    
    /**
     * Alias cho fileSize Ä‘á»ƒ tÆ°Æ¡ng thÃ­ch vá»›i version cÅ©
     */
    public long getSize() {
        return fileSize != null ? fileSize : 0L;
    }
    
    public void setSize(long size) {
        this.fileSize = size;
    }

    /**
     * MIME type cá»§a file (alias cho fileType Ä‘á»ƒ tÆ°Æ¡ng thÃ­ch)
     */
    private String mimeType;
    
    /**
     * Alias cho mimeType Ä‘á»ƒ tÆ°Æ¡ng thÃ­ch vá»›i version cÅ©
     */
    public String getFileType() {
        return mimeType;
    }
    
    public void setFileType(String fileType) {
        this.mimeType = fileType;
    }

    /**
     * Thá»i gian upload
     */
    private LocalDateTime uploadedAt;

    /**
     * ThÃ´ng tin lá»—i náº¿u cÃ³
     */
    private String error;

    /**
     * Danh má»¥c file
     */
    private String category;

    /**
     * ThÃ´ng tin báº£o máº­t
     */
    private SecurityInfo securityInfo;

    /**
     * ThÃ´ng tin quÃ©t virus
     */
    private VirusScanInfo virusScanInfo;

    /**
     * Metadata cá»§a file
     */
    private FileMetadata metadata;

    /**
     * Danh sÃ¡ch thumbnails (cho image files)
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
     * Kiá»ƒm tra xem file cÃ³ pháº£i lÃ  image khÃ´ng
     */
    public boolean isImage() {
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * Kiá»ƒm tra xem file cÃ³ pháº£i lÃ  document khÃ´ng
     */
    public boolean isDocument() {
        return mimeType != null && (
            mimeType.equals("application/pdf") ||
            mimeType.startsWith("application/vnd.openxmlformats-officedocument") ||
            mimeType.startsWith("application/msword")
        );
    }

    /**
     * Láº¥y URL cá»§a thumbnail vá»›i kÃ­ch thÆ°á»›c cá»¥ thá»ƒ
     */
    public String getThumbnailUrl(String size) {
        if (thumbnails == null || thumbnails.isEmpty()) {
            return null;
        }
        
        // TÃ¬m thumbnail vá»›i size phÃ¹ há»£p
        for (String thumbnail : thumbnails) {
            if (thumbnail.contains("_" + size + ".")) {
                return "/uploads/" + thumbnail;
            }
        }
        
        // Return first thumbnail if specific size not found
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
     * Create simple success result (tÆ°Æ¡ng thÃ­ch vá»›i version cÅ©)
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
}
