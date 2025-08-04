package com.classroomapp.classroombackend.repository.hrmanagement;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.classroomapp.classroombackend.model.AttendanceLog;

public interface StaffAttendanceRepository extends JpaRepository<AttendanceLog, Long> {

    @Query("SELECT al FROM AttendanceLog al WHERE al.userId = :userId AND al.date = :date")
    Optional<AttendanceLog> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT al FROM AttendanceLog al WHERE al.userId = :userId AND al.date BETWEEN :startDate AND :endDate ORDER BY al.date DESC")
    List<AttendanceLog> findByUserIdAndDateRange(@Param("userId") Long userId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    List<AttendanceLog> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate);

    @Query("SELECT al FROM AttendanceLog al WHERE al.status = 'LATE' AND al.date = :date")
    List<AttendanceLog> findLateArrivalsForDate(@Param("date") LocalDate date);

    @Query("SELECT al FROM AttendanceLog al WHERE al.status IN ('LATE', 'EARLY_LEAVE', 'ABSENT') AND al.date BETWEEN :startDate AND :endDate")
    List<AttendanceLog> findViolationsInRange(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(al) FROM AttendanceLog al WHERE al.userId = :userId AND al.date BETWEEN :startDate AND :endDate")
    Long getTotalWorkingDays(@Param("userId") Long userId,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate);

    List<AttendanceLog> findByDate(LocalDate date);

    @Query("SELECT DISTINCT al.userId FROM AttendanceLog al WHERE al.date = :date")
    List<Long> findUserIdsWithLogs(@Param("date") LocalDate date);
}
