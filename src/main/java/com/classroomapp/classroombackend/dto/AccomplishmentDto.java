package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccomplishmentDto {
    private Long id;
    private Long userId;
    private String courseTitle;
    private String subject;
    private String teacherName;
    private Double grade;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate completionDate;
    
    private String userName; // Added to include user details in responses
}