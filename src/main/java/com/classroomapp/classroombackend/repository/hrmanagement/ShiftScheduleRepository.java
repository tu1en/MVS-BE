package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho ShiftSchedule entity
 * Cung cấp các query methods cho quản lý shift schedules
 */
@Repository
public interface ShiftScheduleRepository extends JpaRepository<ShiftSchedule, Long> {

    /**
     * Tìm schedules theo trạng thái
     */
    List<ShiftSchedule> findByStatusOrderByCreatedAtDesc(ShiftSchedule.ScheduleStatus status);

    /**
     * Tìm schedules theo loại
     */
    List<ShiftSchedule> findByScheduleTypeOrderByStartDateDesc(ShiftSchedule.ScheduleType scheduleType);

    /**
     * Tìm schedules đang hoạt động (published và trong thời gian)
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' AND " +
           "ss.startDate <= :currentDate AND ss.endDate >= :currentDate " +
           "ORDER BY ss.startDate ASC")
    List<ShiftSchedule> findActiveSchedules(@Param("currentDate") LocalDate currentDate);

    /**
     * Tìm schedule đang hoạt động cho ngày cụ thể
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' AND " +
           "ss.startDate <= :date AND ss.endDate >= :date " +
           "ORDER BY ss.createdAt DESC")
    Optional<ShiftSchedule> findActiveScheduleForDate(@Param("date") LocalDate date);

    /**
     * Tìm schedules theo người tạo
     */
    List<ShiftSchedule> findByCreatedByIdOrderByCreatedAtDesc(Long createdById);

    /**
     * Tìm schedules theo người xuất bản
     */
    List<ShiftSchedule> findByPublishedByIdOrderByPublishedAtDesc(Long publishedById);

    /**
     * Tìm schedules trong khoảng thời gian
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.startDate <= :endDate AND ss.endDate >= :startDate " +
           "ORDER BY ss.startDate ASC")
    List<ShiftSchedule> findByDateRange(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

    /**
     * Tìm schedules có overlap với khoảng thời gian
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status IN ('DRAFT', 'PUBLISHED') AND " +
           "ss.startDate <= :endDate AND ss.endDate >= :startDate AND " +
           "(:excludeId IS NULL OR ss.id != :excludeId) " +
           "ORDER BY ss.startDate ASC")
    List<ShiftSchedule> findOverlappingSchedules(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("excludeId") Long excludeId);

    /**
     * Search schedules với filters
     */
    @Query("SELECT ss FROM ShiftSchedule ss " +
           "LEFT JOIN ss.createdBy cb " +
           "LEFT JOIN ss.publishedBy pb WHERE " +
           "(:status IS NULL OR ss.status = :status) AND " +
           "(:scheduleType IS NULL OR ss.scheduleType = :scheduleType) AND " +
           "(:createdById IS NULL OR ss.createdBy.id = :createdById) AND " +
           "(:startDate IS NULL OR ss.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR ss.endDate <= :endDate) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(ss.scheduleName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(ss.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(cb.fullName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY ss.startDate DESC, ss.createdAt DESC")
    Page<ShiftSchedule> searchSchedules(@Param("status") ShiftSchedule.ScheduleStatus status,
                                       @Param("scheduleType") ShiftSchedule.ScheduleType scheduleType,
                                       @Param("createdById") Long createdById,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate,
                                       @Param("search") String search,
                                       Pageable pageable);

    /**
     * Tìm schedules cần archive (đã kết thúc > 30 ngày)
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' AND " +
           "ss.endDate < :cutoffDate " +
           "ORDER BY ss.endDate ASC")
    List<ShiftSchedule> findSchedulesNeedingArchive(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Tìm schedules sắp bắt đầu (để gửi notification)
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' AND " +
           "ss.startDate BETWEEN :today AND :notificationDate " +
           "ORDER BY ss.startDate ASC")
    List<ShiftSchedule> findUpcomingSchedules(@Param("today") LocalDate today,
                                             @Param("notificationDate") LocalDate notificationDate);

    /**
     * Đếm schedules theo trạng thái
     */
    @Query("SELECT ss.status, COUNT(ss) FROM ShiftSchedule ss " +
           "GROUP BY ss.status")
    List<Object[]> countByStatus();

    /**
     * Đếm schedules theo loại
     */
    @Query("SELECT ss.scheduleType, COUNT(ss) FROM ShiftSchedule ss " +
           "GROUP BY ss.scheduleType")
    List<Object[]> countByType();

    /**
     * Tính tổng assignments trong schedules
     */
    @Query("SELECT " +
           "COUNT(ss) as totalSchedules, " +
           "SUM(ss.totalAssignments) as totalAssignments, " +
           "AVG(ss.totalAssignments) as avgAssignments " +
           "FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' AND " +
           "ss.startDate BETWEEN :startDate AND :endDate")
    Object[] getScheduleStatistics(@Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * Tìm schedules có nhiều assignments nhất
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' " +
           "ORDER BY ss.totalAssignments DESC")
    List<ShiftSchedule> findSchedulesWithMostAssignments(Pageable pageable);

    /**
     * Tìm schedules theo tháng/năm
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "YEAR(ss.startDate) = :year AND " +
           "(:month IS NULL OR MONTH(ss.startDate) = :month) " +
           "ORDER BY ss.startDate ASC")
    List<ShiftSchedule> findByYearAndMonth(@Param("year") int year,
                                          @Param("month") Integer month);

    /**
     * Kiểm tra xem có schedule nào đang hoạt động không
     */
    @Query("SELECT COUNT(ss) > 0 FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' AND " +
           "ss.startDate <= :currentDate AND ss.endDate >= :currentDate")
    boolean hasActiveSchedule(@Param("currentDate") LocalDate currentDate);

    /**
     * Tìm schedule gần nhất sẽ bắt đầu
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status = 'PUBLISHED' AND " +
           "ss.startDate > :currentDate " +
           "ORDER BY ss.startDate ASC")
    Optional<ShiftSchedule> findNextSchedule(@Param("currentDate") LocalDate currentDate);

    /**
     * Cập nhật số lượng assignments
     */
    @Query("UPDATE ShiftSchedule ss SET ss.totalAssignments = " +
           "(SELECT COUNT(sa) FROM ShiftAssignment sa WHERE sa.schedule.id = ss.id), " +
           "ss.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE ss.id = :scheduleId")
    int updateAssignmentCount(@Param("scheduleId") Long scheduleId);

    /**
     * Cập nhật trạng thái schedule
     */
    @Query("UPDATE ShiftSchedule ss SET ss.status = :status, ss.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE ss.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") ShiftSchedule.ScheduleStatus status);

    /**
     * Archive schedules cũ
     */
    @Query("UPDATE ShiftSchedule ss SET ss.status = 'ARCHIVED', " +
           "ss.archivedAt = CURRENT_TIMESTAMP, ss.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE ss.status = 'PUBLISHED' AND ss.endDate < :cutoffDate")
    int archiveOldSchedules(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * Tìm schedules draft cũ (chưa publish > 7 ngày)
     */
    @Query("SELECT ss FROM ShiftSchedule ss WHERE " +
           "ss.status = 'DRAFT' AND " +
           "ss.createdAt < :cutoffDate " +
           "ORDER BY ss.createdAt ASC")
    List<ShiftSchedule> findOldDraftSchedules(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Xóa schedules draft cũ
     */
    @Query("DELETE FROM ShiftSchedule ss WHERE " +
           "ss.status = 'DRAFT' AND " +
           "ss.totalAssignments = 0 AND " +
           "ss.createdAt < :cutoffDate")
    int deleteOldEmptyDrafts(@Param("cutoffDate") LocalDateTime cutoffDate);
}
