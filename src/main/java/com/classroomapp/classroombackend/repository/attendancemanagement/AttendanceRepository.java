package com.classroomapp.classroombackend.repository.attendancemanagement;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.dto.attendancemanagement.MyAttendanceHistoryDto;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;

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
    List<Attendance> findByStatus(AttendanceStatus status);    // Find attendance records for a user within a date range
    @Query("SELECT a FROM Attendance a JOIN a.session s WHERE a.student = :user AND s.sessionDate BETWEEN :startDate AND :endDate")
    List<Attendance> findByStudentAndDateRange(@Param("user") User user,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);
    
    // Get attendance summary for a session
    @Query("SELECT a.status, COUNT(a) FROM Attendance a WHERE a.session = :session GROUP BY a.status")
    List<Object[]> getAttendanceSummaryBySession(@Param("session") AttendanceSession session);      // Find attendance records for a classroom within date range
    @Query("SELECT a FROM Attendance a JOIN a.session s WHERE s.classroom.id = :classroomId AND s.sessionDate BETWEEN :startDate AND :endDate")
    List<Attendance> findByClassroomAndDateRange(@Param("classroomId") Long classroomId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    
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
    
    // Find attendance records by session and status
    List<Attendance> findBySessionAndStatus(AttendanceSession session, AttendanceStatus status);    // Find attendance records by user, classroom and date range for teacher records
    @Query("SELECT a FROM Attendance a JOIN a.session s WHERE a.student = :user AND s.classroom.id = :#{#classroom.id} " +
           "AND s.sessionDate BETWEEN :startDate AND :endDate")
    List<Attendance> findByUserAndClassroomAndSessionDateBetweenAndIsTeacherRecordTrue(
        @Param("user") User user, 
        @Param("classroom") Classroom classroom, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);

    Optional<Attendance> findBySession_IdAndStudent_Id(Long sessionId, Long studentId);

    // Alternative simpler query for debugging
    @Query("SELECT a FROM Attendance a " +
           "JOIN a.session s " +
           "WHERE a.student.id = :studentId AND s.classroom.id = :classroomId " +
           "ORDER BY s.sessionDate DESC")
    List<Attendance> findAttendanceRecordsForDebugging(@Param("studentId") Long studentId, @Param("classroomId") Long classroomId);

    @Query("SELECT new com.classroomapp.classroombackend.dto.attendancemanagement.MyAttendanceHistoryDto(l.id, l.title, s.sessionDate, a.status) " +
           "FROM Attendance a " +
           "JOIN a.session s " +
           "JOIN s.lecture l " +
           "WHERE a.student.id = :studentId AND s.classroom.id = :classroomId " +
           "ORDER BY s.sessionDate DESC")
    List<MyAttendanceHistoryDto> findStudentAttendanceHistoryByCourse(@Param("studentId") Long studentId, @Param("classroomId") Long classroomId);

    @Query("SELECT a FROM Attendance a JOIN a.session s WHERE s.classroom.id = :classroomId AND a.student.id = :studentId")
    List<Attendance> findByStudentIdAndSessionClassroomId(@Param("studentId") Long studentId, @Param("classroomId") Long classroomId);

    @Query("SELECT " +
            "SUM(CASE WHEN a.status IN (com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus.PRESENT, com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus.LATE) THEN 1 ELSE 0 END) * 100.0 / COUNT(a.id) " +
            "FROM Attendance a WHERE a.session.classroom.id IN :classroomIds")
    Double getAverageAttendanceByClassroomIds(@Param("classroomIds") List<Long> classroomIds);
}
