package com.classroomapp.classroombackend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.util.SecurityUtils;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 🎯 FIXED FILE CONTROLLER
 * 
 * ✅ FIXES APPLIED:
 * 1. Added missing imports
 * 2. Fixed string literal escaping issues
 * 3. Added FileUploadResponse class
 * 4. Fixed syntax errors in string concatenation
 * 5. Corrected path separator handling
 * 
 * ⚠️ DEBUGGING FEATURES:
 * - Health check endpoint
 * - Detailed logging
 * - PostConstruct verification
 */
@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"}, allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final SecurityUtils securityUtils;

    @Value("${file.upload.dir:uploads}")
    private String uploadDir;

    @Value("${file.max-size:10485760}") // 10MB default
    private long maxFileSize;

    @Value("${server.port:8088}")
    private String serverPort;

    /**
     * 🔍 DEBUG: Verify controller is loaded
     */
    @PostConstruct
    public void init() {
        log.info("🎯 FileController initialized successfully!");
        log.info("📁 Upload directory: {}", uploadDir);
        log.info("📏 Max file size: {} MB", maxFileSize / (1024 * 1024));
        log.info("🌐 Server port: {}", serverPort);
    }

    // Allowed file types cho assignments
    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp",
        "application/pdf",
        "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel", 
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "text/plain", "text/csv",
        "application/zip"
    );

    /**
     * 🔍 DEBUG: Simple health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        log.info("🏥 FileController health check called");
        return ResponseEntity.ok("FileController is working! ✅");
    }

    /**
     * 📤 UPLOAD FILE - Fixed mapping
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "assignments") String category,
            HttpServletRequest request) {
        
        log.info("🎯 FileController.uploadFile called!");
        log.info("📁 File upload request: filename={}, size={}, category={}", 
                file.getOriginalFilename(), file.getSize(), category);

        try {
            // ✅ 1. Validate file
            validateFile(file);

            // ✅ 2. Get current user info
            User currentUser = securityUtils.getCurrentUser();
            String uploaderInfo = currentUser != null ? 
                String.format("user_%d_%s", currentUser.getId(), currentUser.getUsername()) : 
                "anonymous_" + request.getRemoteAddr().replace(".", "_");

            log.info("👤 Uploader info: {}", uploaderInfo);

            // ✅ 3. Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uniqueFilename = String.format("%s_%s_%s%s", 
                uploaderInfo, timestamp, UUID.randomUUID().toString().substring(0, 8), fileExtension);

            log.info("📝 Generated filename: {}", uniqueFilename);

            // ✅ 4. Create upload directory structure
            Path categoryPath = Paths.get(uploadDir, category);
            Path datePath = categoryPath.resolve(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM")));
            
            if (!Files.exists(datePath)) {
                Files.createDirectories(datePath);
                log.info("📂 Created directory: {}", datePath);
            }

            // ✅ 5. Save file
            Path targetPath = datePath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("✅ File saved successfully: {}", targetPath);

            // ✅ 6. Generate public URL
            String baseUrl = getBaseUrl(request);
            String relativePath = String.format("%s/%s/%s", 
                category, 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM")), 
                uniqueFilename);
            // ✅ FIXED: Proper string concatenation without escape issues
            String fileUrl = String.format("%s/api/files/download/%s", baseUrl, relativePath.replace("\\", "/"));

            log.info("🌐 Generated file URL: {}", fileUrl);

            // ✅ 7. Create response
            FileUploadResponse response = FileUploadResponse.builder()
                .success(true)
                .filename(uniqueFilename)
                .originalFilename(originalFilename)
                .fileUrl(fileUrl)
                .filePath(relativePath)
                .mimeType(file.getContentType())
                .fileSize(file.getSize())
                .category(category)
                .uploadedAt(LocalDateTime.now())
                .build();

            log.info("🎉 File upload completed successfully!");

            return ResponseEntity.ok().body(response);

        } catch (IllegalArgumentException e) {
            log.warn("❌ File validation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Validation error: " + e.getMessage());
        } catch (IOException e) {
            log.error("💥 File upload failed due to IO error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Upload failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("💥 Unexpected error during file upload: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unexpected error occurred");
        }
    }
        // Test endpoint đơn giản
        @GetMapping("/test")
        public ResponseEntity<String> test() {
            log.info("🧪 Test endpoint called!");
            return ResponseEntity.ok("FileController is working!");
        }

    /**
     * 📥 DOWNLOAD FILE - Compatible với existing URLs
     */
    @GetMapping("/download/{category}/{year}/{month}/{filename:.+}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String category,
            @PathVariable String year,
            @PathVariable String month,
            @PathVariable String filename) {
        
        log.info("📥 Download request: {}/{}/{}/{}", category, year, month, filename);
        
        try {
            // ✅ Construct file path
            Path filePath = Paths.get(uploadDir)
                .resolve(category)
                .resolve(year)
                .resolve(month)
                .resolve(filename);
            
            // ✅ Security check
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path resolvedPath = filePath.toAbsolutePath().normalize();
            
            if (!resolvedPath.startsWith(uploadPath)) {
                log.warn("⚠️ Path traversal attempt detected: {}", filename);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // ✅ Check if file exists
            if (!Files.exists(filePath)) {
                log.warn("📄 File not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // ✅ Create resource
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                log.warn("📄 File not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            // ✅ Determine content type
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // ✅ Extract original filename
            String originalFilename = extractOriginalFilename(filename);

            log.info("✅ Serving file: {} as {}", filePath, originalFilename);

            // ✅ FIXED: Proper string quoting in header
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + originalFilename + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(resource);

        } catch (Exception e) {
            log.error("💥 Error serving file: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 📥 ALTERNATIVE DOWNLOAD - Support old format URLs
     */
    @GetMapping("/download/{path:.+}")
    public ResponseEntity<Resource> downloadFileAlternative(@PathVariable String path) {
        log.info("📥 Alternative download request: {}", path);
        
        // Parse path like "assignments/2025/01/filename.ext"
        String[] parts = path.split("/");
        if (parts.length >= 4) {
            return downloadFile(parts[0], parts[1], parts[2], 
                String.join("/", Arrays.copyOfRange(parts, 3, parts.length)));
        }
        
        return ResponseEntity.notFound().build();
    }

    /**
     * 🔒 VALIDATE FILE
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // ✅ Check file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException(String.format(
                "File too large. Maximum size is %d MB", maxFileSize / (1024 * 1024)));
        }

        // ✅ Check file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_FILE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                "File type not allowed. Allowed types: " + String.join(", ", ALLOWED_FILE_TYPES));
        }

        // ✅ Check filename
        String filename = file.getOriginalFilename();
        if (filename == null || filename.trim().isEmpty()) {
            throw new IllegalArgumentException("Filename is required");
        }

        // ✅ Check for dangerous filename patterns  
        // ✅ FIXED: Proper escape sequences for backslash
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename");
        }
    }

    /**
     * 🔧 UTILITY METHODS
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String extractOriginalFilename(String filename) {
        if (filename == null || !filename.contains("_")) {
            return filename;
        }
        
        String[] parts = filename.split("_");
        if (parts.length >= 3) {
            return parts[parts.length - 1];
        }
        return filename;
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if ((scheme.equals("http") && serverPort != 80) || 
            (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath);
        return url.toString();
    }

    /**
     * 📝 FILE UPLOAD RESPONSE DTO
     */
    @Data
    @Builder
    public static class FileUploadResponse {
        private boolean success;
        private String filename;
        private String originalFilename;
        private String fileUrl;
        private String filePath;
        private String mimeType;
        private long fileSize;
        private String category;
        private LocalDateTime uploadedAt;
    }
}