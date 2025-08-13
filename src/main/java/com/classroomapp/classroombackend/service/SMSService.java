package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.SMSStatistics;
import com.classroomapp.classroombackend.model.usermanagement.User;

/**
 * Service interface for SMS operations
 */
public interface SMSService {
    
    /**
     * Send SMS to a phone number
     * @param phoneNumber the recipient phone number
     * @param message the message content
     * @return true if sent successfully, false otherwise
     */
    boolean sendSMS(String phoneNumber, String message);
    
    /**
     * Send attendance notification SMS to parent
     * @param student the absent student
     * @param attendanceSessionId the attendance session ID
     * @param className the class name
     * @param sessionTime the session time
     */
    void sendAttendanceNotification(User student, Long attendanceSessionId, String className, String sessionTime);
    
    /**
     * Process scheduled SMS notifications
     * This method is called by the scheduler to send pending SMS messages
     */
    void processScheduledSMS();
    
    /**
     * Retry failed SMS notifications
     */
    void retryFailedSMS();
    
    /**
     * Create SMS message content from template
     * @param student the student
     * @param className the class name
     * @param sessionTime the session time
     * @return formatted SMS message
     */
    String createAttendanceMessage(User student, String className, String sessionTime);
    
    /**
     * Check if SMS service is enabled
     * @return true if SMS service is enabled
     */
    boolean isSmsEnabled();
    
    /**
     * Get SMS statistics
     * @return SMS statistics object
     */
    SMSStatistics getSMSStatistics();
}