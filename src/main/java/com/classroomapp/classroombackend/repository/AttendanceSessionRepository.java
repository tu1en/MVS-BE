package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.AttendanceSession;
import com.classroomapp.classroombackend.model.Classroom;
import com.classroomapp.classroombackend.model.User;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    
    // Find active sessions for a classroom
    List<AttendanceSession> findByClassroomAndIsActiveTrue(Classroom classroom);
    
    // Find active sessions for a teacher
    List<AttendanceSession> findByTeacherAndIsActiveTrue(User teacher);
    
    // Find sessions between two dates
    List<AttendanceSession> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find all sessions for a classroom
    List<AttendanceSession> findByClassroom(Classroom classroom);
    
    // Find all sessions for a teacher
    List<AttendanceSession> findByTeacher(User teacher);
    
    // Find active session by ID and classroom
    Optional<AttendanceSession> findByIdAndClassroomAndIsActiveTrue(Long id, Classroom classroom);
    
    // Find if a teacher has any active session currently
    boolean existsByTeacherAndIsActiveTrueAndStartTimeBeforeAndEndTimeAfter(
        User teacher, LocalDateTime currentTime, LocalDateTime currentTime2);
    
    // Find active sessions that overlap with the given time
    List<AttendanceSession> findByIsActiveTrueAndStartTimeBeforeAndEndTimeAfter(
        LocalDateTime currentTime, LocalDateTime currentTime2);
} 