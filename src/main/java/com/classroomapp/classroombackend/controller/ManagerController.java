package com.classroomapp.classroombackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        // Return all fields expected by the frontend
        Map<String, Object> stats = Map.of(
            "totalUsers", 100,
            "totalCourses", 20,
            "totalSchedules", 10,
            "totalMessages", 5
        );
        return ResponseEntity.ok(stats);
    }
} 