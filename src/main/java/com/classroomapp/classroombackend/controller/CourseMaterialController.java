package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.classroomapp.classroombackend.dto.CourseMaterialDto;
import com.classroomapp.classroombackend.dto.UploadMaterialDto;
import com.classroomapp.classroombackend.service.CourseMaterialService;

@RestController
@RequestMapping("/api/materials")
@CrossOrigin(origins = "*")
public class CourseMaterialController {

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
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long materialId) {
        try {
            CourseMaterialDto material = courseMaterialService.getMaterialById(materialId);
            byte[] fileContent = courseMaterialService.downloadMaterial(materialId);
            
            ByteArrayResource resource = new ByteArrayResource(fileContent);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(material.getFileType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + material.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
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
