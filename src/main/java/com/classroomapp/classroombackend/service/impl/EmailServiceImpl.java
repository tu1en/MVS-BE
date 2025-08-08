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
    public void sendEmail(String to, String subject, String text) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true); // true = isHtml
            helper.setFrom(fromEmail);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
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
        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("role", role);
        context.setVariable("username", username);
        context.setVariable("password", password);
        String body = templateEngine.process("email/request-approved", context);
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

    @Override
    public void sendApprovalEmail(String to, String fullName, String roleName, String temporaryPassword) {
        String subject = "Yêu cầu tạo tài khoản học sinh đã được phê duyệt";
        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("role", roleName);
        context.setVariable("username", to);
        context.setVariable("password", temporaryPassword);
        String body = templateEngine.process("email/request-approved", context);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendInterviewInvitationEmail(String to, String name, String jobTitle) {
        String subject = "Thư mời phỏng vấn vị trí: " + jobTitle;
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("jobTitle", jobTitle);
        String body = templateEngine.process("email/interview-invitation", context);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendInterviewScheduledEmail(String to, String name, String jobTitle, String interviewTime) {
        String subject = "Thông báo lịch phỏng vấn vị trí: " + jobTitle;
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("interviewTime", interviewTime);
        String body = templateEngine.process("email/interview-scheduled", context);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendInterviewRejectionEmail(String to, String name, String jobTitle, String reason) {
        String subject = "Kết quả ứng tuyển vị trí: " + jobTitle;
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("reason", reason);
        String body = templateEngine.process("email/interview-rejected", context);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendInterviewRejectionEmail(String to, String name, String jobTitle, String reason, String evaluation) {
        String subject = "Kết quả ứng tuyển vị trí: " + jobTitle;
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("reason", reason);
        context.setVariable("evaluation", evaluation);
        String body = templateEngine.process("email/interview-rejected", context);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendOfferResendEmail(String to, String name, String jobTitle, String offer) {
        String subject = "Thông Báo Offer - " + jobTitle;
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("offer", offer);
<<<<<<< HEAD
        String body = templateEngine.process("email/offer-resend", context);
=======
        
        // Tính toán chi tiết lương từ offer
        try {
            if (offer != null && !offer.trim().isEmpty()) {
                String cleanOffer = offer.replaceAll("[^0-9]", "");
                java.math.BigDecimal grossSalary = new java.math.BigDecimal(cleanOffer);
                com.classroomapp.classroombackend.util.TopCVCalculation.SalaryCalculationResult salaryDetails = 
                    com.classroomapp.classroombackend.util.TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
                context.setVariable("salaryDetails", salaryDetails);
            }
        } catch (Exception e) {
            log.warn("Could not calculate salary details for offer: {}", offer, e);
        }
        
        // Sử dụng template mới với chi tiết lương
        String body = templateEngine.process("email/offer-resend-with-details", context);
        sendEmail(to, subject, body);
    }

    @Override
    public void sendOfferResendEmailWithDetails(String to, String name, String jobTitle, String offer, Object salaryDetails) {
        String subject = "Thông Báo Offer Chi Tiết - " + jobTitle;
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("jobTitle", jobTitle);
        context.setVariable("offer", offer);
        context.setVariable("salaryDetails", salaryDetails);
        String body = templateEngine.process("email/offer-resend-with-details", context);
>>>>>>> 17a86eaddc86600ac77c6d96372b947571caf746
        sendEmail(to, subject, body);
    }
// Thêm method này vào EmailServiceImpl.java (sau method sendInterviewRejectionEmail)

@Override
public void sendEnrollmentRequestConfirmation(String to, String studentName, String courseName, 
                                            String courseSubject, Integer courseDuration, 
                                            String courseFee, String message) {
    String subject = "Xác nhận yêu cầu đăng ký khóa học: " + courseName;
    Context context = new Context();
    context.setVariable("studentName", studentName);
    context.setVariable("courseName", courseName);
    context.setVariable("courseSubject", courseSubject);
    context.setVariable("courseDuration", courseDuration);
    context.setVariable("courseFee", courseFee);
    context.setVariable("message", message);
    String body = templateEngine.process("email/enrollment-confirmation", context);
    sendEmail(to, subject, body);
}

@Override
public void sendNewEnrollmentNotificationToManager(String to, String studentName, String studentEmail,
                                                  String courseName, String courseSubject, 
                                                  Integer courseDuration, String courseFee,
                                                  String message, String dashboardUrl) {
    String subject = "Yêu cầu đăng ký khóa học mới từ " + studentName;
    Context context = new Context();
    context.setVariable("studentName", studentName);
    context.setVariable("studentEmail", studentEmail);
    context.setVariable("courseName", courseName);
    context.setVariable("courseSubject", courseSubject);
    context.setVariable("courseDuration", courseDuration);
    context.setVariable("courseFee", courseFee);
    context.setVariable("message", message);
    context.setVariable("dashboardUrl", dashboardUrl);
    String body = templateEngine.process("email/new-enrollment-notification", context);
    sendEmail(to, subject, body);
}

@Override
public void sendEnrollmentApprovalNotification(String to, String studentName, String courseName,
                                              String courseSubject, Integer courseDuration,
                                              String courseFee, String instructorName,
                                              String approvedBy, String paymentUrl) {
    String subject = "Yêu cầu đăng ký khóa học đã được chấp thuận: " + courseName;
    Context context = new Context();
    context.setVariable("studentName", studentName);
    context.setVariable("courseName", courseName);
    context.setVariable("courseSubject", courseSubject);
    context.setVariable("courseDuration", courseDuration);
    context.setVariable("courseFee", courseFee);
    context.setVariable("instructorName", instructorName);
    context.setVariable("approvedBy", approvedBy);
    context.setVariable("paymentUrl", paymentUrl);
    String body = templateEngine.process("email/enrollment-approval", context);
    sendEmail(to, subject, body);
}

@Override
public void sendEnrollmentRejectionNotification(String to, String studentName, String courseName,
                                               String courseSubject, String rejectionReason,
                                               String reviewedBy, String coursesUrl, String contactUrl) {
    String subject = "Yêu cầu đăng ký khóa học bị từ chối: " + courseName;
    Context context = new Context();
    context.setVariable("studentName", studentName);
    context.setVariable("courseName", courseName);
    context.setVariable("courseSubject", courseSubject);
    context.setVariable("rejectionReason", rejectionReason);
    context.setVariable("reviewedBy", reviewedBy);
    context.setVariable("coursesUrl", coursesUrl);
    context.setVariable("contactUrl", contactUrl);
    String body = templateEngine.process("email/enrollment-rejection", context);
    sendEmail(to, subject, body);
}

} 