package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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

import com.classroomapp.classroombackend.dto.request.CreateEnrollmentRequestDto;
import com.classroomapp.classroombackend.dto.response.EnrollmentRequestDto;
import com.classroomapp.classroombackend.entity.EnrollmentRequest.EnrollmentStatus;
import com.classroomapp.classroombackend.service.EnrollmentRequestService;
import com.classroomapp.classroombackend.service.impl.UserServiceImpl;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for enrollment request management
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/enrollment-requests")
@RequiredArgsConstructor
@PreAuthorize("hasRole('STUDENT') or hasRole('MANAGER') or hasRole('ADMIN')")
@Slf4j
public class EnrollmentRequestController {
    
    private final EnrollmentRequestService enrollmentRequestService;
    private final UserServiceImpl userService;
    
    /**
     * Create a new enrollment request (STUDENT only)
     */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<EnrollmentRequestDto> createRequest(
            @RequestBody @Valid CreateEnrollmentRequestDto dto,
            Authentication auth) {
        
        log.info("Creating enrollment request for course template: {}", dto.getCourseTemplateId());
        
        try {
            Long studentId = getUserId(auth);
            EnrollmentRequestDto result = enrollmentRequestService.createEnrollmentRequest(dto, studentId);
            
            log.info("Created enrollment request with ID: {}", result.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
            
        } catch (RuntimeException e) {
            log.warn("Error creating enrollment request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error creating enrollment request", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get enrollment requests by status (MANAGER/ADMIN only)
     */
    @GetMapping
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentRequestDto>> getPendingRequests(
            @RequestParam(defaultValue = "PENDING") String status) {
        
        log.info("Fetching enrollment requests with status: {}", status);
        
        try {
            EnrollmentStatus enrollmentStatus = EnrollmentStatus.valueOf(status.toUpperCase());
            List<EnrollmentRequestDto> requests = enrollmentRequestService.getRequestsByStatus(enrollmentStatus);
            
            log.info("Found {} enrollment requests with status: {}", requests.size(), status);
            return ResponseEntity.ok(requests);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid status parameter: {}", status);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching enrollment requests", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Approve an enrollment request (MANAGER/ADMIN only)
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<EnrollmentRequestDto> approveRequest(
            @PathVariable Long id,
            Authentication auth) {
        
        log.info("Approving enrollment request with ID: {}", id);
        
        try {
            Long managerId = getUserId(auth);
            EnrollmentRequestDto result = enrollmentRequestService.approveRequest(id, managerId);
            
            log.info("Approved enrollment request with ID: {}", id);
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            log.warn("Error approving enrollment request {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error approving enrollment request: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Reject an enrollment request (MANAGER/ADMIN only)
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<EnrollmentRequestDto> rejectRequest(
            @PathVariable Long id,
            @RequestParam String reason,
            Authentication auth) {
        
        log.info("Rejecting enrollment request with ID: {} with reason: {}", id, reason);
        
        try {
            Long managerId = getUserId(auth);
            EnrollmentRequestDto result = enrollmentRequestService.rejectRequest(id, managerId, reason);
            
            log.info("Rejected enrollment request with ID: {}", id);
            return ResponseEntity.ok(result);
            
        } catch (RuntimeException e) {
            log.warn("Error rejecting enrollment request {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Unexpected error rejecting enrollment request: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get student's own enrollment requests (STUDENT only)
     */
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<EnrollmentRequestDto>> getMyRequests(Authentication auth) {
        
        log.info("Fetching enrollment requests for current student");
        
        try {
            Long studentId = getUserId(auth);
            List<EnrollmentRequestDto> requests = enrollmentRequestService.getStudentRequests(studentId);
            
            log.info("Found {} enrollment requests for student: {}", requests.size(), studentId);
            return ResponseEntity.ok(requests);
            
        } catch (Exception e) {
            log.error("Error fetching student enrollment requests", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get enrollment request by ID (Any authenticated user)
     */
    @GetMapping("/{id}")
    public ResponseEntity<EnrollmentRequestDto> getRequestById(@PathVariable Long id) {
        
        log.info("Fetching enrollment request with ID: {}", id);
        
        try {
            EnrollmentRequestDto request = enrollmentRequestService.getRequestById(id);
            return ResponseEntity.ok(request);
            
        } catch (RuntimeException e) {
            log.warn("Enrollment request not found: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching enrollment request: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Helper method to extract user ID from authentication
     */
    private Long getUserId(Authentication auth) {
        try {
            // This assumes your JWT contains the user ID
            // You may need to adjust this based on your authentication implementation
            String username = auth.getName();
            return userService.findByUsername(username).getId();
        } catch (Exception e) {
            log.error("Error extracting user ID from authentication", e);
            throw new RuntimeException("Could not determine user ID");
        }
    }
}