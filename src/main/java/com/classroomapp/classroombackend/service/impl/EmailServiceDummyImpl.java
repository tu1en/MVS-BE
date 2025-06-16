package com.classroomapp.classroombackend.service.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.service.EmailService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@ConditionalOnProperty(name = "email.service.enabled", havingValue = "false", matchIfMissing = true)
public class EmailServiceDummyImpl implements EmailService {
    
    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("DUMMY EMAIL SERVICE: Would send email to {} with subject: {}", to, subject);
        log.debug("Email body: {}", body);
    }
    
    @Override
    public void sendRequestStatusNotification(String to, String fullName, String requestedRole, String status, String reason) {
        log.info("DUMMY EMAIL SERVICE: Would send request status notification to {} ({}) - Role: {}, Status: {}", 
                to, fullName, requestedRole, status);
        if (reason != null) {
            log.info("Reason: {}", reason);
        }
    }
    
    @Override
    public void sendAccountInfoEmail(String to, String fullName, String role, String username, String password) {
        log.info("DUMMY EMAIL SERVICE: Would send account info email to {} ({}) - Username: {}", 
                to, fullName, username);
    }
    
    @Override
    public void sendFormCompletionConfirmation(String to, String fullName, String requestedRole) {
        log.info("DUMMY EMAIL SERVICE: Would send form completion confirmation to {} ({}) - Role: {}", 
                to, fullName, requestedRole);
    }
}
