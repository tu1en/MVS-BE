package com.classroomapp.classroombackend.dto.classroommanagement;

import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailsDto {
    
    // Basic course information
    private Long id;
    private String name;
    private String description;
    private String section;
    private String subject;
    
    // Teacher information
    private UserDto teacher;
    
    // Student information
    private List<UserDto> students;
    private int totalStudents;
    
    // Course content
    private SyllabusDto syllabus;
    private List<ScheduleDto> schedules;
    
    // Assignments
    private List<AssignmentDto> assignments;
    private int totalAssignments;
    private int activeAssignments;
    
    // Statistics
    private CourseStatistics statistics;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CourseStatistics {
        private int totalStudents;
        private int totalAssignments;
        private int activeAssignments;
        private int completedAssignments;
        private int totalSubmissions;
        private int gradedSubmissions;
        private double averageGrade;
        private double completionRate; // Percentage of assignments completed
    }
}
