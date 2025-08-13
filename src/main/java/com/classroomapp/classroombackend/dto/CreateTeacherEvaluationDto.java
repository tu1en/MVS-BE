package com.classroomapp.classroombackend.dto;

import jakarta.validation.constraints.*;

public class CreateTeacherEvaluationDto {
    @NotNull(message = "Teacher ID is required")
    private Long teacherId;
    
    @NotNull(message = "Class session ID is required")
    private Long classSessionId;
    
    @Min(value = 1, message = "Teaching quality score must be between 1 and 5")
    @Max(value = 5, message = "Teaching quality score must be between 1 and 5")
    @NotNull(message = "Teaching quality score is required")
    private Integer teachingQualityScore;
    
    @Min(value = 1, message = "Student interaction score must be between 1 and 5")
    @Max(value = 5, message = "Student interaction score must be between 1 and 5")
    @NotNull(message = "Student interaction score is required")
    private Integer studentInteractionScore;
    
    @Min(value = 1, message = "Punctuality score must be between 1 and 5")
    @Max(value = 5, message = "Punctuality score must be between 1 and 5")
    @NotNull(message = "Punctuality score is required")
    private Integer punctualityScore;
    
    @Size(max = 1000, message = "Comments must not exceed 1000 characters")
    private String comments;

    // Constructors
    public CreateTeacherEvaluationDto() {}

    public CreateTeacherEvaluationDto(Long teacherId, Long classSessionId, Integer teachingQualityScore, 
                                    Integer studentInteractionScore, Integer punctualityScore, String comments) {
        this.teacherId = teacherId;
        this.classSessionId = classSessionId;
        this.teachingQualityScore = teachingQualityScore;
        this.studentInteractionScore = studentInteractionScore;
        this.punctualityScore = punctualityScore;
        this.comments = comments;
    }

    // Getters and setters
    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public Long getClassSessionId() {
        return classSessionId;
    }

    public void setClassSessionId(Long classSessionId) {
        this.classSessionId = classSessionId;
    }

    public Integer getTeachingQualityScore() {
        return teachingQualityScore;
    }

    public void setTeachingQualityScore(Integer teachingQualityScore) {
        this.teachingQualityScore = teachingQualityScore;
    }

    public Integer getStudentInteractionScore() {
        return studentInteractionScore;
    }

    public void setStudentInteractionScore(Integer studentInteractionScore) {
        this.studentInteractionScore = studentInteractionScore;
    }

    public Integer getPunctualityScore() {
        return punctualityScore;
    }

    public void setPunctualityScore(Integer punctualityScore) {
        this.punctualityScore = punctualityScore;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}