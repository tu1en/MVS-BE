package com.classroomapp.classroombackend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.CourseRepository;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.StudentMessageRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.StudentMessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/manager")
@PreAuthorize("hasRole('MANAGER')")
@RequiredArgsConstructor
@Slf4j
public class ManagerController {
    
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final CourseRepository courseRepository;
    private final StudentMessageRepository messageRepository;
    private final ScheduleRepository scheduleRepository;
    private final StudentMessageService messageService;

    /**
     * Get dashboard statistics for manager
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication authentication) {
        try {
            log.info("Getting dashboard stats for manager: {}", authentication.getName());
            
            // Get current manager info
            String username = authentication.getName();
            Optional<User> currentUser = userRepository.findByUsername(username);
            if (currentUser.isEmpty()) {
                currentUser = userRepository.findByEmail(username);
            }
            
            if (currentUser.isEmpty()) {
                log.error("Manager user not found: {}", username);
                return ResponseEntity.notFound().build();
            }
            
            // Calculate real statistics from database
            Map<String, Object> stats = new HashMap<>();
            
            // Total users count
            long totalUsers = userRepository.count();
            stats.put("totalUsers", totalUsers);
            
            // Total courses count
            long totalCourses = courseRepository.count();
            stats.put("totalCourses", totalCourses);
            
            // Total schedules count
            long totalSchedules = scheduleRepository.count();
            stats.put("totalSchedules", totalSchedules);
            
            // Messages count (simplified - count all messages)
            long totalMessages = messageRepository.count();
            stats.put("totalMessages", totalMessages);
            
            // Additional useful statistics (using roleId instead of role names)
            long totalStudents = userRepository.countByRoleId(1); // Assuming roleId 1 = STUDENT
            stats.put("totalStudents", totalStudents);
            
            long totalTeachers = userRepository.countByRoleId(2); // Assuming roleId 2 = TEACHER
            stats.put("totalTeachers", totalTeachers);
            
            long activeClassrooms = classroomRepository.count(); // Simplified - count all classrooms
            stats.put("activeClassrooms", activeClassrooms);
            
            // Recent activity (simplified counts)
            long newUsersThisWeek = userRepository.count(); // Simplified for now
            stats.put("newUsersThisWeek", newUsersThisWeek);
            
            long newCoursesThisWeek = courseRepository.count(); // Simplified for now
            stats.put("newCoursesThisWeek", newCoursesThisWeek);
            
            log.info("Successfully calculated dashboard stats: {} users, {} courses, {} schedules", 
                    totalUsers, totalCourses, totalSchedules);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            log.error("Error getting dashboard stats for manager: {}", e.getMessage(), e);
            
            // Return basic error-safe stats
            Map<String, Object> errorStats = new HashMap<>();
            errorStats.put("totalUsers", 0);
            errorStats.put("totalCourses", 0);
            errorStats.put("totalSchedules", 0);
            errorStats.put("totalMessages", 0);
            errorStats.put("error", "Could not load complete statistics");
            
            return ResponseEntity.ok(errorStats);
        }
    }

    /**
     * Get user management statistics
     */
    @GetMapping("/users/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            log.info("Getting user management statistics");
            
            Map<String, Object> userStats = new HashMap<>();
            
            // Count users by role (simplified using roleId)
            long studentCount = userRepository.countByRoleId(1);    // STUDENT
            long teacherCount = userRepository.countByRoleId(2);    // TEACHER  
            long managerCount = userRepository.countByRoleId(3);    // MANAGER
            long adminCount = userRepository.countByRoleId(4);      // ADMIN
            long accountantCount = userRepository.countByRoleId(5); // ACCOUNTANT
            
            userStats.put("studentCount", studentCount);
            userStats.put("teacherCount", teacherCount);
            userStats.put("managerCount", managerCount);
            userStats.put("adminCount", adminCount);
            userStats.put("accountantCount", accountantCount);
            
            // Active vs inactive users (simplified)
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.findActiveUsers().size();
            long inactiveUsers = totalUsers - activeUsers;
            
            userStats.put("activeUsers", activeUsers);
            userStats.put("inactiveUsers", inactiveUsers);
            userStats.put("totalUsers", totalUsers);
            
            return ResponseEntity.ok(userStats);
            
        } catch (Exception e) {
            log.error("Error getting user statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get course management statistics
     */
    @GetMapping("/courses/stats")
    public ResponseEntity<Map<String, Object>> getCourseStats() {
        try {
            log.info("Getting course management statistics");
            
            Map<String, Object> courseStats = new HashMap<>();
            
            // Course counts (simplified)
            long totalCourses = courseRepository.count();
            long activeCourses = totalCourses; // Simplified - assume all are active
            long inactiveCourses = 0;
            long completedCourses = 0;
            
            courseStats.put("activeCourses", activeCourses);
            courseStats.put("inactiveCourses", inactiveCourses);
            courseStats.put("completedCourses", completedCourses);
            courseStats.put("totalCourses", totalCourses);
            
            // Classroom statistics
            long totalClassrooms = classroomRepository.count();
            long activeClassrooms = totalClassrooms; // Simplified
            
            courseStats.put("totalClassrooms", totalClassrooms);
            courseStats.put("activeClassrooms", activeClassrooms);
            
            return ResponseEntity.ok(courseStats);
            
        } catch (Exception e) {
            log.error("Error getting course statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get system health overview
     */
    @GetMapping("/system/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        try {
            log.info("Getting system health overview");
            
            Map<String, Object> healthStats = new HashMap<>();
            
            // Database connectivity check
            long userCount = userRepository.count();
            healthStats.put("databaseConnected", userCount >= 0);
            
            // Recent activity indicators (simplified)
            long recentActivity = messageRepository.count(); // Simplified - total message count
            
            healthStats.put("recentActivity", recentActivity);
            healthStats.put("systemUptime", "System running");
            healthStats.put("lastUpdated", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(healthStats);
            
        } catch (Exception e) {
            log.error("Error getting system health: {}", e.getMessage(), e);
            
            Map<String, Object> errorHealth = new HashMap<>();
            errorHealth.put("databaseConnected", false);
            errorHealth.put("systemStatus", "ERROR");
            errorHealth.put("lastError", e.getMessage());
            
            return ResponseEntity.ok(errorHealth);
        }
    }
}