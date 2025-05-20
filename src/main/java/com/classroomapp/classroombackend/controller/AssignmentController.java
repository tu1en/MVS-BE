package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.AssignmentDto;
import com.classroomapp.classroombackend.dto.CreateAssignmentDto;
import com.classroomapp.classroombackend.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    
    @GetMapping("/{id}")
    public ResponseEntity<AssignmentDto> GetAssignmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.GetAssignmentById(id));
    }
    
    @PostMapping
    public ResponseEntity<AssignmentDto> CreateAssignment(@Valid @RequestBody CreateAssignmentDto createAssignmentDto) {
        return new ResponseEntity<>(assignmentService.CreateAssignment(createAssignmentDto), HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentDto> UpdateAssignment(
            @PathVariable Long id,
            @Valid @RequestBody CreateAssignmentDto updateAssignmentDto) {
        return ResponseEntity.ok(assignmentService.UpdateAssignment(id, updateAssignmentDto));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> DeleteAssignment(@PathVariable Long id) {
        assignmentService.DeleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<List<AssignmentDto>> GetAssignmentsByClassroom(@PathVariable Long classroomId) {
        return ResponseEntity.ok(assignmentService.GetAssignmentsByClassroom(classroomId));
    }
    
    @GetMapping("/classroom/{classroomId}/upcoming")
    public ResponseEntity<List<AssignmentDto>> GetUpcomingAssignmentsByClassroom(@PathVariable Long classroomId) {
        return ResponseEntity.ok(assignmentService.GetUpcomingAssignmentsByClassroom(classroomId));
    }
    
    @GetMapping("/classroom/{classroomId}/past")
    public ResponseEntity<List<AssignmentDto>> GetPastAssignmentsByClassroom(@PathVariable Long classroomId) {
        return ResponseEntity.ok(assignmentService.GetPastAssignmentsByClassroom(classroomId));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<AssignmentDto>> SearchAssignmentsByTitle(@RequestParam String title) {
        return ResponseEntity.ok(assignmentService.SearchAssignmentsByTitle(title));
    }
} 