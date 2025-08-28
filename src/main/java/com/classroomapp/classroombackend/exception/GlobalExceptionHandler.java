package com.classroomapp.classroombackend.exception;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

/**
 * Handles exceptions globally for the application
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle specific ResourceNotFoundException
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle validation errors from @Valid annotation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        
        body.put("message", "Validation failed");
        body.put("errors", errors);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<?> handleAuthenticationException(Exception ex, WebRequest request) {
        log.error("Authentication error: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("error", "Unauthorized");
        body.put("message", "Authentication failed: " + ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }
    
    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        log.error("Access denied: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Forbidden");
        body.put("message", "Access denied: You do not have permission to access this resource");
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("security_message", ex.getMessage());
        
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }
    
    /**
     * Handle 404 Not Found errors
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException ex, WebRequest request) {
        log.error("Resource not found: {}", ex.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", "The requested resource was not found");
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle client abort exceptions (client disconnected unexpectedly)
     * This is a common issue when users close browser tabs or lose connection
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIOException(IOException ex, WebRequest request) {
        String errorMessage = ex.getMessage();
        
        // Kiểm tra xem có phải là ClientAbortException không
        if (errorMessage != null && (
            errorMessage.contains("An established connection was aborted by the software in your host machine") ||
            errorMessage.contains("Broken pipe") ||
            errorMessage.contains("Connection reset by peer") ||
            errorMessage.contains("Software caused connection abort"))) {
            
            // Log ở mức WARN thay vì ERROR vì đây không phải lỗi server
            log.warn("Client connection aborted: {} - Path: {} - User-Agent: {}", 
                errorMessage, 
                request.getDescription(false).replace("uri=", ""),
                request.getHeader("User-Agent"));
            
            // Không cần trả về response vì client đã ngắt kết nối
            return null;
        }
        
        // Nếu là IOException khác, log như bình thường
        log.error("IO Exception: {}", ex.getMessage(), ex);
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An I/O error occurred");
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("exception_type", ex.getClass().getName());
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle general exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        // Kiểm tra xem có phải là ClientAbortException không (một số framework throw Exception thay vì IOException)
        if (ex.getMessage() != null && ex.getMessage().contains("An established connection was aborted by the software in your host machine")) {
            log.warn("Client connection aborted (via Exception handler): {} - Path: {} - User-Agent: {}", 
                ex.getMessage(), 
                request.getDescription(false).replace("uri=", ""),
                request.getHeader("User-Agent"));
            return null;
        }
        
        log.error("Global exception: {} - Path: {} - User-Agent: {}", 
            ex.getMessage(), 
            request.getDescription(false).replace("uri=", ""),
            request.getHeader("User-Agent"), ex);
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred");
        body.put("path", request.getDescription(false).replace("uri=", ""));
        body.put("exception_type", ex.getClass().getName());
        
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(
            IllegalArgumentException exception, WebRequest request) {
        
        log.error("Validation error: {}", exception.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", exception.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle business logic exceptions with 400 Bad Request
     */
    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<?> handleBusinessLogicException(
            BusinessLogicException exception, WebRequest request) {
        
        log.warn("Business logic error: {}", exception.getMessage());
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", new Date());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", exception.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}