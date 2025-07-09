package com.classroomapp.classroombackend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.service.AssignmentService;
import com.classroomapp.classroombackend.service.ClassroomService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class TestController {

    @Autowired
    private ClassroomService classroomService;
    
    @Autowired
    private AssignmentService assignmentService;

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "API is working!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cors-test")
    public ResponseEntity<String> testCors(@RequestBody(required = false) String body) {
        return ResponseEntity.ok("CORS test successful. Received: " + body);
    }
    
    @GetMapping("/debug/teacher/{teacherId}")
    public ResponseEntity<Map<String, Object>> debugTeacherData(@PathVariable Long teacherId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ClassroomDto> classrooms = classroomService.GetClassroomsByTeacher(teacherId);
            List<AssignmentDto> assignments = assignmentService.getAssignmentsByTeacher(teacherId);
            
            response.put("teacherId", teacherId);
            response.put("classrooms", classrooms);
            response.put("assignments", assignments);
            response.put("classroomCount", classrooms.size());
            response.put("assignmentCount", assignments.size());
            response.put("status", "success");
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "error");
        }
        
        return ResponseEntity.ok(response);
    }
} 