package com.classroomapp.classroombackend.exception;

/**
 * Exception for business logic errors that should not trigger transaction rollback.
 * This exception is used for validation errors, duplicate data, etc.
 */
public class BusinessLogicException extends RuntimeException {
    
    public BusinessLogicException(String message) {
        super(message);
    }
    
    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
    }
}
