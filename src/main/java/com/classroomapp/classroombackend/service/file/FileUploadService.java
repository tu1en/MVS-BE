package com.classroomapp.classroombackend.service.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.config.FileUploadConfig;
import com.classroomapp.classroombackend.dto.common.FileUploadResponse;
import com.classroomapp.classroombackend.exception.FileUploadException;
import com.classroomapp.classroombackend.model.file.UploadedFile;
import com.classroomapp.classroombackend.repository.file.UploadedFileRepository;
import com.classroomapp.classroombackend.service.file.image.ImageProcessingService;
import com.classroomapp.classroombackend.service.file.security.FileSecurityService;
import com.classroomapp.classroombackend.service.file.virus.VirusScanService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * File Upload Service vá»›i comprehensive security
 * Xá»­ lÃ½ upload, validation, virus scanning, vÃ  image processing
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FileUploadService {

    private final FileUploadConfig fileUploadConfig;
    private final FileSecurityService fileSecurityService;
    private final VirusScanService virusScanService;
    private final ImageProcessingService imageProcessingService;
    private final UploadedFileRepository uploadedFileRepository;

    /**
     * Upload single file vá»›i security validation
     */
    public FileUploadResponse uploadFile(MultipartFile file, String category, String uploadedBy) {
        log.info("Uploading file: {} in category: {} by user: {}", 
                file.getOriginalFilename(), category, uploadedBy);

        try {
            // Validate file
            validateFile(file, category);

            // Security checks
            fileSecurityService.performSecurityChecks(file, category);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String sanitizedFilename = fileUploadConfig.sanitizeFilename(originalFilename);
            String uniqueFilename = generateUniqueFilename(sanitizedFilename);

            // Create upload directory
            Path uploadPath = createUploadDirectory(category);
            Path filePath = uploadPath.resolve(uniqueFilename);

            // Save file to disk
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Virus scan
            if (fileUploadConfig.getSecurity().isEnableVirusScanning()) {
                virusScanService.scanFile(filePath);
            }

            // Process image if applicable
            List<String> thumbnails = new ArrayList<>();
            if (isImageFile(file) && fileUploadConfig.getImageProcessing().isEnableResizing()) {
                imageProcessingService.processImage(filePath, category);
                if (fileUploadConfig.getImageProcessing().isGenerateThumbnails()) {
                    thumbnails = imageProcessingService.generateThumbnails(filePath);
                }
            }

            // Save to database
            UploadedFile uploadedFile = createUploadedFileRecord(
                file, category, uploadedBy, uniqueFilename, filePath.toString(), thumbnails);
            uploadedFile = uploadedFileRepository.save(uploadedFile);

            log.info("File uploaded successfully: {} with ID: {}", uniqueFilename, uploadedFile.getId());

            return FileUploadResponse.builder()
                .success(true)
                .fileId(uploadedFile.getId())
                .filename(uniqueFilename)
                .originalFilename(originalFilename)
                .filePath(getRelativeFilePath(filePath))
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .category(category)
                .thumbnails(thumbnails)
                .uploadedAt(uploadedFile.getUploadedAt())
                .build();

        } catch (Exception e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            throw new FileUploadException("KhÃ´ng thá»ƒ upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Upload multiple files
     */
    public List<FileUploadResponse> uploadMultipleFiles(List<MultipartFile> files, String category, String uploadedBy) {
        log.info("Uploading {} files in category: {} by user: {}", files.size(), category, uploadedBy);

        List<FileUploadResponse> results = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                FileUploadResponse result = uploadFile(file, category, uploadedBy);
                results.add(result);
            } catch (Exception e) {
                log.error("Error uploading file {}: {}", file.getOriginalFilename(), e.getMessage());
                errors.add(file.getOriginalFilename() + ": " + e.getMessage());
                
                results.add(FileUploadResponse.builder()
                    .success(false)
                    .originalFilename(file.getOriginalFilename())
                    .error(e.getMessage())
                    .build());
            }
        }

        if (!errors.isEmpty()) {
            log.warn("Some files failed to upload: {}", String.join(", ", errors));
        }

        return results;
    }

    /**
     * Delete file
     */
    public void deleteFile(Long fileId, String deletedBy) {
        log.info("Deleting file with ID: {} by user: {}", fileId, deletedBy);

        UploadedFile uploadedFile = uploadedFileRepository.findById(fileId)
            .orElseThrow(() -> new FileUploadException("File khÃ´ng tá»“n táº¡i vá»›i ID: " + fileId));

        try {
            // Delete physical file
            Path filePath = Paths.get(uploadedFile.getFilePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            // Delete thumbnails
            if (uploadedFile.getThumbnails() != null) {
                for (String thumbnail : uploadedFile.getThumbnails()) {
                    Path thumbnailPath = Paths.get(fileUploadConfig.getUploadDir(), "thumbnails", thumbnail);
                    if (Files.exists(thumbnailPath)) {
                        Files.delete(thumbnailPath);
                    }
                }
            }

            // Update database record
            uploadedFile.setDeleted(true);
            uploadedFile.setDeletedAt(LocalDateTime.now());
            uploadedFile.setDeletedBy(deletedBy);
            uploadedFileRepository.save(uploadedFile);

            log.info("File deleted successfully: {}", fileId);

        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            throw new FileUploadException("KhÃ´ng thá»ƒ xÃ³a file: " + e.getMessage(), e);
        }
    }

    /**
     * Get file info
     */
    public UploadedFile getFileInfo(Long fileId) {
        return uploadedFileRepository.findByIdAndDeletedFalse(fileId)
            .orElseThrow(() -> new FileUploadException("File khÃ´ng tá»“n táº¡i vá»›i ID: " + fileId));
    }

    /**
     * Get files by category
     */
    public List<UploadedFile> getFilesByCategory(String category) {
        return uploadedFileRepository.findByCategoryAndDeletedFalse(category);
    }

    /**
     * Get files by uploader
     */
    public List<UploadedFile> getFilesByUploader(String uploadedBy) {
        return uploadedFileRepository.findByUploadedByAndDeletedFalse(uploadedBy);
    }

    /**
     * Validate file before upload
     */
    private void validateFile(MultipartFile file, String category) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new FileUploadException("TÃªn file khÃ´ng há»£p lá»‡");
        }

        // Check file size
        long maxSize = fileUploadConfig.getFileSizeLimit(category);
        if (file.getSize() > maxSize) {
            throw new FileUploadException(String.format(
                "File quÃ¡ lá»›n. KÃ­ch thÆ°á»›c tá»‘i Ä‘a cho %s lÃ  %d MB", 
                category, maxSize / (1024 * 1024)));
        }

        // Check file extension
        String extension = getFileExtension(originalFilename);
        if (!fileUploadConfig.isExtensionAllowed(extension, category)) {
            throw new FileUploadException(String.format(
                "Äá»‹nh dáº¡ng file khÃ´ng Ä‘Æ°á»£c phÃ©p. CÃ¡c Ä‘á»‹nh dáº¡ng cho phÃ©p: %s",
                String.join(", ", fileUploadConfig.getAllowedExtensions(category))));
        }

        // Check MIME type
        String mimeType = file.getContentType();
        if (!fileUploadConfig.isMimeTypeAllowed(mimeType, category)) {
            throw new FileUploadException("Loáº¡i file khÃ´ng Ä‘Æ°á»£c phÃ©p: " + mimeType);
        }

        // Check if file is blocked
        if (fileUploadConfig.isFileBlocked(originalFilename, mimeType)) {
            throw new FileUploadException("File bá»‹ cháº·n vÃ¬ lÃ½ do báº£o máº­t");
        }
    }

    /**
     * Create upload directory if not exists
     */
    private Path createUploadDirectory(String category) throws IOException {
        Path uploadPath = Paths.get(fileUploadConfig.getFullUploadPath(category));
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        return uploadPath;
    }

    /**
     * Generate unique filename
     */
    private String generateUniqueFilename(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String nameWithoutExtension = getFilenameWithoutExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return nameWithoutExtension + "_" + uuid + "." + extension;
    }

    /**
     * Create UploadedFile record
     */
    private UploadedFile createUploadedFileRecord(MultipartFile file, String category, 
                                                 String uploadedBy, String filename, 
                                                 String filePath, List<String> thumbnails) {
        return UploadedFile.builder()
            .originalFilename(file.getOriginalFilename())
            .filename(filename)
            .filePath(filePath)
            .fileSize(file.getSize())
            .mimeType(file.getContentType())
            .category(category)
            .uploadedBy(uploadedBy)
            .uploadedAt(LocalDateTime.now())
            .thumbnails(thumbnails)
            .deleted(false)
            .build();
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Get filename without extension
     */
    private String getFilenameWithoutExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return filename;
        }
        return filename.substring(0, filename.lastIndexOf("."));
    }

    /**
     * Check if file is image
     */
    private boolean isImageFile(MultipartFile file) {
        String mimeType = file.getContentType();
        return mimeType != null && mimeType.startsWith("image/");
    }

    /**
     * Get relative file path for URL
     */
    private String getRelativeFilePath(Path filePath) {
        Path uploadDir = Paths.get(fileUploadConfig.getUploadDir());
        return uploadDir.relativize(filePath).toString().replace("\\", "/");
    }

    /**
     * Clean up old files (scheduled task)
     */
    public void cleanupOldFiles(int daysOld) {
        log.info("Cleaning up files older than {} days", daysOld);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        List<UploadedFile> oldFiles = uploadedFileRepository.findDeletedFilesOlderThan(cutoffDate);
        
        for (UploadedFile file : oldFiles) {
            try {
                Path filePath = Paths.get(file.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
                uploadedFileRepository.delete(file);
            } catch (IOException e) {
                log.error("Error cleaning up file {}: {}", file.getId(), e.getMessage());
            }
        }
        
        log.info("Cleaned up {} old files", oldFiles.size());
    }

    /**
     * Get storage statistics
     */
    public FileStorageStats getStorageStats() {
        long totalFiles = uploadedFileRepository.countByDeletedFalse();
        long totalSize = uploadedFileRepository.sumFileSizeByDeletedFalse();
        
        return FileStorageStats.builder()
            .totalFiles(totalFiles)
            .totalSize(totalSize)
            .build();
    }

    @lombok.Builder
    @lombok.Data
    public static class FileStorageStats {
        private long totalFiles;
        private long totalSize;
    }
}
