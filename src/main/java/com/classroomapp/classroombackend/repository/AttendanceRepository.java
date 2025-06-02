package com.classroomapp.classroombackend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Attendance;
import com.classroomapp.classroombackend.model.Classroom;
import com.classroomapp.classroombackend.model.User;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    // Find attendance records for a specific user in a specific classroom
    List<Attendance> findByUserAndClassroom(User user, Classroom classroom);
    
    // Find attendance records for a specific session date
    List<Attendance> findBySessionDate(LocalDateTime sessionDate);
    
    // Find attendance records for a classroom on a specific date
    List<Attendance> findByClassroomAndSessionDateBetween(
        Classroom classroom, LocalDateTime startDate, LocalDateTime endDate);
    
    // Find teacher attendance records for a specific classroom
    List<Attendance> findByClassroomAndIsTeacherRecordTrue(Classroom classroom);
    
    // Find student attendance records for a specific classroom
    List<Attendance> findByClassroomAndIsTeacherRecordFalse(Classroom classroom);
    
    // Find if a teacher has already been marked present for a session
    Optional<Attendance> findByUserAndClassroomAndSessionDateBetweenAndIsTeacherRecordTrue(
        User teacher, Classroom classroom, LocalDateTime startTime, LocalDateTime endTime);
    
    // Find if a user has attendance record in specific classroom between dates
    Optional<Attendance> findByUserAndClassroomAndSessionDateBetween(
        User user, Classroom classroom, LocalDateTime startTime, LocalDateTime endTime);
    
    // Find all attendance records marked by a specific teacher
    List<Attendance> findByMarkedBy(User teacher);
} 