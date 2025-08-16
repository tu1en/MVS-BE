package com.classroomapp.classroombackend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/test")
public class TestWebhookController {

    @PostMapping("/webhook-test")
    public ResponseEntity<Map<String, Object>> testWebhook() {
        try {
            // Tạo dữ liệu test đơn giản
            Map<String, Object> testData = new HashMap<>();
            testData.put("event", "TEST_ATTENDANCE");
            testData.put("message", "Đây là test webhook từ hệ thống điểm danh");
            testData.put("timestamp", System.currentTimeMillis());
            testData.put("testData", "Test thành công!");
            
            // Gửi webhook đến N8N
            sendTestWebhookToN8N(testData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test webhook đã được gửi thành công");
            response.put("data", testData);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Error in testWebhook: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    private void sendTestWebhookToN8N(Map<String, Object> testData) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // URL N8N webhook - bạn có thể thay đổi port nếu cần
            String n8nUrl = "http://localhost:5678/webhook/test-attendance";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(testData, headers);
            
            // Gửi POST request
            restTemplate.postForEntity(n8nUrl, request, String.class);
            
            System.out.println("✅ Test webhook sent to N8N successfully");
            System.out.println("📤 Data sent: " + testData);
            
        } catch (Exception e) {
            System.err.println("❌ Failed to send test webhook to N8N: " + e.getMessage());
            throw e; // Re-throw để controller có thể xử lý
        }
    }
    
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Test controller is working");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
