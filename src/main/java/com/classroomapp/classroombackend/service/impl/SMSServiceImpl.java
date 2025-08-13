package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.dto.SMSStatistics;
import com.classroomapp.classroombackend.model.SMSNotification;
import com.classroomapp.classroombackend.model.SMSStatus;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.SMSNotificationRepository;
import com.classroomapp.classroombackend.service.SMSService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SMSServiceImpl implements SMSService {
    
    private static final Logger log = LoggerFactory.getLogger(SMSServiceImpl.class);
    
    @Value("${sms.gateway.enabled:true}")
    private boolean smsEnabled;
    
    @Value("${sms.gateway.base-url}")
    private String gatewayBaseUrl;
    
    @Value("${sms.gateway.username}")
    private String gatewayUsername;
    
    @Value("${sms.gateway.password}")
    private String gatewayPassword;
    
    @Value("${attendance.sms.template}")
    private String smsTemplate;
    
    @Value("${attendance.sms.delay-minutes:15}")
    private int delayMinutes;
    
    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_DELAY_MINUTES = 30;
    
    @Autowired
    private SMSNotificationRepository smsRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Override
    @Async
    public boolean sendSMS(String phoneNumber, String message) {
        if (!smsEnabled) {
            log.info("SMS service is disabled, skipping SMS to {}", phoneNumber);
            return false;
        }
        
        try {
            log.info("Sending SMS to {}: {}", phoneNumber, message);
            
            String url = gatewayBaseUrl + "/message";
            
            // Create request body for Android SMS Gateway
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("phoneNumbers", Arrays.asList(phoneNumber));
            
            Map<String, String> textMessage = new HashMap<>();
            textMessage.put("text", message);
            requestBody.put("textMessage", textMessage);
            
            // Set authentication headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String auth = Base64.getEncoder().encodeToString(
                (gatewayUsername + ":" + gatewayPassword).getBytes()
            );
            headers.set("Authorization", "Basic " + auth);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Send SMS
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("SMS sent successfully to {}", phoneNumber);
                return true;
            } else {
                log.error("Failed to send SMS to {}: {}", phoneNumber, response.getBody());
                return false;
            }
            
        } catch (Exception e) {
            log.error("Error sending SMS to {}: {}", phoneNumber, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    @Transactional
    public void sendAttendanceNotification(User student, Long attendanceSessionId, String className, String sessionTime) {
        if (!smsEnabled) {
            log.info("SMS service is disabled, skipping attendance notification for student {}", student.getId());
            return;
        }
        
        if (student.getParentPhone() == null || student.getParentPhone().trim().isEmpty()) {
            log.warn("No parent phone number for student {} ({})", student.getId(), student.getFullName());
            return;
        }
        
        // Check if notification already exists
        if (smsRepository.existsByStudentIdAndAttendanceSessionId(student.getId(), attendanceSessionId)) {
            log.info("SMS notification already exists for student {} in attendance session {}", 
                    student.getId(), attendanceSessionId);
            return;
        }
        
        // Create message from template
        String message = createAttendanceMessage(student, className, sessionTime);
        
        // Create SMS notification record
        SMSNotification notification = new SMSNotification();
        notification.setStudent(student);
        notification.setAttendanceSessionId(attendanceSessionId);
        notification.setParentPhone(student.getParentPhone());
        notification.setMessageContent(message);
        notification.setSendTime(LocalDateTime.now().plusMinutes(delayMinutes));
        notification.setStatus(SMSStatus.PENDING);
        
        SMSNotification savedNotification = smsRepository.save(notification);
        
        log.info("Created SMS notification {} for student {} to be sent at {}", 
                savedNotification.getId(), student.getId(), savedNotification.getSendTime());
    }
    
    @Override
    @Transactional
    public void processScheduledSMS() {
        if (!smsEnabled) {
            return;
        }
        
        LocalDateTime currentTime = LocalDateTime.now();
        List<SMSNotification> readyToSend = smsRepository.findReadyToSend(currentTime);
        
        log.info("Processing {} scheduled SMS notifications", readyToSend.size());
        
        for (SMSNotification notification : readyToSend) {
            try {
                boolean sent = sendSMS(notification.getParentPhone(), notification.getMessageContent());
                
                if (sent) {
                    notification.setStatus(SMSStatus.SENT);
                    notification.setUpdatedAt(LocalDateTime.now());
                    log.info("Successfully sent SMS notification {}", notification.getId());
                } else {
                    notification.setStatus(SMSStatus.FAILED);
                    notification.setErrorMessage("Failed to send SMS via gateway");
                    notification.setUpdatedAt(LocalDateTime.now());
                    log.error("Failed to send SMS notification {}", notification.getId());
                }
                
                smsRepository.save(notification);
                
            } catch (Exception e) {
                notification.setStatus(SMSStatus.FAILED);
                notification.setErrorMessage(e.getMessage());
                notification.setUpdatedAt(LocalDateTime.now());
                smsRepository.save(notification);
                
                log.error("Error processing SMS notification {}: {}", notification.getId(), e.getMessage(), e);
            }
        }
    }
    
    @Override
    @Transactional
    public void retryFailedSMS() {
        if (!smsEnabled) {
            return;
        }
        
        LocalDateTime retryAfterTime = LocalDateTime.now().minusMinutes(RETRY_DELAY_MINUTES);
        List<SMSNotification> failedNotifications = smsRepository.findFailedForRetry(MAX_RETRY_COUNT, retryAfterTime);
        
        log.info("Retrying {} failed SMS notifications", failedNotifications.size());
        
        for (SMSNotification notification : failedNotifications) {
            try {
                boolean sent = sendSMS(notification.getParentPhone(), notification.getMessageContent());
                
                notification.setRetryCount(notification.getRetryCount() + 1);
                notification.setLastRetryTime(LocalDateTime.now());
                
                if (sent) {
                    notification.setStatus(SMSStatus.SENT);
                    notification.setErrorMessage(null);
                    log.info("Successfully retried SMS notification {}", notification.getId());
                } else {
                    if (notification.getRetryCount() >= MAX_RETRY_COUNT) {
                        notification.setStatus(SMSStatus.FAILED);
                        notification.setErrorMessage("Max retry count reached");
                        log.error("Max retry count reached for SMS notification {}", notification.getId());
                    } else {
                        notification.setStatus(SMSStatus.RETRY);
                        log.warn("Retry failed for SMS notification {}, will retry again later", notification.getId());
                    }
                }
                
                smsRepository.save(notification);
                
            } catch (Exception e) {
                notification.setRetryCount(notification.getRetryCount() + 1);
                notification.setLastRetryTime(LocalDateTime.now());
                
                if (notification.getRetryCount() >= MAX_RETRY_COUNT) {
                    notification.setStatus(SMSStatus.FAILED);
                    notification.setErrorMessage("Max retry count reached: " + e.getMessage());
                } else {
                    notification.setStatus(SMSStatus.RETRY);
                    notification.setErrorMessage(e.getMessage());
                }
                
                smsRepository.save(notification);
                log.error("Error retrying SMS notification {}: {}", notification.getId(), e.getMessage(), e);
            }
        }
    }
    
    @Override
    public String createAttendanceMessage(User student, String className, String sessionTime) {
        String parentName = student.getParentName() != null && !student.getParentName().trim().isEmpty() 
                          ? student.getParentName() 
                          : "Quý phụ huynh";
        
        return smsTemplate
                .replace("{parentName}", parentName)
                .replace("{studentName}", student.getFullName())
                .replace("{className}", className)
                .replace("{time}", sessionTime);
    }
    
    @Override
    public boolean isSmsEnabled() {
        return smsEnabled;
    }
    
    @Override
    @Transactional(readOnly = true)
    public SMSStatistics getSMSStatistics() {
        LocalDateTime lastWeek = LocalDateTime.now().minusDays(7);
        
        Long totalSent = smsRepository.countByStatus(SMSStatus.SENT);
        Long totalPending = smsRepository.countByStatus(SMSStatus.PENDING);
        Long totalFailed = smsRepository.countByStatus(SMSStatus.FAILED);
        Long sentToday = smsRepository.countSentToday();
        
        // Get status breakdown
        List<Object[]> statusStats = smsRepository.getStatusStatistics(lastWeek);
        Map<String, Long> statusBreakdown = new HashMap<>();
        
        for (Object[] stat : statusStats) {
            String status = stat[0].toString();
            Long count = (Long) stat[1];
            statusBreakdown.put(status, count);
        }
        
        // Calculate success rate
        Long totalAttempts = totalSent + totalFailed;
        Double successRate = totalAttempts > 0 ? (totalSent.doubleValue() / totalAttempts.doubleValue()) * 100 : 0.0;
        
        return new SMSStatistics(totalSent, totalPending, totalFailed, sentToday, statusBreakdown, successRate);
    }
}