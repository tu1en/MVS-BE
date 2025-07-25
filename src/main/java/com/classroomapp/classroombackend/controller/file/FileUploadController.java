package com.classroomapp.classroombackend.controller.file;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.common.FileUploadResponse;
import com.classroomapp.classroombackend.model.file.UploadedFile;
import com.classroomapp.classroombackend.service.file.FileUploadService;
import com.classroomapp.classroombackend.service.file.security.FileSecurityService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * File Upload Controller vá»›i comprehensive security
 * Xá»­ lÃ½ upload, download, vÃ  quáº£n lÃ½ files
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "File Upload Management", description = "APIs cho quáº£n lÃ½ file upload vá»›i báº£o máº­t")
@SecurityRequirement(name = "bearerAuth")
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final FileSecurityService fileSecurityService;

    @Operation(summary = "Upload single file", 
               description = "Upload má»™t file vá»›i security validation")
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "File category") @RequestParam("category") String category,
            @Parameter(description = "File description") @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        
        log.info("Upload file request: {} in category: {} by user: {}", 
                file.getOriginalFilename(), category, authentication.getName());

        // Check upload permission
        if (!fileSecurityService.hasUploadPermission(authentication.getName(), category)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("KhÃ´ng cÃ³ quyá»n upload vÃ o danh má»¥c nÃ y"));
        }

        FileUploadResponse result = fileUploadService.uploadFile(file, category, authentication.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(result, "Upload file thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Upload multiple files", 
               description = "Upload nhiá»u files cÃ¹ng lÃºc")
    @PostMapping("/upload-multiple")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<FileUploadResponse>>> uploadMultipleFiles(
            @Parameter(description = "Files to upload") @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "File category") @RequestParam("category") String category,
            Authentication authentication) {
        
        log.info("Upload multiple files request: {} files in category: {} by user: {}", 
                files.size(), category, authentication.getName());

        // Check upload permission
        if (!fileSecurityService.hasUploadPermission(authentication.getName(), category)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("KhÃ´ng cÃ³ quyá»n upload vÃ o danh má»¥c nÃ y"));
        }

        List<FileUploadResponse> results = fileUploadService.uploadMultipleFiles(files, category, authentication.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(results, "Upload files thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Download file", 
               description = "Download file theo ID")
    @GetMapping("/download/{fileId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<Resource> downloadFile(
            @Parameter(description = "File ID") @PathVariable Long fileId,
            Authentication authentication,
            HttpServletRequest request) {
        
        log.info("Download file request: {} by user: {}", fileId, authentication.getName());

        try {
            UploadedFile uploadedFile = fileUploadService.getFileInfo(fileId);
            
            // Check if file is quarantined
            if (uploadedFile.getQuarantined()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Path filePath = Paths.get(uploadedFile.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Update download statistics
            uploadedFile.incrementDownloadCount(authentication.getName());
            // Note: In real implementation, you'd save this back to database

            // Determine content type
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            } catch (IOException ex) {
                log.info("Could not determine file type.");
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + uploadedFile.getOriginalFilename() + "\"")
                .body(resource);

        } catch (MalformedURLException e) {
            log.error("Error downloading file {}: {}", fileId, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get file info", 
               description = "Láº¥y thÃ´ng tin file theo ID")
    @GetMapping("/{fileId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<UploadedFile>> getFileInfo(
            @Parameter(description = "File ID") @PathVariable Long fileId) {
        
        log.info("Get file info request: {}", fileId);

        UploadedFile file = fileUploadService.getFileInfo(fileId);
        return ResponseEntity.ok(ApiResponse.success(file, "Láº¥y thÃ´ng tin file thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Delete file", 
               description = "XÃ³a file theo ID")
    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or " +
                 "@fileSecurityService.canDeleteFile(#fileId, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "File ID") @PathVariable Long fileId,
            Authentication authentication) {
        
        log.info("Delete file request: {} by user: {}", fileId, authentication.getName());

        fileUploadService.deleteFile(fileId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "XÃ³a file thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Get files by category", 
               description = "Láº¥y danh sÃ¡ch files theo category")
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<UploadedFile>>> getFilesByCategory(
            @Parameter(description = "File category") @PathVariable String category) {
        
        log.info("Get files by category request: {}", category);

        List<UploadedFile> files = fileUploadService.getFilesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(files, "Láº¥y files theo category thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Get my files", 
               description = "Láº¥y danh sÃ¡ch files cá»§a tÃ´i")
    @GetMapping("/my-files")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<UploadedFile>>> getMyFiles(Authentication authentication) {
        log.info("Get my files request by user: {}", authentication.getName());

        List<UploadedFile> files = fileUploadService.getFilesByUploader(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(files, "Láº¥y files cá»§a tÃ´i thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Search files", 
               description = "TÃ¬m kiáº¿m files vá»›i filters")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<Page<UploadedFile>>> searchFiles(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String search,
            @Parameter(description = "File category") @RequestParam(required = false) String category,
            @Parameter(description = "Uploader") @RequestParam(required = false) String uploadedBy,
            @Parameter(description = "MIME type") @RequestParam(required = false) String mimeType,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) int size) {
        
        log.info("Search files request with search: {}, category: {}", search, category);

        // Implementation would use repository search method
        // For now, return empty page
        Pageable pageable = PageRequest.of(page, size);
        Page<UploadedFile> files = Page.empty(pageable);
        
        return ResponseEntity.ok(ApiResponse.success(files, "TÃ¬m kiáº¿m files thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Get file security report", 
               description = "Láº¥y bÃ¡o cÃ¡o báº£o máº­t cá»§a file")
    @PostMapping("/security-report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<FileSecurityService.FileSecurityReport>> getSecurityReport(
            @Parameter(description = "File to analyze") @RequestParam("file") MultipartFile file) {
        
        log.info("Security report request for file: {}", file.getOriginalFilename());

        FileSecurityService.FileSecurityReport report = fileSecurityService.generateSecurityReport(file);
        return ResponseEntity.ok(ApiResponse.success(report, "Táº¡o bÃ¡o cÃ¡o báº£o máº­t thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Get storage statistics", 
               description = "Láº¥y thá»‘ng kÃª storage")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<FileUploadService.FileStorageStats>> getStorageStatistics() {
        log.info("Get storage statistics request");

        FileUploadService.FileStorageStats stats = fileUploadService.getStorageStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Láº¥y thá»‘ng kÃª storage thÃ nh cÃ´ng"));
    }

    @Operation(summary = "Serve uploaded file", 
               description = "Serve file cho public access")
    @GetMapping("/serve/{category}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
            @Parameter(description = "File category") @PathVariable String category,
            @Parameter(description = "Filename") @PathVariable String filename,
            HttpServletRequest request) {
        
        try {
            Path filePath = Paths.get("uploads").resolve(category).resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Security check: ensure file is within allowed directory
            Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
            Path resolvedPath = filePath.toAbsolutePath().normalize();
            
            if (!resolvedPath.startsWith(uploadDir)) {
                log.warn("Path traversal attempt detected: {}", filename);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Determine content type
            String contentType = null;
            try {
                contentType = Files.probeContentType(filePath);
            } catch (IOException ex) {
                log.debug("Could not determine file type for: {}", filename);
            }

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
                .body(resource);

        } catch (MalformedURLException e) {
            log.error("Error serving file {}/{}: {}", category, filename, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Cleanup old files", 
               description = "Dá»n dáº¹p files cÅ© (Admin only)")
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cleanupOldFiles(
            @Parameter(description = "Days old") @RequestParam(defaultValue = "30") int daysOld) {
        
        log.info("Cleanup old files request: {} days old", daysOld);

        fileUploadService.cleanupOldFiles(daysOld);
        return ResponseEntity.ok(ApiResponse.success(null, "Dá»n dáº¹p files cÅ© thÃ nh cÃ´ng"));
    }

    /**
     * Exception handler for file upload errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleFileUploadException(Exception e) {
        log.error("File upload error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Lá»—i xá»­ lÃ½ file: " + e.getMessage()));
    }
}
