package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssessmentDto {
    
    @NotBlank(message = "Title is required")
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;
    
    @NotNull(message = "Course ID is required")
    @JsonProperty("courseId")
    private Long courseId;
    
    @JsonProperty("lectureId")
    private Long lectureId;
    
    @NotBlank(message = "Assessment type is required")
    @JsonProperty("type")
    private String type; // QUIZ, ASSIGNMENT, EXAM, PROJECT
    
    @JsonProperty("assessmentType")
    private String assessmentType;
    
    @NotNull(message = "Total marks is required")
    @Min(value = 0, message = "Total marks must be non-negative")
    @JsonProperty("totalMarks")
    private Double totalMarks;
    
    @Min(value = 0, message = "Passing marks must be non-negative")
    @JsonProperty("passingMarks")
    private Double passingMarks;
    
    @JsonProperty("duration")
    private Integer duration; // in minutes
    
    @JsonProperty("timeLimit")
    private Integer timeLimit; // in minutes
    
    @NotNull(message = "Start time is required")
    @JsonProperty("startTime")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    @JsonProperty("endTime")
    private LocalDateTime endTime;
    
    @JsonProperty("allowMultipleAttempts")
    private Boolean allowMultipleAttempts = false;
    
    @JsonProperty("maxAttempts")
    private Integer maxAttempts = 1;
    
    @JsonProperty("isVisible")
    private Boolean isVisible = true;
    
    @JsonProperty("instructions")
    private String instructions;
    
    @JsonProperty("questions")
    private List<String> questions;
    
    @JsonProperty("attachments")
    private List<String> attachments;
}
