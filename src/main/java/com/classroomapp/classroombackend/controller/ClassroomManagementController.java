package com.classroomapp.classroombackend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDetailsDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CourseDetailsDto;
import com.classroomapp.classroombackend.dto.classroommanagement.CreateClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.EnrollmentRequestDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateClassroomDto;
import com.classroomapp.classroombackend.dto.exammangement.ExamDto;
import com.classroomapp.classroombackend.service.ClassroomService;
import com.classroomapp.classroombackend.service.ExamService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Classroom Management Controller - Routes classroom-management API calls to existing ClassroomService
 * This controller acts as a bridge for frontend calls using /classroom-management prefix
 */
@RestController
@RequestMapping("/api/classroom-management/classrooms")
@RequiredArgsConstructor
@Slf4j
public class ClassroomManagementController {

    private final ClassroomService classroomService;
    private final ExamService examService;

    @GetMapping
    public ResponseEntity<List<ClassroomDto>> getAllClassrooms() {
        log.info("🔍 ClassroomManagementController.getAllClassrooms called");
        try {
            List<ClassroomDto> classrooms = classroomService.getAllClassrooms();
            log.info("✅ Successfully retrieved {} classrooms", classrooms.size());
            return ResponseEntity.ok(classrooms);
        } catch (Exception e) {
            log.error("❌ Error retrieving all classrooms: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClassroomDto> GetClassroomById(@PathVariable Long id) {
        return ResponseEntity.ok(classroomService.GetClassroomById(id));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ClassroomDetailsDto> CreateClassroom(
            @Valid @RequestBody CreateClassroomDto createClassroomDto) {
        return ResponseEntity.ok(classroomService.createClassroom(createClassroomDto));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEACHER')")
    public ResponseEntity<ClassroomDto> UpdateClassroom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassroomDto updateClassroomDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(classroomService.UpdateClassroom(id, updateClassroomDto, userDetails));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> DeleteClassroom(@PathVariable Long id) {
        classroomService.DeleteClassroom(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<ClassroomDto>> GetClassroomsByTeacher(@PathVariable Long teacherId) {
        return ResponseEntity.ok(classroomService.GetClassroomsByTeacher(teacherId));
    }
    
    @GetMapping("/teacher/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClassroomDto>> GetClassroomsByCurrentTeacher() {
        log.info("🔍 ClassroomManagementController.GetClassroomsByCurrentTeacher called");
        try {
            List<ClassroomDto> classrooms = classroomService.GetClassroomsByCurrentTeacher();
            log.info("✅ Successfully retrieved {} classrooms for current teacher", classrooms.size());
            return ResponseEntity.ok(classrooms);
        } catch (Exception e) {
            log.error("❌ Error retrieving classrooms for current teacher: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/student/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ClassroomDto>> GetClassroomsByCurrentStudent() {
        log.info("🔍 ClassroomManagementController.GetClassroomsByCurrentStudent called");
        try {
            List<ClassroomDto> classrooms = classroomService.getClassroomsByCurrentStudent();
            log.info("✅ Successfully retrieved {} classrooms for current student", classrooms.size());
            return ResponseEntity.ok(classrooms);
        } catch (Exception e) {
            log.error("❌ Error retrieving classrooms for current student: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ClassroomDto>> GetClassroomsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(classroomService.GetClassroomsByStudent(studentId));
    }
    
    @PostMapping("/{classroomId}/enrollments")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEACHER')")
    public ResponseEntity<Void> enrollStudent(@PathVariable Long classroomId, @Valid @RequestBody EnrollmentRequestDto enrollmentRequest) {
        classroomService.EnrollStudent(classroomId, enrollmentRequest.getStudentId());
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{classroomId}/students/{studentId}")
    public ResponseEntity<Void> UnenrollStudent(
            @PathVariable Long classroomId,
            @PathVariable Long studentId) {
        classroomService.UnenrollStudent(classroomId, studentId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ClassroomDto>> SearchClassrooms(@RequestParam String name) {
        return ResponseEntity.ok(classroomService.SearchClassroomsByName(name));
    }
    
    @GetMapping("/subject/{subject}")
    public ResponseEntity<List<ClassroomDto>> GetClassroomsBySubject(@PathVariable String subject) {
        return ResponseEntity.ok(classroomService.GetClassroomsBySubject(subject));
    }
    
    @GetMapping("/{id}/details")
    public ResponseEntity<CourseDetailsDto> GetCourseDetails(@PathVariable Long id) {
        return ResponseEntity.ok(classroomService.GetCourseDetails(id));
    }
    
    @GetMapping("/{classroomId}/students")
    public ResponseEntity<List<UserDto>> GetClassroomStudents(@PathVariable Long classroomId) {
        return ResponseEntity.ok(classroomService.getStudentsInClassroom(classroomId).stream()
                .map(user -> {
                    UserDto dto = new UserDto();
                    dto.setId(user.getId());
                    dto.setName(user.getFullName());
                    dto.setEmail(user.getEmail());
                    dto.setEnabled("active".equalsIgnoreCase(user.getStatus()));
                    dto.setRoles(java.util.Collections.singleton(user.getRole()));
                    return dto;
                }).collect(java.util.stream.Collectors.toList()));
    }

    @GetMapping("/{classroomId}/exams")
    public ResponseEntity<List<ExamDto>> getExamsByClassroomId(@PathVariable Long classroomId) {
        log.info("=== DEBUG: Getting exams for classroom {} via classroom-management API ===", classroomId);
        try {
            List<ExamDto> exams = examService.getExamsByClassroomId(classroomId);
            log.info("=== DEBUG: Found {} exams ===", exams.size());
            return ResponseEntity.ok(exams);
        } catch (Exception e) {
            log.error("=== DEBUG: Error getting exams: {} ===", e.getMessage());
            throw e;
        }
    }
}