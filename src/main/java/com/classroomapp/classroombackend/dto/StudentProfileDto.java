package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for student profile updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StudentProfileDto extends ProfileUpdateDto {
    private LocalDate enrollmentDate;
}
