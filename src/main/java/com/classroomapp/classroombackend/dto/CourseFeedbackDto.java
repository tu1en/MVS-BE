package com.classroomapp.classroombackend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseFeedbackDto {
    private Long id;
    
    @NotNull(message = "Student ID không được để trống")
    private Long studentId;
    private String studentName;
    
    @NotNull(message = "Classroom ID không được để trống")
    private Long classroomId;
    private String classroomName;
    
    @NotNull(message = "Teacher ID không được để trống")
    private Long teacherId;
    private String teacherName;
    
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    
    @NotBlank(message = "Nội dung feedback không được để trống")
    private String content;
    
    @NotNull(message = "Đánh giá tổng thể không được để trống")
    @Min(value = 1, message = "Đánh giá phải từ 1-5")
    @Max(value = 5, message = "Đánh giá phải từ 1-5")
    private Integer overallRating;
    
    @Min(value = 1, message = "Đánh giá phải từ 1-5")
    @Max(value = 5, message = "Đánh giá phải từ 1-5")
    private Integer teachingQualityRating;
    
    @Min(value = 1, message = "Đánh giá phải từ 1-5")
    @Max(value = 5, message = "Đánh giá phải từ 1-5")
    private Integer courseMaterialRating;
    
    @Min(value = 1, message = "Đánh giá phải từ 1-5")
    @Max(value = 5, message = "Đánh giá phải từ 1-5")
    private Integer supportRating;
    
    private String category = "GENERAL"; // GENERAL, TEACHING, MATERIAL, SUPPORT, SUGGESTION
    private String status = "SUBMITTED"; // SUBMITTED, REVIEWED, ACKNOWLEDGED
    
    private Boolean isAnonymous = false;
    
    private LocalDateTime reviewedAt;
    private Long reviewedById;
    private String reviewedByName;
    private String response;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Explicit getters to resolve compilation issues
    public Long getClassroomId() { return classroomId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Integer getOverallRating() { return overallRating; }
    public Integer getTeachingQualityRating() { return teachingQualityRating; }
    public Integer getCourseMaterialRating() { return courseMaterialRating; }
    public Integer getSupportRating() { return supportRating; }
    public String getCategory() { return category; }
    public Boolean getIsAnonymous() { return isAnonymous; }
    
    // Explicit setters to resolve compilation issues
    public void setStudentName(String studentName) { this.studentName = studentName; }
}
