package com.classroomapp.classroombackend.exception;

/**
 * Exception cho image processing errors
 */
public class ImageProcessingException extends RuntimeException {
    
    public ImageProcessingException(String message) {
        super(message);
    }
    
    public ImageProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
