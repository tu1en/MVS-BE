package com.classroomapp.classroombackend.service.hrmanagement;

import java.time.LocalDate;
import java.util.List;

import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation;

/**
 * Service interface for detecting attendance violations
 */
public interface ViolationDetectionService {
    
    /**
     * Detect violations for a specific date
     * @param date the date to check violations for
     * @return list of detected violations
     */
    List<AttendanceViolation> detectViolationsForDate(LocalDate date);
    
    /**
     * Detect violations for a specific user on a specific date
     * @param userId the user ID
     * @param date the date to check
     * @return list of detected violations for the user
     */
    List<AttendanceViolation> detectViolationsForUser(Long userId, LocalDate date);
    
    /**
     * Detect late arrival violations
     * @param date the date to check
     * @param toleranceMinutes tolerance in minutes
     * @return list of late arrival violations
     */
    List<AttendanceViolation> detectLateArrivals(LocalDate date, int toleranceMinutes);
    
    /**
     * Detect early departure violations
     * @param date the date to check
     * @param toleranceMinutes tolerance in minutes
     * @return list of early departure violations
     */
    List<AttendanceViolation> detectEarlyDepartures(LocalDate date, int toleranceMinutes);
    
    /**
     * Detect missing check-in violations
     * @param date the date to check
     * @return list of missing check-in violations
     */
    List<AttendanceViolation> detectMissingCheckIns(LocalDate date);
    
    /**
     * Detect missing check-out violations
     * @param date the date to check
     * @return list of missing check-out violations
     */
    List<AttendanceViolation> detectMissingCheckOuts(LocalDate date);
    
    /**
     * Detect absent without leave violations
     * @param date the date to check
     * @return list of absent violations
     */
    List<AttendanceViolation> detectAbsentWithoutLeave(LocalDate date);
    
    /**
     * Run daily violation detection (for cron job)
     * @param date the date to process (usually yesterday)
     * @return summary of detected violations
     */
    ViolationDetectionSummary runDailyDetection(LocalDate date);
    
    /**
     * Reprocess violations for a date range
     * @param startDate start date
     * @param endDate end date
     * @return summary of reprocessed violations
     */
    ViolationDetectionSummary reprocessViolations(LocalDate startDate, LocalDate endDate);
    
    /**
     * Get violation detection configuration
     * @return current configuration
     */
    ViolationDetectionConfig getDetectionConfig();
    
    /**
     * Update violation detection configuration
     * @param config new configuration
     */
    void updateDetectionConfig(ViolationDetectionConfig config);
    
    /**
     * Check if violation already exists
     * @param userId user ID
     * @param date violation date
     * @param violationType violation type
     * @return true if violation exists
     */
    boolean violationExists(Long userId, LocalDate date, AttendanceViolation.ViolationType violationType);
    
    /**
     * Calculate violation severity based on deviation
     * @param violationType the violation type
     * @param deviationMinutes deviation in minutes
     * @return calculated severity
     */
    AttendanceViolation.ViolationSeverity calculateSeverity(AttendanceViolation.ViolationType violationType, 
                                                           int deviationMinutes);
    
    /**
     * Generate system description for violation
     * @param violationType the violation type
     * @param deviationMinutes deviation in minutes
     * @param expectedTime expected time
     * @param actualTime actual time
     * @return system-generated description
     */
    String generateSystemDescription(AttendanceViolation.ViolationType violationType,
                                   Integer deviationMinutes,
                                   java.time.LocalTime expectedTime,
                                   java.time.LocalTime actualTime);
    
    /**
     * Summary class for violation detection results
     */
    class ViolationDetectionSummary {
        private LocalDate processedDate;
        private int totalViolationsDetected;
        private int lateArrivals;
        private int earlyDepartures;
        private int missingCheckIns;
        private int missingCheckOuts;
        private int absentWithoutLeave;
        private int duplicatesSkipped;
        private long processingTimeMs;
        
        // Constructors, getters, and setters
        public ViolationDetectionSummary() {}
        
        public ViolationDetectionSummary(LocalDate processedDate) {
            this.processedDate = processedDate;
        }
        
        // Getters and setters
        public LocalDate getProcessedDate() { return processedDate; }
        public void setProcessedDate(LocalDate processedDate) { this.processedDate = processedDate; }
        
        public int getTotalViolationsDetected() { return totalViolationsDetected; }
        public void setTotalViolationsDetected(int totalViolationsDetected) { this.totalViolationsDetected = totalViolationsDetected; }
        
        public int getLateArrivals() { return lateArrivals; }
        public void setLateArrivals(int lateArrivals) { this.lateArrivals = lateArrivals; }
        
        public int getEarlyDepartures() { return earlyDepartures; }
        public void setEarlyDepartures(int earlyDepartures) { this.earlyDepartures = earlyDepartures; }
        
        public int getMissingCheckIns() { return missingCheckIns; }
        public void setMissingCheckIns(int missingCheckIns) { this.missingCheckIns = missingCheckIns; }
        
        public int getMissingCheckOuts() { return missingCheckOuts; }
        public void setMissingCheckOuts(int missingCheckOuts) { this.missingCheckOuts = missingCheckOuts; }
        
        public int getAbsentWithoutLeave() { return absentWithoutLeave; }
        public void setAbsentWithoutLeave(int absentWithoutLeave) { this.absentWithoutLeave = absentWithoutLeave; }
        
        public int getDuplicatesSkipped() { return duplicatesSkipped; }
        public void setDuplicatesSkipped(int duplicatesSkipped) { this.duplicatesSkipped = duplicatesSkipped; }
        
        public long getProcessingTimeMs() { return processingTimeMs; }
        public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }
        
        public void incrementLateArrivals() { this.lateArrivals++; this.totalViolationsDetected++; }
        public void incrementEarlyDepartures() { this.earlyDepartures++; this.totalViolationsDetected++; }
        public void incrementMissingCheckIns() { this.missingCheckIns++; this.totalViolationsDetected++; }
        public void incrementMissingCheckOuts() { this.missingCheckOuts++; this.totalViolationsDetected++; }
        public void incrementAbsentWithoutLeave() { this.absentWithoutLeave++; this.totalViolationsDetected++; }
        public void incrementDuplicatesSkipped() { this.duplicatesSkipped++; }

        @Override
        public String toString() {
            return String.format("ViolationDetectionSummary{date=%s, total=%d, late=%d, early=%d, missingIn=%d, missingOut=%d, absent=%d, duplicates=%d, time=%dms}",
                processedDate, totalViolationsDetected, lateArrivals, earlyDepartures, missingCheckIns, missingCheckOuts, absentWithoutLeave, duplicatesSkipped, processingTimeMs);
        }
    }

    /**
     * Configuration class for violation detection
     */
    class ViolationDetectionConfig {
        private int lateArrivalToleranceMinutes = 15;
        private int earlyDepartureToleranceMinutes = 15;
        private int minorViolationThresholdMinutes = 15;
        private int moderateViolationThresholdMinutes = 30;
        private int majorViolationThresholdMinutes = 60;
        private boolean enableAutoDetection = true;
        private boolean enableNotifications = true;
        
        // Constructors, getters, and setters
        public ViolationDetectionConfig() {}
        
        public int getLateArrivalToleranceMinutes() { return lateArrivalToleranceMinutes; }
        public void setLateArrivalToleranceMinutes(int lateArrivalToleranceMinutes) { this.lateArrivalToleranceMinutes = lateArrivalToleranceMinutes; }
        
        public int getEarlyDepartureToleranceMinutes() { return earlyDepartureToleranceMinutes; }
        public void setEarlyDepartureToleranceMinutes(int earlyDepartureToleranceMinutes) { this.earlyDepartureToleranceMinutes = earlyDepartureToleranceMinutes; }
        
        public int getMinorViolationThresholdMinutes() { return minorViolationThresholdMinutes; }
        public void setMinorViolationThresholdMinutes(int minorViolationThresholdMinutes) { this.minorViolationThresholdMinutes = minorViolationThresholdMinutes; }
        
        public int getModerateViolationThresholdMinutes() { return moderateViolationThresholdMinutes; }
        public void setModerateViolationThresholdMinutes(int moderateViolationThresholdMinutes) { this.moderateViolationThresholdMinutes = moderateViolationThresholdMinutes; }
        
        public int getMajorViolationThresholdMinutes() { return majorViolationThresholdMinutes; }
        public void setMajorViolationThresholdMinutes(int majorViolationThresholdMinutes) { this.majorViolationThresholdMinutes = majorViolationThresholdMinutes; }
        
        public boolean isEnableAutoDetection() { return enableAutoDetection; }
        public void setEnableAutoDetection(boolean enableAutoDetection) { this.enableAutoDetection = enableAutoDetection; }
        
        public boolean isEnableNotifications() { return enableNotifications; }
        public void setEnableNotifications(boolean enableNotifications) { this.enableNotifications = enableNotifications; }
    }
}
