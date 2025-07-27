package com.classroomapp.classroombackend.service.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation;
import com.classroomapp.classroombackend.repository.hrmanagement.AttendanceViolationRepository;
import com.classroomapp.classroombackend.service.hrmanagement.ViolationDetectionService;
import com.classroomapp.classroombackend.service.hrmanagement.ViolationDetectionService.ViolationDetectionSummary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled service for automated violation detection
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ViolationDetectionScheduler {
    
    private final ViolationDetectionService violationDetectionService;
    private final AttendanceViolationRepository violationRepository;
    
    /**
     * Daily violation detection job - runs at 1:00 AM every day
     * Processes previous day's attendance for violations
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void runDailyViolationDetection() {
        LocalDate processingDate = LocalDate.now().minusDays(1);
        log.info("Starting daily violation detection for date: {}", processingDate);
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Run violation detection for yesterday
            ViolationDetectionSummary summary = violationDetectionService.runDailyDetection(processingDate);
            
            long endTime = System.currentTimeMillis();
            summary.setProcessingTimeMs(endTime - startTime);
            
            log.info("Daily violation detection completed: {}", summary);
            
            // Log individual violation counts for monitoring
            logViolationMetrics(summary);
            
        } catch (Exception e) {
            log.error("Error during daily violation detection for date: {}", processingDate, e);
        }
    }
    
    /**
     * Weekly violation summary job - runs every Sunday at 3:00 AM
     * Generates weekly violation statistics
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    public void generateWeeklyViolationSummary() {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(6); // Last 7 days
        
        log.info("Generating weekly violation summary from {} to {}", startDate, endDate);
        
        try {
            // Get violation statistics for the week
            long totalViolations = violationRepository.countViolationsInDateRange(startDate, endDate);
            long pendingExplanations = violationRepository.countByStatus(AttendanceViolation.ViolationStatus.NEEDS_EXPLANATION);
            long pendingReviews = violationRepository.countByStatus(AttendanceViolation.ViolationStatus.PENDING_REVIEW);
            long resolvedViolations = violationRepository.countByStatus(AttendanceViolation.ViolationStatus.RESOLVED);
            
            log.info("Weekly violation summary: Total={}, Pending Explanations={}, Pending Reviews={}, Resolved={}", 
                    totalViolations, pendingExplanations, pendingReviews, resolvedViolations);
            
            // TODO: Send weekly summary report to managers via email/notification
            
        } catch (Exception e) {
            log.error("Error generating weekly violation summary", e);
        }
    }
    
    /**
     * Overdue violation escalation job - runs every day at 9:00 AM
     * Escalates violations that haven't been explained after 3 days
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void escalateOverdueViolations() {
        log.info("Starting overdue violation escalation process");
        
        try {
            // Get violations that are overdue (no explanation after 3 days)
            List<com.classroomapp.classroombackend.dto.hrmanagement.AttendanceViolationDto> overdueViolations = 
                violationDetectionService.getOverdueViolations(3);
            
            if (overdueViolations.isEmpty()) {
                log.info("No overdue violations found for escalation");
                return;
            }
            
            log.info("Found {} overdue violations for escalation", overdueViolations.size());
            
            // Process each overdue violation
            for (var violation : overdueViolations) {
                try {
                    // Escalate the violation to manager
                    violationDetectionService.escalateViolation(
                        violation.getId(), 
                        1L, // System user ID for auto-escalation
                        "Auto-escalated: No explanation provided within 3 days"
                    );
                    
                    log.debug("Escalated overdue violation ID: {} for user: {}", 
                            violation.getId(), violation.getUserId());
                    
                } catch (Exception e) {
                    log.error("Error escalating violation ID: {}", violation.getId(), e);
                }
            }
            
            log.info("Completed overdue violation escalation for {} violations", overdueViolations.size());
            
        } catch (Exception e) {
            log.error("Error during overdue violation escalation", e);
        }
    }
    
    /**
     * Monthly violation cleanup job - runs on the 1st day of each month at 2:00 AM
     * Archives old resolved violations and generates monthly reports
     */
    @Scheduled(cron = "0 0 2 1 * ?")
    public void monthlyViolationCleanup() {
        log.info("Starting monthly violation cleanup and archival");
        
        try {
            LocalDate cutoffDate = LocalDate.now().minusMonths(6); // Archive violations older than 6 months
            
            // Archive old resolved violations
            List<AttendanceViolation> oldViolations = violationRepository
                .findOldResolvedViolations(cutoffDate);
            
            log.info("Found {} old violations to archive (before {})", oldViolations.size(), cutoffDate);
            
            // Update status to archived
            for (AttendanceViolation violation : oldViolations) {
                violation.setStatus(AttendanceViolation.ViolationStatus.ARCHIVED);
                violationRepository.save(violation);
            }
            
            log.info("Archived {} old violations", oldViolations.size());
            
        } catch (Exception e) {
            log.error("Error during monthly violation cleanup", e);
        }
    }
    
    /**
     * Manual violation reprocessing for a specific date range
     * Can be triggered via API or admin interface
     */
    public ViolationDetectionSummary reprocessViolationsForDateRange(LocalDate startDate, LocalDate endDate) {
        log.info("Manual reprocessing violations from {} to {}", startDate, endDate);
        
        try {
            return violationDetectionService.reprocessViolations(startDate, endDate);
        } catch (Exception e) {
            log.error("Error during manual violation reprocessing from {} to {}", startDate, endDate, e);
            throw e;
        }
    }
    
    /**
     * Log violation metrics for monitoring purposes
     */
    private void logViolationMetrics(ViolationDetectionSummary summary) {
        // These logs can be consumed by monitoring systems like ELK stack
        log.info("METRIC: violation.detection.total={}", summary.getTotalViolationsDetected());
        log.info("METRIC: violation.detection.late_arrivals={}", summary.getLateArrivals());
        log.info("METRIC: violation.detection.early_departures={}", summary.getEarlyDepartures());
        log.info("METRIC: violation.detection.missing_check_ins={}", summary.getMissingCheckIns());
        log.info("METRIC: violation.detection.missing_check_outs={}", summary.getMissingCheckOuts());
        log.info("METRIC: violation.detection.absent_without_leave={}", summary.getAbsentWithoutLeave());
        log.info("METRIC: violation.detection.processing_time_ms={}", summary.getProcessingTimeMs());
        log.info("METRIC: violation.detection.duplicates_skipped={}", summary.getDuplicatesSkipped());
    }
}