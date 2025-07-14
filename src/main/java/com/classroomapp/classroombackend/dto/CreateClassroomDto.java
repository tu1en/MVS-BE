package com.classroomapp.classroombackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateClassroomDto {

    @NotBlank(message = "Classroom name is required")
    @Size(min = 3, max = 100, message = "Classroom name must be between 3 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private String section;

    private String subject;

    @NotNull(message = "Teacher is required")
    private Long teacherId;

    @NotNull(message = "Course is required")
    private Long courseId;
}