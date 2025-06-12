package com.classroomapp.classroombackend.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.AttendanceDto;
import com.classroomapp.classroombackend.dto.AttendanceSessionDto;
import com.classroomapp.classroombackend.model.Attendance.AttendanceStatus;
import com.classroomapp.classroombackend.service.AttendanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {

    private final AttendanceService attendanceService;

    // Attendance Session Management
    @PostMapping("/sessions")
    public ResponseEntity<AttendanceSessionDto> createAttendanceSession(
            @RequestParam Long classroomId,
            @RequestParam Long teacherId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "100.0") Double locationRadius) {
        
        log.info("Creating attendance session for classroom {} by teacher {}", classroomId, teacherId);
        
        AttendanceSessionDto session = attendanceService.createAttendanceSession(
                classroomId, teacherId, title, description, startTime, endTime, 
                latitude, longitude, locationRadius);
        
        return new ResponseEntity<>(session, HttpStatus.CREATED);
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<AttendanceSessionDto> getAttendanceSession(@PathVariable Long sessionId) {
        AttendanceSessionDto session = attendanceService.getAttendanceSessionById(sessionId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions/classroom/{classroomId}")
    public ResponseEntity<List<AttendanceSessionDto>> getAttendanceSessionsByClassroom(@PathVariable Long classroomId) {
        List<AttendanceSessionDto> sessions = attendanceService.getAttendanceSessionsByClassroom(classroomId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/teacher/{teacherId}")
    public ResponseEntity<List<AttendanceSessionDto>> getAttendanceSessionsByTeacher(@PathVariable Long teacherId) {
        List<AttendanceSessionDto> sessions = attendanceService.getAttendanceSessionsByTeacher(teacherId);
        return ResponseEntity.ok(sessions);
    }

    @PutMapping("/sessions/{sessionId}/start")
    public ResponseEntity<AttendanceSessionDto> startAttendanceSession(@PathVariable Long sessionId) {
        log.info("Starting attendance session: {}", sessionId);
        AttendanceSessionDto session = attendanceService.startAttendanceSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @PutMapping("/sessions/{sessionId}/end")
    public ResponseEntity<AttendanceSessionDto> endAttendanceSession(@PathVariable Long sessionId) {
        log.info("Ending attendance session: {}", sessionId);
        AttendanceSessionDto session = attendanceService.endAttendanceSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteAttendanceSession(@PathVariable Long sessionId) {
        log.info("Deleting attendance session: {}", sessionId);
        attendanceService.deleteAttendanceSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    // Attendance Marking
    @PostMapping("/mark")
    public ResponseEntity<AttendanceDto> markAttendance(
            @RequestParam Long sessionId,
            @RequestParam Long userId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        
        log.info("Marking attendance for user {} in session {}", userId, sessionId);
        
        AttendanceDto attendance = attendanceService.markAttendance(sessionId, userId, latitude, longitude);
        return new ResponseEntity<>(attendance, HttpStatus.CREATED);
    }

    @PutMapping("/{attendanceId}/status")
    public ResponseEntity<AttendanceDto> updateAttendanceStatus(
            @PathVariable Long attendanceId,
            @RequestParam AttendanceStatus status) {
        
        log.info("Updating attendance {} to status {}", attendanceId, status);
        
        AttendanceDto attendance = attendanceService.updateAttendanceStatus(attendanceId, status);
        return ResponseEntity.ok(attendance);
    }

    // Attendance Retrieval
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<AttendanceDto>> getAttendanceBySession(@PathVariable Long sessionId) {
        List<AttendanceDto> attendanceList = attendanceService.getAttendanceBySession(sessionId);
        return ResponseEntity.ok(attendanceList);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByUser(@PathVariable Long userId) {
        List<AttendanceDto> attendanceList = attendanceService.getAttendanceByUser(userId);
        return ResponseEntity.ok(attendanceList);
    }

    @GetMapping("/user/{userId}/daterange")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByUserAndDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<AttendanceDto> attendanceList = attendanceService.getAttendanceByUserAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(attendanceList);
    }

    @GetMapping("/classroom/{classroomId}/daterange")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByClassroomAndDateRange(
            @PathVariable Long classroomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<AttendanceDto> attendanceList = attendanceService.getAttendanceByClassroomAndDateRange(classroomId, startDate, endDate);
        return ResponseEntity.ok(attendanceList);
    }

    // Attendance Reports and Analytics
    @GetMapping("/session/{sessionId}/summary")
    public ResponseEntity<Map<AttendanceStatus, Long>> getAttendanceSummaryBySession(@PathVariable Long sessionId) {
        Map<AttendanceStatus, Long> summary = attendanceService.getAttendanceSummaryBySession(sessionId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/classroom/{classroomId}/percentage")
    public ResponseEntity<Map<Long, Double>> getStudentAttendancePercentageByClassroom(@PathVariable Long classroomId) {
        Map<Long, Double> percentages = attendanceService.getStudentAttendancePercentageByClassroom(classroomId);
        return ResponseEntity.ok(percentages);
    }

    @GetMapping("/session/{sessionId}/absent")
    public ResponseEntity<List<AttendanceDto>> getAbsentStudents(@PathVariable Long sessionId) {
        List<AttendanceDto> absentStudents = attendanceService.getAbsentStudents(sessionId);
        return ResponseEntity.ok(absentStudents);
    }

    @GetMapping("/session/{sessionId}/late")
    public ResponseEntity<List<AttendanceDto>> getLateStudents(@PathVariable Long sessionId) {
        List<AttendanceDto> lateStudents = attendanceService.getLateStudents(sessionId);
        return ResponseEntity.ok(lateStudents);
    }

    // Validation Endpoints
    @GetMapping("/validate/user/{userId}/classroom/{classroomId}")
    public ResponseEntity<Boolean> isUserInClassroom(@PathVariable Long userId, @PathVariable Long classroomId) {
        boolean isInClassroom = attendanceService.isUserInClassroom(userId, classroomId);
        return ResponseEntity.ok(isInClassroom);
    }

    @GetMapping("/validate/location")
    public ResponseEntity<Boolean> isWithinLocationRadius(
            @RequestParam Double userLat,
            @RequestParam Double userLon,
            @RequestParam Double sessionLat,
            @RequestParam Double sessionLon,
            @RequestParam Double radius) {
        
        boolean isWithinRadius = attendanceService.isWithinLocationRadius(userLat, userLon, sessionLat, sessionLon, radius);
        return ResponseEntity.ok(isWithinRadius);
    }

    @GetMapping("/validate/mark/{sessionId}/user/{userId}")
    public ResponseEntity<Boolean> canMarkAttendance(@PathVariable Long sessionId, @PathVariable Long userId) {
        boolean canMark = attendanceService.canMarkAttendance(sessionId, userId);
        return ResponseEntity.ok(canMark);
    }
    
    // Add a new endpoint to get attendance records for the current student
    @GetMapping("/student")
    public ResponseEntity<Map<String, Object>> getStudentAttendance() {
        log.info("Getting attendance records for current student");
        
        // In a real implementation, you would get the student ID from the security context
        // For demonstration, we'll create some sample attendance records
        List<AttendanceDto> attendanceRecords = new ArrayList<>();
        
        // Create sample data
        for (int i = 1; i <= 10; i++) {
            // Create a new instance of AttendanceDto using builder pattern instead of setters
            AttendanceDto record = AttendanceDto.builder()
                .id((long) i)
                .sessionId((long) (i % 3) + 1)
                .userId(4L) // Assuming student ID 4 is the current user
                
                // Vary the status for demonstration
                .status(i % 4 == 0 ? AttendanceStatus.ABSENT : 
                       (i % 4 == 1 ? AttendanceStatus.LATE : AttendanceStatus.PRESENT))
                
                // Set dates
                .createdAt(LocalDateTime.now().minusDays(i))
                .markedAt(LocalDateTime.now().minusDays(i))
                .build();
            
            attendanceRecords.add(record);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Retrieved student attendance records successfully");
        response.put("data", attendanceRecords);
        
        return ResponseEntity.ok(response);
    }
}
