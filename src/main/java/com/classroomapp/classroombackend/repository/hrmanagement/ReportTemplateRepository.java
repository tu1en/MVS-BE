package com.classroomapp.classroombackend.repository.hrmanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.hrmanagement.ReportTemplate;

/**
 * Repository for ReportTemplate entity
 */
@Repository
public interface ReportTemplateRepository extends JpaRepository<ReportTemplate, Long> {
    
    /**
     * Find template by code
     */
    Optional<ReportTemplate> findByTemplateCode(String templateCode);
    
    /**
     * Find active templates
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.isActive = true " +
           "ORDER BY r.sortOrder ASC, r.templateName ASC")
    Page<ReportTemplate> findAllActive(Pageable pageable);
    
    /**
     * Find templates by category
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.reportCategory = :category " +
           "AND r.isActive = true " +
           "ORDER BY r.sortOrder ASC, r.templateName ASC")
    Page<ReportTemplate> findByCategory(@Param("category") ReportTemplate.ReportCategory category, 
                                       Pageable pageable);
    
    /**
     * Find templates by type
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.reportType = :type " +
           "AND r.isActive = true " +
           "ORDER BY r.sortOrder ASC, r.templateName ASC")
    Page<ReportTemplate> findByType(@Param("type") ReportTemplate.ReportType type, 
                                   Pageable pageable);
    
    /**
     * Find public templates
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.isPublic = true " +
           "AND r.isActive = true " +
           "ORDER BY r.sortOrder ASC, r.templateName ASC")
    List<ReportTemplate> findPublicTemplates();
    
    /**
     * Find system templates
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.isSystemTemplate = true " +
           "AND r.isActive = true " +
           "ORDER BY r.sortOrder ASC, r.templateName ASC")
    List<ReportTemplate> findSystemTemplates();
    
    /**
     * Find templates created by user
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.createdBy = :userId " +
           "ORDER BY r.createdAt DESC")
    Page<ReportTemplate> findByCreatedBy(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find accessible templates for user
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.isActive = true " +
           "AND (r.isPublic = true " +
           "OR r.createdBy = :userId " +
           "OR (r.isSystemTemplate = true AND :userRole IN ('MANAGER', 'ADMIN'))) " +
           "ORDER BY r.sortOrder ASC, r.templateName ASC")
    Page<ReportTemplate> findAccessibleByUser(@Param("userId") Long userId, 
                                             @Param("userRole") String userRole, 
                                             Pageable pageable);
    
    /**
     * Search templates by name or description
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.isActive = true " +
           "AND (LOWER(r.templateName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(r.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY r.sortOrder ASC, r.templateName ASC")
    Page<ReportTemplate> searchByNameOrDescription(@Param("searchTerm") String searchTerm, 
                                                  Pageable pageable);
    
    /**
     * Find templates by category and type
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.reportCategory = :category " +
           "AND r.reportType = :type " +
           "AND r.isActive = true " +
           "ORDER BY r.sortOrder ASC, r.templateName ASC")
    List<ReportTemplate> findByCategoryAndType(@Param("category") ReportTemplate.ReportCategory category,
                                              @Param("type") ReportTemplate.ReportType type);
    
    /**
     * Count templates by category
     */
    @Query("SELECT r.reportCategory, COUNT(r) FROM ReportTemplate r " +
           "WHERE r.isActive = true " +
           "GROUP BY r.reportCategory " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> countByCategory();
    
    /**
     * Count templates by type
     */
    @Query("SELECT r.reportType, COUNT(r) FROM ReportTemplate r " +
           "WHERE r.isActive = true " +
           "GROUP BY r.reportType " +
           "ORDER BY COUNT(r) DESC")
    List<Object[]> countByType();
    
    /**
     * Find most used templates (based on generated reports count)
     */
    @Query("SELECT r, COUNT(g) as usageCount FROM ReportTemplate r " +
           "LEFT JOIN GeneratedReport g ON g.template = r " +
           "WHERE r.isActive = true " +
           "GROUP BY r " +
           "ORDER BY COUNT(g) DESC")
    List<Object[]> findMostUsedTemplates(Pageable pageable);
    
    /**
     * Find templates with chart configuration
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.isActive = true " +
           "AND r.chartConfig IS NOT NULL " +
           "AND r.chartConfig != '' " +
           "ORDER BY r.templateName ASC")
    List<ReportTemplate> findWithChartConfig();
    
    /**
     * Find templates with filters
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.isActive = true " +
           "AND r.filtersConfig IS NOT NULL " +
           "AND r.filtersConfig != '' " +
           "ORDER BY r.templateName ASC")
    List<ReportTemplate> findWithFilters();
    
    /**
     * Find templates with parameters
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.isActive = true " +
           "AND r.parameters IS NOT NULL " +
           "AND r.parameters != '' " +
           "ORDER BY r.templateName ASC")
    List<ReportTemplate> findWithParameters();
    
    /**
     * Check if template code exists
     */
    @Query("SELECT COUNT(r) > 0 FROM ReportTemplate r WHERE r.templateCode = :templateCode " +
           "AND (:excludeId IS NULL OR r.id != :excludeId)")
    boolean existsByTemplateCode(@Param("templateCode") String templateCode, 
                                @Param("excludeId") Long excludeId);
    
    /**
     * Find templates updated recently
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.updatedAt >= :sinceDate " +
           "ORDER BY r.updatedAt DESC")
    List<ReportTemplate> findUpdatedSince(@Param("sinceDate") java.time.LocalDateTime sinceDate);
    
    /**
     * Find templates by complexity (estimated)
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.isActive = true " +
           "AND LENGTH(r.sqlQuery) > :minQueryLength " +
           "ORDER BY LENGTH(r.sqlQuery) DESC")
    List<ReportTemplate> findComplexTemplates(@Param("minQueryLength") int minQueryLength);
    
    /**
     * Get template usage statistics
     */
    @Query("SELECT " +
           "COUNT(DISTINCT r) as totalTemplates, " +
           "COUNT(DISTINCT CASE WHEN r.isSystemTemplate = true THEN r END) as systemTemplates, " +
           "COUNT(DISTINCT CASE WHEN r.isPublic = true THEN r END) as publicTemplates, " +
           "COUNT(DISTINCT CASE WHEN r.reportType = 'CHART' THEN r END) as chartTemplates, " +
           "COUNT(DISTINCT CASE WHEN r.reportType = 'DASHBOARD' THEN r END) as dashboardTemplates " +
           "FROM ReportTemplate r WHERE r.isActive = true")
    Object[] getTemplateStatistics();
    
    /**
     * Find templates by creator role ID
     */
    @Query("SELECT r FROM ReportTemplate r " +
           "JOIN User u ON r.createdBy = u.id " +
           "WHERE u.roleId = :roleId " +
           "AND r.isActive = true " +
           "ORDER BY r.createdAt DESC")
    Page<ReportTemplate> findByCreatorRole(@Param("roleId") Integer roleId, Pageable pageable);
    
    /**
     * Find orphaned templates (creator no longer exists)
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.createdBy NOT IN " +
           "(SELECT u.id FROM User u) " +
           "AND r.isSystemTemplate = false")
    List<ReportTemplate> findOrphanedTemplates();
    
    /**
     * Find templates that need migration (old format)
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.isActive = true " +
           "AND (r.columnsConfig IS NULL OR r.columnsConfig = '' " +
           "OR r.parameters IS NULL OR r.parameters = '')")
    List<ReportTemplate> findTemplatesNeedingMigration();
    
    /**
     * Get next sort order for category
     */
    @Query("SELECT COALESCE(MAX(r.sortOrder), 0) + 1 FROM ReportTemplate r " +
           "WHERE r.reportCategory = :category")
    Integer getNextSortOrderForCategory(@Param("category") ReportTemplate.ReportCategory category);
    
    /**
     * Find similar templates (by name similarity)
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.isActive = true " +
           "AND r.id != :excludeId " +
           "AND LOWER(r.templateName) LIKE LOWER(CONCAT('%', :namePart, '%')) " +
           "ORDER BY r.templateName ASC")
    List<ReportTemplate> findSimilarTemplates(@Param("namePart") String namePart, 
                                             @Param("excludeId") Long excludeId);
    
    /**
     * Find templates by SQL pattern (for security audit)
     */
    @Query("SELECT r FROM ReportTemplate r WHERE r.isActive = true " +
           "AND LOWER(r.sqlQuery) LIKE LOWER(CONCAT('%', :sqlPattern, '%')) " +
           "ORDER BY r.templateName ASC")
    List<ReportTemplate> findBySqlPattern(@Param("sqlPattern") String sqlPattern);
}
