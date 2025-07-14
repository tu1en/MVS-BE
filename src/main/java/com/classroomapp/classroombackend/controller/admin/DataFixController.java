package com.classroomapp.classroombackend.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.config.seed.DataFixReport;
import com.classroomapp.classroombackend.config.seed.DataFixUtility;

/**
 * Admin controller for data fix operations
 * Chỉ admin mới có thể truy cập các endpoint này
 */
@RestController
@RequestMapping("/api/admin/data-fix")
@PreAuthorize("hasRole('ADMIN')")
public class DataFixController {
    
    @Autowired
    private DataFixUtility dataFixUtility;
    
    /**
     * Fix tất cả các vấn đề dữ liệu đã biết
     */
    @PostMapping("/fix-all")
    public ResponseEntity<DataFixReport> fixAllIssues() {
        try {
            DataFixReport report = dataFixUtility.fixAllKnownIssues();
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Fix orphaned submissions
     */
    @PostMapping("/fix-orphaned-submissions")
    public ResponseEntity<String> fixOrphanedSubmissions() {
        try {
            int count = dataFixUtility.fixOrphanedSubmissions();
            return ResponseEntity.ok("Fixed " + count + " orphaned submissions");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Fix orphaned enrollments
     */
    @PostMapping("/fix-orphaned-enrollments")
    public ResponseEntity<String> fixOrphanedEnrollments() {
        try {
            int count = dataFixUtility.fixOrphanedEnrollments();
            return ResponseEntity.ok("Fixed " + count + " orphaned enrollments");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Fix duplicate user emails
     */
    @PostMapping("/fix-duplicate-emails")
    public ResponseEntity<String> fixDuplicateEmails() {
        try {
            int count = dataFixUtility.fixDuplicateUserEmails();
            return ResponseEntity.ok("Fixed " + count + " duplicate email issues");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Fix invalid assignment points
     */
    @PostMapping("/fix-invalid-assignment-points")
    public ResponseEntity<String> fixInvalidAssignmentPoints() {
        try {
            int count = dataFixUtility.fixInvalidAssignmentPoints();
            return ResponseEntity.ok("Fixed " + count + " invalid assignment points");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Fix classroom teacher references
     */
    @PostMapping("/fix-classroom-teachers")
    public ResponseEntity<String> fixClassroomTeachers() {
        try {
            int count = dataFixUtility.fixClassroomTeacherReferences();
            return ResponseEntity.ok("Fixed " + count + " classroom teacher references");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Fix lectures with missing dates
     */
    @PostMapping("/fix-lecture-dates")
    public ResponseEntity<String> fixLectureDates() {
        try {
            int count = dataFixUtility.fixLectureMissingDates();
            return ResponseEntity.ok("Fixed " + count + " lectures with missing dates");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * DANGEROUS: Cleanup tất cả dữ liệu
     * Chỉ dùng cho development/testing
     */
    @DeleteMapping("/cleanup-all-data")
    @PreAuthorize("hasRole('ADMIN') and @environment.getActiveProfiles()[0] != 'prod'")
    public ResponseEntity<String> cleanupAllData() {
        try {
            dataFixUtility.cleanupAllData();
            return ResponseEntity.ok("All data cleaned up successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
