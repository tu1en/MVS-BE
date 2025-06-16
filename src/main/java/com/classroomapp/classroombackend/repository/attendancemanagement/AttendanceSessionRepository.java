package com.classroomapp.classroombackend.repository.attendancemanagement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession.SessionStatus;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;

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
      // Find sessions within a date range
    List<AttendanceSession> findByStartTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find sessions for a classroom within date range
    List<AttendanceSession> findByClassroomAndStartTimeBetween(Classroom classroom, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find current active session for a classroom
    List<AttendanceSession> findByClassroomAndStatusOrderByStartTimeDesc(Classroom classroom, SessionStatus status);
    
    // Find scheduled sessions that should be activated
    List<AttendanceSession> findByStatusAndStartTimeLessThanEqual(SessionStatus status, LocalDateTime currentTime);
    
    // Find active sessions that should be completed
    List<AttendanceSession> findByStatusAndEndTimeLessThanEqual(SessionStatus status, LocalDateTime currentTime);
    
    // Count sessions for a classroom
    long countByClassroom(Classroom classroom);
    
    // Count sessions by teacher
    long countByTeacher(User teacher);
    
    // Find sessions for teacher within date range
    List<AttendanceSession> findByTeacherAndStartTimeBetween(User teacher, LocalDateTime startDate, LocalDateTime endDate);
}
