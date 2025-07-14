package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.classroomapp.classroombackend.dto.CourseMaterialDto;
import com.classroomapp.classroombackend.dto.UploadMaterialDto;
import com.classroomapp.classroombackend.service.CourseMaterialService;

@RestController
@RequestMapping("/api/materials")
@CrossOrigin(origins = "*")
public class CourseMaterialController {

    private static final Logger log = LoggerFactory.getLogger(CourseMaterialController.class);

    @Autowired
    private CourseMaterialService courseMaterialService;

    /**
     * Upload a new course material
     */
    @PostMapping("/upload")
    public ResponseEntity<CourseMaterialDto> uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("classroomId") Long classroomId,
            @RequestParam(value = "isPublic", defaultValue = "true") Boolean isPublic,
            @RequestParam("uploadedBy") Long uploadedBy) {
        try {
            UploadMaterialDto uploadDto = new UploadMaterialDto();
            uploadDto.setTitle(title);
            uploadDto.setDescription(description);
            uploadDto.setClassroomId(classroomId);
            uploadDto.setIsPublic(isPublic);

            CourseMaterialDto material = courseMaterialService.uploadMaterial(uploadDto, file, uploadedBy);
            return ResponseEntity.ok(material);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all materials for a classroom
     */
    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<List<CourseMaterialDto>> getMaterialsByClassroom(@PathVariable Long classroomId) {
        try {
            List<CourseMaterialDto> materials = courseMaterialService.getMaterialsByClassroom(classroomId);
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get materials by course ID (alias for classroom ID)
     * Added to match frontend API expectation
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<CourseMaterialDto>> getMaterialsByCourse(@PathVariable Long courseId) {
        try {
            // Course ID is equivalent to classroom ID in this context
            List<CourseMaterialDto> materials = courseMaterialService.getMaterialsByClassroom(courseId);
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get public materials for a classroom
     */
    @GetMapping("/classroom/{classroomId}/public")
    public ResponseEntity<List<CourseMaterialDto>> getPublicMaterialsByClassroom(@PathVariable Long classroomId) {
        try {
            List<CourseMaterialDto> materials = courseMaterialService.getPublicMaterialsByClassroom(classroomId);
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download a material file
     */
    @GetMapping("/download/{materialId}")
    public ResponseEntity<byte[]> downloadMaterial(@PathVariable Long materialId) {
        // Get current authenticated user for logging
        String currentUser = getCurrentAuthenticatedUser();

        try {
            log.info("🔽 MATERIAL DOWNLOAD: Starting download for material ID: {} by user: {}", materialId, currentUser);

            // Check if material exists first
            CourseMaterialDto material;
            try {
                material = courseMaterialService.getMaterialById(materialId);
                log.info("🔽 MATERIAL DOWNLOAD: Found material: {} - {} (Size: {} bytes, Type: {})",
                    material.getFileName(), material.getFilePath(), material.getFileSize(), material.getFileType());
                log.info("🔽 MATERIAL DOWNLOAD: Material belongs to classroom: {}, uploaded by: {}",
                    material.getClassroomId(), material.getUploadedBy());
            } catch (Exception e) {
                log.error("🔽 MATERIAL DOWNLOAD: Material not found - ID {}: {}", materialId, e.getMessage());
                log.error("🔽 MATERIAL DOWNLOAD: Exception type: {}", e.getClass().getSimpleName());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Error-Message", "Tài liệu không tồn tại với ID: " + materialId)
                        .build();
            }

            // Download file content
            byte[] fileContent;
            try {
                log.info("🔽 MATERIAL DOWNLOAD: Attempting to read file content for material ID: {}", materialId);
                fileContent = courseMaterialService.downloadMaterial(materialId);
                log.info("🔽 MATERIAL DOWNLOAD: Successfully read file content, size: {} bytes",
                        fileContent != null ? fileContent.length : 0);
            } catch (RuntimeException e) {
                log.error("🔽 MATERIAL DOWNLOAD: File system error for material ID {}: {}", materialId, e.getMessage());
                log.error("🔽 MATERIAL DOWNLOAD: Exception type: {}, Stack trace: ", e.getClass().getSimpleName(), e);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Error-Message", "File không tồn tại: " + e.getMessage())
                        .build();
            } catch (Exception e) {
                log.error("🔽 MATERIAL DOWNLOAD: Unexpected error during file read for material ID {}: {}", materialId, e.getMessage());
                log.error("🔽 MATERIAL DOWNLOAD: Unexpected exception type: {}, Stack trace: ", e.getClass().getSimpleName(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .header("X-Error-Message", "Lỗi server không mong đợi: " + e.getMessage())
                        .build();
            }

            if (fileContent == null || fileContent.length == 0) {
                log.warn("🔽 MATERIAL DOWNLOAD: Empty file content for material ID: {}", materialId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header("X-Error-Message", "Nội dung file không tồn tại hoặc file rỗng")
                        .build();
            }

            // Xác định Content-Type an toàn
            String contentType = material.getFileType();
            if (contentType == null || contentType.trim().isEmpty()) {
                contentType = "application/octet-stream"; // Default binary type
                log.info("Sử dụng content-type mặc định: {}", contentType);
            }

            // Đảm bảo filename an toàn cho download
            String safeFileName = material.getFileName();
            if (safeFileName == null || safeFileName.trim().isEmpty()) {
                safeFileName = "download_" + materialId;
                log.warn("Filename rỗng, sử dụng tên mặc định: {}", safeFileName);
            }

            log.info("Tải xuống thành công tài liệu ID: {}, filename: {}, kích thước: {} bytes, Content-Type: {}",
                    materialId, safeFileName, fileContent.length, contentType);

            // Return byte array directly - let Spring handle Content-Type automatically
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFileName + "\"")
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length))
                    .header("X-Original-Content-Type", material.getFileType()) // Keep original type in custom header
                    .body(fileContent);

        } catch (Exception e) {
            log.error("🔽 MATERIAL DOWNLOAD: Unexpected error during download process for material ID {}: {}", materialId, e.getMessage(), e);
            log.error("🔽 MATERIAL DOWNLOAD: User: {}, Exception type: {}", currentUser, e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Error-Message", "Lỗi server không mong đợi: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Get current authenticated user for logging purposes
     */
    private String getCurrentAuthenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserDetails) {
                    return ((UserDetails) principal).getUsername();
                } else {
                    return principal.toString();
                }
            }
        } catch (Exception e) {
            log.debug("Could not get current authenticated user: {}", e.getMessage());
        }
        return "anonymous";
    }

    /**
     * Alternative download endpoint to match frontend service expectation
     */
    @GetMapping("/{materialId}/download")
    public ResponseEntity<byte[]> downloadMaterialAlternative(@PathVariable Long materialId) {
        return downloadMaterial(materialId);
    }

    /**
     * Delete a material
     */
    @DeleteMapping("/{materialId}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long materialId) {
        try {
            courseMaterialService.deleteMaterial(materialId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search materials
     */
    @GetMapping("/search")
    public ResponseEntity<List<CourseMaterialDto>> searchMaterials(
            @RequestParam Long classroomId,
            @RequestParam String searchTerm) {
        try {
            List<CourseMaterialDto> materials = courseMaterialService.searchMaterials(classroomId, searchTerm);
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get materials by file type
     */
    @GetMapping("/classroom/{classroomId}/type/{fileType}")
    public ResponseEntity<List<CourseMaterialDto>> getMaterialsByFileType(
            @PathVariable Long classroomId, 
            @PathVariable String fileType) {
        try {
            List<CourseMaterialDto> materials = courseMaterialService.getMaterialsByFileType(classroomId, fileType);
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get total downloads for a classroom
     */
    @GetMapping("/classroom/{classroomId}/downloads/total")
    public ResponseEntity<Long> getTotalDownloads(@PathVariable Long classroomId) {
        try {
            Long totalDownloads = courseMaterialService.getTotalDownloadsByClassroom(classroomId);
            return ResponseEntity.ok(totalDownloads);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
