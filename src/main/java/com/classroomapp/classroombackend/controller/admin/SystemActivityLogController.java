package com.classroomapp.classroombackend.controller.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.response.SystemActivityLogResponse;
import com.classroomapp.classroombackend.entity.SystemActivityLog;
import com.classroomapp.classroombackend.service.SystemActivityLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for admin system activity logs
 */
@RestController
@RequestMapping("/api/admin/system-logs")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class SystemActivityLogController {
    
    private final SystemActivityLogService logService;
    
    /**
     * Get all system activity logs with pagination
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SystemActivityLogResponse>>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting all system logs - page: {}, size: {}", page, size);
        
        try {
            Page<SystemActivityLogResponse> logs = logService.getAllLogs(page, size);
            
            return ResponseEntity.ok(
                ApiResponse.<Page<SystemActivityLogResponse>>builder()
                    .success(true)
                    .message("Lấy danh sách log thành công")
                    .data(logs)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting system logs", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<Page<SystemActivityLogResponse>>builder()
                    .success(false)
                    .message("Lỗi khi lấy danh sách log: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Get logs by user ID
     */
    @GetMapping("/by-user")
    public ResponseEntity<ApiResponse<Page<SystemActivityLogResponse>>> getLogsByUserId(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting logs for user ID: {}", userId);
        
        try {
            Page<SystemActivityLogResponse> logs = logService.getLogsByUserId(userId, page, size);
            
            return ResponseEntity.ok(
                ApiResponse.<Page<SystemActivityLogResponse>>builder()
                    .success(true)
                    .message("Lấy log theo user thành công")
                    .data(logs)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting logs by user ID", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<Page<SystemActivityLogResponse>>builder()
                    .success(false)
                    .message("Lỗi khi lấy log theo user: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Get logs by username
     */
    @GetMapping("/by-username")
    public ResponseEntity<ApiResponse<Page<SystemActivityLogResponse>>> getLogsByUsername(
            @RequestParam String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting logs for username: {}", username);
        
        try {
            Page<SystemActivityLogResponse> logs = logService.getLogsByUsername(username, page, size);
            
            return ResponseEntity.ok(
                ApiResponse.<Page<SystemActivityLogResponse>>builder()
                    .success(true)
                    .message("Lấy log theo username thành công")
                    .data(logs)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting logs by username", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<Page<SystemActivityLogResponse>>builder()
                    .success(false)
                    .message("Lỗi khi lấy log theo username: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Get logs by action
     */
    @GetMapping("/by-action")
    public ResponseEntity<ApiResponse<Page<SystemActivityLogResponse>>> getLogsByAction(
            @RequestParam String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting logs for action: {}", action);
        
        try {
            Page<SystemActivityLogResponse> logs = logService.getLogsByAction(action, page, size);
            
            return ResponseEntity.ok(
                ApiResponse.<Page<SystemActivityLogResponse>>builder()
                    .success(true)
                    .message("Lấy log theo action thành công")
                    .data(logs)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting logs by action", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<Page<SystemActivityLogResponse>>builder()
                    .success(false)
                    .message("Lỗi khi lấy log theo action: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Get logs by date range
     */
    @GetMapping("/by-date-range")
    public ResponseEntity<ApiResponse<Page<SystemActivityLogResponse>>> getLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting logs from {} to {}", startDate, endDate);
        
        try {
            Page<SystemActivityLogResponse> logs = logService.getLogsByDateRange(startDate, endDate, page, size);
            
            return ResponseEntity.ok(
                ApiResponse.<Page<SystemActivityLogResponse>>builder()
                    .success(true)
                    .message("Lấy log theo khoảng thời gian thành công")
                    .data(logs)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting logs by date range", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<Page<SystemActivityLogResponse>>builder()
                    .success(false)
                    .message("Lỗi khi lấy log theo thời gian: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Get logs by log level
     */
    @GetMapping("/by-level")
    public ResponseEntity<ApiResponse<Page<SystemActivityLogResponse>>> getLogsByLogLevel(
            @RequestParam SystemActivityLog.LogLevel logLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Getting logs for level: {}", logLevel);
        
        try {
            Page<SystemActivityLogResponse> logs = logService.getLogsByLogLevel(logLevel, page, size);
            
            return ResponseEntity.ok(
                ApiResponse.<Page<SystemActivityLogResponse>>builder()
                    .success(true)
                    .message("Lấy log theo level thành công")
                    .data(logs)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting logs by log level", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<Page<SystemActivityLogResponse>>builder()
                    .success(false)
                    .message("Lỗi khi lấy log theo level: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Get recent failed activities
     */
    @GetMapping("/failed")
    public ResponseEntity<ApiResponse<List<SystemActivityLogResponse>>> getRecentFailedActivities(
            @RequestParam(defaultValue = "50") int limit) {
        
        log.info("Getting recent failed activities - limit: {}", limit);
        
        try {
            List<SystemActivityLogResponse> logs = logService.getRecentFailedActivities(limit);
            
            return ResponseEntity.ok(
                ApiResponse.<List<SystemActivityLogResponse>>builder()
                    .success(true)
                    .message("Lấy log lỗi thành công")
                    .data(logs)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting failed activities", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<List<SystemActivityLogResponse>>builder()
                    .success(false)
                    .message("Lỗi khi lấy log lỗi: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Get activity statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<List<Object[]>>> getActivityStatistics() {
        log.info("Getting activity statistics");
        
        try {
            List<Object[]> statistics = logService.getActivityStatistics();
            
            return ResponseEntity.ok(
                ApiResponse.<List<Object[]>>builder()
                    .success(true)
                    .message("Lấy thống kê hoạt động thành công")
                    .data(statistics)
                    .build()
            );
        } catch (Exception e) {
            log.error("Error getting activity statistics", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<List<Object[]>>builder()
                    .success(false)
                    .message("Lỗi khi lấy thống kê: " + e.getMessage())
                    .build()
            );
        }
    }
    
    /**
     * Generate sample test data for SystemActivityLog
     */
    // @PostMapping("/generate-test-data")
    public ResponseEntity<ApiResponse<String>> generateTestData() {
        log.info("Generating test data for SystemActivityLog");
        
        try {
            // Generate some sample logs
            String[] actions = {"User Login", "Create Course", "Update Profile", "Delete Assignment", "View Dashboard"};
            String[] users = {"admin", "teacher1", "student1", "manager1"};
            SystemActivityLog.LogLevel[] levels = {
                SystemActivityLog.LogLevel.INFO, 
                SystemActivityLog.LogLevel.WARN, 
                SystemActivityLog.LogLevel.ERROR,
                SystemActivityLog.LogLevel.DEBUG
            };
            
            for (int i = 0; i < 20; i++) {
                String action = actions[i % actions.length];
                String username = users[i % users.length];
                SystemActivityLog.LogLevel level = levels[i % levels.length];
                boolean success = i % 4 != 2; // Make some entries fail
                
                logService.logActivity(
                    (long)(i % 4 + 1), // userId 1-4
                    username,
                    action,
                    "TEST_DATA",
                    (long)(i + 1),
                    "127.0.0.1",
                    "Mozilla/5.0 Test Browser",
                    level,
                    "Test activity log entry #" + (i + 1),
                    success
                );
            }
            
            return ResponseEntity.ok(
                ApiResponse.<String>builder()
                    .success(true)
                    .message("Tạo dữ liệu test thành công")
                    .data("Generated 20 test log entries")
                    .build()
            );
        } catch (Exception e) {
            log.error("Error generating test data", e);
            return ResponseEntity.internalServerError().body(
                ApiResponse.<String>builder()
                    .success(false)
                    .message("Lỗi khi tạo dữ liệu test: " + e.getMessage())
                    .build()
            );
        }
    }
}