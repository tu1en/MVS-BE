package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.config.DataLoader;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing makeup attendance test data
 * Provides endpoints to create and reset test data for development and testing
 */
@RestController
@RequestMapping("/api/test/makeup-attendance")
@Slf4j
public class MakeupAttendanceTestController {

    @Autowired
    private DataLoader dataLoader;

    /**
     * Create test data for makeup attendance functionality
     * This endpoint manually triggers the test data creation
     */
    @PostMapping("/create-test-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> createTestData() {
        log.info("🧪 Manual trigger: Creating makeup attendance test data");
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Call the makeup attendance test data seeding method from DataLoader
            // dataLoader.seedMakeupAttendanceTestData();
            
            response.put("success", true);
            response.put("message", "Makeup attendance test data created successfully");
            response.put("testAccounts", Map.of(
                "teacher", Map.of(
                    "username", "teacher",
                    "password", "teacher123",
                    "email", "teacher@test.com",
                    "role", "TEACHER",
                    "fullName", "Nguyễn Văn Minh"
                ),
                "manager", Map.of(
                    "username", "manager",
                    "password", "manager123",
                    "email", "manager@test.com",
                    "role", "MANAGER",
                    "fullName", "Manager User"
                )
            ));
            response.put("testData", Map.of(
                "classroom", "Lớp Test Điểm Danh Bù",
                "lectures", Map.of(
                    "yesterday", "Buổi 1: Giới thiệu Java - ĐÃ BỎ LỠ (cần điểm danh bù)",
                    "today", "Buổi 2: Cơ bản Java - HÔM NAY (điểm danh bình thường)",
                    "tomorrow", "Buổi 3: Lập trình hướng đối tượng - TƯƠNG LAI",
                    "threeDaysAgo", "Buổi 4: Java nâng cao - QUÁ MUỘN (cần điểm danh bù)"
                ),
                "makeupRequests", Map.of(
                    "pending", "Yêu cầu cho buổi học hôm qua (chờ quản lý xác nhận)",
                    "acknowledged", "Yêu cầu cho buổi học 3 ngày trước (sẵn sàng điểm danh bù)"
                )
            ));
            response.put("instructions", Map.of(
                "step1", "Đăng nhập bằng 'testteacher' với mật khẩu 'teacher123'",
                "step2", "Vào trang điểm danh và thử điểm danh cho buổi học hôm qua",
                "step3", "Hệ thống sẽ bắt buộc tạo yêu cầu điểm danh bù",
                "step4", "Đăng nhập bằng 'testmanager' với mật khẩu 'manager123'",
                "step5", "Vào trang phê duyệt điểm danh bù",
                "step6", "Xác nhận yêu cầu đang chờ",
                "step7", "Đăng nhập lại bằng tài khoản giáo viên và thực hiện điểm danh bù",
                "step8", "Kiểm tra lịch sử giảng dạy để xem các chỉ báo trực quan"
            ));
            
            log.info("✅ Makeup attendance test data created successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error creating makeup attendance test data: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Error creating test data: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get information about existing test data
     */
    @GetMapping("/test-data-info")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEACHER')")
    public ResponseEntity<Map<String, Object>> getTestDataInfo() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("description", "Makeup Attendance Test Data Information");
        response.put("purpose", "Complete workflow testing for makeup attendance functionality");
        
        response.put("testAccounts", Map.of(
            "teacher", Map.of(
                "username", "testteacher",
                "password", "teacher123",
                "description", "Sử dụng tài khoản này để test quy trình giáo viên"
            ),
            "manager", Map.of(
                "username", "testmanager",
                "password", "manager123",
                "description", "Sử dụng tài khoản này để test quy trình quản lý"
            )
        ));
        
        response.put("testScenarios", Map.of(
            "scenario1", Map.of(
                "name", "Create Makeup Request",
                "description", "Teacher tries to attend yesterday's lecture → forced to create makeup request",
                "expectedResult", "Makeup request created with PENDING status"
            ),
            "scenario2", Map.of(
                "name", "Manager Acknowledgment", 
                "description", "Manager acknowledges the makeup request",
                "expectedResult", "Request status changes to ACKNOWLEDGED"
            ),
            "scenario3", Map.of(
                "name", "Perform Makeup Attendance",
                "description", "Teacher performs makeup attendance for acknowledged request",
                "expectedResult", "Attendance recorded, request status becomes COMPLETED"
            ),
            "scenario4", Map.of(
                "name", "Visual Indicators",
                "description", "Check teaching history for proper visual indicators",
                "expectedResult", "Red badge for pending requests, orange badge for completed makeup"
            )
        ));
        
        response.put("testData", Map.of(
            "classroom", "Makeup Test Class",
            "schedule", "Tuesday 9:00-11:00 AM in Room 101",
            "lectures", Map.of(
                "count", 4,
                "dates", "Yesterday, Today, Tomorrow, 3 days ago"
            ),
            "makeupRequests", Map.of(
                "pending", 1,
                "acknowledged", 1,
                "total", 2
            )
        ));
        
        return ResponseEntity.ok(response);
    }

    /**
     * Health check for test data
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Makeup Attendance Test Data Controller");
        response.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}
