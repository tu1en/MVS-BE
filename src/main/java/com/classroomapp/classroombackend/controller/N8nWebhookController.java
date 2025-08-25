package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSubmitDto;
import com.classroomapp.classroombackend.dto.notification.ZaloNotificationDto;
import com.classroomapp.classroombackend.service.N8nWebhookService;
import com.classroomapp.classroombackend.service.TimetableService;
import com.classroomapp.classroombackend.dto.TimetableEventDto;
import com.classroomapp.classroombackend.dto.CreateEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller để tích hợp với n8n workflows
 * Xử lý các webhook events và trigger automation workflows
 */
@RestController
@RequestMapping("/api/n8n/webhooks")
@RequiredArgsConstructor
@Slf4j
public class N8nWebhookController {
    
    private final N8nWebhookService n8nWebhookService;
    private final TimetableService timetableService;
    
    /**
     * Webhook endpoint để trigger Zalo notification workflow khi có điểm danh
     */
    @PostMapping("/attendance-submitted")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> onAttendanceSubmitted(
            @Valid @RequestBody AttendanceSubmitDto attendanceData,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("🔔 N8n webhook triggered: attendance submitted for classroom {}", 
                    attendanceData.getClassroomId());
            
            // Gọi n8n webhook service để xử lý
            boolean success = n8nWebhookService.triggerAttendanceNotification(attendanceData, authentication);
            
            response.put("success", success);
            response.put("message", success ? "Attendance notification workflow triggered" : "Failed to trigger workflow");
            response.put("classroomId", attendanceData.getClassroomId());
            response.put("recordCount", attendanceData.getRecords().size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error triggering attendance notification workflow: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Webhook endpoint để trigger notification khi tạo timetable event mới
     */
    @PostMapping("/timetable-event-created")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> onTimetableEventCreated(
            @Valid @RequestBody CreateEventDto eventData,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("🔔 N8n webhook triggered: timetable event created - {}", eventData.getTitle());
            
            // Tạo event trước
            TimetableEventDto createdEvent = timetableService.createEvent(eventData, 1L); // TODO: Get real user ID
            
            // Trigger n8n workflow để gửi thông báo
            boolean success = n8nWebhookService.triggerTimetableNotification(createdEvent);
            
            response.put("success", success);
            response.put("message", success ? "Timetable notification workflow triggered" : "Failed to trigger workflow");
            response.put("eventId", createdEvent.getId());
            response.put("eventTitle", createdEvent.getTitle());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error triggering timetable notification workflow: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Webhook endpoint để nhận callback từ n8n sau khi xử lý xong
     */
    @PostMapping("/notification-callback")
    public ResponseEntity<Map<String, Object>> onNotificationCallback(
            @RequestBody Map<String, Object> callbackData) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("📞 N8n callback received: {}", callbackData);
            
            // Xử lý callback từ n8n (log kết quả, update database, etc.)
            n8nWebhookService.handleNotificationCallback(callbackData);
            
            response.put("success", true);
            response.put("message", "Callback processed successfully");
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error processing n8n callback: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Test endpoint để kiểm tra kết nối với n8n
     */
    @GetMapping("/test-connection")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> testN8nConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isConnected = n8nWebhookService.testConnection();
            
            response.put("connected", isConnected);
            response.put("message", isConnected ? "n8n connection successful" : "n8n connection failed");
            response.put("n8nUrl", n8nWebhookService.getN8nBaseUrl());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error testing n8n connection: {}", e.getMessage(), e);
            response.put("connected", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Endpoint để trigger manual test notification
     */
    @PostMapping("/test-notification")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> sendTestNotification() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("🧪 Sending test notification via n8n...");
            
            // Tạo test data
            ZaloNotificationDto testData = n8nWebhookService.createTestNotificationData();
            
            // Gửi qua n8n
            boolean success = n8nWebhookService.sendTestNotification(testData);
            
            response.put("success", success);
            response.put("message", success ? "Test notification sent successfully" : "Failed to send test notification");
            response.put("testData", testData);
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error sending test notification: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Endpoint để lấy thống kê notifications
     */
    @GetMapping("/notification-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        try {
            Map<String, Object> stats = n8nWebhookService.getNotificationStatistics();
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("❌ Error getting notification stats: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
