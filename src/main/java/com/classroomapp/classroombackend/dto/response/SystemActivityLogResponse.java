package com.classroomapp.classroombackend.dto.response;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.entity.SystemActivityLog.LogLevel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for SystemActivityLog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemActivityLogResponse {
    
    private Long id;
    private Long userId;
    private String username;
    private String action;
    private String resourceType;
    private Long resourceId;
    private String ipAddress;
    private String userAgent;
    private LogLevel logLevel;
    private String details;
    private Boolean success;
    private LocalDateTime createdAt;
}