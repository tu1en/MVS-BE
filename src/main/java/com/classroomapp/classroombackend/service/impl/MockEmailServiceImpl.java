package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@Slf4j
public class MockEmailServiceImpl implements EmailService {

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("MOCK EMAIL: To: {}, Subject: {}", to, subject);
        log.info("MOCK EMAIL BODY: {}", body);
    }

    @Override
    public void sendRequestStatusNotification(String to, String fullName, String requestedRole, String status, String reason) {
        log.info("MOCK STATUS NOTIFICATION: To: {}, Full Name: {}, Role: {}, Status: {}", to, fullName, requestedRole, status);
        if (reason != null) {
            log.info("MOCK REJECTION REASON: {}", reason);
        }
    }

    @Override
    public void sendFormCompletionConfirmation(String to, String fullName, String requestedRole) {
        log.info("MOCK FORM CONFIRMATION: To: {}, Full Name: {}, Role: {}", to, fullName, requestedRole);
    }
} 