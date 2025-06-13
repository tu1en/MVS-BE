package com.classroomapp.classroombackend.dto.classroommanagement;

import java.util.List;
import java.util.Set;

import com.classroomapp.classroombackend.dto.usermanagement.UserDetailsDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomDetailsDto {
    
    private Long id;
    
    @NotBlank(message = "Classroom name is required")
    @Size(min = 3, max = 100, message = "Classroom name must be between 3 and 100 characters")
    private String name;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    private String section;
    
    private String subject;
    
    // Extended teacher information
    private UserDetailsDto teacher;
    
    // Basic student information
    private Set<Long> studentIds;
    
    private Integer studentCount;
    
    // Syllabus information
    private SyllabusDto syllabus;
    
    // Schedule information
    private List<ScheduleDto> schedules;
}
