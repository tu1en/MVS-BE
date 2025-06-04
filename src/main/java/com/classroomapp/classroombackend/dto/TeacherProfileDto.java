package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for teacher profile updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TeacherProfileDto extends ProfileUpdateDto {
    private String department;
    private LocalDate hireDate;
}
