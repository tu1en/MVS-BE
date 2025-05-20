package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.ClassroomDto;
import com.classroomapp.classroombackend.dto.CreateClassroomDto;
import com.classroomapp.classroombackend.service.ClassroomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;
    
    @GetMapping("/{id}")
    public ResponseEntity<ClassroomDto> GetClassroomById(@PathVariable Long id) {
        return ResponseEntity.ok(classroomService.GetClassroomById(id));
    }
    
    @PostMapping
    public ResponseEntity<ClassroomDto> CreateClassroom(
            @Valid @RequestBody CreateClassroomDto createClassroomDto,
            @RequestParam Long teacherId) {
        return new ResponseEntity<>(classroomService.CreateClassroom(createClassroomDto, teacherId), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ClassroomDto> UpdateClassroom(
            @PathVariable Long id,
            @Valid @RequestBody CreateClassroomDto updateClassroomDto) {
        return ResponseEntity.ok(classroomService.UpdateClassroom(id, updateClassroomDto));
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
    
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ClassroomDto>> GetClassroomsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(classroomService.GetClassroomsByStudent(studentId));
    }
    
    @PostMapping("/{classroomId}/students/{studentId}")
    public ResponseEntity<Void> EnrollStudent(
            @PathVariable Long classroomId,
            @PathVariable Long studentId) {
        classroomService.EnrollStudent(classroomId, studentId);
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
} 