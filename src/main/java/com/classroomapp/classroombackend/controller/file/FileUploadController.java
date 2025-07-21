package com.classroomapp.classroombackend.controller.file;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.file.FileUploadResult;
import com.classroomapp.classroombackend.model.file.UploadedFile;
import com.classroomapp.classroombackend.service.file.FileUploadService;
import com.classroomapp.classroombackend.service.file.security.FileSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * File Upload Controller với comprehensive security
 * Xử lý upload, download, và quản lý files
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
@Validated
@Tag(name = "File Upload Management", description = "APIs cho quản lý file upload với bảo mật")
@SecurityRequirement(name = "bearerAuth")
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final FileSecurityService fileSecurityService;

    @Operation(summary = "Upload single file", 
               description = "Upload một file với security validation")
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<FileUploadResult>> uploadFile(
            @Parameter(description = "File to upload") @RequestParam("file") MultipartFile file,
            @Parameter(description = "File category") @RequestParam("category") String category,
            @Parameter(description = "File description") @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        
        log.info("Upload file request: {} in category: {} by user: {}", 
                file.getOriginalFilename(), category, authentication.getName());

        // Check upload permission
        if (!fileSecurityService.hasUploadPermission(authentication.getName(), category)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Không có quyền upload vào danh mục này"));
        }

        FileUploadResult result = fileUploadService.uploadFile(file, category, authentication.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(result, "Upload file thành công"));
    }

    @Operation(summary = "Upload multiple files", 
               description = "Upload nhiều files cùng lúc")
    @PostMapping("/upload-multiple")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<FileUploadResult>>> uploadMultipleFiles(
            @Parameter(description = "Files to upload") @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "File category") @RequestParam("category") String category,
            Authentication authentication) {
        
        log.info("Upload multiple files request: {} files in category: {} by user: {}", 
                files.size(), category, authentication.getName());

        // Check upload permission
        if (!fileSecurityService.hasUploadPermission(authentication.getName(), category)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Không có quyền upload vào danh mục này"));
        }

        List<FileUploadResult> results = fileUploadService.uploadMultipleFiles(files, category, authentication.getName());
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(results, "Upload files thành công"));
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
               description = "Lấy thông tin file theo ID")
    @GetMapping("/{fileId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<UploadedFile>> getFileInfo(
            @Parameter(description = "File ID") @PathVariable Long fileId) {
        
        log.info("Get file info request: {}", fileId);

        UploadedFile file = fileUploadService.getFileInfo(fileId);
        return ResponseEntity.ok(ApiResponse.success(file, "Lấy thông tin file thành công"));
    }

    @Operation(summary = "Delete file", 
               description = "Xóa file theo ID")
    @DeleteMapping("/{fileId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or " +
                 "@fileSecurityService.canDeleteFile(#fileId, authentication.name)")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "File ID") @PathVariable Long fileId,
            Authentication authentication) {
        
        log.info("Delete file request: {} by user: {}", fileId, authentication.getName());

        fileUploadService.deleteFile(fileId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Xóa file thành công"));
    }

    @Operation(summary = "Get files by category", 
               description = "Lấy danh sách files theo category")
    @GetMapping("/category/{category}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER') or hasRole('ACCOUNTANT')")
    public ResponseEntity<ApiResponse<List<UploadedFile>>> getFilesByCategory(
            @Parameter(description = "File category") @PathVariable String category) {
        
        log.info("Get files by category request: {}", category);

        List<UploadedFile> files = fileUploadService.getFilesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(files, "Lấy files theo category thành công"));
    }

    @Operation(summary = "Get my files", 
               description = "Lấy danh sách files của tôi")
    @GetMapping("/my-files")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('TEACHER')")
    public ResponseEntity<ApiResponse<List<UploadedFile>>> getMyFiles(Authentication authentication) {
        log.info("Get my files request by user: {}", authentication.getName());

        List<UploadedFile> files = fileUploadService.getFilesByUploader(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(files, "Lấy files của tôi thành công"));
    }

    @Operation(summary = "Search files", 
               description = "Tìm kiếm files với filters")
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
        
        return ResponseEntity.ok(ApiResponse.success(files, "Tìm kiếm files thành công"));
    }

    @Operation(summary = "Get file security report", 
               description = "Lấy báo cáo bảo mật của file")
    @PostMapping("/security-report")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<FileSecurityService.FileSecurityReport>> getSecurityReport(
            @Parameter(description = "File to analyze") @RequestParam("file") MultipartFile file) {
        
        log.info("Security report request for file: {}", file.getOriginalFilename());

        FileSecurityService.FileSecurityReport report = fileSecurityService.generateSecurityReport(file);
        return ResponseEntity.ok(ApiResponse.success(report, "Tạo báo cáo bảo mật thành công"));
    }

    @Operation(summary = "Get storage statistics", 
               description = "Lấy thống kê storage")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<FileUploadService.FileStorageStats>> getStorageStatistics() {
        log.info("Get storage statistics request");

        FileUploadService.FileStorageStats stats = fileUploadService.getStorageStats();
        return ResponseEntity.ok(ApiResponse.success(stats, "Lấy thống kê storage thành công"));
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
               description = "Dọn dẹp files cũ (Admin only)")
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cleanupOldFiles(
            @Parameter(description = "Days old") @RequestParam(defaultValue = "30") int daysOld) {
        
        log.info("Cleanup old files request: {} days old", daysOld);

        fileUploadService.cleanupOldFiles(daysOld);
        return ResponseEntity.ok(ApiResponse.success(null, "Dọn dẹp files cũ thành công"));
    }

    /**
     * Exception handler for file upload errors
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleFileUploadException(Exception e) {
        log.error("File upload error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Lỗi xử lý file: " + e.getMessage()));
    }
}
