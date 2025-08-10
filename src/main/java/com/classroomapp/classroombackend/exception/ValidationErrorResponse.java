package com.classroomapp.classroombackend.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Validation error response with field errors
 */
@Getter
@Setter
@NoArgsConstructor
public class ValidationErrorResponse extends ErrorResponse {
    private Map<String, String> errors;
    
    public ValidationErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, Map<String, String> errors) {
        super();
        this.setTimestamp(timestamp);
        this.setStatus(status);
        this.setError(error);
        this.setMessage(message);
        this.setPath(path);
        this.errors = errors;
    }
}
