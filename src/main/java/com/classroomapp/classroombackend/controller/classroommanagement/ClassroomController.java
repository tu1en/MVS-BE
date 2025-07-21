package com.classroomapp.classroombackend.controller.classroommanagement;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
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
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateClassroomDto;
import com.classroomapp.classroombackend.service.classroommanagement.ClassroomService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller cho Classroom Management
 */
@RestController
@RequestMapping("/api/classroom-management/classrooms")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(originPatterns = {"http://localhost:3000", "http://localhost:5173"}, allowedHeaders = "*", allowCredentials = "true")
public class ClassroomController {

    private final ClassroomService classroomService;

    /**
     * Lấy tất cả classrooms với pagination và search
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ClassroomDto>> getAllClassrooms(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20) Pageable pageable,
            Authentication authentication) {

        log.info("🔍 Getting all classrooms - User: {}, Search: {}", authentication.getName(), search);

        Page<ClassroomDto> classrooms;
        if (search != null && !search.trim().isEmpty()) {
            classrooms = classroomService.searchClassrooms(search.trim(), pageable);
        } else {
            classrooms = classroomService.getAllClassrooms(pageable);
        }

        return ResponseEntity.ok(classrooms);
    }

    /**
     * Lấy classroom theo ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ClassroomDto> getClassroomById(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("🔍 Getting classroom by ID: {} - User: {}", id, authentication.getName());
        ClassroomDto classroom = classroomService.getClassroomById(id);
        return ResponseEntity.ok(classroom);
    }

    /**
     * Lấy chi tiết classroom (bao gồm students, schedule)
     */
    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN','STUDENT')")
    public ResponseEntity<ClassroomDto> getClassroomDetails(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("🔍 Getting classroom details by ID: {} - User: {}", id, authentication.getName());
        ClassroomDto classroomDetails = classroomService.getClassroomDetails(id);
        return ResponseEntity.ok(classroomDetails);
    }

    /**
     * Tạo classroom mới
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ClassroomDto> createClassroom(
            @Valid @RequestBody CreateClassroomDto createDto,
            Authentication authentication) {

        log.info("📝 Creating new classroom: {} - User: {}", createDto.getClassroomName(), authentication.getName());
        ClassroomDto newClassroom = classroomService.createClassroom(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(newClassroom);
    }

    /**
     * Cập nhật classroom
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ClassroomDto> updateClassroom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateClassroomDto updateDto,
            Authentication authentication) {

        log.info("📝 Updating classroom ID: {} - User: {}", id, authentication.getName());
        ClassroomDto updatedClassroom = classroomService.updateClassroom(id, updateDto);
        return ResponseEntity.ok(updatedClassroom);
    }

    /**
     * Xóa classroom
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteClassroom(
            @PathVariable Long id,
            Authentication authentication) {

        log.info("🗑️ Deleting classroom ID: {} - User: {}", id, authentication.getName());
        classroomService.deleteClassroom(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Xóa classroom thành công");
        response.put("deletedId", id);

        return ResponseEntity.ok(response);
    }

    /**
     * Lấy classrooms theo teacher ID
     */
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ClassroomDto>> getClassroomsByTeacher(
            @PathVariable Long teacherId,
            Authentication authentication) {

        log.info("🔍 Getting classrooms for teacher ID: {} - User: {}", teacherId, authentication.getName());
        List<ClassroomDto> classrooms = classroomService.getClassroomsByTeacher(teacherId);
        return ResponseEntity.ok(classrooms);
    }

    /**
     * ✅ Lấy classrooms mà student hiện tại đang học
     */
    @GetMapping("/student/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ClassroomDto>> getClassroomsForCurrentStudent(Authentication authentication) {
        String username = authentication.getName();
        log.info("📚 Getting classrooms for current student: {}", username);
        List<ClassroomDto> classrooms = classroomService.getClassroomsByStudentUsername(username);
        return ResponseEntity.ok(classrooms);
    }

    /**
     * ✅ Lấy classrooms theo student ID (Admin/Manager)
     */
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<ClassroomDto>> getClassroomsByStudentId(
            @PathVariable Long studentId,
            Authentication authentication) {

        log.info("📚 Getting classrooms for student ID: {} - User: {}", studentId, authentication.getName());
        List<ClassroomDto> classrooms = classroomService.getClassroomsByStudentId(studentId);
        return ResponseEntity.ok(classrooms);
    }
}
