package com.classroomapp.classroombackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource cannot be found
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor with resourceName, fieldName, and fieldValue
     * 
     * @param resourceName the name of the resource being requested
     * @param fieldName the field name used for the lookup
     * @param fieldValue the value of the field for the lookup
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }
} 