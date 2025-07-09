package com.classroomapp.classroombackend.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller specifically for handling material downloads in various formats
 * This is a convenience controller that handles multiple URL patterns for downloads
 */
@RestController
@RequestMapping("/api/materials-alt")
@CrossOrigin(origins = "*")
public class MaterialControllerNew {

    /**
     * Mock material DTO for demo purposes
     */
    static class MaterialItem {
        private Long id;
        private String fileName;
        private String fileType;
        private String description;
        private String uploadedBy;
        private Long fileSize;
        private String downloadUrl;
        
        public MaterialItem(Long id, String fileName, String fileType, String description, String uploadedBy) {
            this.id = id;
            this.fileName = fileName;
            this.fileType = fileType;
            this.description = description;
            this.uploadedBy = uploadedBy;
            this.fileSize = 1024000L; // Mock file size
            this.downloadUrl = "/api/materials-alt/" + id + "/download";
        }
        
        // Getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getUploadedBy() { return uploadedBy; }
        public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    }
    
    // Sample materials
    private List<MaterialItem> sampleMaterials = Arrays.asList(
        new MaterialItem(3000L, "Java Programming Guide.pdf", "application/pdf", "Hướng dẫn lập trình Java từ cơ bản đến nâng cao", "Nguyễn Văn Toán"),
        new MaterialItem(3001L, "Database Design Examples.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Các ví dụ thiết kế cơ sở dữ liệu", "Nguyễn Văn Toán"),
        new MaterialItem(3002L, "Web Development Tutorial.mp4", "video/mp4", "Video hướng dẫn phát triển web", "Nguyễn Văn Toán")
    );

    /**
     * Handle download with format: /{id}/download
     */
    @GetMapping("/{materialId}/download")
    public ResponseEntity<Resource> downloadMaterialFormat1(@PathVariable Long materialId) {
        return generateMockDownload(materialId);
    }
    
    /**
     * Handle download with format: /download/{id}
     */
    @GetMapping("/download/{materialId}")
    public ResponseEntity<Resource> downloadMaterialFormat2(@PathVariable Long materialId) {
        return generateMockDownload(materialId);
    }
    
    /**
     * Handle materials list by course with a different endpoint pattern to avoid conflicts
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<MaterialItem>> getMaterialsByCourse(@PathVariable Long courseId) {
        System.out.println("Getting materials for course ID: " + courseId);
        // Return sample materials
        return ResponseEntity.ok(sampleMaterials);
    }
    
    /**
     * Generate a mock download response
     */
    private ResponseEntity<Resource> generateMockDownload(Long materialId) {
        // Find the material with the specified ID (or use a default)
        MaterialItem material = sampleMaterials.stream()
                .filter(m -> m.getId().equals(materialId))
                .findFirst()
                .orElse(new MaterialItem(materialId, "unknown.txt", "text/plain", "Unknown material", "System"));
        
        // Create mock content
        String mockContent = "This is a mock content for material: " + material.getFileName();
        ByteArrayResource resource = new ByteArrayResource(mockContent.getBytes());
        
        // Set headers for download
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(material.getFileType()))
                .contentLength(mockContent.getBytes().length)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + material.getFileName() + "\"")
                .body(resource);
    }
}
