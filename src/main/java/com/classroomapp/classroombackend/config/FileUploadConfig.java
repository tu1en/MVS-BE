package com.classroomapp.classroombackend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Map;

/**
 * File Upload Configuration với Security Settings
 * Cấu hình upload paths, file types, size limits và security policies
 */
@Configuration
@ConfigurationProperties(prefix = "app.file-upload")
@Data
public class FileUploadConfig implements WebMvcConfigurer {

    /**
     * Base upload directory
     */
    private String uploadDir = "uploads";

    /**
     * Maximum file size (in bytes) - default 10MB
     */
    private long maxFileSize = 10 * 1024 * 1024;

    /**
     * Maximum request size (in bytes) - default 50MB
     */
    private long maxRequestSize = 50 * 1024 * 1024;

    /**
     * Allowed file extensions by category
     */
    private Map<String, List<String>> allowedExtensions = Map.of(
        "image", List.of("jpg", "jpeg", "png", "gif", "bmp", "webp"),
        "document", List.of("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt"),
        "video", List.of("mp4", "avi", "mov", "wmv", "flv", "webm"),
        "audio", List.of("mp3", "wav", "flac", "aac", "ogg"),
        "archive", List.of("zip", "rar", "7z", "tar", "gz")
    );

    /**
     * Allowed MIME types by category
     */
    private Map<String, List<String>> allowedMimeTypes = Map.of(
        "image", List.of(
            "image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp"
        ),
        "document", List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain"
        ),
        "video", List.of(
            "video/mp4", "video/avi", "video/quicktime", "video/x-ms-wmv", 
            "video/x-flv", "video/webm"
        ),
        "audio", List.of(
            "audio/mpeg", "audio/wav", "audio/flac", "audio/aac", "audio/ogg"
        ),
        "archive", List.of(
            "application/zip", "application/x-rar-compressed", "application/x-7z-compressed",
            "application/x-tar", "application/gzip"
        )
    );

    /**
     * File size limits by category (in bytes)
     */
    private Map<String, Long> fileSizeLimits = Map.of(
        "image", 5L * 1024 * 1024,      // 5MB
        "document", 10L * 1024 * 1024,  // 10MB
        "video", 100L * 1024 * 1024,    // 100MB
        "audio", 20L * 1024 * 1024,     // 20MB
        "archive", 50L * 1024 * 1024    // 50MB
    );

    /**
     * Upload paths by category
     */
    private Map<String, String> uploadPaths = Map.of(
        "image", "images",
        "document", "documents",
        "video", "videos",
        "audio", "audio",
        "archive", "archives",
        "avatar", "avatars",
        "attachment", "attachments"
    );

    /**
     * Security settings
     */
    private SecuritySettings security = new SecuritySettings();

    /**
     * Virus scanning settings
     */
    private VirusScanSettings virusScan = new VirusScanSettings();

    /**
     * Image processing settings
     */
    private ImageProcessingSettings imageProcessing = new ImageProcessingSettings();

    @Data
    public static class SecuritySettings {
        /**
         * Enable virus scanning
         */
        private boolean enableVirusScanning = true;

        /**
         * Enable file content validation
         */
        private boolean enableContentValidation = true;

        /**
         * Enable path traversal protection
         */
        private boolean enablePathTraversalProtection = true;

        /**
         * Enable filename sanitization
         */
        private boolean enableFilenameSanitization = true;

        /**
         * Maximum filename length
         */
        private int maxFilenameLength = 255;

        /**
         * Quarantine directory for suspicious files
         */
        private String quarantineDir = "quarantine";

        /**
         * Blocked file patterns (regex)
         */
        private List<String> blockedPatterns = List.of(
            ".*\\.(exe|bat|cmd|com|pif|scr|vbs|js|jar|sh)$",
            ".*\\.php.*",
            ".*\\.jsp.*",
            ".*\\.asp.*"
        );

        /**
         * Blocked MIME types
         */
        private List<String> blockedMimeTypes = List.of(
            "application/x-executable",
            "application/x-msdownload",
            "application/x-msdos-program",
            "application/x-java-archive",
            "text/x-php",
            "application/x-httpd-php"
        );
    }

    @Data
    public static class VirusScanSettings {
        /**
         * Enable ClamAV integration
         */
        private boolean enableClamAV = false;

        /**
         * ClamAV daemon host
         */
        private String clamAVHost = "localhost";

        /**
         * ClamAV daemon port
         */
        private int clamAVPort = 3310;

        /**
         * Scan timeout (in seconds)
         */
        private int scanTimeout = 30;

        /**
         * Action on virus detection
         */
        private String virusAction = "QUARANTINE"; // QUARANTINE, DELETE, REJECT
    }

    @Data
    public static class ImageProcessingSettings {
        /**
         * Enable image resizing
         */
        private boolean enableResizing = true;

        /**
         * Maximum image width
         */
        private int maxWidth = 2048;

        /**
         * Maximum image height
         */
        private int maxHeight = 2048;

        /**
         * Image quality (0-100)
         */
        private int quality = 85;

        /**
         * Generate thumbnails
         */
        private boolean generateThumbnails = true;

        /**
         * Thumbnail sizes
         */
        private List<String> thumbnailSizes = List.of("150x150", "300x300");

        /**
         * Strip EXIF data for privacy
         */
        private boolean stripExifData = true;
    }

    /**
     * Configure static resource handlers
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/")
                .setCachePeriod(3600); // 1 hour cache

        // Serve thumbnails
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations("file:" + uploadDir + "/thumbnails/")
                .setCachePeriod(86400); // 24 hours cache
    }

    /**
     * Get allowed extensions for category
     */
    public List<String> getAllowedExtensions(String category) {
        return allowedExtensions.getOrDefault(category, List.of());
    }

    /**
     * Get allowed MIME types for category
     */
    public List<String> getAllowedMimeTypes(String category) {
        return allowedMimeTypes.getOrDefault(category, List.of());
    }

    /**
     * Get file size limit for category
     */
    public long getFileSizeLimit(String category) {
        return fileSizeLimits.getOrDefault(category, maxFileSize);
    }

    /**
     * Get upload path for category
     */
    public String getUploadPath(String category) {
        return uploadPaths.getOrDefault(category, "misc");
    }

    /**
     * Check if file extension is allowed
     */
    public boolean isExtensionAllowed(String extension, String category) {
        if (extension == null || category == null) {
            return false;
        }
        return getAllowedExtensions(category).contains(extension.toLowerCase());
    }

    /**
     * Check if MIME type is allowed
     */
    public boolean isMimeTypeAllowed(String mimeType, String category) {
        if (mimeType == null || category == null) {
            return false;
        }
        return getAllowedMimeTypes(category).contains(mimeType.toLowerCase());
    }

    /**
     * Check if file is blocked by security patterns
     */
    public boolean isFileBlocked(String filename, String mimeType) {
        if (filename == null) {
            return true;
        }

        // Check blocked patterns
        for (String pattern : security.getBlockedPatterns()) {
            if (filename.toLowerCase().matches(pattern)) {
                return true;
            }
        }

        // Check blocked MIME types
        if (mimeType != null && security.getBlockedMimeTypes().contains(mimeType.toLowerCase())) {
            return true;
        }

        return false;
    }

    /**
     * Sanitize filename
     */
    public String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "unnamed_file";
        }

        // Remove path traversal attempts
        filename = filename.replaceAll("\\.\\./", "");
        filename = filename.replaceAll("\\.\\\\", "");

        // Remove dangerous characters
        filename = filename.replaceAll("[<>:\"/\\\\|?*]", "_");

        // Remove control characters
        filename = filename.replaceAll("[\\x00-\\x1f\\x7f]", "");

        // Limit length
        if (filename.length() > security.getMaxFilenameLength()) {
            String extension = "";
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = filename.substring(dotIndex);
                filename = filename.substring(0, dotIndex);
            }
            
            int maxNameLength = security.getMaxFilenameLength() - extension.length();
            filename = filename.substring(0, Math.min(filename.length(), maxNameLength)) + extension;
        }

        // Ensure filename is not empty after sanitization
        if (filename.trim().isEmpty()) {
            filename = "sanitized_file";
        }

        return filename;
    }

    /**
     * Get full upload directory path
     */
    public String getFullUploadPath(String category) {
        return uploadDir + "/" + getUploadPath(category);
    }

    /**
     * Get quarantine directory path
     */
    public String getQuarantinePath() {
        return uploadDir + "/" + security.getQuarantineDir();
    }
}
