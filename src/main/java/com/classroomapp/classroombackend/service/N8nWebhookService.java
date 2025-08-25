package com.classroomapp.classroombackend.service;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSubmitDto;
import com.classroomapp.classroombackend.dto.notification.ZaloNotificationDto;
import com.classroomapp.classroombackend.dto.TimetableEventDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service để tích hợp với n8n workflows
 * Xử lý việc gửi data đến n8n webhooks và nhận callbacks
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class N8nWebhookService {
    
    private final RestTemplate restTemplate;
    private final ZaloNotificationService zaloNotificationService;
    
    @Value("${n8n.webhook.base-url:http://localhost:5678}")
    private String n8nBaseUrl;
    
    @Value("${n8n.webhook.attendance-path:/webhook/attendance-notification}")
    private String attendanceWebhookPath;
    
    @Value("${n8n.webhook.timetable-path:/webhook/timetable-notification}")
    private String timetableWebhookPath;
    
    @Value("${n8n.webhook.enabled:true}")
    private boolean n8nEnabled;
    
    /**
     * Trigger n8n workflow cho attendance notification
     */
    public boolean triggerAttendanceNotification(AttendanceSubmitDto attendanceData, Authentication authentication) {
        if (!n8nEnabled) {
            log.warn("⚠️ n8n webhook is disabled, skipping notification");
            return false;
        }
        
        try {
            log.info("🚀 Triggering n8n attendance notification workflow...");
            
            // Build notification data sử dụng existing service
            ZaloNotificationDto notificationData = zaloNotificationService.buildNotificationData(attendanceData, 1L); // TODO: Get real teacher ID
            
            // Gửi đến n8n webhook
            String webhookUrl = n8nBaseUrl + attendanceWebhookPath;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<ZaloNotificationDto> request = new HttpEntity<>(notificationData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(webhookUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ n8n attendance notification triggered successfully");
                return true;
            } else {
                log.error("❌ n8n webhook returned error status: {}", response.getStatusCode());
                return false;
            }
            
        } catch (Exception e) {
            log.error("❌ Error triggering n8n attendance notification: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Trigger n8n workflow cho timetable notification
     */
    public boolean triggerTimetableNotification(TimetableEventDto eventData) {
        if (!n8nEnabled) {
            log.warn("⚠️ n8n webhook is disabled, skipping notification");
            return false;
        }
        
        try {
            log.info("🚀 Triggering n8n timetable notification workflow for event: {}", eventData.getTitle());
            
            // Tạo payload cho timetable notification
            Map<String, Object> payload = new HashMap<>();
            payload.put("event", eventData);
            payload.put("timestamp", LocalDateTime.now());
            payload.put("type", "TIMETABLE_EVENT_CREATED");
            
            // Gửi đến n8n webhook
            String webhookUrl = n8nBaseUrl + timetableWebhookPath;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(webhookUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ n8n timetable notification triggered successfully");
                return true;
            } else {
                log.error("❌ n8n webhook returned error status: {}", response.getStatusCode());
                return false;
            }
            
        } catch (Exception e) {
            log.error("❌ Error triggering n8n timetable notification: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Xử lý callback từ n8n sau khi workflow hoàn thành
     */
    public void handleNotificationCallback(Map<String, Object> callbackData) {
        try {
            log.info("📞 Processing n8n callback: {}", callbackData);
            
            String type = (String) callbackData.get("type");
            Boolean success = (Boolean) callbackData.get("success");
            String message = (String) callbackData.get("message");
            
            // Log kết quả
            if (Boolean.TRUE.equals(success)) {
                log.info("✅ n8n workflow completed successfully: {} - {}", type, message);
            } else {
                log.error("❌ n8n workflow failed: {} - {}", type, message);
            }
            
            // TODO: Lưu vào database để tracking
            // notificationLogService.saveNotificationResult(callbackData);
            
        } catch (Exception e) {
            log.error("❌ Error processing n8n callback: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Test kết nối với n8n
     */
    public boolean testConnection() {
        try {
            String testUrl = n8nBaseUrl + "/healthz";
            ResponseEntity<String> response = restTemplate.getForEntity(testUrl, String.class);
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            log.error("❌ n8n connection test failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Tạo test notification data
     */
    public ZaloNotificationDto createTestNotificationData() {
        // Tạo test attendance data
        AttendanceSubmitDto.AttendanceRecord record = new AttendanceSubmitDto.AttendanceRecord();
        record.setStudentId(1L);
        record.setStatus("ABSENT");
        record.setNote("Test notification từ n8n integration");
        
        AttendanceSubmitDto testAttendance = new AttendanceSubmitDto();
        testAttendance.setClassroomId(1L);
        testAttendance.setLectureId(1L);
        testAttendance.setRecords(Arrays.asList(record));
        
        // Build notification data
        return zaloNotificationService.buildNotificationData(testAttendance, 1L);
    }
    
    /**
     * Gửi test notification
     */
    public boolean sendTestNotification(ZaloNotificationDto testData) {
        try {
            String webhookUrl = n8nBaseUrl + attendanceWebhookPath;
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-Test-Mode", "true");
            
            HttpEntity<ZaloNotificationDto> request = new HttpEntity<>(testData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(webhookUrl, request, Map.class);
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            log.error("❌ Error sending test notification: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Lấy thống kê notifications
     */
    public Map<String, Object> getNotificationStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("n8nEnabled", n8nEnabled);
        stats.put("n8nBaseUrl", n8nBaseUrl);
        stats.put("attendanceWebhookPath", attendanceWebhookPath);
        stats.put("timetableWebhookPath", timetableWebhookPath);
        stats.put("connectionStatus", testConnection());
        stats.put("timestamp", LocalDateTime.now());
        
        // TODO: Thêm thống kê từ database
        // stats.put("totalNotificationsSent", notificationLogService.getTotalCount());
        // stats.put("successRate", notificationLogService.getSuccessRate());
        
        return stats;
    }
    
    /**
     * Getter cho n8n base URL (để test)
     */
    public String getN8nBaseUrl() {
        return n8nBaseUrl;
    }
}
