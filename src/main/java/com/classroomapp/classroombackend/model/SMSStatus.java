package com.classroomapp.classroombackend.model;

/**
 * Enumeration for SMS notification status
 */
public enum SMSStatus {
    PENDING,    // SMS is queued to be sent
    SENT,       // SMS was sent successfully
    FAILED,     // SMS sending failed
    RETRY       // SMS is being retried
}