package com.classroomapp.classroombackend.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpHeaders;

import com.classroomapp.classroombackend.model.Parent;
import com.classroomapp.classroombackend.model.ParentLeaveNotice;
import com.classroomapp.classroombackend.model.StudentParent;
import com.classroomapp.classroombackend.security.JwtUtil;
import com.classroomapp.classroombackend.service.ParentLeaveNoticeService;
import com.classroomapp.classroombackend.service.ParentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Parent Controller - API endpoints for parent functionality
 * Based on PARENT_ROLE_SPEC.md requirements
 */
@RestController
@RequestMapping("/api/parent")
@Slf4j
@PreAuthorize("hasRole('PARENT')")
public class ParentController {

    private final ParentService parentService;
    private final ParentLeaveNoticeService leaveNoticeService;
    private final JwtUtil jwtUtil;

    @Autowired
    public ParentController(ParentService parentService, 
                           ParentLeaveNoticeService leaveNoticeService,
                           JwtUtil jwtUtil) {
        this.parentService = parentService;
        this.leaveNoticeService = leaveNoticeService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Get current parent's children
     */
    @GetMapping("/children")
    public ResponseEntity<List<StudentParent>> getChildren(HttpServletRequest request) {
        try {
            Long parentId = getParentIdFromToken(request);
            List<StudentParent> children = parentService.getChildrenByParentId(parentId);
            return ResponseEntity.ok(children);
        } catch (Exception e) {
            log.error("Error getting children for parent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create leave notice for child
     */
    @PostMapping("/children/{childId}/leave-notices")
    public ResponseEntity<Map<String, Object>> createLeaveNotice(
            @PathVariable Long childId,
            @Valid @RequestBody CreateLeaveNoticeRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long parentId = getParentIdFromToken(httpRequest);
            String token = getTokenFromRequest(httpRequest);
            
            // Validate parent has access to this child
            if (!jwtUtil.validateParentChildAccess(token, childId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            ParentLeaveNotice notice;
            
            switch (request.getType()) {
                case "FULL_DAY":
                    notice = leaveNoticeService.createFullDayNotice(
                        parentId, childId, request.getDate(), 
                        ParentLeaveNotice.ReasonCode.valueOf(request.getReasonCode()), 
                        request.getNote()
                    );
                    break;
                case "LATE":
                    notice = leaveNoticeService.createLateNotice(
                        parentId, childId, request.getDate(), request.getArriveAt(),
                        ParentLeaveNotice.ReasonCode.valueOf(request.getReasonCode()), 
                        request.getNote()
                    );
                    break;
                case "EARLY":
                    notice = leaveNoticeService.createEarlyNotice(
                        parentId, childId, request.getDate(), request.getLeaveAt(),
                        ParentLeaveNotice.ReasonCode.valueOf(request.getReasonCode()), 
                        request.getNote()
                    );
                    break;
                default:
                    return ResponseEntity.badRequest().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", notice.getId());
            response.put("status", notice.getStatus());
            response.put("message", "Leave notice created successfully");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid leave notice request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating leave notice", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get leave notices for child
     */
    @GetMapping("/children/{childId}/leave-notices")
    public ResponseEntity<List<ParentLeaveNotice>> getLeaveNotices(
            @PathVariable Long childId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest httpRequest) {
        try {
            Long parentId = getParentIdFromToken(httpRequest);
            String token = getTokenFromRequest(httpRequest);
            
            // Validate parent has access to this child
            if (!jwtUtil.validateParentChildAccess(token, childId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<ParentLeaveNotice> notices;
            if (from != null && to != null) {
                notices = leaveNoticeService.getNoticesForStudentInDateRange(childId, from, to);
            } else {
                notices = leaveNoticeService.getNoticesByStudentId(childId);
            }

            return ResponseEntity.ok(notices);
        } catch (Exception e) {
            log.error("Error getting leave notices for child {}", childId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all leave notices for parent
     */
    @GetMapping("/leave-notices")
    public ResponseEntity<List<ParentLeaveNotice>> getAllLeaveNotices(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletRequest request) {
        try {
            Long parentId = getParentIdFromToken(request);
            
            List<ParentLeaveNotice> notices;
            if (from != null && to != null) {
                notices = leaveNoticeService.getNoticesForParentInDateRange(parentId, from, to);
            } else {
                notices = leaveNoticeService.getNoticesByParentId(parentId);
            }

            return ResponseEntity.ok(notices);
        } catch (Exception e) {
            log.error("Error getting leave notices for parent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get parent dashboard stats
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(HttpServletRequest request) {
        try {
            Long parentId = getParentIdFromToken(request);
            
            Map<String, Object> stats = new HashMap<>();
            
            // Children count
            Long childrenCount = parentService.countChildrenByParentId(parentId);
            stats.put("childrenCount", childrenCount);
            
            // Pending leave notices
            Long pendingNotices = leaveNoticeService.countPendingNoticesByParentId(parentId);
            stats.put("pendingLeaveNotices", pendingNotices);
            
            // Notice statistics
            ParentLeaveNoticeService.NoticeStatistics noticeStats = 
                leaveNoticeService.getNoticeStatisticsForParent(parentId);
            stats.put("noticeStatistics", noticeStats);
            
            // Recent notices
            List<ParentLeaveNotice> recentNotices = leaveNoticeService.getRecentNotices(7);
            stats.put("recentNotices", recentNotices);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting dashboard stats for parent", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get parent profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Parent> getProfile(HttpServletRequest request) {
        try {
            Long parentId = getParentIdFromToken(request);
            Optional<Parent> parent = parentService.getParentById(parentId);
            
            if (parent.isPresent()) {
                return ResponseEntity.ok(parent.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error getting parent profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update parent profile
     */
    @PutMapping("/profile")
    public ResponseEntity<Parent> updateProfile(
            @Valid @RequestBody UpdateParentProfileRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long parentId = getParentIdFromToken(httpRequest);
            
            Parent parentUpdates = new Parent();
            parentUpdates.setName(request.getName());
            parentUpdates.setPhone(request.getPhone());
            parentUpdates.setEmail(request.getEmail());
            
            Parent updatedParent = parentService.updateParent(parentId, parentUpdates);
            return ResponseEntity.ok(updatedParent);
        } catch (Exception e) {
            log.error("Error updating parent profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete leave notice
     */
    @DeleteMapping("/leave-notices/{noticeId}")
    public ResponseEntity<Void> deleteLeaveNotice(
            @PathVariable Long noticeId,
            HttpServletRequest request) {
        try {
            Long parentId = getParentIdFromToken(request);
            leaveNoticeService.deleteNotice(noticeId, parentId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid delete request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error deleting leave notice {}", noticeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get child's schedule (timetable)
     */
    @GetMapping("/children/{childId}/schedule")
    public ResponseEntity<List<Map<String, Object>>> getChildSchedule(
            @PathVariable Long childId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest httpRequest) {
        try {
            Long parentId = getParentIdFromToken(httpRequest);
            String token = getTokenFromRequest(httpRequest);
            
            // Validate parent has access to this child
            if (!jwtUtil.validateParentChildAccess(token, childId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Set default date range if not provided (current month)
            if (startDate == null) {
                startDate = LocalDate.now().withDayOfMonth(1);
            }
            if (endDate == null) {
                endDate = startDate.plusMonths(1).minusDays(1);
            }

            List<Map<String, Object>> schedule = parentService.getChildSchedule(childId, startDate, endDate);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            log.error("Error getting schedule for child {}", childId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get child's exam schedule
     */
    @GetMapping("/children/{childId}/exams")
    public ResponseEntity<List<Map<String, Object>>> getChildExams(
            @PathVariable Long childId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest httpRequest) {
        try {
            Long parentId = getParentIdFromToken(httpRequest);
            String token = getTokenFromRequest(httpRequest);
            
            // Validate parent has access to this child
            if (!jwtUtil.validateParentChildAccess(token, childId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Set default date range if not provided (current month)
            if (startDate == null) {
                startDate = LocalDate.now().withDayOfMonth(1);
            }
            if (endDate == null) {
                endDate = startDate.plusMonths(1).minusDays(1);
            }

            List<Map<String, Object>> exams = parentService.getChildExams(childId, startDate, endDate);
            return ResponseEntity.ok(exams);
        } catch (Exception e) {
            log.error("Error getting exams for child {}", childId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get child's billing information (invoices and payments)
     */
    @GetMapping("/children/{childId}/billing")
    public ResponseEntity<Map<String, Object>> getChildBilling(
            @PathVariable Long childId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            HttpServletRequest httpRequest) {
        try {
            Long parentId = getParentIdFromToken(httpRequest);
            String token = getTokenFromRequest(httpRequest);
            
            // Validate parent has access to this child
            if (!jwtUtil.validateParentChildAccess(token, childId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Check if parent has billing access for this child
            if (!parentService.hasChildBillingAccess(parentId, childId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Map<String, Object> billingData = parentService.getChildBillingData(childId, startDate, endDate);
            return ResponseEntity.ok(billingData);
        } catch (Exception e) {
            log.error("Error getting billing data for child {}", childId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Download invoice/receipt PDF
     */
    @GetMapping("/billing/download/{documentId}")
    public ResponseEntity<byte[]> downloadBillingDocument(
            @PathVariable Long documentId,
            @RequestParam String type, // "invoice" or "receipt"
            HttpServletRequest httpRequest) {
        try {
            Long parentId = getParentIdFromToken(httpRequest);
            
            byte[] documentData = parentService.getBillingDocument(parentId, documentId, type);
            
            String filename = String.format("%s_%d.pdf", type, documentId);
            
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .body(documentData);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid billing document request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error downloading billing document {}", documentId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper methods

    private Long getParentIdFromToken(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        String email = jwtUtil.getSubjectFromToken(token);
        
        Optional<Parent> parent = parentService.getParentByEmail(email);
        if (parent.isPresent()) {
            return parent.get().getId();
        } else {
            throw new IllegalArgumentException("Parent not found for token");
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new IllegalArgumentException("No valid token found");
    }

    // DTO Classes
    public static class CreateLeaveNoticeRequest {
        private String type; // FULL_DAY, LATE, EARLY
        private LocalDate date;
        private LocalTime arriveAt; // For LATE type
        private LocalTime leaveAt;  // For EARLY type
        private String reasonCode;
        private String note;

        // Getters and setters
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public LocalTime getArriveAt() { return arriveAt; }
        public void setArriveAt(LocalTime arriveAt) { this.arriveAt = arriveAt; }

        public LocalTime getLeaveAt() { return leaveAt; }
        public void setLeaveAt(LocalTime leaveAt) { this.leaveAt = leaveAt; }

        public String getReasonCode() { return reasonCode; }
        public void setReasonCode(String reasonCode) { this.reasonCode = reasonCode; }

        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }

    public static class UpdateParentProfileRequest {
        private String name;
        private String phone;
        private String email;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
}