package com.classroomapp.classroombackend.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.model.ParentLeaveNotice;
import com.classroomapp.classroombackend.security.JwtUtil;
import com.classroomapp.classroombackend.service.ParentLeaveNoticeService;
import com.classroomapp.classroombackend.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Teacher-Parent Controller - API endpoints for teachers to manage parent leave notices
 * Based on PARENT_ROLE_SPEC.md requirements
 */
@RestController
@RequestMapping("/api/teacher")
@Slf4j
@PreAuthorize("hasRole('TEACHER') or hasRole('TEACHING_ASSISTANT')")
public class TeacherParentController {

    private final ParentLeaveNoticeService leaveNoticeService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public TeacherParentController(ParentLeaveNoticeService leaveNoticeService,
                                  UserService userService,
                                  JwtUtil jwtUtil) {
        this.leaveNoticeService = leaveNoticeService;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Get leave notices for teacher review
     * Query by date and optionally classId
     */
    @GetMapping("/leave-notices")
    public ResponseEntity<List<ParentLeaveNotice>> getLeaveNotices(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long classId,
            HttpServletRequest request) {
        try {
            Long teacherId = getTeacherIdFromToken(request);
            log.info("Teacher {} requesting leave notices for date: {}, classId: {}", teacherId, date, classId);

            List<ParentLeaveNotice> notices;
            
            if (date != null) {
                notices = leaveNoticeService.getPendingNoticesByDate(date);
            } else {
                notices = leaveNoticeService.getPendingNotices();
            }

            // TODO: Filter by teacher's classes/students
            // This would require additional service methods to get teacher's students
            
            return ResponseEntity.ok(notices);
        } catch (Exception e) {
            log.error("Error getting leave notices for teacher", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Acknowledge a leave notice
     */
    @PostMapping("/leave-notices/{noticeId}/ack")
    public ResponseEntity<Map<String, Object>> acknowledgeLeaveNotice(
            @PathVariable Long noticeId,
            HttpServletRequest request) {
        try {
            Long teacherId = getTeacherIdFromToken(request);
            log.info("Teacher {} acknowledging leave notice {}", teacherId, noticeId);

            ParentLeaveNotice acknowledgedNotice = leaveNoticeService.acknowledgeNotice(noticeId, teacherId);

            Map<String, Object> response = new HashMap<>();
            response.put("id", acknowledgedNotice.getId());
            response.put("status", acknowledgedNotice.getStatus());
            response.put("ackAt", acknowledgedNotice.getAckAt());
            response.put("message", "Leave notice acknowledged successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid acknowledge request: {}", e.getMessage());
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Error acknowledging leave notice {}", noticeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get pending leave notices count for notification badge
     */
    @GetMapping("/leave-notices/pending-count")
    public ResponseEntity<Map<String, Object>> getPendingLeaveNoticesCount(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request) {
        try {
            Long teacherId = getTeacherIdFromToken(request);
            log.info("Teacher {} requesting pending leave notices count for date: {}", teacherId, date);

            List<ParentLeaveNotice> pendingNotices;
            if (date != null) {
                pendingNotices = leaveNoticeService.getPendingNoticesByDate(date);
            } else {
                pendingNotices = leaveNoticeService.getPendingNotices();
            }

            // TODO: Filter by teacher's students
            
            Map<String, Object> response = new HashMap<>();
            response.put("count", pendingNotices.size());
            response.put("date", date != null ? date : LocalDate.now());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting pending leave notices count for teacher", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get today's leave notices
     */
    @GetMapping("/leave-notices/today")
    public ResponseEntity<List<ParentLeaveNotice>> getTodayLeaveNotices(HttpServletRequest request) {
        try {
            Long teacherId = getTeacherIdFromToken(request);
            log.info("Teacher {} requesting today's leave notices", teacherId);

            List<ParentLeaveNotice> todayNotices = leaveNoticeService.getTodayNotices();
            
            // TODO: Filter by teacher's students
            
            return ResponseEntity.ok(todayNotices);
        } catch (Exception e) {
            log.error("Error getting today's leave notices for teacher", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get leave notices for specific student (if teacher has access)
     */
    @GetMapping("/students/{studentId}/leave-notices")
    public ResponseEntity<List<ParentLeaveNotice>> getStudentLeaveNotices(
            @PathVariable Long studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        try {
            Long teacherId = getTeacherIdFromToken(request);
            log.info("Teacher {} requesting leave notices for student {}", teacherId, studentId);

            // TODO: Validate teacher has access to this student
            
            List<ParentLeaveNotice> notices;
            if (from != null && to != null) {
                notices = leaveNoticeService.getNoticesForStudentInDateRange(studentId, from, to);
            } else {
                notices = leaveNoticeService.getNoticesByStudentId(studentId);
            }

            return ResponseEntity.ok(notices);
        } catch (Exception e) {
            log.error("Error getting leave notices for student {}", studentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Mark leave notices as reviewed/notified
     */
    @PostMapping("/leave-notices/mark-notified")
    public ResponseEntity<Map<String, Object>> markLeaveNoticesAsNotified(
            @RequestBody List<Long> noticeIds,
            HttpServletRequest request) {
        try {
            Long teacherId = getTeacherIdFromToken(request);
            log.info("Teacher {} marking {} leave notices as notified", teacherId, noticeIds.size());

            // For now, just log the action
            // In a full implementation, you might update a separate tracking table
            log.info("Would mark notice IDs as notified: {}", noticeIds);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Leave notices marked as notified");
            response.put("count", noticeIds.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error marking leave notices as notified", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get leave notice statistics for teacher dashboard
     */
    @GetMapping("/leave-notices/stats")
    public ResponseEntity<Map<String, Object>> getLeaveNoticeStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        try {
            Long teacherId = getTeacherIdFromToken(request);
            log.info("Teacher {} requesting leave notice statistics", teacherId);

            // Get notices for date range or default to current month
            LocalDate startDate = from != null ? from : LocalDate.now().withDayOfMonth(1);
            LocalDate endDate = to != null ? to : LocalDate.now();

            List<ParentLeaveNotice> notices = leaveNoticeService.getNoticesByDateRange(startDate, endDate);
            
            // TODO: Filter by teacher's students

            // Calculate statistics
            long totalNotices = notices.size();
            long pendingNotices = notices.stream()
                .filter(n -> n.getStatus() == ParentLeaveNotice.NoticeStatus.SENT || 
                            n.getStatus() == ParentLeaveNotice.NoticeStatus.DELIVERED)
                .count();
            long acknowledgedNotices = notices.stream()
                .filter(n -> n.getStatus() == ParentLeaveNotice.NoticeStatus.ACKNOWLEDGED)
                .count();

            // Count by type
            long fullDayNotices = notices.stream()
                .filter(n -> n.getType() == ParentLeaveNotice.NoticeType.FULL_DAY)
                .count();
            long lateNotices = notices.stream()
                .filter(n -> n.getType() == ParentLeaveNotice.NoticeType.LATE)
                .count();
            long earlyNotices = notices.stream()
                .filter(n -> n.getType() == ParentLeaveNotice.NoticeType.EARLY)
                .count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalNotices", totalNotices);
            stats.put("pendingNotices", pendingNotices);
            stats.put("acknowledgedNotices", acknowledgedNotices);
            stats.put("fullDayNotices", fullDayNotices);
            stats.put("lateNotices", lateNotices);
            stats.put("earlyNotices", earlyNotices);
            stats.put("periodFrom", startDate);
            stats.put("periodTo", endDate);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting leave notice statistics for teacher", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper methods

    private Long getTeacherIdFromToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        String email = jwtUtil.getSubjectFromToken(token);
        
        // Get user ID from email
        // This would require a method in UserService to get user by email
        // For now, we'll extract from token or use a mock implementation
        
        try {
            // Mock implementation - in real system, get from UserService
            return 1L; // Replace with actual teacher ID lookup
        } catch (Exception e) {
            throw new IllegalArgumentException("Không tìm thấy giáo viên cho token");
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("Không tìm thấy token hợp lệ");
    }
}