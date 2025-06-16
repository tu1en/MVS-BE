package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ClassroomService;

/**
 * Teacher-specific controller for teacher dashboard, schedule, and courses
 */
@RestController
@RequestMapping("/api/teacher")
@CrossOrigin(origins = "http://localhost:3000")
public class TeacherController {

    @Autowired
    private ClassroomService classroomService;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Get teacher's schedule
     * Frontend calls: /teacher/schedule
     */
    @GetMapping("/schedule")
    public ResponseEntity<List<Map<String, Object>>> getTeacherSchedule(Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            // Mock schedule data for teacher
            List<Map<String, Object>> schedule = new ArrayList<>();
            
            // Thứ 2
            Map<String, Object> schedule1 = new HashMap<>();
            schedule1.put("id", 1);
            schedule1.put("day", 1); // Monday
            schedule1.put("className", "Lớp Java Advanced");
            schedule1.put("subject", "Lập trình Java");
            schedule1.put("start", "08:00");
            schedule1.put("end", "10:00");
            schedule1.put("teacherName", currentUser.getFullName());
            schedule1.put("room", "P.101");
            schedule1.put("studentCount", 25);
            schedule1.put("materialsUrl", "#");
            schedule1.put("meetUrl", null);
            schedule.add(schedule1);
            
            // Thứ 3
            Map<String, Object> schedule2 = new HashMap<>();
            schedule2.put("id", 2);
            schedule2.put("day", 2); // Tuesday
            schedule2.put("className", "Lớp Database Design");
            schedule2.put("subject", "Thiết kế CSDL");
            schedule2.put("start", "14:00");
            schedule2.put("end", "16:00");
            schedule2.put("teacherName", currentUser.getFullName());
            schedule2.put("room", "P.205");
            schedule2.put("studentCount", 30);
            schedule2.put("materialsUrl", null);
            schedule2.put("meetUrl", "#");
            schedule.add(schedule2);
            
            // Thứ 4
            Map<String, Object> schedule3 = new HashMap<>();
            schedule3.put("id", 3);
            schedule3.put("day", 3); // Wednesday
            schedule3.put("className", "Lớp Web Development");
            schedule3.put("subject", "Phát triển Web");
            schedule3.put("start", "10:00");
            schedule3.put("end", "12:00");
            schedule3.put("teacherName", currentUser.getFullName());
            schedule3.put("room", "Lab.A1");
            schedule3.put("studentCount", 20);
            schedule3.put("materialsUrl", "#");
            schedule3.put("meetUrl", "#");
            schedule.add(schedule3);
            
            return ResponseEntity.ok(schedule);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Get teacher's courses
     * Frontend calls: /teacher/courses
     */
    @GetMapping("/courses")
    public ResponseEntity<List<ClassroomDto>> getTeacherCourses(Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            // Use existing classroom service
            List<ClassroomDto> courses = classroomService.GetClassroomsByTeacher(currentUser.getId());
            return ResponseEntity.ok(courses);
            
        } catch (Exception e) {
            // Return empty list if error
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * Get teacher dashboard stats
     * Frontend calls: /teacher/dashboard-stats
     */
    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getTeacherDashboardStats(Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            // Get teacher's classrooms
            List<ClassroomDto> classrooms = classroomService.GetClassroomsByTeacher(currentUser.getId());
            
            // Calculate stats
            Map<String, Object> stats = new HashMap<>();
            
            Map<String, Object> classStats = new HashMap<>();
            classStats.put("totalClasses", classrooms.size());
            classStats.put("activeClasses", classrooms.size()); // Assume all are active
            classStats.put("totalStudents", classrooms.size() * 25); // Mock student count
            
            Map<String, Object> assignmentStats = new HashMap<>();
            assignmentStats.put("totalAssignments", 15);
            assignmentStats.put("pendingGrading", 5);
            assignmentStats.put("graded", 10);
            
            Map<String, Object> attendanceStats = new HashMap<>();
            attendanceStats.put("totalSessions", 45);
            attendanceStats.put("averageAttendance", 85.5);
            
            stats.put("classStats", classStats);
            stats.put("assignmentStats", assignmentStats);
            stats.put("attendanceStats", attendanceStats);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
