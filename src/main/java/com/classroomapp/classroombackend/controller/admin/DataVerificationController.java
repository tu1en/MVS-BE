package com.classroomapp.classroombackend.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.config.seed.DataVerificationReport;
import com.classroomapp.classroombackend.config.seed.DataVerificationService;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;

/**
 * Admin controller for data verification operations
 * Chá»‰ admin má»›i cÃ³ thá»ƒ truy cáº­p cÃ¡c endpoint nÃ y
 */
@RestController
@RequestMapping("/api/admin/data-verification")
@PreAuthorize("hasRole('ADMIN')")
public class DataVerificationController {
    
    @Autowired
    private DataVerificationService verificationService;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ClassroomEnrollmentRepository classroomEnrollmentRepository;
    
    /**
     * Cháº¡y comprehensive data verification
     */
    @GetMapping("/run")
    public ResponseEntity<DataVerificationReport> runVerification() {
        try {
            DataVerificationReport report = verificationService.runManualVerification();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Láº¥y health status nhanh
     */
    @GetMapping("/health")
    public ResponseEntity<String> getHealthStatus() {
        try {
            String status = verificationService.getHealthStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("ERROR: " + e.getMessage());
        }
    }
    
    /**
     * Láº¥y JSON report
     */
    @GetMapping("/report/json")
    public ResponseEntity<String> getJsonReport() {
        try {
            String jsonReport = verificationService.runVerificationAndGetJsonReport();
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(jsonReport);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * Kiá»ƒm tra cÃ³ critical issues khÃ´ng
     */
    @GetMapping("/critical-check")
    public ResponseEntity<Boolean> hasCriticalIssues() {
        try {
            boolean hasCritical = verificationService.hasCriticalIssues();
            return ResponseEntity.ok(hasCritical);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Kiá»ƒm tra submissions consistency - tÃ¬m submissions tá»« non-enrolled students
     */
    @GetMapping("/submissions-consistency")
    public ResponseEntity<Map<String, Object>> checkSubmissionsConsistency() {
        try {
            Map<String, Object> result = new HashMap<>();

            // Query Ä‘á»ƒ tÃ¬m submissions tá»« non-enrolled students
            List<Object[]> invalidSubmissions = submissionRepository.findSubmissionsFromNonEnrolledStudents();

            result.put("totalInvalidSubmissions", invalidSubmissions.size());
            result.put("invalidSubmissions", invalidSubmissions);
            result.put("status", invalidSubmissions.isEmpty() ? "HEALTHY" : "ISSUES_FOUND");
            result.put("message", invalidSubmissions.isEmpty() ?
                "All submissions are from enrolled students" :
                "Found " + invalidSubmissions.size() + " submissions from non-enrolled students");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "ERROR");
            return ResponseEntity.internalServerError().body(error);
        }
    }
}
