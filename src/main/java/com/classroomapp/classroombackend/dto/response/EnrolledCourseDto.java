package com.classroomapp.classroombackend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class EnrolledCourseDto {
    
    private Long id;
    private String name;
    private String description;
    private String teacherName;
    private String teacherEmail;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String status; // ACTIVE, COMPLETED, PAUSED
    private Integer progress; // 0-100
    private Integer totalLessons;
    private Integer completedLessons;
    private Double totalHours;
    private Double completedHours;
    private LocalDateTime enrolledAt;
    private LocalDateTime lastAccessedAt;
    
    // Course template info
    private Double price;
    private String level; // BASIC, INTERMEDIATE, ADVANCED
    private String category;
    private List<String> tags;
    private Double rating;
    private Integer totalStudents;
    
    // Progress tracking
    private List<LessonProgressDto> lessonsProgress;
    private List<AssignmentProgressDto> assignmentsProgress;
    
    // Course materials
    private List<CourseMaterialSummaryDto> materials;
    
    // Certificates
    private CertificateDto certificate;
    
    @Data
    public static class LessonProgressDto {
        private Long lessonId;
        private String lessonName;
        private Boolean completed;
        private Integer progressPercent;
        private LocalDateTime completedAt;
        private Long timeSpentMinutes;
    }
    
    @Data
    public static class AssignmentProgressDto {
        private Long assignmentId;
        private String assignmentName;
        private String status; // NOT_STARTED, IN_PROGRESS, SUBMITTED, GRADED
        private Double score;
        private Double maxScore;
        private LocalDateTime submittedAt;
        private LocalDateTime dueDate;
    }
    
    @Data
    public static class CourseMaterialSummaryDto {
        private Long materialId;
        private String name;
        private String type;
        private String url;
        private Boolean downloaded;
    }
    
    @Data
    public static class CertificateDto {
        private Long certificateId;
        private String certificateUrl;
        private LocalDateTime issuedAt;
        private String status; // PENDING, ISSUED, EXPIRED
    }
}