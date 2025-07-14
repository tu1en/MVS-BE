package com.classroomapp.classroombackend.config.seed;

/**
 * Exception được throw khi có lỗi trong quá trình seeding
 */
public class SeedingException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public SeedingException(String message) {
        super(message);
    }
    
    public SeedingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SeedingException(Throwable cause) {
        super(cause);
    }
}
