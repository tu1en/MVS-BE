package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.response.SystemActivityLogResponse;
import com.classroomapp.classroombackend.entity.SystemActivityLog;
import com.classroomapp.classroombackend.repository.administration.SystemActivityLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for handling system activity logs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemActivityLogService {
    
    private final SystemActivityLogRepository logRepository;
    
    /**
     * Log system activity
     */
    @Transactional
    public void logActivity(Long userId, String username, String action, String resourceType, 
                           Long resourceId, String ipAddress, String userAgent, 
                           SystemActivityLog.LogLevel logLevel, String details, Boolean success) {
        try {
            SystemActivityLog activityLog = SystemActivityLog.builder()
                    .userId(userId)
                    .username(username)
                    .action(action)
                    .resourceType(resourceType)
                    .resourceId(resourceId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .logLevel(logLevel)
                    .details(details)
                    .success(success)
                    .build();
            
            logRepository.save(activityLog);
            log.debug("Activity logged: {} by user {}", action, username);
        } catch (Exception e) {
            log.error("Ghi log hoạt động thất bại: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get all activity logs with pagination
     */
    public Page<SystemActivityLogResponse> getAllLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SystemActivityLog> logs = logRepository.findAll(pageable);
        return logs.map(this::convertToResponse);
    }
    
    /**
     * Get logs by user ID
     */
    public Page<SystemActivityLogResponse> getLogsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SystemActivityLog> logs = logRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return logs.map(this::convertToResponse);
    }
    
    /**
     * Get logs by username
     */
    public Page<SystemActivityLogResponse> getLogsByUsername(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SystemActivityLog> logs = logRepository.findByUsernameContainingIgnoreCaseOrderByCreatedAtDesc(username, pageable);
        return logs.map(this::convertToResponse);
    }
    
    /**
     * Get logs by action
     */
    public Page<SystemActivityLogResponse> getLogsByAction(String action, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SystemActivityLog> logs = logRepository.findByActionContainingIgnoreCaseOrderByCreatedAtDesc(action, pageable);
        return logs.map(this::convertToResponse);
    }
    
    /**
     * Get logs by date range
     */
    public Page<SystemActivityLogResponse> getLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SystemActivityLog> logs = logRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate, pageable);
        return logs.map(this::convertToResponse);
    }
    
    /**
     * Get logs by log level
     */
    public Page<SystemActivityLogResponse> getLogsByLogLevel(SystemActivityLog.LogLevel logLevel, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SystemActivityLog> logs = logRepository.findByLogLevelOrderByCreatedAtDesc(logLevel, pageable);
        return logs.map(this::convertToResponse);
    }
    
    /**
     * Get failed activities
     */
    public List<SystemActivityLogResponse> getRecentFailedActivities(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<SystemActivityLog> logs = logRepository.findRecentFailedActivities(pageable);
        return logs.stream().map(this::convertToResponse).collect(Collectors.toList());
    }
    
    /**
     * Get activity statistics
     */
    public List<Object[]> getActivityStatistics() {
        return logRepository.getActivityStatistics();
    }
    
    /**
     * Convert entity to response DTO
     */
    private SystemActivityLogResponse convertToResponse(SystemActivityLog log) {
        return SystemActivityLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .username(log.getUsername())
                .action(log.getAction())
                .resourceType(log.getResourceType())
                .resourceId(log.getResourceId())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .logLevel(log.getLogLevel())
                .details(log.getDetails())
                .success(log.getSuccess())
                .createdAt(log.getCreatedAt())
                .build();
    }
}