package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.SMSStatistics;
import com.classroomapp.classroombackend.service.SMSService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for SMS management and configuration
 */
@RestController
@RequestMapping("/api/sms")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
public class SMSController {
    
    private static final Logger log = LoggerFactory.getLogger(SMSController.class);
    
    @Autowired
    private SMSService smsService;
    
    /**
     * Get SMS statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<SMSStatistics> getSMSStatistics() {
        try {
            log.info("Fetching SMS statistics");
            SMSStatistics statistics = smsService.getSMSStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("Error fetching SMS statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Check if SMS service is enabled
     */
    @GetMapping("/status")
    public ResponseEntity<SmsStatusResponse> getSMSStatus() {
        try {
            log.info("Checking SMS service status");
            boolean enabled = smsService.isSmsEnabled();
            return ResponseEntity.ok(new SmsStatusResponse(enabled));
        } catch (Exception e) {
            log.error("Error checking SMS status: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Test SMS sending
     */
    @PostMapping("/test")
    public ResponseEntity<TestSmsResponse> testSMS(@RequestBody TestSmsRequest request) {
        try {
            log.info("Testing SMS sending to {}", request.getPhoneNumber());
            
            if (!smsService.isSmsEnabled()) {
                return ResponseEntity.ok(new TestSmsResponse(false, "SMS service is disabled"));
            }
            
            boolean sent = smsService.sendSMS(request.getPhoneNumber(), request.getMessage());
            
            if (sent) {
                return ResponseEntity.ok(new TestSmsResponse(true, "SMS sent successfully"));
            } else {
                return ResponseEntity.ok(new TestSmsResponse(false, "Failed to send SMS"));
            }
            
        } catch (Exception e) {
            log.error("Error testing SMS: {}", e.getMessage(), e);
            return ResponseEntity.ok(new TestSmsResponse(false, "Error: " + e.getMessage()));
        }
    }
    
    // DTOs for request/response
    public static class SmsStatusResponse {
        private boolean enabled;
        
        public SmsStatusResponse(boolean enabled) {
            this.enabled = enabled;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }
    
    public static class TestSmsRequest {
        private String phoneNumber;
        private String message;
        
        public String getPhoneNumber() {
            return phoneNumber;
        }
        
        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    public static class TestSmsResponse {
        private boolean success;
        private String message;
        
        public TestSmsResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}