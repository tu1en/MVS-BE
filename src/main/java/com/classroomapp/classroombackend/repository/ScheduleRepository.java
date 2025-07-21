package com.classroomapp.classroombackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.classroomapp.classroombackend.model.Schedule;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    
    // Find schedules by teacher ID
    List<Schedule> findByTeacherId(Long teacherId);
    
    // Find schedules by classroom ID
    List<Schedule> findByClassroomId(Long classroomId);
    
    // Find schedules by day of week
    List<Schedule> findByDayOfWeek(Integer dayOfWeek);
    
    // Find schedules by teacher ID and day of week
    List<Schedule> findByTeacherIdAndDayOfWeek(Long teacherId, Integer dayOfWeek);
    
    // Find schedules by teacher ID with sorted days and times
    @Query("SELECT s FROM Schedule s WHERE s.teacher.id = :teacherId ORDER BY s.dayOfWeek, s.startTime")
    List<Schedule> findByTeacherIdOrderByDayAndTime(@Param("teacherId") Long teacherId);

    List<Schedule> findByClassroomIdIn(List<Long> classroomIds);
    
    // Find schedules by user ID (for general user schedule queries)
    @Query("SELECT s FROM Schedule s WHERE s.teacher.id = :userId OR s.classroom.id IN (SELECT c.id FROM Classroom c WHERE c.student.id = :userId)")
    List<Schedule> findByUserId(@Param("userId") Long userId);
    
    // Find schedules by student ID
    @Query("SELECT s FROM Schedule s WHERE s.classroom.id IN (SELECT c.id FROM Classroom c WHERE c.student.id = :studentId)")
    List<Schedule> findByStudentId(@Param("studentId") Long studentId);
} 