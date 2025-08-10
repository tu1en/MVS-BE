package com.classroomapp.classroombackend.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.StaffAttendanceLogDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceRecordDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceResultDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSubmitDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.CreateAttendanceSessionDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.MyAttendanceHistoryDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.TeachingHistoryDto;
import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AttendanceLogService;
import com.classroomapp.classroombackend.service.AttendanceService;
import com.classroomapp.classroombackend.service.StaffAttendanceService;

import lombok.RequiredArgsConstructor;

/**
 * REST Controller for handling attendance related APIs.
 * This controller provides secure endpoints for managing and viewing attendance records.
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository; // Needed to fetch user from security context
    @Autowired
    private AttendanceLogService attendanceLogService;
    
    @Autowired
    private StaffAttendanceService staffAttendanceService;

    // This controller is now mostly deprecated in favor of AttendanceSessionController.
    // The getAttendanceResult endpoint is kept here as it's a general query
    // not strictly tied to a single "session" action.

    @GetMapping("/classroom/{classroomId}/student/{studentId}")
    @PreAuthorize("hasAnyRole('TEACHER', 'STUDENT')")
    public ResponseEntity<AttendanceResultDto> getAttendanceResult(
            @PathVariable Long classroomId,
            @PathVariable Long studentId) {
        AttendanceResultDto result = attendanceService.getAttendanceResult(classroomId, studentId);
        return ResponseEntity.ok(result);
    }
    
    /**
     * Gets the attendance status for all enrolled students for a specific lecture.
     * Accessible only by users with the 'TEACHER' role.
     */
    @GetMapping("/lecture/{lectureId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<AttendanceRecordDto>> getAttendanceForLecture(
            @PathVariable Long lectureId,
            @RequestParam Long classroomId) {
        List<AttendanceRecordDto> records = attendanceService.getAttendanceForLecture(lectureId, classroomId);
        return ResponseEntity.ok(records);
    }

    /**
     * Gets the personal attendance history for the currently authenticated student in a specific classroom.
     * Accessible by any authenticated user for their own record.
     */
    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MyAttendanceHistoryDto>> getMyHistory(@RequestParam Long classroomId) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            System.out.println("User from security context: " + userDetails.getUsername());
            
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found from security context: " + userDetails.getUsername()));
            
            System.out.println("Found user: " + currentUser.getId() + " - " + currentUser.getEmail());
            System.out.println("Requesting attendance history for classroom: " + classroomId);

            List<MyAttendanceHistoryDto> history = attendanceService.getMyAttendanceHistory(currentUser.getId(), classroomId);
            System.out.println("Found " + history.size() + " attendance records");
            
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            System.err.println("Error in getMyHistory: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

    /**
     * Gets the personal attendance history for the currently authenticated student in a specific classroom.
     * Accessible by any authenticated user for their own record.
     */
    @GetMapping("/my-attendance-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MyAttendanceHistoryDto>> getMyAttendanceHistory(@RequestParam Long classroomId) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            System.out.println("User from security context: " + userDetails.getUsername());
            
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found from security context: " + userDetails.getUsername()));
            
            System.out.println("Found user: " + currentUser.getId() + " - " + currentUser.getEmail());
            System.out.println("Requesting attendance history for classroom: " + classroomId);

            List<MyAttendanceHistoryDto> history = attendanceService.getMyAttendanceHistory(currentUser.getId(), classroomId);
            System.out.println("Found " + history.size() + " attendance records");
            
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            System.err.println("Error in getMyAttendanceHistory: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new ArrayList<>());
        }
    }

// Thay thế method getPersonalAttendanceHistory hiện có bằng:

@GetMapping("/my-history-range")
@PreAuthorize("isAuthenticated()")
public ResponseEntity<Map<String, Object>> getPersonalAttendanceHistory(
        @RequestParam Long userId,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
    try {
        List<AttendanceLog> history = attendanceLogService.getPersonalAttendanceHistory(userId, startDate, endDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", history);
        response.put("totalElements", history.size());
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        System.err.println("Error in getPersonalAttendanceHistory: " + e.getMessage());
        e.printStackTrace();
        Map<String, Object> response = new HashMap<>();
        response.put("data", new ArrayList<>());
        return ResponseEntity.ok(response);
    }
}
    /**
     * Gets the attendance history for a specific student in a specific classroom.
     * Accessible only by users with the 'TEACHER' role for viewing any student's record.
     */
    @GetMapping("/history/student/{studentId}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<MyAttendanceHistoryDto>> getStudentAttendanceHistoryForTeacher(
            @PathVariable Long studentId,
            @RequestParam Long classroomId) {
        List<MyAttendanceHistoryDto> history = attendanceService.getMyAttendanceHistory(studentId, classroomId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Gets the teaching history for the currently authenticated teacher.
     * This endpoint shows all lectures where the teacher was automatically clocked-in.
     * Accessible only by users with the 'TEACHER' role.
     */
    @GetMapping("/teaching-history")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<List<TeachingHistoryDto>> getMyTeachingHistory() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("Teaching history request from user: " + userDetails.getUsername());
        
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found from security context"));
        
        System.out.println("Current user ID: " + currentUser.getId() + ", Role: " + currentUser.getRole());
        
        List<TeachingHistoryDto> history = attendanceService.getTeachingHistory(currentUser.getId());
        System.out.println("Found " + history.size() + " teaching history records for user " + currentUser.getId());
        
        return ResponseEntity.ok(history);
    }
    
    /**
     * Gets the teaching history for a specific teacher.
     * Accessible only by users with the 'MANAGER' or 'ADMIN' role.
     */
    @GetMapping("/teaching-history/{teacherId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<TeachingHistoryDto>> getTeacherTeachingHistory(@PathVariable Long teacherId) {
        List<TeachingHistoryDto> history = attendanceService.getTeachingHistory(teacherId);
        return ResponseEntity.ok(history);
    }

    /**
     * Submits attendance records for a lecture.
     * Accessible only by users with the 'TEACHER' role.
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<String> submitAttendance(@RequestBody AttendanceSubmitDto submitDto) {
        attendanceService.submitAttendance(submitDto);
        return ResponseEntity.ok("Attendance records submitted successfully");
    }

    @GetMapping("/teacher-status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<List<AttendanceLog>> getTeacherAttendanceStatus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String shift) {
        List<AttendanceLog> logs = attendanceLogService.getTeacherAttendanceStatus(date, shift);
        return ResponseEntity.ok(logs);
    }

@GetMapping("/daily-shift")
@PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
public ResponseEntity<Map<String, Object>> getDailyAttendanceByShift(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam String shift,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    try {
        List<AttendanceLog> allLogs = attendanceLogService.getDailyAttendanceByShift(date, shift);
        
        // Pagination logic
        int totalElements = allLogs.size();
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        List<AttendanceLog> paginatedLogs = new ArrayList<>();
        if (startIndex < totalElements) {
            paginatedLogs = allLogs.subList(startIndex, endIndex);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("data", paginatedLogs);
        response.put("totalElements", totalElements);
        response.put("totalPages", (int) Math.ceil((double) totalElements / size));
        response.put("currentPage", page);
        response.put("pageSize", size);
        
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        System.err.println("Error in getDailyAttendanceByShift: " + e.getMessage());
        e.printStackTrace();
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("data", new ArrayList<>());
        errorResponse.put("totalElements", 0);
        errorResponse.put("error", e.getMessage());
        
        return ResponseEntity.ok(errorResponse);
    }
}

    @GetMapping("/all-logs")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<List<StaffAttendanceLogDto>> getAllStaffAttendanceLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<StaffAttendanceLogDto> logs = staffAttendanceService.getAllStaffAttendanceLogsByDate(date);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/my-attendance-summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttendanceLog>> getMyAttendanceSummary(@RequestParam Long userId) {
        List<AttendanceLog> history = attendanceLogService.getPersonalAttendanceHistory(userId, null, null);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/my-attendance-history-old")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<AttendanceLog>> getMyAttendanceHistoryOld(@RequestParam Long userId) {
        List<AttendanceLog> history = attendanceLogService.getPersonalAttendanceHistory(userId, null, null);
        return ResponseEntity.ok(history);
    }



    /**
     * Gets all attendance sessions for the current teacher
     */
    @GetMapping("/sessions/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getTeacherSessions() {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found from security context"));
            
            // Get all sessions for classrooms where this user is the teacher
            List<AttendanceSession> sessions = attendanceSessionRepository.findByClassroom_TeacherId(currentUser.getId());
            
            List<Map<String, Object>> sessionData = sessions.stream().map(session -> {
                Map<String, Object> sessionMap = new HashMap<>();
                sessionMap.put("id", session.getId());
                sessionMap.put("name", session.getLecture() != null ? session.getLecture().getTitle() : "Attendance Session");
                sessionMap.put("classroomId", session.getClassroom().getId());
                sessionMap.put("classroomName", session.getClassroom().getName());
                sessionMap.put("startTime", session.getCreatedAt());
                sessionMap.put("endTime", session.getExpiresAt());
                sessionMap.put("status", session.getIsOpen() ? "ACTIVE" : "ENDED");
                sessionMap.put("requireLocation", false); // Default value
                sessionMap.put("maxDistance", null);
                return sessionMap;
            }).collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", sessionData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in getTeacherSessions: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("data", new ArrayList<>());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Gets all attendance records for the current teacher
     */
    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> getTeacherAttendance() {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found from security context"));
            
            // Get all attendance records from sessions in classrooms where this user is the teacher
            List<Attendance> attendanceRecords = attendanceRepository.findBySession_Classroom_TeacherId(currentUser.getId());
            
            List<Map<String, Object>> attendanceData = attendanceRecords.stream().map(attendance -> {
                Map<String, Object> attendanceMap = new HashMap<>();
                attendanceMap.put("id", attendance.getId());
                attendanceMap.put("sessionId", attendance.getSession().getId());
                attendanceMap.put("studentId", attendance.getStudent().getId());
                attendanceMap.put("studentName", attendance.getStudent().getFullName());
                attendanceMap.put("studentCode", attendance.getStudent().getEmail());
                attendanceMap.put("status", attendance.getStatus().name());
                attendanceMap.put("checkedAt", attendance.getSession().getCreatedAt()); // Approximate check time
                return attendanceMap;
            }).collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("data", attendanceData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in getTeacherAttendance: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("data", new ArrayList<>());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Creates a new attendance session
     */
    @PostMapping("/sessions")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> createAttendanceSession(@RequestBody Map<String, Object> sessionData) {
        try {
            UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found from security context"));
            
            // Extract data from the request
            Long classroomId = Long.valueOf(sessionData.get("classroomId").toString());
            String name = (String) sessionData.get("name");
            String startTimeStr = (String) sessionData.get("startTime");
            String endTimeStr = (String) sessionData.get("endTime");
            
            // Parse times
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr.replace(" ", "T"));
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr.replace(" ", "T"));
            
            // Create DTO
            CreateAttendanceSessionDto createDto = new CreateAttendanceSessionDto();
            createDto.setClassroomId(classroomId);
            createDto.setEndTime(endTime.atZone(ZoneId.systemDefault()).toInstant());
            
            // Create the session
            AttendanceSession session = attendanceService.createSession(createDto);
            
            // Create response
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> sessionResponse = new HashMap<>();
            sessionResponse.put("id", session.getId());
            sessionResponse.put("name", name);
            sessionResponse.put("classroomId", session.getClassroom().getId());
            sessionResponse.put("classroomName", session.getClassroom().getName());
            sessionResponse.put("startTime", session.getCreatedAt());
            sessionResponse.put("endTime", session.getExpiresAt());
            sessionResponse.put("status", session.getIsOpen() ? "ACTIVE" : "ENDED");
            
            response.put("data", sessionResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in createAttendanceSession: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to create attendance session"));
        }
    }

    /**
     * Updates attendance session status
     */
    @PutMapping("/sessions/{sessionId}/status")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> updateSessionStatus(@PathVariable Long sessionId, @RequestBody Map<String, String> statusData) {
        try {
            String newStatus = statusData.get("status");
            
            if ("ENDED".equals(newStatus)) {
                attendanceService.closeSession(sessionId);
            } else if ("ACTIVE".equals(newStatus)) {
                // Reopen session logic if needed
                AttendanceSession session = attendanceSessionRepository.findById(sessionId)
                        .orElseThrow(() -> new RuntimeException("Session not found"));
                session.setIsOpen(true);
                session.setExpiresAt(LocalDateTime.now().plusHours(1));
                attendanceSessionRepository.save(session);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in updateSessionStatus: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Failed to update session status"));
        }
    }
}
