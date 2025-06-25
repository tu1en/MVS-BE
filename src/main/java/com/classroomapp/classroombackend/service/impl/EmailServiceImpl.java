package com.classroomapp.classroombackend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.classroomapp.classroombackend.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@ConditionalOnProperty(name = "email.service.enabled", havingValue = "true", matchIfMissing = false)
public class EmailServiceImpl implements EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Value("${spring.mail.from.email}")
    private String fromEmail;
    
    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // Set to true for HTML content
            
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: " + to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendRequestStatusNotification(String to, String fullName, String requestedRole, String status, String reason) {
        String subject;
        String body;        
        if ("APPROVED".equals(status)) {
            subject = "Yêu cầu " + requestedRole + " của bạn đã được chấp thuận";
            Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("role", requestedRole);
            body = templateEngine.process("email/request-approved", context);
        } else {
            subject = "Yêu cầu " + requestedRole + " của bạn đã bị từ chối";
            Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("role", requestedRole);
            context.setVariable("reason", reason);
            body = templateEngine.process("email/request-rejected", context);
        }
        
        sendEmail(to, subject, body);
    }    @Override
    public void sendAccountInfoEmail(String to, String fullName, String role, String username, String password) {
        String subject = "Thông tin tài khoản MVS Classroom của bạn";
        String body = generateAccountInfoEmailBody(fullName, role, username, password);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendFormCompletionConfirmation(String to, String fullName, String requestedRole) {
        String subject = "Đã nhận yêu cầu: Đăng ký vai trò " + requestedRole;
        Context context = new Context();
        context.setVariable("name", fullName);
        context.setVariable("role", requestedRole);
        String body = templateEngine.process("email/request-received", context);
        sendEmail(to, subject, body);
    }
      // This method is kept for potential future use
    private String generateStatusEmailBody(String fullName, String role, String status, String reason) {
        if ("APPROVED".equals(status)) {
            Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("role", role);
            return templateEngine.process("email/request-approved", context);
        } else {
            Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("role", role);
            context.setVariable("reason", reason);
            return templateEngine.process("email/request-rejected", context);
        }
    }
    

    private String generateAccountInfoEmailBody(String fullName, String role, String username, String password) {
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("<!DOCTYPE html><html><head><style>");
        bodyBuilder.append("body {font-family: Arial, sans-serif; line-height: 1.6; color: #333;}");
        bodyBuilder.append(".header {background-color: #52c41a; color: white; padding: 20px; text-align: center;}");
        bodyBuilder.append(".credentials {background-color: #f9f9f9; border: 1px solid #ddd; padding: 15px; margin: 15px 0; border-radius: 5px;}");
        bodyBuilder.append("</style></head><body>");
        bodyBuilder.append("<div class='header'><h1>Tài khoản MVS Classroom của bạn</h1></div>");
        bodyBuilder.append("<div style='padding: 20px;'>");
        bodyBuilder.append("<p>Kính gửi ").append(fullName).append(",</p>");
        bodyBuilder.append("<p>Tài khoản của bạn đã được tạo thành công trong hệ thống với vai trò <strong>").append(role).append("</strong>.</p>");
        bodyBuilder.append("<p>Đây là thông tin đăng nhập của bạn:</p>");
        bodyBuilder.append("<div class='credentials'>");
        bodyBuilder.append("<p><strong>Tên đăng nhập:</strong> ").append(username).append("</p>");
        bodyBuilder.append("<p><strong>Mật khẩu tạm thời:</strong> ").append(password).append("</p>");
        bodyBuilder.append("</div>");
        bodyBuilder.append("<p>Vui lòng sử dụng thông tin này để đăng nhập. Vì lý do bảo mật, chúng tôi khuyên bạn nên đổi mật khẩu sau lần đăng nhập đầu tiên.</p>");
        bodyBuilder.append("<p>Nếu bạn có bất kỳ câu hỏi hoặc cần hỗ trợ, vui lòng liên hệ với đội ngũ hỗ trợ của chúng tôi.</p>");
        bodyBuilder.append("<p>Trân trọng,<br>Đội ngũ MVS Classroom</p>");
        bodyBuilder.append("</div><div style='margin-top: 20px; text-align: center; font-size: 12px; color: #666;'>");
        bodyBuilder.append("<p>&copy; MVS Classroom. Đã đăng ký bản quyền.</p></div></body></html>");
        
        return bodyBuilder.toString();
    }
} 