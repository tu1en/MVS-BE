package com.classroomapp.classroombackend.dto;

import java.util.Set;

import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomDto {

    private Long id;

    @NotBlank(message = "Classroom name is required")
    @Size(min = 3, max = 100, message = "Classroom name must be between 3 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private String section;

    private String subject;

    private Long teacherId;

    private String teacherName;

    private Set<Long> studentIds;

    private Integer studentCount;

    private Double progressPercentage;

    // Additional fields for frontend compatibility
    private java.util.List<UserDto> enrolledStudents;

    private java.util.List<AssignmentDto> assignments;

    private Integer assignmentCount;
}