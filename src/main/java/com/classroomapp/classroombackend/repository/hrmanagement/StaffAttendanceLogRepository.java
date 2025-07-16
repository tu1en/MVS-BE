package com.classroomapp.classroombackend.repository.hrmanagement;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.hrmanagement.StaffAttendanceLog;

/**
 * Repository interface for StaffAttendanceLog entity
 */
@Repository
public interface StaffAttendanceLogRepository extends JpaRepository<StaffAttendanceLog, Long> {
    
    /**
     * Find attendance log by user and date
     * @param userId the user ID
     * @param attendanceDate the attendance date
     * @return optional attendance log
     */
    Optional<StaffAttendanceLog> findByUserIdAndAttendanceDate(Long userId, LocalDate attendanceDate);
    
    /**
     * Find attendance logs by user ID
     * @param userId the user ID
     * @return list of attendance logs
     */
    List<StaffAttendanceLog> findByUserIdOrderByAttendanceDateDesc(Long userId);
    
    /**
     * Find attendance logs by user ID with pagination
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return page of attendance logs
     */
    Page<StaffAttendanceLog> findByUserIdOrderByAttendanceDateDesc(Long userId, Pageable pageable);
    
    /**
     * Find attendance logs by date range
     * @param startDate start date
     * @param endDate end date
     * @return list of attendance logs
     */
    List<StaffAttendanceLog> findByAttendanceDateBetweenOrderByAttendanceDateDesc(LocalDate startDate, LocalDate endDate);
    
    /**
     * Find attendance logs by user and date range
     * @param userId the user ID
     * @param startDate start date
     * @param endDate end date
     * @return list of attendance logs
     */
    List<StaffAttendanceLog> findByUserIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(
        Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find attendance logs for a specific date
     * @param attendanceDate the attendance date
     * @return list of attendance logs
     */
    List<StaffAttendanceLog> findByAttendanceDateOrderByUserIdAsc(LocalDate attendanceDate);
    
    /**
     * Find incomplete attendance logs (missing check-in or check-out)
     * @param attendanceDate the attendance date
     * @return list of incomplete logs
     */
    @Query("SELECT sal FROM StaffAttendanceLog sal " +
           "WHERE sal.attendanceDate = :attendanceDate " +
           "AND (sal.checkInTime IS NULL OR sal.checkOutTime IS NULL)")
    List<StaffAttendanceLog> findIncompleteLogsForDate(@Param("attendanceDate") LocalDate attendanceDate);
    
    /**
     * Find users who haven't checked in by a specific time
     * @param attendanceDate the attendance date
     * @param cutoffTime the cutoff time
     * @return list of user IDs
     */
    @Query("SELECT DISTINCT u.id FROM User u " +
           "WHERE u.roleId != 2 " + // Exclude teachers
           "AND NOT EXISTS (SELECT 1 FROM StaffAttendanceLog sal " +
           "                WHERE sal.user.id = u.id " +
           "                AND sal.attendanceDate = :attendanceDate " +
           "                AND sal.checkInTime IS NOT NULL)")
    List<Long> findUsersWithoutCheckIn(@Param("attendanceDate") LocalDate attendanceDate);
    
    /**
     * Find users who haven't checked out by end of day
     * @param attendanceDate the attendance date
     * @return list of user IDs
     */
    @Query("SELECT DISTINCT sal.user.id FROM StaffAttendanceLog sal " +
           "WHERE sal.attendanceDate = :attendanceDate " +
           "AND sal.checkInTime IS NOT NULL " +
           "AND sal.checkOutTime IS NULL")
    List<Long> findUsersWithoutCheckOut(@Param("attendanceDate") LocalDate attendanceDate);
    
    /**
     * Find attendance logs with late arrivals
     * @param attendanceDate the attendance date
     * @param lateThresholdMinutes minutes after expected start time
     * @return list of late attendance logs
     */
    @Query("SELECT sal FROM StaffAttendanceLog sal " +
           "JOIN UserShiftAssignment usa ON usa.user.id = sal.user.id " +
           "JOIN usa.workShift ws " +
           "WHERE sal.attendanceDate = :attendanceDate " +
           "AND usa.startDate <= :attendanceDate " +
           "AND usa.endDate >= :attendanceDate " +
           "AND usa.isActive = true " +
           "AND sal.checkInTime > FUNCTION('DATEADD', MINUTE, :lateThresholdMinutes, ws.startTime)")
    List<StaffAttendanceLog> findLateArrivals(@Param("attendanceDate") LocalDate attendanceDate, 
                                            @Param("lateThresholdMinutes") int lateThresholdMinutes);
    
    /**
     * Find attendance logs with early departures
     * @param attendanceDate the attendance date
     * @param earlyThresholdMinutes minutes before expected end time
     * @return list of early departure logs
     */
    @Query("SELECT sal FROM StaffAttendanceLog sal " +
           "JOIN UserShiftAssignment usa ON usa.user.id = sal.user.id " +
           "JOIN usa.workShift ws " +
           "WHERE sal.attendanceDate = :attendanceDate " +
           "AND usa.startDate <= :attendanceDate " +
           "AND usa.endDate >= :attendanceDate " +
           "AND usa.isActive = true " +
           "AND sal.checkOutTime < FUNCTION('DATEADD', MINUTE, -:earlyThresholdMinutes, ws.endTime)")
    List<StaffAttendanceLog> findEarlyDepartures(@Param("attendanceDate") LocalDate attendanceDate, 
                                               @Param("earlyThresholdMinutes") int earlyThresholdMinutes);
    
    /**
     * Get attendance statistics for a user in a date range
     * @param userId the user ID
     * @param startDate start date
     * @param endDate end date
     * @return attendance statistics
     */
    @Query("SELECT " +
           "COUNT(sal) as totalDays, " +
           "SUM(CASE WHEN sal.checkInTime IS NOT NULL AND sal.checkOutTime IS NOT NULL THEN 1 ELSE 0 END) as completeDays, " +
           "SUM(CASE WHEN sal.checkInTime IS NULL THEN 1 ELSE 0 END) as missingCheckIn, " +
           "SUM(CASE WHEN sal.checkOutTime IS NULL THEN 1 ELSE 0 END) as missingCheckOut " +
           "FROM StaffAttendanceLog sal " +
           "WHERE sal.user.id = :userId " +
           "AND sal.attendanceDate BETWEEN :startDate AND :endDate")
    Object[] getAttendanceStatistics(@Param("userId") Long userId, 
                                   @Param("startDate") LocalDate startDate, 
                                   @Param("endDate") LocalDate endDate);
    
    /**
     * Get monthly attendance summary for all users
     * @param year the year
     * @param month the month
     * @return monthly attendance summary
     */
    @Query("SELECT " +
           "sal.user.id as userId, " +
           "sal.user.fullName as userName, " +
           "COUNT(sal) as totalDays, " +
           "SUM(CASE WHEN sal.checkInTime IS NOT NULL AND sal.checkOutTime IS NOT NULL THEN 1 ELSE 0 END) as completeDays " +
           "FROM StaffAttendanceLog sal " +
           "WHERE YEAR(sal.attendanceDate) = :year " +
           "AND MONTH(sal.attendanceDate) = :month " +
           "GROUP BY sal.user.id, sal.user.fullName " +
           "ORDER BY sal.user.fullName")
    List<Object[]> getMonthlyAttendanceSummary(@Param("year") int year, @Param("month") int month);
    
    /**
     * Check if user has attendance record for date
     * @param userId the user ID
     * @param attendanceDate the attendance date
     * @return true if exists
     */
    boolean existsByUserIdAndAttendanceDate(Long userId, LocalDate attendanceDate);
    
    /**
     * Count attendance logs for user in date range
     * @param userId the user ID
     * @param startDate start date
     * @param endDate end date
     * @return count of logs
     */
    long countByUserIdAndAttendanceDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Find attendance logs created by specific user
     * @param createdBy the user ID who created the logs
     * @return list of logs
     */
    List<StaffAttendanceLog> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    /**
     * Find attendance logs by user ID and date range (alias method)
     * @param userId the user ID
     * @param startDate start date
     * @param endDate end date
     * @return list of attendance logs
     */
    default List<StaffAttendanceLog> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return findByUserIdAndAttendanceDateBetweenOrderByAttendanceDateDesc(userId, startDate, endDate);
    }
}
