package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho ShiftAssignment entity
 * Cung cấp các query methods cho quản lý shift assignments
 */
@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {

    /**
     * Tìm assignments theo employee và ngày
     */
    List<ShiftAssignment> findByEmployeeIdAndAssignmentDateOrderByPlannedStartTimeAsc(Long employeeId, LocalDate date);

    /**
     * Tìm assignments theo employee trong khoảng thời gian
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.employee.id = :employeeId AND " +
           "sa.assignmentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sa.assignmentDate ASC, sa.plannedStartTime ASC")
    List<ShiftAssignment> findByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * Tìm assignments theo ngày
     */
    List<ShiftAssignment> findByAssignmentDateOrderByPlannedStartTimeAsc(LocalDate date);

    /**
     * Tìm assignments theo ngày và trạng thái
     */
    List<ShiftAssignment> findByAssignmentDateAndStatusOrderByPlannedStartTimeAsc(LocalDate date, 
                                                                                  ShiftAssignment.AssignmentStatus status);

    /**
     * Tìm assignments theo schedule
     */
    List<ShiftAssignment> findByScheduleIdOrderByAssignmentDateAscPlannedStartTimeAsc(Long scheduleId);

    /**
     * Tìm assignments theo shift template
     */
    List<ShiftAssignment> findByShiftTemplateIdOrderByAssignmentDateDesc(Long shiftTemplateId);

    /**
     * Kiểm tra xung đột thời gian cho employee
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.employee.id = :employeeId AND " +
           "sa.assignmentDate = :date AND " +
           "sa.status NOT IN ('CANCELLED') AND " +
           "(:excludeId IS NULL OR sa.id != :excludeId) AND " +
           "((sa.plannedStartTime < :endTime AND sa.plannedEndTime > :startTime))")
    List<ShiftAssignment> findConflictingAssignments(@Param("employeeId") Long employeeId,
                                                     @Param("date") LocalDate date,
                                                     @Param("startTime") LocalTime startTime,
                                                     @Param("endTime") LocalTime endTime,
                                                     @Param("excludeId") Long excludeId);

    /**
     * Kiểm tra minimum rest time violations
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.employee.id = :employeeId AND " +
           "sa.status NOT IN ('CANCELLED') AND " +
           "(:excludeId IS NULL OR sa.id != :excludeId) AND " +
           "(" +
           "  (sa.assignmentDate = :previousDate AND " +
           "   FUNCTION('DATEDIFF', HOUR, sa.plannedEndTime, :startTime) < 8) OR " +
           "  (sa.assignmentDate = :nextDate AND " +
           "   FUNCTION('DATEDIFF', HOUR, :endTime, sa.plannedStartTime) < 8)" +
           ")")
    List<ShiftAssignment> findRestTimeViolations(@Param("employeeId") Long employeeId,
                                                 @Param("previousDate") LocalDate previousDate,
                                                 @Param("nextDate") LocalDate nextDate,
                                                 @Param("startTime") LocalTime startTime,
                                                 @Param("endTime") LocalTime endTime,
                                                 @Param("excludeId") Long excludeId);

    /**
     * Tìm assignments cần check-in (scheduled và trong thời gian)
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.status = 'SCHEDULED' AND " +
           "sa.assignmentDate = :today AND " +
           "sa.plannedStartTime <= :currentTime AND " +
           "sa.plannedStartTime >= :startWindow " +
           "ORDER BY sa.plannedStartTime ASC")
    List<ShiftAssignment> findPendingCheckIns(@Param("today") LocalDate today,
                                              @Param("currentTime") LocalTime currentTime,
                                              @Param("startWindow") LocalTime startWindow);

    /**
     * Tìm assignments cần check-out (in progress và gần hết giờ)
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.status = 'IN_PROGRESS' AND " +
           "sa.assignmentDate = :today AND " +
           "sa.plannedEndTime <= :endWindow " +
           "ORDER BY sa.plannedEndTime ASC")
    List<ShiftAssignment> findPendingCheckOuts(@Param("today") LocalDate today,
                                               @Param("endWindow") LocalTime endWindow);

    /**
     * Tìm assignments theo tuần
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.assignmentDate BETWEEN :weekStart AND :weekEnd " +
           "AND (:employeeId IS NULL OR sa.employee.id = :employeeId) " +
           "ORDER BY sa.assignmentDate ASC, sa.plannedStartTime ASC")
    List<ShiftAssignment> findByWeek(@Param("weekStart") LocalDate weekStart,
                                     @Param("weekEnd") LocalDate weekEnd,
                                     @Param("employeeId") Long employeeId);

    /**
     * Tìm assignments theo tháng
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "YEAR(sa.assignmentDate) = :year AND MONTH(sa.assignmentDate) = :month " +
           "AND (:employeeId IS NULL OR sa.employee.id = :employeeId) " +
           "ORDER BY sa.assignmentDate ASC, sa.plannedStartTime ASC")
    List<ShiftAssignment> findByMonth(@Param("year") int year,
                                      @Param("month") int month,
                                      @Param("employeeId") Long employeeId);

    /**
     * Search assignments với filters
     */
    @Query("SELECT sa FROM ShiftAssignment sa " +
           "JOIN sa.employee e " +
           "JOIN sa.shiftTemplate st WHERE " +
           "(:employeeId IS NULL OR sa.employee.id = :employeeId) AND " +
           "(:startDate IS NULL OR sa.assignmentDate >= :startDate) AND " +
           "(:endDate IS NULL OR sa.assignmentDate <= :endDate) AND " +
           "(:status IS NULL OR sa.status = :status) AND " +
           "(:attendanceStatus IS NULL OR sa.attendanceStatus = :attendanceStatus) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(e.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(st.templateName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY sa.assignmentDate DESC, sa.plannedStartTime ASC")
    Page<ShiftAssignment> searchAssignments(@Param("employeeId") Long employeeId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           @Param("status") ShiftAssignment.AssignmentStatus status,
                                           @Param("attendanceStatus") ShiftAssignment.AttendanceStatus attendanceStatus,
                                           @Param("search") String search,
                                           Pageable pageable);

    /**
     * Tính tổng giờ làm việc của employee trong khoảng thời gian
     */
    @Query("SELECT " +
           "COALESCE(SUM(sa.plannedHours), 0) as totalPlannedHours, " +
           "COALESCE(SUM(sa.actualHours), 0) as totalActualHours, " +
           "COALESCE(SUM(sa.overtimeHours), 0) as totalOvertimeHours " +
           "FROM ShiftAssignment sa WHERE " +
           "sa.employee.id = :employeeId AND " +
           "sa.assignmentDate BETWEEN :startDate AND :endDate AND " +
           "sa.status IN ('COMPLETED', 'IN_PROGRESS')")
    Object[] calculateWorkingHours(@Param("employeeId") Long employeeId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * Đếm assignments theo trạng thái
     */
    @Query("SELECT sa.status, COUNT(sa) FROM ShiftAssignment sa WHERE " +
           "sa.assignmentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY sa.status")
    List<Object[]> countByStatusInDateRange(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * Tìm employees có nhiều assignments nhất
     */
    @Query("SELECT sa.employee, COUNT(sa) as assignmentCount FROM ShiftAssignment sa WHERE " +
           "sa.assignmentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY sa.employee " +
           "ORDER BY assignmentCount DESC")
    List<Object[]> findTopEmployeesByAssignments(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 Pageable pageable);

    /**
     * Tìm assignments có overtime
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.overtimeHours > 0 AND " +
           "sa.assignmentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sa.overtimeHours DESC")
    List<ShiftAssignment> findOvertimeAssignments(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * Tìm assignments bị trễ hoặc về sớm
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.attendanceStatus IN ('LATE', 'EARLY_LEAVE') AND " +
           "sa.assignmentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sa.assignmentDate DESC")
    List<ShiftAssignment> findAttendanceIssues(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * Cập nhật trạng thái assignment
     */
    @Query("UPDATE ShiftAssignment sa SET sa.status = :status, sa.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE sa.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") ShiftAssignment.AssignmentStatus status);

    /**
     * Tìm assignments có thể swap
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.employee.id != :employeeId AND " +
           "sa.assignmentDate = :date AND " +
           "sa.status = 'SCHEDULED' AND " +
           "sa.shiftTemplate.id = :shiftTemplateId " +
           "ORDER BY sa.plannedStartTime ASC")
    List<ShiftAssignment> findSwappableAssignments(@Param("employeeId") Long employeeId,
                                                   @Param("date") LocalDate date,
                                                   @Param("shiftTemplateId") Long shiftTemplateId);

    /**
     * Tìm assignments của employee trong tuần hiện tại
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.employee.id = :employeeId AND " +
           "sa.assignmentDate >= :weekStart AND sa.assignmentDate <= :weekEnd " +
           "ORDER BY sa.assignmentDate ASC, sa.plannedStartTime ASC")
    List<ShiftAssignment> findCurrentWeekAssignments(@Param("employeeId") Long employeeId,
                                                     @Param("weekStart") LocalDate weekStart,
                                                     @Param("weekEnd") LocalDate weekEnd);
}
