package com.classroomapp.classroombackend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled tasks for SMS notifications
 */
@Component
public class SMSNotificationScheduler {
    
    private static final Logger log = LoggerFactory.getLogger(SMSNotificationScheduler.class);
    
    @Autowired
    private SMSService smsService;
    
    @Value("${attendance.sms.enabled:true}")
    private boolean smsEnabled;
    
    /**
     * Process scheduled SMS notifications every minute
     * This checks for pending SMS messages that are ready to be sent
     */
    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void processScheduledSMS() {
        if (!smsEnabled) {
            return;
        }
        
        try {
            log.debug("Running scheduled SMS processing task");
            smsService.processScheduledSMS();
        } catch (Exception e) {
            log.error("Error processing scheduled SMS notifications: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Retry failed SMS notifications every 30 minutes
     * This attempts to resend SMS messages that previously failed
     */
    @Scheduled(fixedRate = 30 * 60 * 1000) // Every 30 minutes
    public void retryFailedSMS() {
        if (!smsEnabled) {
            return;
        }
        
        try {
            log.info("Running SMS retry task");
            smsService.retryFailedSMS();
        } catch (Exception e) {
            log.error("Error retrying failed SMS notifications: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Clean up old SMS notifications daily at 2 AM
     * Removes SMS notification records older than 30 days
     */
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2:00 AM
    public void cleanupOldSMSRecords() {
        if (!smsEnabled) {
            return;
        }
        
        try {
            log.info("Running SMS cleanup task");
            // This would call a cleanup method in SMSService if implemented
            // smsService.cleanupOldRecords();
        } catch (Exception e) {
            log.error("Error cleaning up old SMS records: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Generate SMS statistics report daily at 8 AM
     * Logs SMS statistics for monitoring purposes
     */
    @Scheduled(cron = "0 0 8 * * *") // Daily at 8:00 AM
    public void generateSMSStatistics() {
        if (!smsEnabled) {
            return;
        }
        
        try {
            log.info("Generating SMS statistics report");
            var stats = smsService.getSMSStatistics();
            
            log.info("SMS Statistics - Sent: {}, Pending: {}, Failed: {}, Success Rate: {}%",
                    stats.getTotalSent(),
                    stats.getTotalPending(),
                    stats.getTotalFailed(),
                    String.format("%.2f", stats.getSuccessRate()));
                    
        } catch (Exception e) {
            log.error("Error generating SMS statistics: {}", e.getMessage(), e);
        }
    }
}