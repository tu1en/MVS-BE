package com.classroomapp.classroombackend.dto;

import com.classroomapp.classroombackend.entity.enumeration.SyllabusStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateSyllabusRequest {
    private String name;           // Tên syllabus: VD "Toán 11"
    private String code;         // Mã: VD "MATH11"
    private String description;  // Mô tả chi tiết
    private String subject;        // Môn học: Toán, Lý, Hóa...
    private String gradeLevel;     // Cấp độ: Lớp 11, THPT...
}