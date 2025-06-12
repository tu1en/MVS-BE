package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentQuestionDto {
    private Long id;
    
    @NotNull(message = "Student ID không được để trống")
    private Long studentId;
    private String studentName;
    
    @NotNull(message = "Teacher ID không được để trống")
    private Long teacherId;
    private String teacherName;
    
    @NotBlank(message = "Chủ đề không được để trống")
    private String subject;
    
    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String content;
    
    private String priority = "MEDIUM"; // LOW, MEDIUM, HIGH, URGENT
    private String status = "PENDING"; // PENDING, ANSWERED, CLOSED
    
    private String answer;
    private LocalDateTime answeredAt;
    private Long answeredById;
    private String answeredByName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
