package com.classroomapp.classroombackend.service.file.security;

import com.classroomapp.classroombackend.config.FileUploadConfig;
import com.classroomapp.classroombackend.exception.FileSecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * File Security Service
 * Thực hiện các kiểm tra bảo mật cho file upload
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileSecurityService {

    private final FileUploadConfig fileUploadConfig;

    // File signatures (magic numbers) for validation
    private static final Map<String, byte[][]> FILE_SIGNATURES = new HashMap<>();
    
    static {
        // Image signatures
        FILE_SIGNATURES.put("image/jpeg", new byte[][]{
            {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
        });
        FILE_SIGNATURES.put("image/png", new byte[][]{
            {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
        });
        FILE_SIGNATURES.put("image/gif", new byte[][]{
            {0x47, 0x49, 0x46, 0x38, 0x37, 0x61}, // GIF87a
            {0x47, 0x49, 0x46, 0x38, 0x39, 0x61}  // GIF89a
        });
        
        // Document signatures
        FILE_SIGNATURES.put("application/pdf", new byte[][]{
            {0x25, 0x50, 0x44, 0x46} // %PDF
        });
        FILE_SIGNATURES.put("application/msword", new byte[][]{
            {(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1}
        });
        FILE_SIGNATURES.put("application/vnd.openxmlformats-officedocument.wordprocessingml.document", new byte[][]{
            {0x50, 0x4B, 0x03, 0x04} // ZIP signature (DOCX is ZIP-based)
        });
        
        // Archive signatures
        FILE_SIGNATURES.put("application/zip", new byte[][]{
            {0x50, 0x4B, 0x03, 0x04},
            {0x50, 0x4B, 0x05, 0x06},
            {0x50, 0x4B, 0x07, 0x08}
        });
        
        // Executable signatures (to block)
        FILE_SIGNATURES.put("application/x-executable", new byte[][]{
            {0x4D, 0x5A}, // MZ (Windows PE)
            {0x7F, 0x45, 0x4C, 0x46} // ELF (Linux)
        });
    }

    /**
     * Perform comprehensive security checks
     */
    public void performSecurityChecks(MultipartFile file, String category) {
        log.debug("Performing security checks for file: {}", file.getOriginalFilename());

        try {
            // 1. Filename security check
            validateFilename(file.getOriginalFilename());

            // 2. Content type validation
            validateContentType(file, category);

            // 3. File signature validation
            if (fileUploadConfig.getSecurity().isEnableContentValidation()) {
                validateFileSignature(file);
            }

            // 4. Path traversal protection
            if (fileUploadConfig.getSecurity().isEnablePathTraversalProtection()) {
                checkPathTraversal(file.getOriginalFilename());
            }

            // 5. Malicious content detection
            detectMaliciousContent(file);

            // 6. File size bomb detection
            detectFileSizeBomb(file);

            log.debug("Security checks passed for file: {}", file.getOriginalFilename());

        } catch (Exception e) {
            log.error("Security check failed for file {}: {}", file.getOriginalFilename(), e.getMessage());
            throw new FileSecurityException("File không đạt yêu cầu bảo mật: " + e.getMessage(), e);
        }
    }

    /**
     * Validate filename for security issues
     */
    private void validateFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            throw new FileSecurityException("Tên file không hợp lệ");
        }

        // Check for null bytes
        if (filename.contains("\0")) {
            throw new FileSecurityException("Tên file chứa ký tự không hợp lệ");
        }

        // Check for control characters
        if (filename.matches(".*[\\x00-\\x1f\\x7f].*")) {
            throw new FileSecurityException("Tên file chứa ký tự điều khiển không hợp lệ");
        }

        // Check for reserved names (Windows)
        String[] reservedNames = {"CON", "PRN", "AUX", "NUL", "COM1", "COM2", "COM3", "COM4", 
                                 "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", 
                                 "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
        
        String nameWithoutExtension = filename.contains(".") ? 
            filename.substring(0, filename.lastIndexOf(".")) : filename;
        
        for (String reserved : reservedNames) {
            if (reserved.equalsIgnoreCase(nameWithoutExtension)) {
                throw new FileSecurityException("Tên file sử dụng từ khóa dành riêng: " + reserved);
            }
        }

        // Check filename length
        if (filename.length() > fileUploadConfig.getSecurity().getMaxFilenameLength()) {
            throw new FileSecurityException("Tên file quá dài (tối đa " + 
                fileUploadConfig.getSecurity().getMaxFilenameLength() + " ký tự)");
        }
    }

    /**
     * Validate content type matches file extension
     */
    private void validateContentType(MultipartFile file, String category) {
        String declaredMimeType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        if (declaredMimeType == null) {
            throw new FileSecurityException("Không thể xác định loại file");
        }

        // Check if MIME type is in blocked list
        if (fileUploadConfig.getSecurity().getBlockedMimeTypes().contains(declaredMimeType.toLowerCase())) {
            throw new FileSecurityException("Loại file bị cấm: " + declaredMimeType);
        }

        // Validate MIME type matches category
        if (!fileUploadConfig.isMimeTypeAllowed(declaredMimeType, category)) {
            throw new FileSecurityException("Loại file không phù hợp với danh mục: " + declaredMimeType);
        }
    }

    /**
     * Validate file signature matches declared MIME type
     */
    private void validateFileSignature(MultipartFile file) throws IOException {
        String declaredMimeType = file.getContentType();
        
        if (declaredMimeType == null) {
            return; // Already handled in content type validation
        }

        byte[][] expectedSignatures = FILE_SIGNATURES.get(declaredMimeType);
        if (expectedSignatures == null) {
            // No signature validation for this MIME type
            return;
        }

        try (InputStream inputStream = file.getInputStream()) {
            byte[] fileHeader = new byte[16]; // Read first 16 bytes
            int bytesRead = inputStream.read(fileHeader);
            
            if (bytesRead < 4) {
                throw new FileSecurityException("File quá nhỏ để xác thực");
            }

            boolean signatureMatches = false;
            for (byte[] signature : expectedSignatures) {
                if (bytesRead >= signature.length && 
                    Arrays.equals(Arrays.copyOf(fileHeader, signature.length), signature)) {
                    signatureMatches = true;
                    break;
                }
            }

            if (!signatureMatches) {
                throw new FileSecurityException("Nội dung file không khớp với định dạng được khai báo");
            }
        }
    }

    /**
     * Check for path traversal attempts
     */
    private void checkPathTraversal(String filename) {
        if (filename == null) {
            return;
        }

        // Check for path traversal patterns
        String[] dangerousPatterns = {
            "../", "..\\", ".../", "...\\",
            "%2e%2e%2f", "%2e%2e%5c", "%2e%2e/", "%2e%2e\\",
            "..%2f", "..%5c", "..%c0%af", "..%c1%9c"
        };

        String lowerFilename = filename.toLowerCase();
        for (String pattern : dangerousPatterns) {
            if (lowerFilename.contains(pattern)) {
                throw new FileSecurityException("Tên file chứa mẫu path traversal nguy hiểm");
            }
        }

        // Check for absolute paths
        if (filename.startsWith("/") || filename.matches("^[a-zA-Z]:.*")) {
            throw new FileSecurityException("Tên file không được chứa đường dẫn tuyệt đối");
        }
    }

    /**
     * Detect malicious content patterns
     */
    private void detectMaliciousContent(MultipartFile file) throws IOException {
        // Check for executable signatures
        try (InputStream inputStream = file.getInputStream()) {
            byte[] fileHeader = new byte[16];
            int bytesRead = inputStream.read(fileHeader);
            
            if (bytesRead >= 2) {
                // Check for PE header (Windows executable)
                if (fileHeader[0] == 0x4D && fileHeader[1] == 0x5A) {
                    throw new FileSecurityException("File chứa mã thực thi Windows");
                }
                
                // Check for ELF header (Linux executable)
                if (bytesRead >= 4 && fileHeader[0] == 0x7F && fileHeader[1] == 0x45 && 
                    fileHeader[2] == 0x4C && fileHeader[3] == 0x46) {
                    throw new FileSecurityException("File chứa mã thực thi Linux");
                }
            }
        }

        // Check for script content in text files
        if (file.getContentType() != null && file.getContentType().startsWith("text/")) {
            try (InputStream inputStream = file.getInputStream()) {
                byte[] content = inputStream.readNBytes(1024); // Read first 1KB
                String textContent = new String(content).toLowerCase();
                
                String[] scriptPatterns = {
                    "<script", "javascript:", "vbscript:", "onload=", "onerror=",
                    "<?php", "<%", "<jsp:", "<%@", "eval(", "exec("
                };
                
                for (String pattern : scriptPatterns) {
                    if (textContent.contains(pattern)) {
                        throw new FileSecurityException("File chứa mã script nguy hiểm");
                    }
                }
            }
        }
    }

    /**
     * Detect zip bombs and similar attacks
     */
    private void detectFileSizeBomb(MultipartFile file) {
        // Check for suspiciously small files with large declared sizes
        long fileSize = file.getSize();
        String filename = file.getOriginalFilename();
        
        if (filename != null && filename.toLowerCase().endsWith(".zip")) {
            // For ZIP files, we could implement more sophisticated checks
            // This is a basic check for extremely small ZIP files
            if (fileSize < 100 && fileSize > 0) {
                log.warn("Suspiciously small ZIP file detected: {} bytes", fileSize);
                // Could implement ZIP content analysis here
            }
        }
    }

    /**
     * Quarantine suspicious file
     */
    public void quarantineFile(MultipartFile file, String reason) {
        try {
            String quarantineDir = fileUploadConfig.getQuarantinePath();
            Path quarantinePath = Paths.get(quarantineDir);
            
            if (!Files.exists(quarantinePath)) {
                Files.createDirectories(quarantinePath);
            }
            
            String quarantineFilename = System.currentTimeMillis() + "_" + 
                fileUploadConfig.sanitizeFilename(file.getOriginalFilename());
            Path quarantineFilePath = quarantinePath.resolve(quarantineFilename);
            
            Files.copy(file.getInputStream(), quarantineFilePath);
            
            log.warn("File quarantined: {} -> {} (Reason: {})", 
                file.getOriginalFilename(), quarantineFilename, reason);
                
        } catch (IOException e) {
            log.error("Failed to quarantine file {}: {}", file.getOriginalFilename(), e.getMessage());
        }
    }

    /**
     * Check if user has permission to upload to category
     */
    public boolean hasUploadPermission(String username, String category) {
        // Implement role-based permission checking
        // This would typically check user roles against category permissions
        
        // For now, basic implementation
        if ("admin".equals(category)) {
            // Only admins can upload to admin category
            return hasRole(username, "ADMIN");
        }
        
        return true; // Allow by default for other categories
    }

    /**
     * Check if user has role (placeholder implementation)
     */
    private boolean hasRole(String username, String role) {
        // This should integrate with your authentication system
        // For now, return true as placeholder
        return true;
    }

    /**
     * Generate security report for uploaded file
     */
    public FileSecurityReport generateSecurityReport(MultipartFile file) {
        FileSecurityReport report = new FileSecurityReport();
        report.setFilename(file.getOriginalFilename());
        report.setFileSize(file.getSize());
        report.setMimeType(file.getContentType());
        report.setScanTimestamp(java.time.LocalDateTime.now());
        
        try {
            performSecurityChecks(file, "document"); // Use generic category for report
            report.setSecurityStatus("SAFE");
            report.setThreatLevel("LOW");
        } catch (FileSecurityException e) {
            report.setSecurityStatus("BLOCKED");
            report.setThreatLevel("HIGH");
            report.setSecurityIssues(Arrays.asList(e.getMessage()));
        }
        
        return report;
    }

    @lombok.Data
    public static class FileSecurityReport {
        private String filename;
        private long fileSize;
        private String mimeType;
        private String securityStatus;
        private String threatLevel;
        private java.util.List<String> securityIssues;
        private java.time.LocalDateTime scanTimestamp;
    }
}
