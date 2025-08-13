package com.classroomapp.classroombackend.dto;

public class TeacherEvaluationStatisticsDto {
    private Long teacherId;
    private String teacherName;
    private Long evaluationCount;
    private Double averageOverallScore;
    private Double averageTeachingQualityScore;
    private Double averageStudentInteractionScore;
    private Double averagePunctualityScore;

    // Constructors
    public TeacherEvaluationStatisticsDto() {}

    public TeacherEvaluationStatisticsDto(Long teacherId, String teacherName, Long evaluationCount, 
                                        Double averageOverallScore, Double averageTeachingQualityScore, 
                                        Double averageStudentInteractionScore, Double averagePunctualityScore) {
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.evaluationCount = evaluationCount;
        this.averageOverallScore = averageOverallScore;
        this.averageTeachingQualityScore = averageTeachingQualityScore;
        this.averageStudentInteractionScore = averageStudentInteractionScore;
        this.averagePunctualityScore = averagePunctualityScore;
    }

    // Getters and setters
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

    public Long getEvaluationCount() {
        return evaluationCount;
    }

    public void setEvaluationCount(Long evaluationCount) {
        this.evaluationCount = evaluationCount;
    }

    public Double getAverageOverallScore() {
        return averageOverallScore;
    }

    public void setAverageOverallScore(Double averageOverallScore) {
        this.averageOverallScore = averageOverallScore;
    }

    public Double getAverageTeachingQualityScore() {
        return averageTeachingQualityScore;
    }

    public void setAverageTeachingQualityScore(Double averageTeachingQualityScore) {
        this.averageTeachingQualityScore = averageTeachingQualityScore;
    }

    public Double getAverageStudentInteractionScore() {
        return averageStudentInteractionScore;
    }

    public void setAverageStudentInteractionScore(Double averageStudentInteractionScore) {
        this.averageStudentInteractionScore = averageStudentInteractionScore;
    }

    public Double getAveragePunctualityScore() {
        return averagePunctualityScore;
    }

    public void setAveragePunctualityScore(Double averagePunctualityScore) {
        this.averagePunctualityScore = averagePunctualityScore;
    }
}