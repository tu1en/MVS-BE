package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

public class TeacherEvaluationDto {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private Long evaluatorId;
    private String evaluatorName;
    private LocalDateTime evaluationDate;
    private Integer teachingQualityScore;
    private Integer studentInteractionScore;
    private Integer punctualityScore;
    private Integer overallScore;
    private String comments;
    private Long classSessionId;

    // Constructors
    public TeacherEvaluationDto() {}

    public TeacherEvaluationDto(Long id, Long teacherId, String teacherName, Long evaluatorId, 
                               String evaluatorName, LocalDateTime evaluationDate, 
                               Integer teachingQualityScore, Integer studentInteractionScore, 
                               Integer punctualityScore, Integer overallScore, String comments, 
                               Long classSessionId) {
        this.id = id;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.evaluatorId = evaluatorId;
        this.evaluatorName = evaluatorName;
        this.evaluationDate = evaluationDate;
        this.teachingQualityScore = teachingQualityScore;
        this.studentInteractionScore = studentInteractionScore;
        this.punctualityScore = punctualityScore;
        this.overallScore = overallScore;
        this.comments = comments;
        this.classSessionId = classSessionId;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Long getEvaluatorId() {
        return evaluatorId;
    }

    public void setEvaluatorId(Long evaluatorId) {
        this.evaluatorId = evaluatorId;
    }

    public String getEvaluatorName() {
        return evaluatorName;
    }

    public void setEvaluatorName(String evaluatorName) {
        this.evaluatorName = evaluatorName;
    }

    public LocalDateTime getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(LocalDateTime evaluationDate) {
        this.evaluationDate = evaluationDate;
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

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Long getClassSessionId() {
        return classSessionId;
    }

    public void setClassSessionId(Long classSessionId) {
        this.classSessionId = classSessionId;
    }
}