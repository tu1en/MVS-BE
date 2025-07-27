package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

import com.classroomapp.classroombackend.model.AttendanceStatus;

public interface StaffAttendanceRepository extends JpaRepository<AttendanceLog, Long> {
    
    @Query("SELECT al FROM AttendanceLog al WHERE al.staff = :staff AND al.attendanceDate = :date")
    Optional<AttendanceLog> findByStaffAndDate(@Param("staff") User staff, @Param("date") LocalDate date);
    
    @Query("SELECT al FROM AttendanceLog al WHERE al.staff = :staff AND al.attendanceDate BETWEEN :startDate AND :endDate ORDER BY al.attendanceDate DESC")
    List<AttendanceLog> findByStaffAndDateRange(@Param("staff") User staff, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    List<AttendanceLog> findByAttendanceDateBetweenOrderByAttendanceDateDesc(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT al FROM AttendanceLog al WHERE al.status = 'LATE' AND al.attendanceDate = :date")
    List<AttendanceLog> findLateArrivalsForDate(@Param("date") LocalDate date);
    
    @Query("SELECT al FROM AttendanceLog al WHERE al.status IN ('LATE', 'EARLY_LEAVE', 'ABSENT') AND al.attendanceDate BETWEEN :startDate AND :endDate")
    List<AttendanceLog> findViolationsInRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    List<AttendanceLog> findByStaffAndStatusAndAttendanceDateBetween(
            @Param("staff") User staff,
            @Param("status") AttendanceStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(al.workingHours) FROM AttendanceLog al WHERE al.staff = :staff AND al.attendanceDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalWorkingHours(@Param("staff") User staff, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    List<AttendanceLog> findByAttendanceDate(LocalDate date);
    
    @Query("SELECT al.staff FROM AttendanceLog al WHERE al.attendanceDate = :date")
    List<User> findStaffWithLogs(@Param("date") LocalDate date);
}