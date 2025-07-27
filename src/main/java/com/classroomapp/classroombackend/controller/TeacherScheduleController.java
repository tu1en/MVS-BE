package com.classroomapp.classroombackend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.ScheduleDto;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.security.CustomUserDetails;
import com.classroomapp.classroombackend.security.CustomUserDetailsService;
import com.classroomapp.classroombackend.service.impl.TeacherScheduleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Slf4j
public class TeacherScheduleController {
    
    private final TeacherScheduleService teacherScheduleService;
    private final CustomUserDetailsService userDetailsService;
    
    /**
     * Get teacher schedules in a date range
     */
    @GetMapping("/schedules")
    public ResponseEntity<List<ScheduleDto>> getTeacherSchedules(
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        
        try {
            LocalDate startLocalDate;
            LocalDate endLocalDate;
            
            try {
                startLocalDate = LocalDate.parse(startDate);
                endLocalDate = LocalDate.parse(endDate);
            } catch (DateTimeParseException e) {
                log.error("❌ Invalid date format: {} or {}", startDate, endDate);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(List.of());
            }
            
            log.info("📅 Schedule request: {} to {} from user {}", 
                startDate, endDate, authentication != null ? authentication.getName() : "anonymous");
            
            // Extract and validate teacher ID
            Long teacherId = extractTeacherId(authentication);
            if (teacherId == null) {
                log.error("❌ Failed to extract teacher ID from authentication");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(List.of());
            }
            
            log.info("✅ Authenticated teacher ID: {}", teacherId);
            
            // Get schedules using service
            LocalDateTime startDateTime = startLocalDate.atStartOfDay();
            LocalDateTime endDateTime = endLocalDate.plusDays(1).atStartOfDay();
            
            List<Schedule> schedules = teacherScheduleService.getTeacherSchedulesForDto(
                teacherId, startDateTime, endDateTime);
            
            List<ScheduleDto> scheduleDtos = schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            log.info("📊 Retrieved {} schedules for teacher {}", scheduleDtos.size(), teacherId);
            
            return ResponseEntity.ok(scheduleDtos);
            
        } catch (Exception e) {
            log.error("❌ Error in getTeacherSchedules: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(List.of());
        }
    }

    private ScheduleDto convertToDto(Schedule schedule) {
        ScheduleDto dto = new ScheduleDto();
        dto.setId(schedule.getId());
        dto.setTitle(schedule.getTitle());
        dto.setDescription(schedule.getDescription());
        dto.setStartDatetime(schedule.getStartDatetime());
        dto.setEndDatetime(schedule.getEndDatetime());
        dto.setLocation(schedule.getLocation());
        dto.setColor(schedule.getColor());
        dto.setClassroomId(schedule.getClassroom() != null ? schedule.getClassroom().getId() : null);
        dto.setClassroomName(schedule.getClassroom() != null ? schedule.getClassroom().getName() : null);
        return dto;
    }
    
    /**
     * Get today's schedules for teacher
     */
    @GetMapping("/schedules/today")
    public ResponseEntity<List<Schedule>> getTodaySchedules(Authentication authentication) {
        try {
            log.info("📅 Today schedules request from {}", 
                authentication != null ? authentication.getName() : "anonymous");
            
            Long teacherId = extractTeacherId(authentication);
            if (teacherId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<Schedule> schedules = teacherScheduleService.getTodaySchedules(teacherId);
            log.info("📊 Found {} schedules for today", schedules.size());
            
            return ResponseEntity.ok(schedules);
            
        } catch (Exception e) {
            log.error("❌ Error getting today's schedules: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get upcoming schedules for teacher
     */
    @GetMapping("/schedules/upcoming")
    public ResponseEntity<List<Schedule>> getUpcomingSchedules(Authentication authentication) {
        try {
            log.info("📅 Upcoming schedules request from {}", 
                authentication != null ? authentication.getName() : "anonymous");
            
            Long teacherId = extractTeacherId(authentication);
            if (teacherId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<Schedule> schedules = teacherScheduleService.getUpcomingSchedules(teacherId);
            log.info("📊 Found {} upcoming schedules", schedules.size());
            
            return ResponseEntity.ok(schedules);
            
        } catch (Exception e) {
            log.error("❌ Error getting upcoming schedules: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DEBUG ENDPOINT: Get all schedules for teacher (no date filter)
     */
    @GetMapping("/schedules/all")
    public ResponseEntity<List<Schedule>> getAllTeacherSchedules(Authentication authentication) {
        try {
            log.info("🔍 DEBUG: All schedules request from {}", 
                authentication != null ? authentication.getName() : "anonymous");
            
            Long teacherId = extractTeacherId(authentication);
            if (teacherId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            List<Schedule> schedules = teacherScheduleService.getAllTeacherSchedules(teacherId);
            log.info("📊 Found {} total schedules", schedules.size());
            
            return ResponseEntity.ok(schedules);
            
        } catch (Exception e) {
            log.error("❌ Error getting all schedules: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DEBUG ENDPOINT: Test teacher authentication and database connection
     */
    @GetMapping("/debug/{teacherId}")
    public ResponseEntity<Map<String, Object>> debugTeacher(
            @PathVariable Long teacherId,
            Authentication authentication) {
        
        try {
            log.info("🧪 Debug request for teacher ID: {}", teacherId);
            
            Map<String, Object> debugInfo = new HashMap<>();
            
            // Check authentication
            if (authentication != null) {
                debugInfo.put("authenticated", true);
                debugInfo.put("username", authentication.getName());
                debugInfo.put("authorities", authentication.getAuthorities());
            } else {
                debugInfo.put("authenticated", false);
            }
            
            // Test database queries
            try {
                List<Schedule> allSchedules = teacherScheduleService.getAllTeacherSchedules(teacherId);
                debugInfo.put("totalSchedules", allSchedules.size());
                
                if (!allSchedules.isEmpty()) {
                    Schedule first = allSchedules.get(0);
                    Map<String, Object> sampleSchedule = new HashMap<>();
                    sampleSchedule.put("id", first.getId());
                    sampleSchedule.put("title", first.getTitle());
                    sampleSchedule.put("startDatetime", first.getStartDatetime());
                    sampleSchedule.put("teacherId", first.getTeacherId());
                    sampleSchedule.put("classroomId", first.getClassroomId());
                    debugInfo.put("sampleSchedule", sampleSchedule);
                }
                
                // Test today's schedules
                List<Schedule> todaySchedules = teacherScheduleService.getTodaySchedules(teacherId);
                debugInfo.put("todaySchedules", todaySchedules.size());
                
                // Test upcoming schedules
                List<Schedule> upcomingSchedules = teacherScheduleService.getUpcomingSchedules(teacherId);
                debugInfo.put("upcomingSchedules", upcomingSchedules.size());
                
            } catch (Exception e) {
                debugInfo.put("databaseError", e.getMessage());
            }
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            log.error("❌ Debug endpoint error: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Extract teacher ID from authentication with detailed logging
     */
    private Long extractTeacherId(Authentication authentication) {
        try {
            if (authentication == null) {
                log.error("❌ No authentication provided");
                return null;
            }
            
            String userEmail = authentication.getName();
            log.info("🔐 Processing authentication for user: {}", userEmail);
            
            var userDetails = userDetailsService.loadUserByUsername(userEmail);
            if (!(userDetails instanceof CustomUserDetails)) {
                log.error("❌ Invalid user details type: {}", 
                    userDetails != null ? userDetails.getClass().getSimpleName() : "null");
                return null;
            }
            
            CustomUserDetails userPrincipal = (CustomUserDetails) userDetails;
            log.info("✅ User details loaded: ID={}, Authorities={}", 
                userPrincipal.getId(), userPrincipal.getAuthorities());
            
            // Check if user has TEACHER role
            boolean isTeacher = userPrincipal.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TEACHER"));
                
            if (!isTeacher) {
                log.error("❌ User {} is not a teacher. Roles: {}", 
                    userEmail, userPrincipal.getAuthorities());
                return null;
            }
            
            Long teacherId = userPrincipal.getId();
            log.info("✅ Teacher ID extracted: {}", teacherId);
            
            return teacherId;
            
        } catch (Exception e) {
            log.error("❌ Error extracting teacher ID: {}", e.getMessage(), e);
            return null;
        }
    }
}