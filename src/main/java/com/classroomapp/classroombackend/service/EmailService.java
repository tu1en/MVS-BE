package com.classroomapp.classroombackend.service;

/**
 * Service interface for email operations
 */
public interface EmailService {
    /**
     * Send an email notification
     * @param to Recipient email address
     * @param subject Email subject
     * @param body Email body (can be HTML)
     */
    void sendEmail(String to, String subject, String body);
    
    /**
     * Send a notification for a request status change
     * @param to Recipient email address
     * @param fullName Recipient's full name
     * @param requestedRole The role that was requested
     * @param status The new status (APPROVED/REJECTED)
     * @param reason The reason for rejection (if applicable)
     */
    void sendRequestStatusNotification(String to, String fullName, String requestedRole, String status, String reason);
    
    /**
     * Send account information with credentials
     * @param to Recipient email address
     * @param fullName Recipient's full name
     * @param role User's role
     * @param username Username for login
     * @param password Temporary password
     */
    void sendAccountInfoEmail(String to, String fullName, String role, String username, String password);
    
    /**
     * Send a form completion confirmation
     * @param to Recipient email address
     * @param fullName Recipient's full name
     * @param requestedRole The role that was requested
     */
    void sendFormCompletionConfirmation(String to, String fullName, String requestedRole);

    /**
     * Send an approval email with a temporary password.
     * @param to Recipient email address
     * @param fullName The user's full name
     * @param roleName The user's assigned role
     * @param temporaryPassword The generated temporary password for the user
     */
    void sendApprovalEmail(String to, String fullName, String roleName, String temporaryPassword);

    /**
     * Gửi email mời phỏng vấn cho ứng viên
     * @param to Địa chỉ email ứng viên
     * @param name Tên ứng viên
     * @param jobTitle Vị trí ứng tuyển
     */
    void sendInterviewInvitationEmail(String to, String name, String jobTitle);

    /**
     * Gửi email thông báo lịch phỏng vấn đã được lên
     * @param to Địa chỉ email ứng viên
     * @param name Tên ứng viên
     * @param jobTitle Vị trí ứng tuyển
     * @param interviewTime Thời gian phỏng vấn
     */
    void sendInterviewScheduledEmail(String to, String name, String jobTitle, String interviewTime);

    /**
     * Gửi email từ chối ứng viên phỏng vấn
     * @param to Địa chỉ email ứng viên
     * @param name Tên ứng viên
     * @param jobTitle Vị trí ứng tuyển
     * @param reason Lý do từ chối
     */
    void sendInterviewRejectionEmail(String to, String name, String jobTitle, String reason);

    /**
     * Gửi email từ chối ứng viên phỏng vấn với đánh giá
     * @param to Địa chỉ email ứng viên
     * @param name Tên ứng viên
     * @param jobTitle Vị trí ứng tuyển
     * @param reason Lý do từ chối
     * @param evaluation Đánh giá từ cuộc phỏng vấn
     */
    void sendInterviewRejectionEmail(String to, String name, String jobTitle, String reason, String evaluation);

    /**
     * Gửi email offer riêng biệt cho ứng viên
     * @param to Địa chỉ email ứng viên
     * @param name Tên ứng viên
     * @param jobTitle Vị trí ứng tuyển
     * @param offer Nội dung offer
     */
    void sendOfferResendEmail(String to, String name, String jobTitle, String offer);

    /**
     * Gửi email offer với chi tiết tính lương cho ứng viên
     * @param to Địa chỉ email ứng viên
     * @param name Tên ứng viên
     * @param jobTitle Vị trí ứng tuyển
     * @param offer Nội dung offer
     * @param salaryDetails Chi tiết tính lương
     */
    void sendOfferResendEmailWithDetails(String to, String name, String jobTitle, String offer, Object salaryDetails);
    
    /**
     * Send enrollment request confirmation to student
     * @param to Student email address
     * @param studentName Student's full name
     * @param courseName Course name
     * @param courseSubject Course subject
     * @param courseDuration Course duration in weeks
     * @param courseFee Course fee
     * @param message Student's message (optional)
     */
    void sendEnrollmentRequestConfirmation(String to, String studentName, String courseName, 
                                         String courseSubject, Integer courseDuration, 
                                         String courseFee, String message);
    
    /**
     * Send new enrollment request notification to managers
     * @param to Manager email address
     * @param studentName Student's full name
     * @param studentEmail Student's email
     * @param courseName Course name
     * @param courseSubject Course subject
     * @param courseDuration Course duration in weeks
     * @param courseFee Course fee
     * @param message Student's message (optional)
     * @param dashboardUrl URL to management dashboard
     */
    void sendNewEnrollmentNotificationToManager(String to, String studentName, String studentEmail,
                                               String courseName, String courseSubject, 
                                               Integer courseDuration, String courseFee,
                                               String message, String dashboardUrl);
    
    /**
     * Send enrollment approval notification to student
     * @param to Student email address
     * @param studentName Student's full name
     * @param courseName Course name
     * @param courseSubject Course subject
     * @param courseDuration Course duration in weeks
     * @param courseFee Course fee
     * @param instructorName Instructor name
     * @param approvedBy Manager who approved
     * @param paymentUrl Payment URL (optional)
     */
    void sendEnrollmentApprovalNotification(String to, String studentName, String courseName,
                                           String courseSubject, Integer courseDuration,
                                           String courseFee, String instructorName,
                                           String approvedBy, String paymentUrl);
    
    /**
     * Send enrollment rejection notification to student
     * @param to Student email address
     * @param studentName Student's full name
     * @param courseName Course name
     * @param courseSubject Course subject
     * @param rejectionReason Reason for rejection
     * @param reviewedBy Manager who rejected
     * @param coursesUrl URL to browse other courses
     * @param contactUrl URL to contact support
     */
    void sendEnrollmentRejectionNotification(String to, String studentName, String courseName,
                                            String courseSubject, String rejectionReason,
                                            String reviewedBy, String coursesUrl, String contactUrl);
}
