package com.classroomapp.classroombackend.controller;

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

    @GetMapping("/attendance")
    public ResponseEntity<Map<String, Object>> getAttendanceReport(
            @RequestParam(defaultValue = "month") String period) {
        log.info("Manager requesting attendance report for period: {}", period);
        
        Map<String, Object> response = new HashMap<>();
        
        // Response structure matching frontend expectations
        response.put("title", "Báo cáo Điểm danh - " + period);
        response.put("description", "Thống kê tình hình điểm danh theo " + period);
        
        // Data object with all statistics
        Map<String, Object> data = new HashMap<>();
        data.put("totalSessions", 45);
        data.put("totalStudents", 120);
        data.put("attendanceRate", 85.2);
        data.put("absentStudents", 18);
        
        // Class data for charts and tables
        List<Map<String, Object>> classData = new ArrayList<>();
        String[] classNames = {"Toán 10A", "Văn 10B", "Anh 10C", "Lý 10D", "Hóa 10E"};
        for (String className : classNames) {
            Map<String, Object> classInfo = new HashMap<>();
            classInfo.put("className", className);
            classInfo.put("studentCount", 24);
            classInfo.put("attendanceRate", 75 + (Math.random() * 25));
            classData.add(classInfo);
        }
        data.put("classData", classData);
        
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
        
        // Data object with all statistics
        Map<String, Object> data = new HashMap<>();
        data.put("totalStudents", 120);
        data.put("averageScore", 7.5);
        data.put("excellentCount", 30);
        data.put("goodCount", 35);
        data.put("averageCount", 40);
        data.put("belowAverageCount", 15);
        
        // Subject data for charts
        List<Map<String, Object>> subjectData = new ArrayList<>();
        String[] subjects = {"Toán", "Văn", "Anh", "Lý", "Hóa", "Sinh", "Sử", "Địa"};
        for (String subject : subjects) {
            Map<String, Object> subjectInfo = new HashMap<>();
            subjectInfo.put("subject", subject);
            subjectInfo.put("averageScore", 6.0 + (Math.random() * 3.0));
            subjectData.add(subjectInfo);
        }
        data.put("subjectData", subjectData);
        
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
        
        // Data object with all statistics
        Map<String, Object> data = new HashMap<>();
        data.put("totalRevenue", 2500000000L);
        data.put("totalExpenses", 1800000000L);
        data.put("netProfit", 700000000L);
        data.put("profitMargin", 28.0);
        data.put("studentCount", 1200);
        
        // Monthly revenue data for charts
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        String[] months = {"Tháng 1", "Tháng 2", "Tháng 3"};
        long[] revenues = {850000000L, 800000000L, 850000000L};
        for (int i = 0; i < months.length; i++) {
            Map<String, Object> month = new HashMap<>();
            month.put("month", months[i]);
            month.put("revenue", revenues[i]);
            month.put("expenses", (long)(revenues[i] * 0.72));
            month.put("profit", (long)(revenues[i] * 0.28));
            monthlyData.add(month);
        }
        data.put("monthlyData", monthlyData);
        
        // Expense categories for pie chart
        List<Map<String, Object>> expenseData = new ArrayList<>();
        String[] categories = {"Lương giáo viên", "Cơ sở vật chất", "Học liệu", "Quản lý", "Marketing", "Khác"};
        double[] percentages = {60.0, 15.0, 10.0, 8.0, 5.0, 2.0};
        for (int i = 0; i < categories.length; i++) {
            Map<String, Object> category = new HashMap<>();
            category.put("category", categories[i]);
            category.put("amount", (long)(1800000000L * percentages[i] / 100));
            category.put("percentage", percentages[i]);
            expenseData.add(category);
        }
        data.put("expenseData", expenseData);
        
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