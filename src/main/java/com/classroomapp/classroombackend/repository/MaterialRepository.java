package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.entity.Material;
import com.classroomapp.classroombackend.entity.Material.MaterialType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    
    List<Material> findByLessonTemplateIdOrderBySortOrderAsc(Long lessonTemplateId);
    
    List<Material> findByClassLessonIdOrderBySortOrderAsc(Long classLessonId);
    
    List<Material> findByMaterialTypeOrderByCreatedAtDesc(MaterialType materialType);
    
    List<Material> findByLessonTemplateIdAndMaterialTypeOrderBySortOrderAsc(Long lessonTemplateId, MaterialType materialType);
    
    List<Material> findByClassLessonIdAndMaterialTypeOrderBySortOrderAsc(Long classLessonId, MaterialType materialType);
    
    List<Material> findByIsRequiredTrue();
    
    List<Material> findByUploadedByIdOrderByCreatedAtDesc(Long uploadedById);
    
    @Query("SELECT m FROM Material m WHERE m.title LIKE CONCAT('%', :keyword, '%') OR m.description LIKE CONCAT('%', :keyword, '%')")
    List<Material> searchByTitleOrDescription(@Param("keyword") String keyword);
    
    // Find materials for a specific course template
    @Query("SELECT m FROM Material m WHERE m.lessonTemplate.courseTemplate.id = :courseTemplateId")
    List<Material> findByCourseTemplateId(@Param("courseTemplateId") Long courseTemplateId);
    
    // Find materials for a specific class
    @Query("SELECT m FROM Material m WHERE m.classLesson.classEntity.id = :classId")
    List<Material> findByClassId(@Param("classId") Long classId);
    
    @Query("SELECT m FROM Material m WHERE m.title LIKE %:title% ORDER BY m.createdAt DESC")
    List<Material> findByTitleContainingIgnoreCase(@Param("title") String title);
    
    List<Material> findByLessonTemplateIdIn(List<Long> lessonTemplateIds);
    
    List<Material> findByClassLessonIdIn(List<Long> classLessonIds);
    
    List<Material> findByMimeTypeLike(String mimeTypePattern);
    
    @Query("SELECT m FROM Material m WHERE m.filePath LIKE %:filePathPattern")
    List<Material> findByFilePathContaining(@Param("filePathPattern") String filePathPattern);
    
    // Find materials by file extension
    @Query("SELECT m FROM Material m WHERE lower(m.filePath) LIKE lower(concat('%', :extension, '%'))")
    List<Material> findByFileExtension(@Param("extension") String extension);
    
    // Find required materials for a course template
    @Query("SELECT m FROM Material m WHERE m.lessonTemplate.courseTemplate.id = :courseTemplateId AND m.isRequired = true")
    List<Material> findRequiredMaterialsByCourseTemplateId(@Param("courseTemplateId") Long courseTemplateId);
    
    void deleteByLessonTemplateId(Long lessonTemplateId);
    
    void deleteByClassLessonId(Long classLessonId);
}