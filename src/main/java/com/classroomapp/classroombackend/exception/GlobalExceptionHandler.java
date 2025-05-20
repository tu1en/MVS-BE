package com.classroomapp.classroombackend.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

/**
 * Handles exceptions globally for the application
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle specific ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> HandleResourceNotFoundException(
            ResourceNotFoundException exception, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle validation errors from @Valid annotation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> HandleValidationExceptions(
            MethodArgumentNotValidException exception) {
        
        Map<String, String> errors = new HashMap<>();
        
        // Extract field-specific validation errors
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle general exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> HandleGlobalException(
            Exception exception, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                exception.getMessage(),
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> HandleIllegalArgumentException(
            IllegalArgumentException exception, WebRequest request) {
        
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                exception.getMessage(),
                request.getDescription(false)
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
} 