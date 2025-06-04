package com.classroomapp.classroombackend.service;

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
} 