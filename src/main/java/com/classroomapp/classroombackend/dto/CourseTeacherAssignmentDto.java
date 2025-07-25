package com.classroomapp.classroombackend.dto;

import lombok.Data;

@Data
public class CourseTeacherAssignmentDto {
    private Long id;
    private String courseName;
    private String courseCode;
    private String courseSubject;
    private Long teacherId;
    private String teacherName;
    private String role;
    private String status;
    private String assignedAt;
    private String acceptedAt;
    private String notes;
}