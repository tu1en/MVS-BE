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

    @Override
    public void sendApprovalEmail(String to, String fullName, String roleName, String temporaryPassword) {
        log.info("DUMMY EMAIL SERVICE: Would send approval email to {} ({}) - Role: {}, Password: {}",
                to, fullName, roleName, temporaryPassword);
    }

    @Override
    public void sendInterviewInvitationEmail(String to, String name, String jobTitle) {
        log.info("DUMMY EMAIL SERVICE: Would send interview invitation email to {} ({}) - Job Title: {}",
                to, name, jobTitle);
    }

    @Override
    public void sendInterviewScheduledEmail(String to, String name, String jobTitle, String interviewTime) {
        log.info("DUMMY EMAIL SERVICE: Would send interview scheduled email to {} ({}) - Job Title: {}, Interview Time: {}",
                to, name, jobTitle, interviewTime);
    }

    @Override
    public void sendInterviewRejectionEmail(String to, String name, String jobTitle, String reason) {
        log.info("DUMMY EMAIL SERVICE: Would send interview rejection email to {} ({}) - Job Title: {}, Reason: {}",
                to, name, jobTitle, reason);
    }

    @Override
    public void sendInterviewRejectionEmail(String to, String name, String jobTitle, String reason, String evaluation) {
        log.info("DUMMY EMAIL SERVICE: Would send interview rejection email with evaluation to {} ({}) - Job Title: {}, Reason: {}, Evaluation: {}",
                to, name, jobTitle, reason, evaluation);
    }

    @Override
    public void sendOfferResendEmail(String to, String name, String jobTitle, String offer) {
        log.info("DUMMY EMAIL SERVICE: Would send offer resend email with salary details to {} ({}) - Job Title: {}, Offer: {}",
                to, name, jobTitle, offer);
        
        // Tính toán và log chi tiết lương
        try {
            if (offer != null && !offer.trim().isEmpty()) {
                String cleanOffer = offer.replaceAll("[^0-9]", "");
                java.math.BigDecimal grossSalary = new java.math.BigDecimal(cleanOffer);
                com.classroomapp.classroombackend.util.TopCVCalculation.SalaryCalculationResult salaryDetails = 
                    com.classroomapp.classroombackend.util.TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
                log.info("DUMMY EMAIL SERVICE: Salary details - Gross: {}, Net: {}, Tax: {}, Employee Contribution: {}", 
                    salaryDetails.getGrossSalary(), salaryDetails.getNetSalary(), salaryDetails.getPersonalIncomeTax(), 
                    salaryDetails.getInsuranceDetails().getTotalEmployeeContribution());
            }
        } catch (Exception e) {
            log.warn("Could not calculate salary details for offer: {}", offer, e);
        }
    }

    @Override
    public void sendOfferResendEmailWithDetails(String to, String name, String jobTitle, String offer, Object salaryDetails) {
        log.info("DUMMY EMAIL SERVICE: Would send offer resend email with details to {} ({}) - Job Title: {}, Offer: {}, Salary Details: {}",
                to, name, jobTitle, offer, salaryDetails);
    }

    @Override
    public void sendEnrollmentRequestConfirmation(String to, String studentName, String courseName, 
                                                String courseSubject, Integer courseDuration, 
                                                String courseFee, String message) {
        log.info("DUMMY EMAIL SERVICE: Would send enrollment request confirmation to {} ({}) - Course: {}, Subject: {}, Duration: {} weeks, Fee: {}",
                to, studentName, courseName, courseSubject, courseDuration, courseFee);
        if (message != null) {
            log.info("Student message: {}", message);
        }
    }

    @Override
    public void sendNewEnrollmentNotificationToManager(String to, String studentName, String studentEmail,
                                                      String courseName, String courseSubject, 
                                                      Integer courseDuration, String courseFee,
                                                      String message, String dashboardUrl) {
        log.info("DUMMY EMAIL SERVICE: Would send new enrollment notification to manager {} - Student: {} ({}), Course: {}, Dashboard: {}",
                to, studentName, studentEmail, courseName, dashboardUrl);
    }

    @Override
    public void sendEnrollmentApprovalNotification(String to, String studentName, String courseName,
                                                  String courseSubject, Integer courseDuration,
                                                  String courseFee, String instructorName,
                                                  String approvedBy, String paymentUrl) {
        log.info("DUMMY EMAIL SERVICE: Would send enrollment approval notification to {} ({}) - Course: {}, Instructor: {}, Approved by: {}",
                to, studentName, courseName, instructorName, approvedBy);
        if (paymentUrl != null) {
            log.info("Payment URL: {}", paymentUrl);
        }
    }

    @Override
    public void sendEnrollmentRejectionNotification(String to, String studentName, String courseName,
                                                   String courseSubject, String rejectionReason,
                                                   String reviewedBy, String coursesUrl, String contactUrl) {
        log.info("DUMMY EMAIL SERVICE: Would send enrollment rejection notification to {} ({}) - Course: {}, Reason: {}, Reviewed by: {}",
                to, studentName, courseName, rejectionReason, reviewedBy);
    }
}
