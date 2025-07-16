package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.absencemanagement.AbsenceDTO;
import com.classroomapp.classroombackend.dto.absencemanagement.TeacherLeaveInfoDTO;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.repository.absencemanagement.AbsenceRepository;
import com.classroomapp.classroombackend.service.AbsenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.classroomapp.classroombackend.model.Absence;

@RestController
@RequestMapping("/api/manager/absences")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
@Slf4j
public class ManagerAbsenceController {

    private final AbsenceService absenceService;
    private final UserRepository userRepository;
    private final AbsenceRepository absenceRepository;

    @GetMapping("/debug/count")
    public ResponseEntity<Map<String, Object>> getDebugInfo() {
        Map<String, Object> debugInfo = new HashMap<>();
        
        try {
            long absenceCount = absenceRepository.count();
            long userCount = userRepository.count();
            long teacherCount = userRepository.findByRoleId(2).size(); // TEACHER role
            
            debugInfo.put("totalAbsences", absenceCount);
            debugInfo.put("totalUsers", userCount);
            debugInfo.put("totalTeachers", teacherCount);
            debugInfo.put("status", "success");
            
            log.info("Debug info - Absences: {}, Users: {}, Teachers: {}", absenceCount, userCount, teacherCount);
            
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            debugInfo.put("status", "error");
            log.error("Error getting debug info: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(debugInfo);
    }

    @PostMapping("/debug/seed-absences")
    public ResponseEntity<Map<String, Object>> seedAbsences() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("Manual absence seeding requested");
            
            // Check if teachers exist
            Long[] teacherIds = {201L, 202L, 203L, 204L, 205L, 206L};
            for (Long teacherId : teacherIds) {
                if (!userRepository.existsById(teacherId)) {
                    result.put("error", "Teacher with ID " + teacherId + " not found");
                    result.put("status", "error");
                    return ResponseEntity.badRequest().body(result);
                }
            }
            
            // Force create some test absences directly
            log.info("Creating test absence records manually...");
            
            createTestAbsence(201L, "teacher@test.com", "Nguyễn Văn Minh", 
                LocalDate.now().minusDays(30), LocalDate.now().minusDays(28), 3, 
                "Nghỉ phép để tham gia hội thảo", "APPROVED");
                
            createTestAbsence(202L, "math@test.com", "Trần Văn Đức",
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(12), 3,
                "Xin nghỉ phép để tham dự đám cưới", "PENDING");
                
            createTestAbsence(203L, "literature@test.com", "Phạm Thị Lan",
                LocalDate.now().minusDays(60), LocalDate.now().minusDays(53), 8,
                "Nghỉ phép sinh con", "APPROVED");
            
            long newCount = absenceRepository.count();
            result.put("message", "Successfully created test absence records");
            result.put("totalAbsences", newCount);
            result.put("status", "success");
            
            log.info("Created test absences. Total count now: {}", newCount);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("status", "error");
            log.error("Error seeding absences: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok(result);
    }
    
    private void createTestAbsence(Long userId, String userEmail, String userFullName,
                              LocalDate startDate, LocalDate endDate, Integer numberOfDays,
                              String description, String status) {
        Absence absence = new Absence();
        absence.setUserId(userId);
        absence.setUserEmail(userEmail);
        absence.setUserFullName(userFullName);
        absence.setStartDate(startDate);
        absence.setEndDate(endDate);
        absence.setNumberOfDays(numberOfDays);
        absence.setDescription(description);
        absence.setStatus(status);
        absence.setIsOverLimit(false);
        absence.setCreatedAt(LocalDateTime.now().minusDays(3));
        
        if ("APPROVED".equals(status)) {
            absence.setResultStatus("APPROVED");
            absence.setProcessedAt(LocalDateTime.now().minusDays(2));
        }
        
        absenceRepository.save(absence);
        log.info("Created test absence for {}", userFullName);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<AbsenceDTO>> getAllAbsenceRequests() {
        List<AbsenceDTO> absences = absenceService.getAllAbsenceRequests();
        return ResponseEntity.ok(absences);
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<AbsenceDTO>> getPendingAbsenceRequests() {
        List<AbsenceDTO> pendingAbsences = absenceService.getPendingAbsenceRequests();
        return ResponseEntity.ok(pendingAbsences);
    }

    @GetMapping("/employees")
    public ResponseEntity<List<TeacherLeaveInfoDTO>> getAllEmployeesLeaveInfo() {
        List<TeacherLeaveInfoDTO> employeesInfo = absenceService.getAllTeachersLeaveInfo();
        return ResponseEntity.ok(employeesInfo);
    }

    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<TeacherLeaveInfoDTO> getEmployeeLeaveInfo(@PathVariable Long employeeId) {
        TeacherLeaveInfoDTO employeeInfo = absenceService.getTeacherLeaveInfo(employeeId);
        return ResponseEntity.ok(employeeInfo);
    }

    @PostMapping("/requests/{absenceId}/approve")
    public ResponseEntity<AbsenceDTO> approveAbsence(
            @PathVariable Long absenceId,
            Authentication authentication) {
        try {
            log.info("Processing approval request for absence ID: {}", absenceId);
            Long managerId = getUserIdFromAuthentication(authentication);
            log.info("Manager ID from authentication: {}", managerId);
            AbsenceDTO approvedAbsence = absenceService.approveAbsence(absenceId, managerId);
            return ResponseEntity.ok(approvedAbsence);
        } catch (Exception e) {
            log.error("Error approving absence request {}: {}", absenceId, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/requests/{absenceId}/reject")
    public ResponseEntity<AbsenceDTO> rejectAbsence(
            @PathVariable Long absenceId,
            @RequestBody String reason,
            Authentication authentication) {
        try {
            Long managerId = getUserIdFromAuthentication(authentication);
            AbsenceDTO rejectedAbsence = absenceService.rejectAbsence(absenceId, reason, managerId);
            return ResponseEntity.ok(rejectedAbsence);
        } catch (Exception e) {
            log.error("Error rejecting absence request {}: {}", absenceId, e.getMessage(), e);
            throw e;
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
                    log.error("Authenticated user not found in database: {}", username);
                    return new RuntimeException("Authenticated user not found in database: " + username);
                });
    }
} 