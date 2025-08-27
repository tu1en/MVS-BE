package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ClassroomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for student-specific API endpoints
 * Handles endpoints under /api/student/*
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Slf4j
public class StudentApiController {
    
    private final ClassroomService classroomService;
    private final UserRepository userRepository;
    
    /**
     * Get classes for current authenticated student
     * Frontend calls: GET /api/student/classes
     */
    @GetMapping("/classes")
    public ResponseEntity<List<ClassroomDto>> getStudentClasses(Authentication authentication) {
        log.info("🎓 Getting classes for student: {}", authentication.getName());
        
        try {
            // Get user by email (since JWT contains email)
            String email = authentication.getName();
            User currentUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
            
            log.info("✅ Found user: {} (ID: {})", currentUser.getFullName(), currentUser.getId());
            
            // Get student's classrooms
            List<ClassroomDto> classrooms = classroomService.GetClassroomsByStudent(currentUser.getId());
            
            log.info("📚 Found {} classrooms for student: {}", classrooms.size(), currentUser.getFullName());
            
            return ResponseEntity.ok(classrooms);
            
        } catch (ResourceNotFoundException e) {
            log.error("❌ User not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Error getting student classes: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
