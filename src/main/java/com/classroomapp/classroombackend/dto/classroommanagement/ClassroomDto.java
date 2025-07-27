package com.classroomapp.classroombackend.dto.classroommanagement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.classroomapp.classroombackend.dto.UserDto;
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

    // Missing fields from Classroom entity
    private Long courseId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Navigation properties from ClassroomMapper
    private List<UserDto> enrolledStudents;
    private List<AssignmentDto> assignments;
    
    // Derived fields from ClassroomMapper
    private Integer totalStudents;
    private Integer assignmentCount;
    private Boolean hasStudents;

    // Constructor for simple creation (id, name, description)
    public ClassroomDto(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Double calculateProgress() {
        if (progressPercentage != null) {
            return progressPercentage;
        }
        if (assignmentCount != null && assignmentCount > 0) {
            return Math.min(100.0, (assignmentCount * 10.0));
        }
        return 0.0;
    }
}