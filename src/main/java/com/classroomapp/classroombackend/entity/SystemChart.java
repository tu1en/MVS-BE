package com.classroomapp.classroombackend.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity để lưu trữ system charts/diagrams
 */
@Entity
@Table(name = "system_charts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemChart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "chart_type", nullable = false)
    private ChartType chartType;

    @Column(name = "chart_data", columnDefinition = "TEXT")
    private String chartData;

    @Column(name = "chart_config", columnDefinition = "TEXT")
    private String chartConfig;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ChartType {
        STUDENT_COUNT_BY_MONTH,     // Số lượng học sinh theo tháng
        STUDENT_ATTENDANCE,         // Thống kê điểm danh học sinh
        STUDENT_ABSENCE,            // Học sinh nghỉ học
        COURSE_ENROLLMENT,          // Đăng ký khóa học
        ASSIGNMENT_SUBMISSION,      // Nộp bài tập
        GRADE_DISTRIBUTION,         // Phân bố điểm số
        TEACHER_WORKLOAD,           // Khối lượng công việc giáo viên
        SYSTEM_USAGE,               // Sử dụng hệ thống
        LOGIN_ACTIVITY,             // Hoạt động đăng nhập
        REVENUE_REPORT,             // Báo cáo doanh thu
        BAR_CHART,                  // Biểu đồ cột
        LINE_CHART,                 // Biểu đồ đường
        PIE_CHART,                  // Biểu đồ tròn
        DOUGHNUT_CHART,             // Biểu đồ bánh donut
        AREA_CHART                  // Biểu đồ vùng
    }
}