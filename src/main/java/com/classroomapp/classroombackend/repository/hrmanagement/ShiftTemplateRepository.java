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
 * Cung cấp các query methods cho quản lý shift templates
 */
@Repository
public interface ShiftTemplateRepository extends JpaRepository<ShiftTemplate, Long> {

    /**
     * Tìm tất cả shift templates đang hoạt động
     */
    List<ShiftTemplate> findByIsActiveTrueOrderBySortOrderAsc();

    /**
     * Tìm shift template theo code
     */
    Optional<ShiftTemplate> findByTemplateCodeAndIsActiveTrue(String templateCode);

    /**
     * Kiểm tra xem template code đã tồn tại chưa
     */
    boolean existsByTemplateCode(String templateCode);

    /**
     * Kiểm tra xem template code đã tồn tại chưa (exclude current id)
     */
    boolean existsByTemplateCodeAndIdNot(String templateCode, Long id);

    /**
     * Tìm templates theo tên (case insensitive)
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "LOWER(st.templateName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "AND st.isActive = true " +
           "ORDER BY st.sortOrder ASC")
    List<ShiftTemplate> findByTemplateNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name);

    /**
     * Tìm templates theo khoảng thời gian
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "st.startTime >= :startTime AND st.endTime <= :endTime " +
           "AND st.isActive = true " +
           "ORDER BY st.startTime ASC")
    List<ShiftTemplate> findByTimeRange(@Param("startTime") LocalTime startTime, 
                                       @Param("endTime") LocalTime endTime);

    /**
     * Tìm templates có thể làm tăng ca
     */
    List<ShiftTemplate> findByIsOvertimeEligibleTrueAndIsActiveTrueOrderBySortOrderAsc();

    /**
     * Tìm templates theo tổng số giờ
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "st.totalHours >= :minHours AND st.totalHours <= :maxHours " +
           "AND st.isActive = true " +
           "ORDER BY st.totalHours ASC")
    List<ShiftTemplate> findByTotalHoursBetween(@Param("minHours") java.math.BigDecimal minHours,
                                               @Param("maxHours") java.math.BigDecimal maxHours);

    /**
     * Tìm templates có xung đột thời gian
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "st.id != :excludeId AND st.isActive = true AND " +
           "((st.startTime < :endTime AND st.endTime > :startTime))")
    List<ShiftTemplate> findConflictingTemplates(@Param("startTime") LocalTime startTime,
                                                 @Param("endTime") LocalTime endTime,
                                                 @Param("excludeId") Long excludeId);

    /**
     * Search templates với pagination
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
     * Đếm số lượng assignments sử dụng template
     */
    @Query("SELECT COUNT(sa) FROM ShiftAssignment sa WHERE sa.shiftTemplate.id = :templateId")
    long countAssignmentsByTemplate(@Param("templateId") Long templateId);

    /**
     * Tìm templates được sử dụng nhiều nhất
     */
    @Query("SELECT st FROM ShiftTemplate st " +
           "LEFT JOIN st.assignments sa " +
           "WHERE st.isActive = true " +
           "GROUP BY st " +
           "ORDER BY COUNT(sa) DESC")
    List<ShiftTemplate> findMostUsedTemplates(Pageable pageable);

    /**
     * Tìm templates theo người tạo
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "st.createdBy.id = :createdById " +
           "AND (:isActive IS NULL OR st.isActive = :isActive) " +
           "ORDER BY st.createdAt DESC")
    List<ShiftTemplate> findByCreatedBy(@Param("createdById") Long createdById,
                                       @Param("isActive") Boolean isActive);

    /**
     * Tìm templates có break time
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "st.breakStartTime IS NOT NULL AND st.breakEndTime IS NOT NULL " +
           "AND st.isActive = true " +
           "ORDER BY st.breakDurationMinutes DESC")
    List<ShiftTemplate> findTemplatesWithBreak();

    /**
     * Tìm templates theo màu sắc
     */
    List<ShiftTemplate> findByColorCodeAndIsActiveTrueOrderBySortOrderAsc(String colorCode);

    /**
     * Cập nhật trạng thái active
     */
    @Query("UPDATE ShiftTemplate st SET st.isActive = :isActive, st.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE st.id = :id")
    int updateActiveStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    /**
     * Cập nhật sort order
     */
    @Query("UPDATE ShiftTemplate st SET st.sortOrder = :sortOrder, st.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE st.id = :id")
    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);

    /**
     * Tìm templates có thể sử dụng cho ngày cụ thể (business logic)
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
     * Statistics: Đếm templates theo trạng thái
     */
    @Query("SELECT " +
           "SUM(CASE WHEN st.isActive = true THEN 1 ELSE 0 END) as activeCount, " +
           "SUM(CASE WHEN st.isActive = false THEN 1 ELSE 0 END) as inactiveCount, " +
           "COUNT(st) as totalCount " +
           "FROM ShiftTemplate st")
    Object[] getTemplateStatistics();

    /**
     * Tìm templates tương tự (cùng thời gian)
     */
    @Query("SELECT st FROM ShiftTemplate st WHERE " +
           "st.id != :excludeId AND st.isActive = true AND " +
           "st.startTime = :startTime AND st.endTime = :endTime " +
           "ORDER BY st.templateName ASC")
    List<ShiftTemplate> findSimilarTemplates(@Param("startTime") LocalTime startTime,
                                           @Param("endTime") LocalTime endTime,
                                           @Param("excludeId") Long excludeId);
}
