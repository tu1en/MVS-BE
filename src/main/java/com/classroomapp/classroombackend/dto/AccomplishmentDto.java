package com.classroomapp.classroombackend.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccomplishmentDto {
    private Long id;
    private Long userId;
    private String userName;

    // New fields for personal accomplishments
    private String title;
    private String description;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;

    // Legacy fields for academic records
    private String courseTitle;
    private String subject;
    private String teacherName;
    private Double grade;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate completionDate;
}