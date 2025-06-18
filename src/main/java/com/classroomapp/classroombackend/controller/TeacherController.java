package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ClassroomService;
import com.classroomapp.classroombackend.service.ScheduleService;
import com.classroomapp.classroombackend.service.impl.ScheduleServiceImpl;

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
    
    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private ScheduleServiceImpl scheduleServiceImpl; // Using implementation for sample data generation

    /**
     * Get teacher's schedule
     * Frontend calls: /teacher/schedule
     */
    @GetMapping("/schedule")
    public ResponseEntity<?> getTeacherSchedule(Authentication authentication) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            System.out.println("Teacher schedule requested by: " + username + ", id: " + currentUser.getId());
            
            // Get schedules from database
            List<ScheduleDto> schedules = scheduleService.getSchedulesByTeacher(currentUser.getId());
            
            // If no schedules found, add sample data
            if (schedules.isEmpty()) {
                System.out.println("No schedules found, adding sample data");
                schedules = scheduleServiceImpl.addSampleDataForTeacher(currentUser.getId());
            }
            
            System.out.println("Returning " + schedules.size() + " schedules");
            return ResponseEntity.ok(schedules);
            
        } catch (Exception e) {
            System.err.println("Error in getTeacherSchedule: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Get teacher's schedule by day
     * Frontend calls: /teacher/schedule/day/{dayOfWeek}
     */
    @GetMapping("/schedule/day/{dayOfWeek}")
    public ResponseEntity<?> getTeacherScheduleByDay(
            Authentication authentication,
            @PathVariable Integer dayOfWeek) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            // Validate day of week
            if (dayOfWeek < 0 || dayOfWeek > 6) {
                return ResponseEntity.badRequest().body("Day of week must be between 0 and 6");
            }
            
            // Get schedules for teacher and day
            List<ScheduleDto> schedules = scheduleService.getSchedulesByTeacherAndDay(
                    currentUser.getId(), dayOfWeek);
            
            return ResponseEntity.ok(schedules);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    
    /**
     * Create a new schedule entry
     * Frontend calls: POST /teacher/schedule
     */
    @PostMapping("/schedule")
    public ResponseEntity<?> createSchedule(
            Authentication authentication,
            @RequestBody ScheduleDto scheduleDto) {
        try {
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            // Ensure the teacher ID is set to the current user
            scheduleDto.setTeacherId(currentUser.getId());
            
            // Create schedule
            ScheduleDto createdSchedule = scheduleService.createScheduleEntry(scheduleDto);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
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
