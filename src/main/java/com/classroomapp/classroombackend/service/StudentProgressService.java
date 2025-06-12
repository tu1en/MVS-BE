package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.StudentProgressDto;
import com.classroomapp.classroombackend.dto.ProgressAnalyticsDto;
import com.classroomapp.classroombackend.model.StudentProgress;

import java.math.BigDecimal;
import java.util.List;

public interface StudentProgressService {
    
    /**
     * Create or update student progress
     */
    StudentProgressDto createOrUpdateProgress(StudentProgressDto progressDto);
    
    /**
     * Get student progress for a classroom
     */
    List<StudentProgressDto> getStudentProgressByClassroom(Long studentId, Long classroomId);
    
    /**
     * Get progress for a specific assignment
     */
    StudentProgressDto getProgressByAssignment(Long studentId, Long assignmentId);
    
    /**
     * Get overall progress for a student in a classroom
     */
    StudentProgressDto getOverallProgress(Long studentId, Long classroomId);
    
    /**
     * Get all students' progress for a classroom (teacher view)
     */
    List<StudentProgressDto> getAllStudentsProgress(Long classroomId);
    
    /**
     * Get progress analytics for a classroom
     */
    ProgressAnalyticsDto getProgressAnalytics(Long classroomId);
    
    /**
     * Get students with low progress
     */
    List<StudentProgressDto> getStudentsWithLowProgress(Long classroomId, BigDecimal threshold);
    
    /**
     * Update time spent on a specific activity
     */
    StudentProgressDto updateTimeSpent(Long studentId, Long classroomId, 
                                     StudentProgress.ProgressType progressType, Integer minutesSpent);
    
    /**
     * Calculate and update overall progress for a student
     */
    StudentProgressDto calculateOverallProgress(Long studentId, Long classroomId);
    
    /**
     * Get total time spent by student in classroom
     */
    Integer getTotalTimeSpent(Long studentId, Long classroomId);
    
    /**
     * Bulk update progress for multiple students
     */
    List<StudentProgressDto> bulkUpdateProgress(List<StudentProgressDto> progressList);
}
