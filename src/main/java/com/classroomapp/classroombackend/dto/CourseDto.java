package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for course information formatted specifically for the frontend
 * This matches the data structure expected by the EnrolledCourses.jsx component
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String title;
    private String description;
    private Long teacherId;
    private String teacherName;
    private String startDate;
}
