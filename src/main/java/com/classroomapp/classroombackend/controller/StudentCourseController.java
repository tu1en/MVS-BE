package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.request.CourseEnrollmentDto;
import com.classroomapp.classroombackend.dto.response.EnrolledCourseDto;
import com.classroomapp.classroombackend.dto.response.PublicCourseTemplateDto;
import com.classroomapp.classroombackend.service.CourseTemplateService;
import com.classroomapp.classroombackend.service.StudentCourseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for student course operations
 * Handles enrolled courses, course browsing, and enrollment
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/student/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT')")
@Slf4j
public class StudentCourseController {
    
    private final StudentCourseService studentCourseService;
    private final CourseTemplateService courseTemplateService;
    
    /**
     * Get student's enrolled courses
     */
    @GetMapping("/enrolled")
    public ResponseEntity<List<EnrolledCourseDto>> getEnrolledCourses(Authentication auth) {
        log.info("Fetching enrolled courses for student: {}", auth.getName());
        
        try {
            String username = auth.getName();
            List<EnrolledCourseDto> courses = studentCourseService.getEnrolledCourses(username);
            log.info("Found {} enrolled courses for student: {}", courses.size(), username);
            
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            log.error("Error fetching enrolled courses for: {}", auth.getName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all available courses (enrolled + not enrolled) for student view
     */
    @GetMapping("/all")
    public ResponseEntity<List<PublicCourseTemplateDto>> getAllCoursesForStudent(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            Authentication auth) {
        log.info("Fetching all courses for student: {} with filters - search: '{}', category: '{}', level: '{}'", 
                auth.getName(), search, category, level);
        
        try {
            String username = auth.getName();
            List<PublicCourseTemplateDto> courses = studentCourseService.getAllCoursesWithEnrollmentStatus(username, search, category, level);
            log.info("Found {} courses for student: {}", courses.size(), username);
            
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            log.error("Error fetching all courses for student: {}", auth.getName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get specific course detail for enrolled student
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<EnrolledCourseDto> getEnrolledCourseDetail(
            @PathVariable Long courseId,
            Authentication auth) {
        log.info("Fetching course detail for courseId: {} and student: {}", courseId, auth.getName());
        
        try {
            String username = auth.getName();
            EnrolledCourseDto course = studentCourseService.getEnrolledCourseDetail(courseId, username);
            log.info("Found course detail for: {}", course.getName());
            
            return ResponseEntity.ok(course);
        } catch (RuntimeException e) {
            log.warn("Course not found or student not enrolled: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching course detail for courseId: {} and student: {}", courseId, auth.getName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Enroll student in a course
     */
    @PostMapping("/{courseId}/enroll")
    public ResponseEntity<EnrolledCourseDto> enrollInCourse(
            @PathVariable Long courseId,
            @RequestBody @Valid CourseEnrollmentDto enrollmentDto,
            Authentication auth) {
        log.info("Enrolling student: {} in courseId: {}", auth.getName(), courseId);
        
        try {
            String username = auth.getName();
            EnrolledCourseDto enrolledCourse = studentCourseService.enrollStudentInCourse(courseId, username, enrollmentDto);
            log.info("Successfully enrolled student: {} in course: {}", username, enrolledCourse.getName());
            
            return ResponseEntity.ok(enrolledCourse);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid enrollment request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            log.warn("Enrollment failed: {}", e.getMessage());
            return ResponseEntity.unprocessableEntity().build();
        } catch (Exception e) {
            log.error("Error enrolling student: {} in courseId: {}", auth.getName(), courseId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update course progress for student
     */
    @PostMapping("/{courseId}/progress")
    public ResponseEntity<Void> updateCourseProgress(
            @PathVariable Long courseId,
            @RequestParam int progress,
            Authentication auth) {
        log.info("Updating progress for courseId: {} to {}% for student: {}", courseId, progress, auth.getName());
        
        try {
            String username = auth.getName();
            studentCourseService.updateCourseProgress(courseId, username, progress);
            log.info("Successfully updated progress for student: {} in courseId: {}", username, courseId);
            
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            log.warn("Failed to update progress: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating progress for courseId: {} and student: {}", courseId, auth.getName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get course statistics for student dashboard
     */
    @GetMapping("/stats")
    public ResponseEntity<Object> getCourseStats(Authentication auth) {
        log.info("Fetching course stats for student: {}", auth.getName());
        
        try {
            String username = auth.getName();
            Object stats = studentCourseService.getStudentCourseStats(username);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching course stats for student: {}", auth.getName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Search courses available for enrollment
     */
    @GetMapping("/search")
    public ResponseEntity<List<PublicCourseTemplateDto>> searchAvailableCourses(
            @RequestParam String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            Authentication auth) {
        log.info("Searching courses with query: '{}' for student: {}", query, auth.getName());
        
        try {
            String username = auth.getName();
            List<PublicCourseTemplateDto> courses = studentCourseService.searchAvailableCourses(username, query, category, level);
            log.info("Found {} courses matching search for student: {}", courses.size(), username);
            
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            log.error("Error searching courses for student: {}", auth.getName(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}