package com.classroomapp.classroombackend.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.CourseTeacherAssignmentDto;
import com.classroomapp.classroombackend.entity.CourseTeacher;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.service.CourseTeacherService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/course-assignments")
@RequiredArgsConstructor
public class CourseTeacherController {

    private final CourseTeacherService courseTeacherService;

    // Teacher accepts an assignment
    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> acceptAssignment(@PathVariable Long id,
                                                   @AuthenticationPrincipal User user) {
        courseTeacherService.acceptTeacherAssignment(id, user.getId());
        return ResponseEntity.ok().build();
    }

    // Teacher declines an assignment
    @PutMapping("/{id}/decline")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> declineAssignment(@PathVariable Long id,
                                                    @AuthenticationPrincipal User user) {
        courseTeacherService.declineTeacherAssignment(id, user.getId());
        return ResponseEntity.ok().build();
    }

    // Teacher gets their pending assignments
    @GetMapping("/my-assignments")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<CourseTeacherAssignmentDto>> getMyAssignments(@AuthenticationPrincipal User user) {
        List<CourseTeacher> assignments = courseTeacherService.getPendingAssignments(user.getId());
        return ResponseEntity.ok(assignments.stream()
                .map(this::toAssignmentDto)
                .collect(Collectors.toList()));
    }

    // Manager gets all assignments for a course
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<CourseTeacherAssignmentDto>> getCourseAssignments(@PathVariable Long courseId) {
        List<CourseTeacher> assignments = courseTeacherService.findByCourse(courseId);
        return ResponseEntity.ok(assignments.stream()
                .map(this::toAssignmentDto)
                .collect(Collectors.toList()));
    }

    // Manager removes a teacher from course
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> removeTeacher(@PathVariable Long id,
                                              @AuthenticationPrincipal User user) {
        CourseTeacher assignment = courseTeacherService.findById(id);
        courseTeacherService.removeTeacherFromCourse(assignment.getCourse().getId(), 
                                                      assignment.getTeacher().getId(),
                                                      user.getId());
        return ResponseEntity.noContent().build();
    }

    private CourseTeacherAssignmentDto toAssignmentDto(CourseTeacher assignment) {
        CourseTeacherAssignmentDto dto = new CourseTeacherAssignmentDto();
        dto.setId(assignment.getId());
        dto.setCourseName(assignment.getCourse().getName());
        dto.setCourseCode(assignment.getCourse().getCode());
        dto.setCourseSubject(assignment.getCourse().getSyllabus().getSubject());
        dto.setTeacherId(assignment.getTeacher().getId());
        dto.setTeacherName(assignment.getTeacher().getUsername());
        dto.setRole(assignment.getRole().name());
        dto.setStatus(assignment.getStatus().name());
        dto.setAssignedAt(assignment.getAssignedAt().toString());
        dto.setAcceptedAt(assignment.getAcceptedAt() != null ? assignment.getAcceptedAt().toString() : null);
        dto.setNotes(assignment.getNotes());
        return dto;
    }
}