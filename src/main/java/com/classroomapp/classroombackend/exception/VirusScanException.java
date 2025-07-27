package com.classroomapp.classroombackend.exception;

/**
 * Exception cho virus scan errors
 */
public class VirusScanException extends RuntimeException {
    
    public VirusScanException(String message) {
        super(message);
    }
    
    public VirusScanException(String message, Throwable cause) {
        super(message, cause);
    }
}
