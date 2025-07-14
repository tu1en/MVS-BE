package com.classroomapp.classroombackend.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.ClassroomDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.AssignmentDto;
import com.classroomapp.classroombackend.security.CustomUserDetails;
import com.classroomapp.classroombackend.security.JwtUtil;
import com.classroomapp.classroombackend.service.AssignmentService;
import com.classroomapp.classroombackend.service.ClassroomService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, allowCredentials = "true")
public class TestController {

    @Autowired
    private ClassroomService classroomService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private JwtUtil jwtUtil;

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

    @GetMapping("/debug/auth")
    public ResponseEntity<Map<String, Object>> debugAuthentication(Authentication authentication, HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Basic authentication info
            response.put("isAuthenticated", authentication != null && authentication.isAuthenticated());
            response.put("principal", authentication != null ? authentication.getName() : null);
            response.put("authorities", authentication != null ?
                authentication.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(java.util.stream.Collectors.toList()) : null);

            // JWT token info
            String authHeader = request.getHeader("Authorization");
            response.put("authHeader", authHeader);
            response.put("hasBearer", authHeader != null && authHeader.startsWith("Bearer "));

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                response.put("tokenLength", token.length());
                response.put("tokenValid", jwtUtil.validateToken(token));
                response.put("tokenSubject", jwtUtil.getSubjectFromToken(token));
                response.put("tokenRole", jwtUtil.getRoleFromToken(token));
            }

            // User details if available
            if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                response.put("userId", userDetails.getId());
                response.put("userEmail", userDetails.getUser().getEmail());
                response.put("userRoleId", userDetails.getUser().getRoleId());
                response.put("userRole", userDetails.getUser().getRole());
            }

            response.put("status", "success");
        } catch (Exception e) {
            response.put("error", e.getMessage());
            response.put("status", "error");
        }

        return ResponseEntity.ok(response);
    }

}