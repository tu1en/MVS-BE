package com.classroomapp.classroombackend.service;

import java.util.List;

import com.classroomapp.classroombackend.dto.request.CourseEnrollmentDto;
import com.classroomapp.classroombackend.dto.response.EnrolledCourseDto;
import com.classroomapp.classroombackend.dto.response.PublicCourseTemplateDto;

public interface StudentCourseService {
    
    /**
     * Get all enrolled courses for a student
     */
    List<EnrolledCourseDto> getEnrolledCourses(String username);
    
    /**
     * Get all courses (enrolled + available) with enrollment status for student
     */
    List<PublicCourseTemplateDto> getAllCoursesWithEnrollmentStatus(String username, String search, String category, String level);
    
    /**
     * Get detailed information about an enrolled course
     */
    EnrolledCourseDto getEnrolledCourseDetail(Long courseId, String username);
    
    /**
     * Enroll student in a course
     */
    EnrolledCourseDto enrollStudentInCourse(Long courseId, String username, CourseEnrollmentDto enrollmentDto);
    
    /**
     * Update course progress for student
     */
    void updateCourseProgress(Long courseId, String username, int progress);
    
    /**
     * Get course statistics for student dashboard
     */
    Object getStudentCourseStats(String username);
    
    /**
     * Search available courses for enrollment
     */
    List<PublicCourseTemplateDto> searchAvailableCourses(String username, String query, String category, String level);
}