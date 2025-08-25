package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceSubmitDto;
import com.classroomapp.classroombackend.model.Parent;
import com.classroomapp.classroombackend.model.StudentParent;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.ParentRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.StudentParentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ZaloNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for testing Zalo notification functionality
 * Only available in dev/test profiles
 */
@RestController
@RequestMapping("/api/test/zalo")
@Profile({"dev", "test"})
@RequiredArgsConstructor
@Slf4j
public class ZaloTestController {

    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final ParentRepository parentRepository;
    private final StudentParentRepository studentParentRepository;
    private final ClassroomEnrollmentRepository classroomEnrollmentRepository;
    private final ZaloNotificationService zaloNotificationService;

    /**
     * Get test data summary for Zalo notification testing
     */
    @GetMapping("/data-summary")
    public ResponseEntity<Map<String, Object>> getTestDataSummary() {
        log.info("📋 Getting Zalo test data summary...");
        
        Map<String, Object> summary = new HashMap<>();
        
        try {
            // Find test classroom
            List<Classroom> testClassrooms = classroomRepository.findByNameContainingIgnoreCase("Test Zalo");
            if (testClassrooms.isEmpty()) {
                summary.put("error", "No test classroom found. Please run the application to trigger ZaloNotificationTestSeeder");
                return ResponseEntity.ok(summary);
            }
            
            Classroom testClassroom = testClassrooms.get(0);
            summary.put("classroom", Map.of(
                "id", testClassroom.getId(),
                "name", testClassroom.getName(),
                "teacherId", testClassroom.getTeacher().getId(),
                "teacherName", testClassroom.getTeacher().getFullName()
            ));
            
            // Get enrolled students
            List<ClassroomEnrollment> enrollments = classroomEnrollmentRepository.findByClassroomId(testClassroom.getId());
            summary.put("enrolledStudents", enrollments.stream().map(enrollment -> {
                User student = enrollment.getUser();
                return Map.of(
                    "id", student.getId(),
                    "name", student.getFullName(),
                    "username", student.getUsername()
                );
            }).toList());
            
            // Get parents with phone numbers
            List<Parent> allParents = parentRepository.findAll();
            List<Map<String, Object>> parentInfo = allParents.stream()
                .filter(parent -> parent.getPhone() != null && 
                               (parent.getPhone().equals("0971335989") || parent.getPhone().equals("0859326040")))
                .map(parent -> {
                    List<StudentParent> relationships = studentParentRepository.findActiveChildrenByParentId(parent.getId());
                    return Map.of(
                        "id", parent.getId(),
                        "name", parent.getName(),
                        "phone", parent.getPhone(),
                        "childrenCount", relationships.size(),
                        "children", relationships.stream().map(rel -> {
                            Optional<User> student = userRepository.findById(rel.getStudentId());
                            return student.map(s -> Map.of(
                                "id", s.getId(),
                                "name", s.getFullName()
                            )).orElse(Map.of("id", rel.getStudentId(), "name", "Unknown"));
                        }).toList()
                    );
                }).toList();
            
            summary.put("testParents", parentInfo);
            summary.put("zaloNotificationEnabled", zaloNotificationService.isZaloNotificationEnabled());
            
            // Sample test data for attendance submission
            summary.put("sampleAttendanceData", Map.of(
                "classroomId", testClassroom.getId(),
                "records", enrollments.stream().map(enrollment -> Map.of(
                    "studentId", enrollment.getUser().getId(),
                    "status", "ABSENT", // Test with ABSENT to trigger notification
                    "note", "Test notification từ n8n workflow"
                )).toList()
            ));
            
            log.info("✅ Test data summary generated successfully");
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            log.error("❌ Error getting test data summary: {}", e.getMessage(), e);
            summary.put("error", "Error getting test data: " + e.getMessage());
            return ResponseEntity.ok(summary);
        }
    }

    /**
     * Trigger test Zalo notification for test classroom
     */
    @PostMapping("/trigger-notification")
    public ResponseEntity<Map<String, Object>> triggerTestNotification(
            @RequestBody(required = false) AttendanceSubmitDto attendanceData) {
        
        log.info("🚀 Triggering test Zalo notification...");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // If no data provided, use default test data
            if (attendanceData == null) {
                attendanceData = createDefaultTestAttendanceData();
            }
            
            if (attendanceData == null) {
                result.put("error", "No test classroom found. Please run the application to trigger ZaloNotificationTestSeeder");
                return ResponseEntity.ok(result);
            }
            
            // Get teacher ID for the test classroom
            Optional<Classroom> classroomOpt = classroomRepository.findById(attendanceData.getClassroomId());
            if (classroomOpt.isEmpty()) {
                result.put("error", "Classroom not found: " + attendanceData.getClassroomId());
                return ResponseEntity.ok(result);
            }
            
            Long teacherId = classroomOpt.get().getTeacher().getId();
            
            // Trigger Zalo notification
            zaloNotificationService.sendAttendanceNotification(attendanceData, teacherId);
            
            result.put("success", true);
            result.put("message", "Zalo notification triggered successfully");
            result.put("classroomId", attendanceData.getClassroomId());
            result.put("teacherId", teacherId);
            result.put("recordsCount", attendanceData.getRecords().size());
            
            log.info("✅ Test Zalo notification triggered successfully");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ Error triggering test notification: {}", e.getMessage(), e);
            result.put("error", "Error triggering notification: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * Check Zalo notification service status
     */
    @GetMapping("/service-status")
    public ResponseEntity<Map<String, Object>> getServiceStatus() {
        Map<String, Object> status = new HashMap<>();
        
        status.put("zaloNotificationEnabled", zaloNotificationService.isZaloNotificationEnabled());
        status.put("serviceClass", zaloNotificationService.getClass().getSimpleName());
        
        return ResponseEntity.ok(status);
    }

    private AttendanceSubmitDto createDefaultTestAttendanceData() {
        // Find test classroom
        List<Classroom> testClassrooms = classroomRepository.findByNameContainingIgnoreCase("Test Zalo");
        if (testClassrooms.isEmpty()) {
            return null;
        }
        
        Classroom testClassroom = testClassrooms.get(0);
        List<ClassroomEnrollment> enrollments = classroomEnrollmentRepository.findByClassroomId(testClassroom.getId());
        
        AttendanceSubmitDto attendanceData = new AttendanceSubmitDto();
        attendanceData.setClassroomId(testClassroom.getId());
        
        // Create attendance records for all enrolled students
        List<AttendanceSubmitDto.AttendanceRecord> records = enrollments.stream().map(enrollment -> {
            AttendanceSubmitDto.AttendanceRecord record = new AttendanceSubmitDto.AttendanceRecord();
            record.setStudentId(enrollment.getUser().getId());
            record.setStatus("ABSENT"); // Use ABSENT to trigger notification
            record.setNote("Test notification từ ZaloTestController");
            return record;
        }).toList();
        
        attendanceData.setRecords(records);
        
        return attendanceData;
    }
}
