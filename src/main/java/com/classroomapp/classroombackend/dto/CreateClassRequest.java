package com.classroomapp.classroombackend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassRequest {
    
    @NotNull(message = "Course template ID is required")
    private Long courseTemplateId;
    
    @NotBlank(message = "Class name is required")
    private String className;
    
    private String description;
    
    private Long teacherId;
    
    private Long roomId;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    // End date có thể để trống; BE sẽ tự tính dựa trên số bài học và lịch học nếu không truyền
    private LocalDate endDate;
    
    private String schedule;
    
    private Integer maxStudents = 30;
    
    private Long createdBy;
}