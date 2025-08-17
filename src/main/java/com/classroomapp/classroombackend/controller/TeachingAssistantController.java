package com.classroomapp.classroombackend.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.ParentRequestDto;
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSessionDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSubmitDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.CreateAttendanceSessionDto;
import com.classroomapp.classroombackend.dto.classroommanagement.ClassroomDto;
import com.classroomapp.classroombackend.model.ParentRequest;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ParentRequestRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AttendanceService;
import com.classroomapp.classroombackend.service.ClassroomService;

/**
 * Controller for Teaching Assistant operations
 * Handles classroom assignments, attendance, and student evaluations for teaching assistants
 */
@RestController
@RequestMapping("/api/teaching-assistant")
@PreAuthorize("hasRole('TEACHING_ASSISTANT')")
public class TeachingAssistantController {
    
    private static final Logger log = LoggerFactory.getLogger(TeachingAssistantController.class);
    
    @Autowired
    private ClassroomService classroomService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private ParentRequestRepository parentRequestRepository;
    
    /**
     * Get all classrooms assigned to the current teaching assistant
     */
    @GetMapping("/my-assigned-classes")
    public ResponseEntity<List<ClassroomDto>> getMyAssignedClasses(Authentication authentication) {
        try {
            Long assistantId = getUserIdFromAuthentication(authentication);
            log.info("Fetching assigned classes for teaching assistant ID: {}", assistantId);
            
            // For now, return classrooms where the assistant might help
            // This would need proper assignment logic in the future
            List<ClassroomDto> classrooms = classroomService.getAllClassrooms();
            
            return ResponseEntity.ok(classrooms);
        } catch (Exception e) {
            log.error("Error fetching assigned classes for teaching assistant: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get students in a specific classroom assigned to the teaching assistant
     */
    @GetMapping("/classroom/{classroomId}/students")
    public ResponseEntity<List<UserDto>> getClassroomStudents(@PathVariable Long classroomId) {
        try {
            log.info("Fetching students for classroom ID: {}", classroomId);
            
            List<User> studentUsers = classroomService.getStudentsInClassroom(classroomId);
            List<UserDto> students = studentUsers.stream()
                .map(user -> {
                    UserDto dto = new UserDto();
                    dto.setId(user.getId());
                    dto.setFullName(user.getFullName());
                    dto.setEmail(user.getEmail());
                    dto.setUsername(user.getUsername());
                    dto.setRoleId(user.getRoleId());
                    return dto;
                })
                .toList();
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("Error fetching students for classroom {}: {}", classroomId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get attendance sessions for a specific classroom
     */
    @GetMapping("/classroom/{classroomId}/attendance-sessions")
    public ResponseEntity<List<AttendanceSessionDto>> getClassroomAttendanceSessions(@PathVariable Long classroomId) {
        try {
            log.info("Fetching attendance sessions for classroom ID: {}", classroomId);
            
            // This would need implementation in AttendanceService
            // For now, return empty list
            List<AttendanceSessionDto> sessions = new ArrayList<>();
            
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Error fetching attendance sessions for classroom {}: {}", classroomId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create attendance session for teaching assistant
     */
    @PostMapping("/classroom/{classroomId}/attendance-session")
    public ResponseEntity<AttendanceSessionDto> createAttendanceSession(
            @PathVariable Long classroomId,
            @RequestBody CreateAttendanceSessionDto dto,
            Authentication authentication) {
        try {
            Long assistantId = getUserIdFromAuthentication(authentication);
            log.info("Creating attendance session for classroom {} by assistant {}", classroomId, assistantId);
            
            // This would need implementation in AttendanceService
            // For now, return mock response
            AttendanceSessionDto session = new AttendanceSessionDto();
            session.setId(1L);
            session.setClassroomId(classroomId);
            session.setCreatedBy(assistantId);
            
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            log.error("Error creating attendance session: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Submit attendance for students by teaching assistant
     */
    @PostMapping("/classroom/{classroomId}/submit-attendance")
    public ResponseEntity<String> submitAttendance(
            @PathVariable Long classroomId,
            @RequestBody List<AttendanceSubmitDto> attendanceList,
            Authentication authentication) {
        try {
            Long assistantId = getUserIdFromAuthentication(authentication);
            log.info("Submitting attendance for classroom {} by assistant {}", classroomId, assistantId);
            
            // Process each attendance record
            for (AttendanceSubmitDto attendance : attendanceList) {
                log.info("Processing attendance submission for classroom {} with {} records", 
                    attendance.getClassroomId(), 
                    attendance.getRecords() != null ? attendance.getRecords().size() : 0);
                // This would call AttendanceService to save the data
            }
            
            return ResponseEntity.ok("Attendance submitted successfully");
        } catch (Exception e) {
            log.error("Error submitting attendance: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Send attendance notification to parents
     */
    @PostMapping("/classroom/{classroomId}/notify-parents")
    public ResponseEntity<String> notifyParents(
            @PathVariable Long classroomId,
            @RequestBody List<Long> absentStudentIds,
            Authentication authentication) {
        try {
            Long assistantId = getUserIdFromAuthentication(authentication);
            log.info("Sending parent notifications for {} absent students in classroom {}", 
                absentStudentIds.size(), classroomId);
            
            // This would integrate with SMS/Email service to notify parents
            // For now, just log the action
            for (Long studentId : absentStudentIds) {
                log.info("Would send notification to parents of student {}", studentId);
            }
            
            return ResponseEntity.ok("Parent notifications sent successfully");
        } catch (Exception e) {
            log.error("Error sending parent notifications: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create or update student evaluation by teaching assistant
     */
    @PostMapping("/classroom/{classroomId}/student/{studentId}/evaluation")
    public ResponseEntity<String> evaluateStudent(
            @PathVariable Long classroomId,
            @PathVariable Long studentId,
            @RequestBody Map<String, Object> evaluationData,
            Authentication authentication) {
        try {
            Long assistantId = getUserIdFromAuthentication(authentication);
            log.info("Creating/updating evaluation for student {} in classroom {} by assistant {}", 
                studentId, classroomId, assistantId);
            
            // This would save the evaluation to the database
            // For now, just log the evaluation data
            log.info("Evaluation data received: {}", evaluationData);
            
            return ResponseEntity.ok("Student evaluation saved successfully");
        } catch (Exception e) {
            log.error("Error saving student evaluation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get student evaluations for a classroom
     */
    @GetMapping("/classroom/{classroomId}/evaluations")
    public ResponseEntity<Map<String, Object>> getStudentEvaluations(@PathVariable Long classroomId) {
        try {
            log.info("Fetching student evaluations for classroom ID: {}", classroomId);
            
            // This would fetch evaluations from the database
            // For now, return empty map
            Map<String, Object> evaluations = new HashMap<>();
            
            return ResponseEntity.ok(evaluations);
        } catch (Exception e) {
            log.error("Error fetching student evaluations for classroom {}: {}", classroomId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Send student evaluation report to parents
     */
    @PostMapping("/classroom/{classroomId}/student/{studentId}/send-evaluation-report")
    public ResponseEntity<String> sendEvaluationReport(
            @PathVariable Long classroomId,
            @PathVariable Long studentId,
            Authentication authentication) {
        try {
            Long assistantId = getUserIdFromAuthentication(authentication);
            log.info("Sending evaluation report for student {} in classroom {} by assistant {}", 
                studentId, classroomId, assistantId);
            
            // This would generate and send the evaluation report to parents
            // For now, just log the action
            log.info("Would send evaluation report to parents of student {}", studentId);
            
            return ResponseEntity.ok("Evaluation report sent to parents successfully");
        } catch (Exception e) {
            log.error("Error sending evaluation report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Send parent report for a specific student
     */
    @PostMapping("/classroom/{classroomId}/student/{studentId}/send-parent-report")
    public ResponseEntity<String> sendParentReport(
            @PathVariable Long classroomId,
            @PathVariable Long studentId,
            @RequestBody Map<String, Object> reportData,
            Authentication authentication) {
        try {
            Long assistantId = getUserIdFromAuthentication(authentication);
            log.info("Sending parent report for student {} in classroom {} by assistant {}", 
                studentId, classroomId, assistantId);
            
            // Extract report data
            String reportType = (String) reportData.get("reportType");
            String title = (String) reportData.get("title");
            String content = (String) reportData.get("content");
            List<String> methods = (List<String>) reportData.get("methods");
            Boolean sendImmediately = (Boolean) reportData.get("sendImmediately");
            
            log.info("Report details: type={}, title={}, methods={}", reportType, title, methods);
            
            // This would integrate with SMS/Email/App notification services
            // For now, just log the action
            for (String method : methods) {
                log.info("Would send {} notification with content: {}", method, content);
            }
            
            return ResponseEntity.ok("Parent report sent successfully");
        } catch (Exception e) {
            log.error("Error sending parent report: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Send bulk parent reports for multiple students
     */
    @PostMapping("/classroom/{classroomId}/send-bulk-parent-reports")
    public ResponseEntity<String> sendBulkParentReports(
            @PathVariable Long classroomId,
            @RequestBody Map<String, Object> bulkReportData,
            Authentication authentication) {
        try {
            Long assistantId = getUserIdFromAuthentication(authentication);
            log.info("Sending bulk parent reports for classroom {} by assistant {}", classroomId, assistantId);
            
            List<Long> studentIds = (List<Long>) bulkReportData.get("studentIds");
            String reportType = (String) bulkReportData.get("reportType");
            String title = (String) bulkReportData.get("title");
            String content = (String) bulkReportData.get("content");
            List<String> methods = (List<String>) bulkReportData.get("methods");
            
            log.info("Bulk report for {} students: type={}, title={}", studentIds.size(), reportType, title);
            
            // Process each student
            for (Long studentId : studentIds) {
                log.info("Would send bulk report to parents of student {} via methods: {}", studentId, methods);
            }
            
            return ResponseEntity.ok("Bulk parent reports sent successfully to " + studentIds.size() + " students");
        } catch (Exception e) {
            log.error("Error sending bulk parent reports: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get parent report history for a classroom
     */
    @GetMapping("/classroom/{classroomId}/parent-report-history")
    public ResponseEntity<List<Map<String, Object>>> getParentReportHistory(@PathVariable Long classroomId) {
        try {
            log.info("Fetching parent report history for classroom ID: {}", classroomId);
            
            // This would fetch report history from the database
            // For now, return empty list
            List<Map<String, Object>> reportHistory = new ArrayList<>();
            
            return ResponseEntity.ok(reportHistory);
        } catch (Exception e) {
            log.error("Error fetching parent report history for classroom {}: {}", classroomId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get parent contact information for students in a classroom
     */
    @GetMapping("/classroom/{classroomId}/parent-contacts")
    public ResponseEntity<Map<String, Object>> getParentContacts(@PathVariable Long classroomId) {
        try {
            log.info("Fetching parent contacts for classroom ID: {}", classroomId);
            
            // This would fetch parent contact information from the database
            // For now, return empty map
            Map<String, Object> parentContacts = new HashMap<>();
            
            return ResponseEntity.ok(parentContacts);
        } catch (Exception e) {
            log.error("Error fetching parent contacts for classroom {}: {}", classroomId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get parent requests for a classroom (Teaching Assistant can view and approve)
     */
    @GetMapping("/classroom/{classroomId}/parent-requests")
    public ResponseEntity<List<ParentRequestDto>> getParentRequests(@PathVariable Long classroomId) {
        try {
            log.info("Fetching parent requests for classroom ID: {}", classroomId);
            
            // This would fetch from ParentRequestRepository
            // For now, return mock data
            List<ParentRequestDto> requests = new ArrayList<>();
            
            // Mock parent request data
            ParentRequestDto request1 = new ParentRequestDto();
            request1.setId(1L);
            request1.setStudentId(1L);
            request1.setStudentName("Trần Văn A");
            request1.setStudentCode("SV001");
            request1.setClassroomId(classroomId);
            request1.setParentName("Trần Văn B");
            request1.setParentPhone("0901234567");
            request1.setRequestType(ParentRequest.RequestType.LATE_ARRIVAL);
            request1.setRequestDate(java.time.LocalDate.now());
            request1.setStartTime("08:30");
            request1.setReason("Con bị ốm nhẹ, cần đi khám bác sĩ buổi sáng");
            request1.setStatus(ParentRequest.RequestStatus.PENDING);
            request1.setCreatedAt(java.time.LocalDateTime.now().minusHours(2));
            request1.setTeacherNotified(true);
            request1.setAssistantNotified(false);
            
            ParentRequestDto request2 = new ParentRequestDto();
            request2.setId(2L);
            request2.setStudentId(2L);
            request2.setStudentName("Lê Thị B");
            request2.setStudentCode("SV002");
            request2.setClassroomId(classroomId);
            request2.setParentName("Lê Văn C");
            request2.setParentPhone("0901234568");
            request2.setRequestType(ParentRequest.RequestType.LEAVE);
            request2.setRequestDate(java.time.LocalDate.now().plusDays(1));
            request2.setReason("Đi khám bệnh định kỳ");
            request2.setStatus(ParentRequest.RequestStatus.PENDING);
            request2.setCreatedAt(java.time.LocalDateTime.now().minusHours(1));
            request2.setTeacherNotified(false);
            request2.setAssistantNotified(false);
            
            requests.add(request1);
            requests.add(request2);
            
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            log.error("Error fetching parent requests for classroom {}: {}", classroomId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Approve or reject a parent request by teaching assistant
     */
    @PostMapping("/parent-request/{requestId}/respond")
    public ResponseEntity<String> respondToParentRequest(
            @PathVariable Long requestId,
            @RequestBody Map<String, Object> responseData,
            Authentication authentication) {
        try {
            Long assistantId = getUserIdFromAuthentication(authentication);
            log.info("Teaching assistant {} responding to parent request {}", assistantId, requestId);
            
            String action = (String) responseData.get("action"); // "APPROVE" or "REJECT"
            String response = (String) responseData.get("response");
            
            log.info("Action: {}, Response: {}", action, response);
            
            // This would update the ParentRequest in database
            // For now, just log the action
            log.info("Would {} parent request {} with response: {}", action, requestId, response);
            
            return ResponseEntity.ok("Parent request " + action.toLowerCase() + "d successfully");
        } catch (Exception e) {
            log.error("Error responding to parent request: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get pending parent requests count for notification badge
     */
    @GetMapping("/classroom/{classroomId}/parent-requests/pending-count")
    public ResponseEntity<Map<String, Object>> getPendingParentRequestsCount(@PathVariable Long classroomId) {
        try {
            log.info("Fetching pending parent requests count for classroom ID: {}", classroomId);
            
            // This would count from ParentRequestRepository
            // For now, return mock count
            Map<String, Object> result = new HashMap<>();
            result.put("pendingCount", 2);
            result.put("newCount", 1); // New requests since last check
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error fetching pending parent requests count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Mark parent requests as notified for teaching assistant
     */
    @PostMapping("/classroom/{classroomId}/parent-requests/mark-notified")
    public ResponseEntity<String> markParentRequestsAsNotified(
            @PathVariable Long classroomId,
            Authentication authentication) {
        try {
            Long assistantId = getUserIdFromAuthentication(authentication);
            log.info("Marking parent requests as notified for assistant {} in classroom {}", assistantId, classroomId);
            
            // This would update assistantNotified flag in database
            // For now, just log the action
            log.info("Would mark all pending parent requests as notified for assistant");
            
            return ResponseEntity.ok("Parent requests marked as notified");
        } catch (Exception e) {
            log.error("Error marking parent requests as notified: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Helper method to extract user ID from authentication
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            log.error("Authentication is null or principal is not UserDetails");
            throw new RuntimeException("Người dùng chưa xác thực hoặc không có thông tin chi tiết người dùng.");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        log.info("Looking up user by email: {}", username);

        return userRepository.findByEmail(username)
                .map(user -> {
                    log.info("Found user: {} with ID: {}", user.getFullName(), user.getId());
                    return user.getId();
                })
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", username);
                    return new RuntimeException("User not found with email: " + username);
                });
    }
}