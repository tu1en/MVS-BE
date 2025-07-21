package com.classroomapp.classroombackend.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceRecordDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceResultDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSubmitDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.MyAttendanceHistoryDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.TeachingHistoryDto;
import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AttendanceLogService;
import com.classroomapp.classroombackend.service.AttendanceService;

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
    private final UserRepository userRepository; // Needed to fetch user from security context
    @Autowired
    private AttendanceLogService attendanceLogService;

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

    /**
     * Bridge endpoint for frontend compatibility - maps to my-attendance-history
     * Frontend calls: /api/attendance/my-history
     */
    @GetMapping("/my-history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MyAttendanceHistoryDto>> getMyHistory(@RequestParam Long classroomId) {
        return getMyAttendanceHistory(classroomId);
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
        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found from security context"));
        
        List<TeachingHistoryDto> history = attendanceService.getTeachingHistory(currentUser.getId());
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
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<AttendanceLog>> getTeacherAttendanceStatus(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String shift) {
        List<AttendanceLog> logs = attendanceLogService.getTeacherAttendanceStatus(date, shift);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/daily-shift")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<AttendanceLog>> getDailyAttendanceByShift(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String shift) {
        List<AttendanceLog> logs = attendanceLogService.getDailyAttendanceByShift(date, shift);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/all-logs")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<AttendanceLog>> getAllStaffAttendanceLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AttendanceLog> logs = attendanceLogService.getAllStaffAttendanceLogs(date);
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

    @GetMapping("/my-history-range")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttendanceLog>> getPersonalAttendanceHistory(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        List<AttendanceLog> history = attendanceLogService.getPersonalAttendanceHistory(userId, startDate, endDate);
        return ResponseEntity.ok(history);
    }
}
