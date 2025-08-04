package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho ShiftTemplate entity
 * Cung cáº¥p cÃ¡c query methods cho quáº£n lÃ½ shift templates
 */
@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {

    /**
     * TÃ¬m táº¥t cáº£ shift templates Ä‘ang hoáº¡t Ä‘á»™ng
     */
    List<ShiftTemplate> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * TÃ¬m shift template theo code
     */
    Optional<ShiftTemplate> findByTemplateCodeAndIsActiveTrue(String templateCode);

    /**
     * Kiá»ƒm tra xem template code Ä‘Ã£ tá»“n táº¡i chÆ°a
     */
    boolean existsByTemplateCode(String templateCode);

    /**
     * Kiá»ƒm tra xem template code Ä‘Ã£ tá»“n táº¡i chÆ°a (exclude current id)
     */
    boolean existsByTemplateCodeAndIdNot(String templateCode, Long id);

    /**
     * TÃ¬m templates theo tÃªn (case insensitive)
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "LOWER(st.templateName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "AND st.isActive = true " +
           "ORDER BY st.sortOrder ASC")
    List<ShiftTemplate> findByTemplateNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name);

    /**
     * TÃ¬m templates theo khoáº£ng thá»i gian
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "st.startTime >= :startTime AND st.endTime <= :endTime " +
           "AND st.isActive = true " +
           "ORDER BY st.startTime ASC")
    List<ShiftTemplate> findByTimeRange(@Param("startTime") LocalTime startTime, 
                                       @Param("endTime") LocalTime endTime);

    /**
     * TÃ¬m templates cÃ³ thá»ƒ lÃ m tÄƒng ca
     */
    List<ShiftTemplate> findByIsOvertimeEligibleTrueAndIsActiveTrueOrderBySortOrderAsc();

    /**
     * TÃ¬m templates theo tá»•ng sá»‘ giá»
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "st.totalHours >= :minHours AND st.totalHours <= :maxHours " +
           "AND st.isActive = true " +
           "ORDER BY st.totalHours ASC")
    List<ShiftTemplate> findByTotalHoursBetween(@Param("minHours") java.math.BigDecimal minHours,
                                               @Param("maxHours") java.math.BigDecimal maxHours);

    /**
     * TÃ¬m templates cÃ³ xung Ä‘á»™t thá»i gian
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "st.id != :excludeId AND st.isActive = true AND " +
           "((st.startTime < :endTime AND st.endTime > :startTime))")
    List<ShiftTemplate> findConflictingTemplates(@Param("startTime") LocalTime startTime,
                                                 @Param("endTime") LocalTime endTime,
                                                 @Param("excludeId") Long excludeId);

    /**
     * Search templates vá»›i pagination
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(st.templateName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(st.templateCode) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(st.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:isActive IS NULL OR st.isActive = :isActive) " +
           "ORDER BY st.sortOrder ASC, st.templateName ASC")
    Page<ShiftTemplate> searchTemplates(@Param("search") String search,
                                       @Param("isActive") Boolean isActive,
                                       Pageable pageable);

    /**
     * Äáº¿m sá»‘ lÆ°á»£ng assignments sá»­ dá»¥ng template
     */
    @Query("SELECT COUNT(sa) FROM ShiftAssignment sa WHERE sa.shiftTemplate.id = :templateId")
    long countAssignmentsByTemplate(@Param("templateId") Long templateId);

    /**
     * TÃ¬m templates Ä‘Æ°á»£c sá»­ dá»¥ng nhiá»u nháº¥t
     */
    @Query("SELECT st FROM ShiftTemplate st " +
           "LEFT JOIN st.assignments sa " +
           "WHERE st.isActive = true " +
           "GROUP BY st " +
           "ORDER BY COUNT(sa) DESC")
    List<ShiftTemplate> findMostUsedTemplates(Pageable pageable);

    /**
     * TÃ¬m templates theo ngÆ°á»i táº¡o
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "st.createdBy.id = :createdById " +
           "AND (:isActive IS NULL OR st.isActive = :isActive) " +
           "ORDER BY st.createdAt DESC")
    List<ShiftTemplate> findByCreatedBy(@Param("createdById") Long createdById,
                                       @Param("isActive") Boolean isActive);

    /**
     * TÃ¬m templates cÃ³ break time
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "st.breakStartTime IS NOT NULL AND st.breakEndTime IS NOT NULL " +
           "AND st.isActive = true " +
           "ORDER BY st.breakDurationMinutes DESC")
    List<ShiftTemplate> findTemplatesWithBreak();

    /**
     * TÃ¬m templates theo mÃ u sáº¯c
     */
    List<ShiftTemplate> findByColorCodeAndIsActiveTrueOrderBySortOrderAsc(String colorCode);

    /**
     * Cáº­p nháº­t tráº¡ng thÃ¡i active
     */
    @Query("UPDATE ShiftTemplate st SET st.isActive = :isActive, st.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE st.id = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    /**
     * Cáº­p nháº­t sort order
     */
    @Query("UPDATE ShiftTemplate st SET st.sortOrder = :sortOrder, st.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE st.id = :id")
    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);

    /**
     * TÃ¬m templates cÃ³ thá»ƒ sá»­ dá»¥ng cho ngÃ y cá»¥ thá»ƒ (business logic)
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "st.isActive = true AND " +
           "NOT EXISTS (SELECT 1 FROM ShiftAssignment sa WHERE " +
           "sa.shiftTemplate = st AND sa.assignmentDate = :date AND " +
           "sa.employee.id = :employeeId AND sa.status != 'CANCELLED') " +
           "ORDER BY st.sortOrder ASC")
    List<ShiftTemplate> findAvailableTemplatesForEmployeeAndDate(@Param("employeeId") Long employeeId,
                                                                @Param("date") java.time.LocalDate date);

    /**
     * Statistics: Äáº¿m templates theo tráº¡ng thÃ¡i
     */
    @Query("SELECT " +
           "SUM(CASE WHEN st.isActive = true THEN 1 ELSE 0 END) as activeCount, " +
           "SUM(CASE WHEN st.isActive = false THEN 1 ELSE 0 END) as inactiveCount, " +
           "COUNT(st) as totalCount " +
           "FROM ShiftTemplate st")
    Object[] getTemplateStatistics();

    /**
     * TÃ¬m templates tÆ°Æ¡ng tá»± (cÃ¹ng thá»i gian)
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "st.id != :excludeId AND st.isActive = true AND " +
           "st.startTime = :startTime AND st.endTime = :endTime " +
           "ORDER BY st.templateName ASC")
    List<ShiftTemplate> findSimilarTemplates(@Param("startTime") LocalTime startTime,
                                           @Param("endTime") LocalTime endTime,
                                           @Param("excludeId") Long excludeId);
}
