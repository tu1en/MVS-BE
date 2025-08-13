package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.CreateTeacherEvaluationDto;
import com.classroomapp.classroombackend.dto.TeacherEvaluationDto;
import com.classroomapp.classroombackend.dto.TeacherEvaluationStatisticsDto;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.TeacherEvaluationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/teacher-evaluations")
@PreAuthorize("hasRole('TEACHING_ASSISTANT') or hasRole('MANAGER') or hasRole('ADMIN')")
public class TeacherEvaluationController {
    
    private static final Logger log = LoggerFactory.getLogger(TeacherEvaluationController.class);
    
    @Autowired
    private TeacherEvaluationService evaluationService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Create a new teacher evaluation
     */
    @PostMapping
    public ResponseEntity<TeacherEvaluationDto> createEvaluation(
            @RequestBody @Valid CreateTeacherEvaluationDto dto,
            Authentication authentication) {
        try {
            log.info("Creating teacher evaluation for teacher ID: {}", dto.getTeacherId());
            
            Long evaluatorId = getUserIdFromAuthentication(authentication);
            TeacherEvaluationDto result = evaluationService.createEvaluation(dto, evaluatorId);
            
            log.info("Successfully created teacher evaluation with ID: {}", result.getId());
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            log.error("Evaluation already exists: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating teacher evaluation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all evaluations for a specific teacher
     */
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<TeacherEvaluationDto>> getEvaluationsByTeacher(
            @PathVariable Long teacherId) {
        try {
            log.info("Fetching evaluations for teacher ID: {}", teacherId);
            
            List<TeacherEvaluationDto> evaluations = evaluationService.getEvaluationsByTeacher(teacherId);
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            log.error("Error fetching evaluations for teacher {}: {}", teacherId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get average score for a specific teacher
     */
    @GetMapping("/teacher/{teacherId}/average")
    public ResponseEntity<Double> getAverageScore(@PathVariable Long teacherId) {
        try {
            log.info("Fetching average score for teacher ID: {}", teacherId);
            
            Double average = evaluationService.getAverageScoreByTeacher(teacherId);
            return ResponseEntity.ok(average);
        } catch (Exception e) {
            log.error("Error fetching average score for teacher {}: {}", teacherId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get evaluation statistics for a specific teacher
     */
    @GetMapping("/teacher/{teacherId}/statistics")
    public ResponseEntity<TeacherEvaluationStatisticsDto> getEvaluationStatistics(
            @PathVariable Long teacherId) {
        try {
            log.info("Fetching evaluation statistics for teacher ID: {}", teacherId);
            
            TeacherEvaluationStatisticsDto statistics = evaluationService.getEvaluationStatistics(teacherId);
            return ResponseEntity.ok(statistics);
        } catch (IllegalArgumentException e) {
            log.error("Teacher not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching statistics for teacher {}: {}", teacherId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all evaluations made by the current evaluator
     */
    @GetMapping("/my-evaluations")
    public ResponseEntity<List<TeacherEvaluationDto>> getMyEvaluations(Authentication authentication) {
        try {
            Long evaluatorId = getUserIdFromAuthentication(authentication);
            log.info("Fetching evaluations made by evaluator ID: {}", evaluatorId);
            
            List<TeacherEvaluationDto> evaluations = evaluationService.getEvaluationsByEvaluator(evaluatorId);
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            log.error("Error fetching evaluations for current user: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get a specific evaluation by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeacherEvaluationDto> getEvaluationById(@PathVariable Long id) {
        try {
            log.info("Fetching evaluation with ID: {}", id);
            
            TeacherEvaluationDto evaluation = evaluationService.getEvaluationById(id);
            return ResponseEntity.ok(evaluation);
        } catch (IllegalArgumentException e) {
            log.error("Evaluation not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching evaluation {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update an existing evaluation
     */
    @PutMapping("/{id}")
    public ResponseEntity<TeacherEvaluationDto> updateEvaluation(
            @PathVariable Long id,
            @RequestBody @Valid CreateTeacherEvaluationDto dto) {
        try {
            log.info("Updating evaluation with ID: {}", id);
            
            TeacherEvaluationDto updatedEvaluation = evaluationService.updateEvaluation(id, dto);
            return ResponseEntity.ok(updatedEvaluation);
        } catch (IllegalArgumentException e) {
            log.error("Evaluation not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating evaluation {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete an evaluation
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Long id) {
        try {
            log.info("Deleting evaluation with ID: {}", id);
            
            evaluationService.deleteEvaluation(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Evaluation not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting evaluation {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all evaluations (admin/manager only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<List<TeacherEvaluationDto>> getAllEvaluations() {
        try {
            log.info("Fetching all teacher evaluations");
            
            List<TeacherEvaluationDto> evaluations = evaluationService.getAllEvaluations();
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            log.error("Error fetching all evaluations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Helper method to extract user ID from authentication
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            log.error("Authentication is null or principal is not UserDetails");
            throw new RuntimeException("User is not authenticated or user details are not available.");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername(); // This is typically the email
        log.info("Looking up user by email: {}", username);

        // Find the user by email (username) and return their ID
        return userRepository.findByEmail(username)
                .map(user -> {
                    log.info("Found user: {} with ID: {}", user.getFullName(), user.getId());
                    return user.getId();
                })
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", username);
                    return new RuntimeException("User not found with email: " + username);
                });
    }
}

/**
 * Controller for Teaching Assistant operations
 * Handles classroom assignments, attendance, and student evaluations for teaching assistants
 */
@RestController
@RequestMapping("/api/teaching-assistant")
@PreAuthorize("hasRole('TEACHING_ASSISTANT')")
public class TeachingAssistantController {
    
    private static final Logger log = LoggerFactory.getLogger(TeachingAssistantController.class);
    
    @Autowired
    private ClassroomService classroomService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AttendanceService attendanceService;
    
    /**
     * Get all classrooms assigned to the current teaching assistant
     */
    @GetMapping("/my-assigned-classes")
    public ResponseEntity<List<ClassroomDto>> getMyAssignedClasses(Authentication authentication) {
        try {
            Long assistantId = getUserIdFromAuthentication(authentication);
            log.info("Fetching assigned classes for teaching assistant ID: {}", assistantId);
            
            // For now, return classrooms where the assistant might help
            // This would need proper assignment logic in the future
            List<ClassroomDto> classrooms = classroomService.getAllClassrooms();
            
            return ResponseEntity.ok(classrooms);
        } catch (Exception e) {
            log.error("Error fetching assigned classes for teaching assistant: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get students in a specific classroom assigned to the teaching assistant
     */
    @GetMapping("/classroom/{classroomId}/students")
    public ResponseEntity<List<UserDto>> getClassroomStudents(@PathVariable Long classroomId) {
        try {
            log.info("Fetching students for classroom ID: {}", classroomId);
            
            List<UserDto> students = classroomService.getClassroomStudents(classroomId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("Error fetching students for classroom {}: {}", classroomId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get attendance sessions for a specific classroom
     */
    @GetMapping("/classroom/{classroomId}/attendance-sessions")
    public ResponseEntity<List<AttendanceSessionDto>> getClassroomAttendanceSessions(@PathVariable Long classroomId) {
        try {
            log.info("Fetching attendance sessions for classroom ID: {}", classroomId);
            
            // This would need implementation in AttendanceService
            // For now, return empty list
            List<AttendanceSessionDto> sessions = new ArrayList<>();
            
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Error fetching attendance sessions for classroom {}: {}", classroomId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Helper method to extract user ID from authentication
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            log.error("Authentication is null or principal is not UserDetails");
            throw new RuntimeException("User is not authenticated or user details are not available.");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        log.info("Looking up user by email: {}", username);

        return userRepository.findByEmail(username)
                .map(user -> {
                    log.info("Found user: {} with ID: {}", user.getFullName(), user.getId());
                    return user.getId();
                })
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", username);
                    return new RuntimeException("User not found with email: " + username);
                });
    }
}
