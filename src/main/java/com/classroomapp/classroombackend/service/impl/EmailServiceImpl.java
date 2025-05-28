package com.classroomapp.classroombackend.service.impl;

import com.classroomapp.classroombackend.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    
    @Override
    public void sendEmail(String to, String subject, String body) {
        // Chỉ ghi log thay vì gửi email thực
        log.info("Would send email to: {}, subject: {}", to, subject);
        log.debug("Email body: {}", body);
    }

    @Override
    public void sendRequestStatusNotification(String to, String fullName, String requestedRole, String status, String reason) {
        String subject = "Your " + requestedRole + " Role Request - " + status;
        String body = generateStatusEmailBody(fullName, requestedRole, status, reason);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendFormCompletionConfirmation(String to, String fullName, String requestedRole) {
        String subject = "Request Received: " + requestedRole + " Role Application";
        String body = generateConfirmationEmailBody(fullName, requestedRole);
        sendEmail(to, subject, body);
    }
    
    private String generateStatusEmailBody(String fullName, String role, String status, String reason) {
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("<!DOCTYPE html><html><head><style>");
        bodyBuilder.append("body {font-family: Arial, sans-serif; line-height: 1.6; color: #333;}");
        
        if ("APPROVED".equals(status)) {
            bodyBuilder.append(".header {background-color: #52c41a; color: white; padding: 20px; text-align: center;}");
            bodyBuilder.append("</style></head><body>");
            bodyBuilder.append("<div class='header'><h1>Your Request Has Been Approved!</h1></div>");
            bodyBuilder.append("<div style='padding: 20px;'>");
            bodyBuilder.append("<p>Dear ").append(fullName).append(",</p>");
            bodyBuilder.append("<p>Congratulations! Your request to become a <strong>").append(role).append("</strong> ");
            bodyBuilder.append("in our system has been approved.</p>");
            bodyBuilder.append("<p>You can now login to the system with your account and access all features ");
            bodyBuilder.append("available for ").append(role).append(" users.</p>");
            bodyBuilder.append("<p>We're excited to have you as a part of our community!</p>");
            bodyBuilder.append("<div style='text-align: center;'>");
            bodyBuilder.append("<a href='https://mvsclassroom.com/login' style='display: inline-block; ");
            bodyBuilder.append("background-color: #1677ff; color: white; padding: 10px 20px; ");
            bodyBuilder.append("text-decoration: none; border-radius: 5px;'>Login Now</a></div>");
        } else {
            bodyBuilder.append(".header {background-color: #f5222d; color: white; padding: 20px; text-align: center;}");
            bodyBuilder.append(".reason-box {background-color: #f9f9f9; border-left: 4px solid #f5222d; padding: 15px; margin: 15px 0;}");
            bodyBuilder.append("</style></head><body>");
            bodyBuilder.append("<div class='header'><h1>Request Status Update</h1></div>");
            bodyBuilder.append("<div style='padding: 20px;'>");
            bodyBuilder.append("<p>Dear ").append(fullName).append(",</p>");
            bodyBuilder.append("<p>We have reviewed your request to become a <strong>").append(role).append("</strong> ");
            bodyBuilder.append("in our system. Unfortunately, we are unable to approve your request at this time.</p>");
            bodyBuilder.append("<div class='reason-box'><strong>Reason:</strong><p>").append(reason).append("</p></div>");
            bodyBuilder.append("<p>You are welcome to submit a new request after addressing the reasons mentioned above. ");
            bodyBuilder.append("If you believe there has been a mistake or would like to provide additional information, ");
            bodyBuilder.append("please contact our support team.</p>");
        }
        
        bodyBuilder.append("<p>Best regards,<br>The MVS Classroom Team</p>");
        bodyBuilder.append("</div><div style='margin-top: 20px; text-align: center; font-size: 12px; color: #666;'>");
        bodyBuilder.append("<p>&copy; MVS Classroom. All rights reserved.</p></div></body></html>");
        
        return bodyBuilder.toString();
    }
    
    private String generateConfirmationEmailBody(String fullName, String role) {
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("<!DOCTYPE html><html><head><style>");
        bodyBuilder.append("body {font-family: Arial, sans-serif; line-height: 1.6; color: #333;}");
        bodyBuilder.append(".header {background-color: #1677ff; color: white; padding: 20px; text-align: center;}");
        bodyBuilder.append("</style></head><body>");
        bodyBuilder.append("<div class='header'><h1>We've Received Your Request</h1></div>");
        bodyBuilder.append("<div style='padding: 20px;'>");
        bodyBuilder.append("<p>Dear ").append(fullName).append(",</p>");
        bodyBuilder.append("<p>Thank you for submitting your request to become a <strong>").append(role).append("</strong> ");
        bodyBuilder.append("in our system. We have received your application and it is now being reviewed by our administrators.</p>");
        bodyBuilder.append("<p>Please note that the review process may take up to 48 hours. You will receive ");
        bodyBuilder.append("another email notification once your request has been processed.</p>");
        bodyBuilder.append("<p>If you have any questions or need to provide additional information, please reply ");
        bodyBuilder.append("to this email or contact our support team.</p>");
        bodyBuilder.append("<p>Best regards,<br>The MVS Classroom Team</p>");
        bodyBuilder.append("</div><div style='margin-top: 20px; text-align: center; font-size: 12px; color: #666;'>");
        bodyBuilder.append("<p>&copy; MVS Classroom. All rights reserved.</p></div></body></html>");
        
        return bodyBuilder.toString();
    }
} 