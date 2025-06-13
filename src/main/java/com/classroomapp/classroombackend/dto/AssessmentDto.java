package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("courseId")
    private Long courseId;
    
    @JsonProperty("lectureId")
    private Long lectureId;
    
    @JsonProperty("type")
    private String type; // QUIZ, ASSIGNMENT, EXAM, PROJECT
    
    @JsonProperty("assessmentType")
    private String assessmentType;
    
    @JsonProperty("totalMarks")
    private Double totalMarks;
    
    @JsonProperty("passingMarks")
    private Double passingMarks;
    
    @JsonProperty("duration")
    private Integer duration; // in minutes
    
    @JsonProperty("timeLimit")
    private Integer timeLimit; // in minutes
    
    @JsonProperty("startTime")
    private LocalDateTime startTime;
    
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
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("createdDate")
    private LocalDateTime createdDate;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}
