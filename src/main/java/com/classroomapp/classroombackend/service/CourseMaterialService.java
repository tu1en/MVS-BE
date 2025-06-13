package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.CourseMaterialDto;
import com.classroomapp.classroombackend.dto.UploadMaterialDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CourseMaterialService {
    
    /**
     * Upload a new course material
     */
    CourseMaterialDto uploadMaterial(UploadMaterialDto uploadDto, MultipartFile file, Long uploadedBy);
    
    /**
     * Get all materials for a classroom
     */
    List<CourseMaterialDto> getMaterialsByClassroom(Long classroomId);
    
    /**
     * Get public materials for a classroom
     */
    List<CourseMaterialDto> getPublicMaterialsByClassroom(Long classroomId);
    
    /**
     * Get material by ID
     */
    CourseMaterialDto getMaterialById(Long materialId);
    
    /**
     * Update material details (title, description, visibility)
     */
    CourseMaterialDto updateMaterial(Long materialId, UploadMaterialDto updateDto);
    
    /**
     * Delete a material
     */
    void deleteMaterial(Long materialId);
    
    /**
     * Download material (increments download count)
     */
    byte[] downloadMaterial(Long materialId);
    
    /**
     * Search materials by title or description
     */
    List<CourseMaterialDto> searchMaterials(Long classroomId, String searchTerm);
    
    /**
     * Get materials by file type
     */
    List<CourseMaterialDto> getMaterialsByFileType(Long classroomId, String fileType);
    
    /**
     * Get materials uploaded by a user
     */
    List<CourseMaterialDto> getMaterialsByUploader(Long uploadedBy);
    
    /**
     * Get total download statistics for a classroom
     */
    Long getTotalDownloadsByClassroom(Long classroomId);
}
