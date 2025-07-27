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
 * Cung cáº¥p cÃ¡c query methods cho quáº£n lÃ½ shift assignments
 */
@Repository
public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, Long> {

    /**
     * TÃ¬m assignments theo employee vÃ  ngÃ y
     */
    List<ShiftAssignment> findByEmployeeIdAndAssignmentDateOrderByPlannedStartTimeAsc(Long employeeId, LocalDate date);

    /**
     * TÃ¬m assignments theo employee trong khoáº£ng thá»i gian
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.employee.id = :employeeId AND " +
           "sa.assignmentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sa.assignmentDate ASC, sa.plannedStartTime ASC")
    List<ShiftAssignment> findByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * TÃ¬m assignments theo ngÃ y
     */
    List<ShiftAssignment> findByAssignmentDateOrderByPlannedStartTimeAsc(LocalDate date);

    /**
     * TÃ¬m assignments theo ngÃ y vÃ  tráº¡ng thÃ¡i
     */
    List<ShiftAssignment> findByAssignmentDateAndStatusOrderByPlannedStartTimeAsc(LocalDate date, 
                                                                                  ShiftAssignment.AssignmentStatus status);

    /**
     * TÃ¬m assignments theo schedule
     */
    List<ShiftAssignment> findByScheduleIdOrderByAssignmentDateAscPlannedStartTimeAsc(Long scheduleId);

    /**
     * TÃ¬m assignments theo shift template
     */
    List<ShiftAssignment> findByShiftTemplateIdOrderByAssignmentDateDesc(Long shiftTemplateId);

    /**
     * Kiá»ƒm tra xung Ä‘á»™t thá»i gian cho employee
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
     * Kiá»ƒm tra minimum rest time violations
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
     * TÃ¬m assignments cáº§n check-in (scheduled vÃ  trong thá»i gian)
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
     * TÃ¬m assignments cáº§n check-out (in progress vÃ  gáº§n háº¿t giá»)
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.status = 'IN_PROGRESS' AND " +
           "sa.assignmentDate = :today AND " +
           "sa.plannedEndTime <= :endWindow " +
           "ORDER BY sa.plannedEndTime ASC")
    List<ShiftAssignment> findPendingCheckOuts(@Param("today") LocalDate today,
                                               @Param("endWindow") LocalTime endWindow);

    /**
     * TÃ¬m assignments theo tuáº§n
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.assignmentDate BETWEEN :weekStart AND :weekEnd " +
           "AND (:employeeId IS NULL OR sa.employee.id = :employeeId) " +
           "ORDER BY sa.assignmentDate ASC, sa.plannedStartTime ASC")
    List<ShiftAssignment> findByWeek(@Param("weekStart") LocalDate weekStart,
                                     @Param("weekEnd") LocalDate weekEnd,
                                     @Param("employeeId") Long employeeId);

    /**
     * TÃ¬m assignments theo thÃ¡ng
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "YEAR(sa.assignmentDate) = :year AND MONTH(sa.assignmentDate) = :month " +
           "AND (:employeeId IS NULL OR sa.employee.id = :employeeId) " +
           "ORDER BY sa.assignmentDate ASC, sa.plannedStartTime ASC")
    List<ShiftAssignment> findByMonth(@Param("year") int year,
                                      @Param("month") int month,
                                      @Param("employeeId") Long employeeId);

    /**
     * Search assignments vá»›i filters
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
     * TÃ­nh tá»•ng giá» lÃ m viá»‡c cá»§a employee trong khoáº£ng thá»i gian
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
     * Äáº¿m assignments theo tráº¡ng thÃ¡i
     */
    @Query("SELECT sa.status, COUNT(sa) FROM ShiftAssignment sa WHERE " +
           "sa.assignmentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY sa.status")
    List<Object[]> countByStatusInDateRange(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * TÃ¬m employees cÃ³ nhiá»u assignments nháº¥t
     */
    @Query("SELECT sa.employee, COUNT(sa) as assignmentCount FROM ShiftAssignment sa WHERE " +
           "sa.assignmentDate BETWEEN :startDate AND :endDate " +
           "GROUP BY sa.employee " +
           "ORDER BY assignmentCount DESC")
    List<Object[]> findTopEmployeesByAssignments(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 Pageable pageable);

    /**
     * TÃ¬m assignments cÃ³ overtime
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.overtimeHours > 0 AND " +
           "sa.assignmentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sa.overtimeHours DESC")
    List<ShiftAssignment> findOvertimeAssignments(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    /**
     * TÃ¬m assignments bá»‹ trá»… hoáº·c vá» sá»›m
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.attendanceStatus IN ('LATE', 'EARLY_LEAVE') AND " +
           "sa.assignmentDate BETWEEN :startDate AND :endDate " +
           "ORDER BY sa.assignmentDate DESC")
    List<ShiftAssignment> findAttendanceIssues(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * Cáº­p nháº­t tráº¡ng thÃ¡i assignment
     */
    @Query("UPDATE ShiftAssignment sa SET sa.status = :status, sa.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE sa.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") ShiftAssignment.AssignmentStatus status);

    /**
     * TÃ¬m assignments cÃ³ thá»ƒ swap
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
     * TÃ¬m assignments cá»§a employee trong tuáº§n hiá»‡n táº¡i
     */
    @Query("SELECT sa FROM ShiftAssignment sa WHERE " +
           "sa.employee.id = :employeeId AND " +
           "sa.assignmentDate >= :weekStart AND sa.assignmentDate <= :weekEnd " +
           "ORDER BY sa.assignmentDate ASC, sa.plannedStartTime ASC")
    List<ShiftAssignment> findCurrentWeekAssignments(@Param("employeeId") Long employeeId,
                                                     @Param("weekStart") LocalDate weekStart,
                                                     @Param("weekEnd") LocalDate weekEnd);
}
