package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.service.ManagerReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/manager/reports")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
@Slf4j
public class ManagerReportsController {

    private final ManagerReportService managerReportService;

    @GetMapping("/attendance")
    public ResponseEntity<Map<String, Object>> getAttendanceReport(
            @RequestParam(defaultValue = "month") String period) {
        log.info("Manager requesting attendance report for period: {}", period);

        Map<String, Object> response = new HashMap<>();

        // Response structure matching frontend expectations
        response.put("title", "Báo cáo Điểm danh - " + period);
        response.put("description", "Thống kê tình hình điểm danh theo " + period);

        // Get real data from service
        Map<String, Object> data = managerReportService.getAttendanceReportData(period);

        response.put("data", data);
        response.put("period", period);
        response.put("generatedAt", LocalDate.now().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceReport(
            @RequestParam(defaultValue = "semester") String period) {
        log.info("Manager requesting performance report for period: {}", period);

        Map<String, Object> response = new HashMap<>();

        // Response structure matching frontend expectations
        response.put("title", "Báo cáo Học tập - " + period);
        response.put("description", "Thống kê kết quả học tập theo " + period);

        // Get real data from service
        Map<String, Object> data = managerReportService.getPerformanceReportData(period);

        response.put("data", data);
        response.put("period", period);
        response.put("generatedAt", LocalDate.now().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/financial")
    public ResponseEntity<Map<String, Object>> getFinancialReport(
            @RequestParam(defaultValue = "quarter") String period) {
        log.info("Manager requesting financial report for period: {}", period);

        Map<String, Object> response = new HashMap<>();

        // Response structure matching frontend expectations
        response.put("title", "Báo cáo Tài chính - " + period);
        response.put("description", "Thống kê tình hình tài chính theo " + period);

        // Get real data from service
        Map<String, Object> data = managerReportService.getFinancialReportData(period);

        response.put("data", data);
        response.put("period", period);
        response.put("generatedAt", LocalDate.now().toString());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverviewReport() {
        log.info("Manager requesting overview report");
        
        Map<String, Object> report = new HashMap<>();
        
        // Combine key metrics from all reports
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("totalStudents", 1200);
        metrics.put("totalTeachers", 85);
        metrics.put("totalClasses", 48);
        metrics.put("averageAttendance", 85.2);
        metrics.put("averageGrade", 7.5);
        metrics.put("monthlyRevenue", 850000000L);
        metrics.put("activeAssignments", 15);
        metrics.put("upcomingExams", 8);
        
        List<Map<String, Object>> recentActivities = new ArrayList<>();
        String[] activities = {
            "Học sinh Nguyễn Văn A nộp bài tập Toán",
            "Giáo viên Trần Thị B tạo bài kiểm tra mới",
            "Lớp 10A hoàn thành bài kiểm tra Văn",
            "Phụ huynh Lê Văn C thanh toán học phí",
            "Sinh viên mới đăng ký khóa học Anh văn"
        };
        
        for (int i = 0; i < activities.length; i++) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", i + 1);
            activity.put("description", activities[i]);
            activity.put("timestamp", LocalDate.now().minusDays(i).toString());
            activity.put("type", i % 2 == 0 ? "academic" : "administrative");
            recentActivities.add(activity);
        }
        
        report.put("metrics", metrics);
        report.put("recentActivities", recentActivities);
        report.put("generatedAt", LocalDate.now().toString());
        
        return ResponseEntity.ok(report);
    }
} 