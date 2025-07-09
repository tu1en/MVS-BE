package com.classroomapp.classroombackend.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

class MaterialDto {
    private Long id;
    private String fileName;
    private String fileType;
    private String downloadUrl;
    private String uploadDate;
    private String uploadedBy;
    private long fileSize;
    private String description;
    private String category;
    
    public MaterialDto() {}
    
    public MaterialDto(Long id, String fileName, String fileType, String uploadedBy) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.uploadedBy = uploadedBy;
        this.downloadUrl = "/api/mock-materials/download/" + id;
        this.uploadDate = java.time.LocalDateTime.now().toString();
        this.fileSize = 1024000; // Mock size
        this.description = "Tài liệu học tập";
        this.category = "General";
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }
    public String getDownloadUrl() { return downloadUrl; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    public String getUploadDate() { return uploadDate; }
    public void setUploadDate(String uploadDate) { this.uploadDate = uploadDate; }
    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }
    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}

@RestController
@RequestMapping("/api/mock-materials")
@CrossOrigin(origins = "*")
public class MaterialController {

    private final Map<Long, MaterialDto> materials = new ConcurrentHashMap<>();
    private final Map<Long, byte[]> fileContents = new ConcurrentHashMap<>();
    private final AtomicLong materialIdCounter = new AtomicLong(3000);

    public MaterialController() {
        // Initialize mock materials
        MaterialDto material1 = new MaterialDto(materialIdCounter.getAndIncrement(), 
            "Java Programming Guide.pdf", "PDF", "Thầy Nguyễn Văn A");
        material1.setDescription("Hướng dẫn lập trình Java từ cơ bản đến nâng cao");
        material1.setCategory("Programming");
        materials.put(material1.getId(), material1);
        
        MaterialDto material2 = new MaterialDto(materialIdCounter.getAndIncrement(), 
            "Database Design Examples.docx", "DOCX", "Cô Trần Thị B");
        material2.setDescription("Các ví dụ thiết kế cơ sở dữ liệu");
        material2.setCategory("Database");
        materials.put(material2.getId(), material2);
        
        MaterialDto material3 = new MaterialDto(materialIdCounter.getAndIncrement(), 
            "Web Development Tutorial.mp4", "VIDEO", "Thầy Lê Văn C");
        material3.setDescription("Video hướng dẫn phát triển web");
        material3.setCategory("Web Development");
        materials.put(material3.getId(), material3);
        
        // Mock file contents
        String mockContent = "Đây là nội dung mock của file tài liệu";
        fileContents.put(material1.getId(), mockContent.getBytes());
        fileContents.put(material2.getId(), mockContent.getBytes());
        fileContents.put(material3.getId(), mockContent.getBytes());
    }

    // Get all materials
    @GetMapping
    public ResponseEntity<List<MaterialDto>> getAllMaterials(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        System.out.println("Yêu cầu lấy danh sách tài liệu - Category: " + category + ", Search: " + search);
        
        List<MaterialDto> result = new ArrayList<>(materials.values());
        
        // Filter by category
        if (category != null && !category.isEmpty()) {
            result.removeIf(material -> !material.getCategory().equalsIgnoreCase(category));
        }
        
        // Filter by search term
        if (search != null && !search.isEmpty()) {
            result.removeIf(material -> 
                !material.getFileName().toLowerCase().contains(search.toLowerCase()) &&
                !material.getDescription().toLowerCase().contains(search.toLowerCase()));
        }
        
        return ResponseEntity.ok(result);
    }

    // Upload new material
    @PostMapping("/upload")
    public ResponseEntity<MaterialDto> uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String uploadedBy) {
        
        System.out.println("Yêu cầu upload file: " + file.getOriginalFilename());
        
        try {
            Long materialId = materialIdCounter.getAndIncrement();
            
            String fileName = file.getOriginalFilename();
            String fileType = getFileExtension(fileName);
            
            MaterialDto material = new MaterialDto(materialId, fileName, fileType, 
                uploadedBy != null ? uploadedBy : "Unknown User");
            material.setFileSize(file.getSize());
            material.setDescription(description != null ? description : "Uploaded material");
            material.setCategory(category != null ? category : "General");
            
            // Store file content
            fileContents.put(materialId, file.getBytes());
            materials.put(materialId, material);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(material);
            
        } catch (IOException e) {
            System.err.println("Lỗi upload file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Download material
    @GetMapping("/download/{materialId}")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long materialId) {
        System.out.println("Yêu cầu download tài liệu ID: " + materialId);
        
        MaterialDto material = materials.get(materialId);
        if (material == null) {
            return ResponseEntity.notFound().build();
        }
        
        byte[] content = fileContents.get(materialId);
        if (content == null) {
            // Create mock content if not exists
            content = ("Mock content for " + material.getFileName()).getBytes();
        }
        
        ByteArrayResource resource = new ByteArrayResource(content);
        
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(content.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + material.getFileName() + "\"")
                .body(resource);
    }

    // Alternative download endpoint to match frontend service expectation
    @GetMapping("/{materialId}/download")
    public ResponseEntity<Resource> downloadMaterialAlternative(@PathVariable Long materialId) {
        return downloadMaterial(materialId);
    }

    // Get material details
    @GetMapping("/{materialId}")
    public ResponseEntity<MaterialDto> getMaterialDetails(@PathVariable Long materialId) {
        System.out.println("Yêu cầu lấy chi tiết tài liệu ID: " + materialId);
        
        MaterialDto material = materials.get(materialId);
        if (material != null) {
            return ResponseEntity.ok(material);
        }
        return ResponseEntity.notFound().build();
    }

    // Delete material
    @DeleteMapping("/{materialId}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long materialId) {
        System.out.println("Yêu cầu xóa tài liệu ID: " + materialId);
        
        if (materials.containsKey(materialId)) {
            materials.remove(materialId);
            fileContents.remove(materialId);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
    
    // Get materials by course ID
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<MaterialDto>> getMaterialsByCourse(
            @PathVariable Long courseId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search) {
        System.out.println("Yêu cầu lấy danh sách tài liệu cho khóa học ID: " + courseId);
        
        // In a real implementation, you would filter materials by course ID from the database
        // For mock purposes, we'll return all materials with a mock filtering
        List<MaterialDto> result = new ArrayList<>(materials.values());
        
        // Apply some mock filtering based on course ID (in this case, we'll just return all materials)
        // For demo purposes, maybe filter based on some characteristic of the ID
        if (courseId % 2 == 0) {
            // For even course IDs, filter to keep only PDFs
            result.removeIf(material -> !material.getFileType().equalsIgnoreCase("PDF"));
        }
        
        // Filter by category if provided
        if (category != null && !category.isEmpty()) {
            result.removeIf(material -> !material.getCategory().equalsIgnoreCase(category));
        }
        
        // Filter by search term if provided
        if (search != null && !search.isEmpty()) {
            result.removeIf(material -> 
                !material.getFileName().toLowerCase().contains(search.toLowerCase()) &&
                !material.getDescription().toLowerCase().contains(search.toLowerCase()));
        }
        
        return ResponseEntity.ok(result);
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf('.') == -1) {
            return "UNKNOWN";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toUpperCase();
    }
}
