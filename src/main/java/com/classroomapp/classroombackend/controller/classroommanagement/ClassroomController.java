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
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.dto.classroommanagement.UpdateClassroomDto;
import com.classroomapp.classroombackend.dto.usermanagement.UserDTO;
import com.classroomapp.classroombackend.service.classroommanagement.ClassroomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
        Page<ClassroomDto> classrooms = classroomService.getAllClassrooms(pageable);
        return ResponseEntity.ok(classrooms);
    }

    @GetMapping("/classrooms/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<ClassroomDto> getClassroomById(@PathVariable Long id) {
        ClassroomDto classroom = classroomService.getClassroomById(id);
        return ResponseEntity.ok(classroom);
    }

    @PostMapping("/classrooms")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ClassroomDto> createClassroom(@RequestBody CreateClassroomDto createDto) {
        ClassroomDto createdClassroom = classroomService.createClassroom(createDto);
        return new ResponseEntity<>(createdClassroom, HttpStatus.CREATED);
    }

    @PutMapping("/classrooms/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<ClassroomDto> updateClassroom(@PathVariable Long id, @RequestBody UpdateClassroomDto updateDto) {
        ClassroomDto updatedClassroom = classroomService.updateClassroom(id, updateDto);
        return ResponseEntity.ok(updatedClassroom);
    }

    @DeleteMapping("/classrooms/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteClassroom(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return ResponseEntity.noContent().build();
    }

    // ================= Search Endpoint ================= //

    @GetMapping("/classrooms/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public ResponseEntity<Page<ClassroomDto>> searchClassrooms(@RequestParam String keyword, Pageable pageable) {
        Page<ClassroomDto> classrooms = classroomService.searchClassrooms(keyword, pageable);
        return ResponseEntity.ok(classrooms);
    }

    // ================= Current User Endpoints ================= //

    @GetMapping("/classrooms/current-teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ClassroomDto>> getClassroomsByCurrentTeacher() {
        log.info("ClassroomController: getClassroomsByCurrentTeacher endpoint called");
        List<ClassroomDto> classrooms = classroomService.getClassroomsByCurrentTeacher();
        log.info("ClassroomController: Found {} classrooms for current teacher", classrooms.size());
        return ResponseEntity.ok(classrooms);
    }

    @GetMapping("/teacher/classes")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<ClassroomDto>> getTeacherClassesAlias(Authentication authentication) {
        return getClassroomsByCurrentTeacher();
    }

    @GetMapping("/classrooms/{id}/details")
    public ResponseEntity<ClassroomDto> getClassroomDetails(@PathVariable Long id) {
        ClassroomDto classroom = classroomService.getClassroomDetails(id);
        return ResponseEntity.ok(classroom);
    }

    @GetMapping("/classrooms/current-student")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<ClassroomDto>> getClassroomsByCurrentStudent() {
        List<ClassroomDto> classrooms = classroomService.getClassroomsByCurrentStudent();
        return ResponseEntity.ok(classrooms);
    }
    
    // ================= Enrollment Endpoints ================= //

    @PostMapping("/classrooms/{classroomId}/students/{studentId}/enroll")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> enrollStudent(@PathVariable Long classroomId, @PathVariable Long studentId) {
        classroomService.enrollStudent(classroomId, studentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/classrooms/{classroomId}/students/{studentId}/unenroll")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> unenrollStudent(@PathVariable Long classroomId, @PathVariable Long studentId) {
        classroomService.unenrollStudent(classroomId, studentId);
        return ResponseEntity.ok().build();
    }

    // ================= Student List Endpoint ================= //

    @GetMapping("/classrooms/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<List<UserDTO>> getStudentsInClassroom(@PathVariable Long id) {
        List<UserDTO> students = classroomService.getStudentsInClassroom(id);
        return ResponseEntity.ok(students);
    }

}
