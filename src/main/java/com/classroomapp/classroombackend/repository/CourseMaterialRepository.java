package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long> {
    
    /**
     * Find all materials for a specific classroom
     */
    List<CourseMaterial> findByClassroomIdOrderByUploadDateDesc(Long classroomId);
    
    /**
     * Find public materials for a classroom
     */
    List<CourseMaterial> findByClassroomIdAndIsPublicTrueOrderByUploadDateDesc(Long classroomId);
    
    /**
     * Find materials uploaded by a specific user
     */
    List<CourseMaterial> findByUploadedByOrderByUploadDateDesc(Long uploadedBy);
    
    /**
     * Find materials by file type
     */
    List<CourseMaterial> findByClassroomIdAndFileTypeContainingIgnoreCaseOrderByUploadDateDesc(
            Long classroomId, String fileType);
    
    /**
     * Search materials by title or description
     */
    @Query("SELECT cm FROM CourseMaterial cm WHERE cm.classroomId = :classroomId " +
           "AND (LOWER(cm.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(cm.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY cm.uploadDate DESC")
    List<CourseMaterial> searchMaterials(@Param("classroomId") Long classroomId, 
                                       @Param("searchTerm") String searchTerm);
    
    /**
     * Get total download count for a classroom
     */
    @Query("SELECT SUM(cm.downloadCount) FROM CourseMaterial cm WHERE cm.classroomId = :classroomId")
    Long getTotalDownloadsByClassroom(@Param("classroomId") Long classroomId);
}
