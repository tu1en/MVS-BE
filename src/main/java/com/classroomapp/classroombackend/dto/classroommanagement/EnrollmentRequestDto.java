package com.classroomapp.classroombackend.dto.classroommanagement;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentRequestDto {
    @NotNull(message = "Student ID is required")
    private Long studentId;
} 