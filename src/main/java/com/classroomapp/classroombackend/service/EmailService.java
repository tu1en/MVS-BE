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
}
