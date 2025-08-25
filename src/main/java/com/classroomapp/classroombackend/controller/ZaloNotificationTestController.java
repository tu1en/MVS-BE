package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSubmitDto;
import com.classroomapp.classroombackend.dto.notification.ZaloNotificationDto;
import com.classroomapp.classroombackend.service.ZaloNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for Zalo notification integration
 */
@RestController
@RequestMapping("/api/test/zalo-notification")
@RequiredArgsConstructor
@Slf4j
public class ZaloNotificationTestController {
    
    private final ZaloNotificationService zaloNotificationService;
    
    /**
     * Test endpoint to send sample Zalo notification
     */
    @PostMapping("/send-test")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> sendTestNotification() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Testing Zalo notification integration...");
            
            // Create sample attendance submission data
            AttendanceSubmitDto.AttendanceRecord record1 = new AttendanceSubmitDto.AttendanceRecord();
            record1.setStudentId(1L); // Assuming student with ID 1 exists
            record1.setStatus("ABSENT");
            record1.setNote("Ốm");
            
            AttendanceSubmitDto.AttendanceRecord record2 = new AttendanceSubmitDto.AttendanceRecord();
            record2.setStudentId(2L); // Assuming student with ID 2 exists
            record2.setStatus("PRESENT");
            record2.setNote("");
            
            AttendanceSubmitDto submitDto = new AttendanceSubmitDto();
            submitDto.setClassroomId(1L); // Assuming classroom with ID 1 exists
            submitDto.setLectureId(1L); // Assuming lecture with ID 1 exists
            submitDto.setRecords(Arrays.asList(record1, record2));
            
            // Test with teacher ID 2 (assuming teacher exists)
            Long teacherId = 2L;
            
            // Send notification
            zaloNotificationService.sendAttendanceNotification(submitDto, teacherId);
            
            response.put("success", true);
            response.put("message", "Test notification sent successfully");
            response.put("timestamp", LocalDateTime.now());
            
            log.info("Test notification sent successfully");
            
        } catch (Exception e) {
            log.error("Error sending test notification: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test endpoint to build notification data without sending
     */
    @PostMapping("/build-test-data")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER')")
    public ResponseEntity<ZaloNotificationDto> buildTestNotificationData() {
        try {
            log.info("Building test notification data...");
            
            // Create sample attendance submission data
            AttendanceSubmitDto.AttendanceRecord record = new AttendanceSubmitDto.AttendanceRecord();
            record.setStudentId(1L);
            record.setStatus("ABSENT");
            record.setNote("Ốm");
            
            AttendanceSubmitDto submitDto = new AttendanceSubmitDto();
            submitDto.setClassroomId(1L);
            submitDto.setLectureId(1L);
            submitDto.setRecords(Arrays.asList(record));
            
            // Build notification data
            ZaloNotificationDto notificationData = zaloNotificationService.buildNotificationData(submitDto, 2L);
            
            log.info("Test notification data built successfully");
            return ResponseEntity.ok(notificationData);
            
        } catch (Exception e) {
            log.error("Error building test notification data: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Test endpoint to check if Zalo notification is enabled
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getNotificationStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("zaloNotificationEnabled", zaloNotificationService.isZaloNotificationEnabled());
        status.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Test endpoint to send custom notification data
     */
    @PostMapping("/send-custom")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> sendCustomNotification(@RequestBody AttendanceSubmitDto submitDto) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Sending custom notification for classroomId: {}, lectureId: {}", 
                    submitDto.getClassroomId(), submitDto.getLectureId());
            
            // Use teacher ID 2 as default for testing
            Long teacherId = 2L;
            
            // Send notification
            zaloNotificationService.sendAttendanceNotification(submitDto, teacherId);
            
            response.put("success", true);
            response.put("message", "Custom notification sent successfully");
            response.put("classroomId", submitDto.getClassroomId());
            response.put("lectureId", submitDto.getLectureId());
            response.put("recordCount", submitDto.getRecords().size());
            response.put("timestamp", LocalDateTime.now());
            
            log.info("Custom notification sent successfully");
            
        } catch (Exception e) {
            log.error("Error sending custom notification: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Test endpoint to send raw notification data to n8n webhook
     */
    @PostMapping("/send-raw")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> sendRawNotification(@RequestBody ZaloNotificationDto notificationData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("Sending raw notification data to n8n webhook...");
            
            boolean success = zaloNotificationService.sendToN8nWebhook(notificationData);
            
            response.put("success", success);
            response.put("message", success ? "Raw notification sent successfully" : "Failed to send raw notification");
            response.put("timestamp", LocalDateTime.now());
            
            log.info("Raw notification result: {}", success);
            
        } catch (Exception e) {
            log.error("Error sending raw notification: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
        }
        
        return ResponseEntity.ok(response);
    }
}
