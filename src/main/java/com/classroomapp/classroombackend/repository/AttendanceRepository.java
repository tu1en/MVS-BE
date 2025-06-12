package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Attendance;
import com.classroomapp.classroombackend.model.Attendance.AttendanceStatus;
import com.classroomapp.classroombackend.model.AttendanceSession;
import com.classroomapp.classroombackend.model.User;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    // Find attendance by user and session
    Optional<Attendance> findByStudentAndSession(User user, AttendanceSession session);
    
    // Find all attendance records for a specific session
    List<Attendance> findBySession(AttendanceSession session);
    
    // Find all attendance records for a specific user
    List<Attendance> findByStudent(User user);
    
    // Find attendance by user and session ID
    Optional<Attendance> findByStudentAndSessionId(User user, Long sessionId);
    
    // Find attendance records by status
    List<Attendance> findByStatus(AttendanceStatus status);
    
    // Find attendance records for a user within a date range
    @Query("SELECT a FROM Attendance a WHERE a.student = :user AND a.checkInTime BETWEEN :startDate AND :endDate")
    List<Attendance> findByStudentAndDateRange(@Param("user") User user, 
                                          @Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    // Get attendance summary for a session
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.session = :session GROUP BY a.status")
    List<Object[]> getAttendanceSummaryBySession(@Param("session") AttendanceSession session);
    
    // Find attendance records for a classroom within date range
    @Query("SELECT a FROM Attendance a JOIN a.session s WHERE s.classroom.id = :classroomId AND a.checkInTime BETWEEN :startDate AND :endDate")
    List<Attendance> findByClassroomAndDateRange(@Param("classroomId") Long classroomId,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);
    
    // Get student attendance percentage for a classroom
    @Query("SELECT a.student, " +
           "CAST(SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) AS double) / COUNT(a) * 100 " +
           "FROM Attendance a " +
           "JOIN a.session s " +
           "WHERE s.classroom.id = :classroomId " +
           "GROUP BY a.student")
    List<Object[]> getStudentAttendancePercentageByClassroom(@Param("classroomId") Long classroomId);
    
    // Check if student has already marked attendance for a session
    boolean existsByStudentAndSession(User student, AttendanceSession session);
}
