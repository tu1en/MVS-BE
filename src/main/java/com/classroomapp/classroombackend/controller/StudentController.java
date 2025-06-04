package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.ClassroomDto;
import com.classroomapp.classroombackend.dto.CourseDto;
import com.classroomapp.classroombackend.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for student-specific endpoints
 */
@RestController
@RequestMapping("/api/v1/student")
@RequiredArgsConstructor
public class StudentController {

    private final ClassroomService classroomService;
    
    /**
     * Get all courses (classrooms) where a student is enrolled
     * This endpoint matches the one used in the EnrolledCourses.jsx component
     * 
     * @param studentId ID of the student
     * @return List of classrooms where the student is enrolled
     */
    @GetMapping("/courses")
    public ResponseEntity<List<CourseDto>> getEnrolledCourses(@RequestParam Long studentId) {
        List<ClassroomDto> classrooms = classroomService.GetClassroomsByStudent(studentId);
        
        // Convert ClassroomDto to CourseDto to match the frontend expectations
        List<CourseDto> courses = classrooms.stream()
                .map(this::convertToFrontendFormat)
                .collect(Collectors.toList());
                
        return ResponseEntity.ok(courses);
    }
    
    /**
     * Helper method to convert ClassroomDto to CourseDto format expected by the frontend
     * 
     * @param classroom ClassroomDto from service
     * @return CourseDto formatted for frontend
     */
    private CourseDto convertToFrontendFormat(ClassroomDto classroom) {
        CourseDto course = new CourseDto();
        course.setId(classroom.getId());
        course.setTitle(classroom.getName());
        course.setDescription(classroom.getDescription());
        course.setTeacherId(classroom.getTeacherId());
        course.setTeacherName(classroom.getTeacherName());
        // Set a default start date if needed
        course.setStartDate(java.time.LocalDate.now().toString());
        return course;
    }
}
