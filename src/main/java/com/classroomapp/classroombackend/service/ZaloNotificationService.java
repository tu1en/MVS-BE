package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSubmitDto;
import com.classroomapp.classroombackend.dto.notification.ZaloNotificationDto;

/**
 * Service interface for sending Zalo notifications via n8n webhook
 */
public interface ZaloNotificationService {
    
    /**
     * Send attendance notification to parents via n8n webhook
     * 
     * @param submitDto The attendance submission data
     * @param teacherId The ID of the teacher who submitted the attendance
     */
    void sendAttendanceNotification(AttendanceSubmitDto submitDto, Long teacherId);
    
    /**
     * Build notification data from attendance submission
     * 
     * @param submitDto The attendance submission data
     * @param teacherId The ID of the teacher who submitted the attendance
     * @return ZaloNotificationDto containing all necessary data for n8n
     */
    ZaloNotificationDto buildNotificationData(AttendanceSubmitDto submitDto, Long teacherId);
    
    /**
     * Send notification data to n8n webhook
     * 
     * @param notificationData The notification data to send
     * @return true if successful, false otherwise
     */
    boolean sendToN8nWebhook(ZaloNotificationDto notificationData);
    
    /**
     * Check if Zalo notifications are enabled for the system
     * 
     * @return true if enabled, false otherwise
     */
    boolean isZaloNotificationEnabled();
}
