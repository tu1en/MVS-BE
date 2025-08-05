package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API Response wrapper
 * @param <T> Type of data being returned
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private String error;
    
    // ================== SUCCESS METHODS ==================
    
    /**
     * Success response with data only
     * @param data the response data
     * @param <T> type of data
     * @return ApiResponse with success=true and data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }
    
    /**
     * Success response with data and message
     * @param data the response data
     * @param message success message
     * @param <T> type of data
     * @return ApiResponse with success=true, data, and message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * Success response with message only (for void operations)
     * @param message success message
     * @return ApiResponse with success=true and message
     */
    public static ApiResponse<Void> successMessage(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }
    
    // ================== ERROR METHODS ==================
    
    /**
     * Error response with message only
     * @param message error message
     * @param <T> type of data
     * @return ApiResponse with success=false and error message
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
    
    /**
     * Error response with message and error details
     * @param message error message
     * @param error detailed error information
     * @param <T> type of data
     * @return ApiResponse with success=false, message, and error details
     */
    public static <T> ApiResponse<T> error(String message, String error) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .build();
    }
    
    /**
     * Error response with message and data
     * @param message error message
     * @param data error data (can be validation errors, etc.)
     * @param <T> type of data
     * @return ApiResponse with success=false, message, and data
     */
    public static <T> ApiResponse<T> errorWithData(String message, T data) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(data)
                .build();
    }
}