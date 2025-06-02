package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.AssignmentDto;
import com.classroomapp.classroombackend.dto.CreateAssignmentDto;
import com.classroomapp.classroombackend.service.AssignmentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    
    @GetMapping
    public List<Map<String, Object>> getAllAssignments() {
        List<Map<String, Object>> assignments = new ArrayList<>();
        
        Map<String, Object> assignment1 = new HashMap<>();
        assignment1.put("id", 1);
        assignment1.put("title", "Bài tập Spring Security");
        assignment1.put("description", "Tìm hiểu về JWT");
        assignment1.put("dueDate", "2023-12-31");
        
        assignments.add(assignment1);
        return assignments;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getAssignmentById(@PathVariable int id) {
        Map<String, Object> assignment = new HashMap<>();
        assignment.put("id", id);
        assignment.put("title", "Bài tập " + id);
        assignment.put("description", "Mô tả bài tập " + id);
        assignment.put("dueDate", "2023-12-31");
        return assignment;
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