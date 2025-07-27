package com.classroomapp.classroombackend.dto;

import com.classroomapp.classroombackend.entity.enumeration.SyllabusStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CreateCourseRequest {
    private Long syllabusId;     // ID của syllabus
    private String name;         // Tên khóa: VD "Toán 11 - Khóa tháng 8"
    private String code;         // Mã khóa: VD "MATH11_AUG2024"
    private String description;  // Mô tả chi tiết
    private LocalDate startDate; // Ngày bắt đầu khóa học
    private LocalDate endDate;   // Ngày kết thúc
    private Integer maxStudents; // Số học sinh tối đa (mặc định: 30)
    private BigDecimal price;    // Học phí (VNĐ)
}