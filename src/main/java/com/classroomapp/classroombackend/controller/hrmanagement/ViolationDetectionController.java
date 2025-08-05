package com.classroomapp.classroombackend.controller.hrmanagement;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.service.hrmanagement.ViolationDetectionService;
import com.classroomapp.classroombackend.service.hrmanagement.ViolationDetectionService.ViolationDetectionSummary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for violation detection operations
 * Provides endpoints to test and manage the attendance violation workflow
 */
@RestController
@RequestMapping("/api/admin/violation-detection")
@Tag(name = "Violation Detection", description = "Attendance violation detection and testing endpoints")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
@Slf4j
public class ViolationDetectionController {

    @Autowired
    private ViolationDetectionService violationDetectionService;

    /**
     * Manually trigger violation detection for a specific date
     * This demonstrates the main detectDailyViolations() workflow
     */
    @PostMapping("/detect")
    @Operation(
        summary = "Detect violations for specific date",
        description = "Manually trigger the daily violation detection workflow for testing or reprocessing"
    )
    @ApiResponse(responseCode = "200", description = "Violation detection completed successfully")
    public ResponseEntity<ViolationDetectionSummary> detectViolations(
            @Parameter(description = "Date to detect violations for (YYYY-MM-DD)", example = "2025-08-04")
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        
        log.info("Manual violation detection requested for date: {}", date);
        
        ViolationDetectionSummary summary = violationDetectionService.detectDailyViolations(date);
        
        log.info("Violation detection completed: {}", summary);
        
        return ResponseEntity.ok(summary);
    }

    /**
     * Test endpoint using sample data from ViolationTestDataLoader
     * This will detect violations from the test data created by the DataLoader
     */
    @PostMapping("/test")
    @Operation(
        summary = "Test violation detection with sample data",
        description = "Test the violation detection workflow using pre-loaded sample data"
    )
    @ApiResponse(responseCode = "200", description = "Test completed successfully")
    public ResponseEntity<ViolationDetectionSummary> testViolationDetection() {
        
        LocalDate testDate = LocalDate.of(2025, 8, 4); // Date used in test data
        
        log.info("Testing violation detection with sample data for date: {}", testDate);
        
        ViolationDetectionSummary summary = violationDetectionService.detectDailyViolations(testDate);
        
        log.info("Test violation detection completed: {}", summary);
        
        return ResponseEntity.ok(summary);
    }

    /**
     * Get violation detection configuration
     */
    @GetMapping("/config")
    @Operation(
        summary = "Get violation detection configuration",
        description = "Retrieve current violation detection configuration settings"
    )
    public ResponseEntity<ViolationDetectionService.ViolationDetectionConfig> getConfig() {
        return ResponseEntity.ok(violationDetectionService.getDetectionConfig());
    }

    /**
     * Get detection summary for yesterday (typical daily usage)
     */
    @PostMapping("/detect-yesterday")
    @Operation(
        summary = "Detect violations for yesterday",
        description = "Run violation detection for yesterday's attendance (typical daily workflow)"
    )
    public ResponseEntity<ViolationDetectionSummary> detectYesterday() {
        
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        log.info("Detecting violations for yesterday: {}", yesterday);
        
        ViolationDetectionSummary summary = violationDetectionService.detectDailyViolations(yesterday);
        
        log.info("Yesterday's violation detection completed: {}", summary);
        
        return ResponseEntity.ok(summary);
    }

    /**
     * Reprocess violations for a date range
     */
    @PostMapping("/reprocess")
    @Operation(
        summary = "Reprocess violations for date range",
        description = "Reprocess violation detection for multiple dates"
    )
    public ResponseEntity<ViolationDetectionSummary> reprocessViolations(
            @Parameter(description = "Start date (YYYY-MM-DD)", example = "2025-08-01")
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            
            @Parameter(description = "End date (YYYY-MM-DD)", example = "2025-08-07")
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        log.info("Reprocessing violations for date range: {} to {}", startDate, endDate);
        
        ViolationDetectionSummary summary = violationDetectionService.reprocessViolations(startDate, endDate);
        
        log.info("Reprocessing completed: {}", summary);
        
        return ResponseEntity.ok(summary);
    }
}