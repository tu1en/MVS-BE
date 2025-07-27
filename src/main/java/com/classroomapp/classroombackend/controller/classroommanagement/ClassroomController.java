package com.classroomapp.classroombackend.controller.classroommanagement;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.CreateClassroomDto;
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateClassroomDto;
import com.classroomapp.classroombackend.service.classroommanagement.ClassroomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller để xử lý các API cho Classroom Management
 * Đây là controller chính cho frontend gọi
 */
@RestController
@RequestMapping("/api/classroom-management")
@RequiredArgsConstructor
@Slf4j
public class ClassroomController {

    private final ClassroomService classroomService;

    // ================= CRUD Endpoints ================= //

    @GetMapping("/classrooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Page<ClassroomDto>> getAllClassrooms(Pageable pageable) {
        log.info("Getting all classrooms with pagination");
        Page<ClassroomDto> classrooms = classroomService.getAllClassrooms(pageable);
        return ResponseEntity.ok(classrooms);
    }

    @GetMapping("/classrooms/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ClassroomDto> getClassroomById(@PathVariable Long id) {
        log.info("Getting classroom by id: {}", id);
        ClassroomDto classroom = classroomService.getClassroomById(id);
        return ResponseEntity.ok(classroom);
    }

    @PostMapping("/classrooms")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ClassroomDto> createClassroom(@RequestBody CreateClassroomDto createDto) {
        log.info("Creating new classroom: {}", createDto.getName());
        ClassroomDto createdClassroom = classroomService.createClassroom(createDto);
        return new ResponseEntity<>(createdClassroom, HttpStatus.CREATED);
    }

    @PutMapping("/classrooms/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ClassroomDto> updateClassroom(@PathVariable Long id, @RequestBody UpdateClassroomDto updateDto) {
        log.info("Updating classroom with id: {}", id);
        ClassroomDto updatedClassroom = classroomService.updateClassroom(id, updateDto);
        return ResponseEntity.ok(updatedClassroom);
    }

    @DeleteMapping("/classrooms/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long id) {
        log.info("Deleting classroom with id: {}", id);
        classroomService.deleteClassroom(id);
        return ResponseEntity.noContent().build();
    }

    // ================= Search Endpoint ================= //

    @GetMapping("/classrooms/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Page<ClassroomDto>> searchClassrooms(@RequestParam String keyword, Pageable pageable) {
        log.info("Searching classrooms with keyword: {}", keyword);
        Page<ClassroomDto> classrooms = classroomService.searchClassrooms(keyword, pageable);
        return ResponseEntity.ok(classrooms);
    }

    // ================= Current User Endpoints ================= //

    /**
     * API endpoint mà Frontend đang gọi: /api/classroom-management/classrooms/student/me
     */
    @GetMapping("/classrooms/student/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ClassroomDto>> getMyStudentCourses(Authentication authentication) {
        log.info("Getting classrooms for current student: {}", authentication.getName());
        List<ClassroomDto> classrooms = classroomService.getClassroomsByCurrentStudent();
        log.info("Found {} classrooms for student: {}", classrooms.size(), authentication.getName());
        return ResponseEntity.ok(classrooms);
    }
// Thêm endpoint này vào ClassroomController của bạn

/**
 * Endpoint mà Frontend đang gọi: /api/classroom-management/current-teacher
 * Đây là endpoint ngắn gọn hơn để lấy thông tin teacher hiện tại
 */
@GetMapping("/current-teacher")
@PreAuthorize("hasRole('TEACHER')")
public ResponseEntity<List<ClassroomDto>> getCurrentTeacherClassrooms(Authentication authentication) {
    log.info("Getting classrooms for current teacher: {}", authentication.getName());
    List<ClassroomDto> classrooms = classroomService.getClassroomsByCurrentTeacher();
    log.info("Found {} classrooms for teacher: {}", classrooms.size(), authentication.getName());
    return ResponseEntity.ok(classrooms);
}
    /**
     * Alternative endpoint for student courses
     */
    @GetMapping("/classrooms/current-student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ClassroomDto>> getClassroomsByCurrentStudent() {
        log.info("Getting classrooms for current student (alternative endpoint)");
        List<ClassroomDto> classrooms = classroomService.getClassroomsByCurrentStudent();
        return ResponseEntity.ok(classrooms);
    }

    @GetMapping("/classrooms/teacher/me")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ClassroomDto>> getMyTeacherCourses(Authentication authentication) {
        log.info("Getting classrooms for current teacher: {}", authentication.getName());
        List<ClassroomDto> classrooms = classroomService.getClassroomsByCurrentTeacher();
        log.info("Found {} classrooms for teacher: {}", classrooms.size(), authentication.getName());
        return ResponseEntity.ok(classrooms);
    }

    @GetMapping("/classrooms/current-teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ClassroomDto>> getClassroomsByCurrentTeacher() {
        log.info("Getting classrooms for current teacher (alternative endpoint)");
        List<ClassroomDto> classrooms = classroomService.getClassroomsByCurrentTeacher();
        return ResponseEntity.ok(classrooms);
    }

    @GetMapping("/classrooms/{id}/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ClassroomDto> getClassroomDetails(@PathVariable Long id) {
        log.info("Getting detailed information for classroom: {}", id);
        ClassroomDto classroom = classroomService.getClassroomDetails(id);
        return ResponseEntity.ok(classroom);
    }

    // ================= Enrollment Endpoints ================= //

    @PostMapping("/classrooms/{classroomId}/students/{studentId}/enroll")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> enrollStudent(@PathVariable Long classroomId, @PathVariable Long studentId) {
        log.info("Enrolling student {} in classroom {}", studentId, classroomId);
        classroomService.enrollStudent(classroomId, studentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/classrooms/{classroomId}/students/{studentId}/unenroll")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> unenrollStudent(@PathVariable Long classroomId, @PathVariable Long studentId) {
        log.info("Unenrolling student {} from classroom {}", studentId, classroomId);
        classroomService.unenrollStudent(classroomId, studentId);
        return ResponseEntity.ok().build();
    }

    // ================= Student List Endpoint ================= //

    @GetMapping("/classrooms/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<UserDto>> getStudentsInClassroom(@PathVariable Long id) {
        log.info("Getting students in classroom: {}", id);
        List<UserDto> students = classroomService.getStudentsInClassroom(id);
        return ResponseEntity.ok(students);
    }

    // ================= Student-specific endpoints ================= //

    @GetMapping("/students/{studentId}/classrooms")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<ClassroomDto>> getClassroomsByStudentId(@PathVariable Long studentId) {
        log.info("Getting classrooms for student: {}", studentId);
        List<ClassroomDto> classrooms = classroomService.getClassroomsByStudentId(studentId);
        return ResponseEntity.ok(classrooms);
    }
}