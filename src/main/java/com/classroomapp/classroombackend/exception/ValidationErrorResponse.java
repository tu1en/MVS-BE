package com.classroomapp.classroombackend.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Validation error response with field errors
 */
@Getter
@Setter
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> errors;
    
    public ValidationErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, Map<String, String> errors) {
        super(timestamp, status, error, message, path);
        this.errors = errors;
    }
}
