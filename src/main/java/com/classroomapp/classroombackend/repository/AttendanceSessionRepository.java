package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.AttendanceSession;
import com.classroomapp.classroombackend.model.AttendanceSession.SessionStatus;
import com.classroomapp.classroombackend.model.Classroom;
import com.classroomapp.classroombackend.model.User;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    
    // Find sessions by classroom
    List<AttendanceSession> findByClassroom(Classroom classroom);
    
    // Find sessions by teacher
    List<AttendanceSession> findByTeacher(User teacher);
    
    // Find sessions by status
    List<AttendanceSession> findByStatus(SessionStatus status);
    
    // Find sessions by classroom and status
    List<AttendanceSession> findByClassroomAndStatus(Classroom classroom, SessionStatus status);
    
    // Find active sessions for a classroom
    @Query("SELECT s FROM AttendanceSession s WHERE s.classroom = :classroom AND s.status = 'ACTIVE'")
    List<AttendanceSession> findActiveSessionsByClassroom(@Param("classroom") Classroom classroom);
    
    // Find sessions within a date range
    @Query("SELECT s FROM AttendanceSession s WHERE s.startTime BETWEEN :startDate AND :endDate")
    List<AttendanceSession> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    // Find sessions for a classroom within date range
    @Query("SELECT s FROM AttendanceSession s WHERE s.classroom = :classroom AND s.startTime BETWEEN :startDate AND :endDate")
    List<AttendanceSession> findByClassroomAndDateRange(@Param("classroom") Classroom classroom,
                                                      @Param("startDate") LocalDateTime startDate,
                                                      @Param("endDate") LocalDateTime endDate);
    
    // Find current active session for a classroom (should be at most one)
    @Query("SELECT s FROM AttendanceSession s WHERE s.classroom = :classroom AND s.status = 'ACTIVE' AND s.startTime <= :currentTime AND (s.endTime IS NULL OR s.endTime >= :currentTime)")
    Optional<AttendanceSession> findCurrentActiveSession(@Param("classroom") Classroom classroom, 
                                                        @Param("currentTime") LocalDateTime currentTime);
    
    // Find scheduled sessions that should be activated
    @Query("SELECT s FROM AttendanceSession s WHERE s.status = 'SCHEDULED' AND s.startTime <= :currentTime")
    List<AttendanceSession> findScheduledSessionsToActivate(@Param("currentTime") LocalDateTime currentTime);
    
    // Find active sessions that should be completed
    @Query("SELECT s FROM AttendanceSession s WHERE s.status = 'ACTIVE' AND s.endTime <= :currentTime")
    List<AttendanceSession> findActiveSessionsToComplete(@Param("currentTime") LocalDateTime currentTime);
    
    // Count sessions for a classroom
    long countByClassroom(Classroom classroom);
    
    // Count sessions by teacher
    long countByTeacher(User teacher);
    
    // Find sessions for teacher within date range
    @Query("SELECT s FROM AttendanceSession s WHERE s.teacher = :teacher AND s.startTime BETWEEN :startDate AND :endDate")
    List<AttendanceSession> findByTeacherAndDateRange(@Param("teacher") User teacher,
                                                    @Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate);
}
