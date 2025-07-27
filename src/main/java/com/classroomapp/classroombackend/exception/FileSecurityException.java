package com.classroomapp.classroombackend.exception;

/**
 * Exception cho file security violations
 */
public class FileSecurityException extends RuntimeException {
    
    public FileSecurityException(String message) {
        super(message);
    }
    
    public FileSecurityException(String message, Throwable cause) {
        super(message, cause);
    }
}
