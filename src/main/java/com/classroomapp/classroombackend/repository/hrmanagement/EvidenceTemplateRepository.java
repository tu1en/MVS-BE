package com.classroomapp.classroombackend.repository.hrmanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.hrmanagement.EvidenceTemplate;

/**
 * Repository interface for EvidenceTemplate entity
 */
@Repository
public interface EvidenceTemplateRepository extends JpaRepository<EvidenceTemplate, Long> {
    
    /**
     * Find all active templates ordered by sort order and name
     * @return list of active templates
     */
    List<EvidenceTemplate> findByIsActiveTrueOrderBySortOrderAscTemplateNameAsc();
    
    /**
     * Find templates by category
     * @param category the template category
     * @return list of templates in the category
     */
    List<EvidenceTemplate> findByCategoryAndIsActiveTrueOrderBySortOrderAsc(EvidenceTemplate.TemplateCategory category);
    
    /**
     * Find template by template code
     * @param templateCode the template code
     * @return optional template
     */
    Optional<EvidenceTemplate> findByTemplateCodeAndIsActiveTrue(String templateCode);
    
    /**
     * Find templates by file type
     * @param fileType the file type
     * @return list of templates
     */
    List<EvidenceTemplate> findByFileTypeAndIsActiveTrueOrderByTemplateNameAsc(EvidenceTemplate.FileType fileType);
    
    /**
     * Search templates by name or description
     * @param searchTerm the search term
     * @return list of matching templates
     */
    @Query("SELECT et FROM EvidenceTemplate et " +
           "WHERE et.isActive = true " +
           "AND (LOWER(et.templateName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(et.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY et.sortOrder ASC, et.templateName ASC")
    List<EvidenceTemplate> searchTemplates(@Param("searchTerm") String searchTerm);
    
    /**
     * Find templates by multiple categories
     * @param categories list of categories
     * @return list of templates
     */
    List<EvidenceTemplate> findByCategoryInAndIsActiveTrueOrderBySortOrderAsc(List<EvidenceTemplate.TemplateCategory> categories);
    
    /**
     * Check if template code exists
     * @param templateCode the template code
     * @return true if exists
     */
    boolean existsByTemplateCode(String templateCode);
    
    /**
     * Find templates created by specific user
     * @param createdBy the user ID
     * @return list of templates
     */
    List<EvidenceTemplate> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
    
    /**
     * Get template statistics by category
     * @return category statistics
     */
    @Query("SELECT et.category as category, COUNT(et) as count " +
           "FROM EvidenceTemplate et " +
           "WHERE et.isActive = true " +
           "GROUP BY et.category " +
           "ORDER BY COUNT(et) DESC")
    List<Object[]> getTemplateStatisticsByCategory();
    
    /**
     * Get template statistics by file type
     * @return file type statistics
     */
    @Query("SELECT et.fileType as fileType, COUNT(et) as count " +
           "FROM EvidenceTemplate et " +
           "WHERE et.isActive = true " +
           "GROUP BY et.fileType " +
           "ORDER BY COUNT(et) DESC")
    List<Object[]> getTemplateStatisticsByFileType();
    
    /**
     * Find most popular templates (you could track download counts)
     * @param limit the number of templates to return
     * @return list of popular templates
     */
    @Query("SELECT et FROM EvidenceTemplate et " +
           "WHERE et.isActive = true " +
           "ORDER BY et.sortOrder ASC " +
           "LIMIT :limit")
    List<EvidenceTemplate> findMostPopularTemplates(@Param("limit") int limit);
    
    /**
     * Find templates that need updates (old versions)
     * @return list of templates needing updates
     */
 @Query("SELECT et FROM EvidenceTemplate et " +
       "WHERE et.isActive = true " +
       "AND et.updatedAt < DATEADD(MONTH, -6, CURRENT_DATE) " +
       "ORDER BY et.updatedAt ASC")
    List<EvidenceTemplate> findTemplatesNeedingUpdates();
    
    /**
     * Count active templates
     * @return number of active templates
     */
    long countByIsActiveTrue();
    
    /**
     * Count templates by category
     * @param category the category
     * @return number of templates in category
     */
    long countByCategoryAndIsActiveTrue(EvidenceTemplate.TemplateCategory category);
}