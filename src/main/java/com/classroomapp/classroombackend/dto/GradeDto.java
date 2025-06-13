package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeDto {
    private Long id;
    private Long submissionId;
    private Double grade;
    private String feedback;
    private LocalDateTime gradedDate;
    private String gradedBy;
}
