package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.model.Parent;
import com.classroomapp.classroombackend.model.ParentNotificationPrefs;
import com.classroomapp.classroombackend.model.StudentParent;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.parentmanagement.ParentNotificationPrefsRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.ParentRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.StudentParentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for managing test data setup
 * Used for testing Zalo notifications and other features
 */
@RestController
@RequestMapping("/api/test-data")
@RequiredArgsConstructor
@Slf4j
public class TestDataController {

    private final UserRepository userRepository;
    private final ParentRepository parentRepository;
    private final StudentParentRepository studentParentRepository;
    private final ParentNotificationPrefsRepository notificationPrefsRepository;

    /**
     * Update parent phone numbers for Zalo testing
     * Sets up 2 students with specific parent phone numbers for n8n workflow testing
     */
    @PostMapping("/setup-parent-phones")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> setupParentPhones() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            log.info("🔧 Setting up parent phone numbers for Zalo testing...");
            
            // Target phone numbers for testing
            String phone1 = "0971335989";
            String phone2 = "0859326040";
            
            // Find 2 students (prefer 'student' and 'student123' usernames)
            List<User> students = findTestStudents();
            
            if (students.size() < 2) {
                response.put("success", false);
                response.put("message", "Not enough students found in the system. Need at least 2 students.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Setup parent for student 1
            User student1 = students.get(0);
            Parent parent1 = setupParentForStudent(student1, phone1, "Phụ huynh " + student1.getFullName(), "parent1@test.com");
            results.add(createStudentParentResult(student1, parent1, phone1));
            
            // Setup parent for student 2
            User student2 = students.get(1);
            Parent parent2 = setupParentForStudent(student2, phone2, "Phụ huynh " + student2.getFullName(), "parent2@test.com");
            results.add(createStudentParentResult(student2, parent2, phone2));
            
            response.put("success", true);
            response.put("message", "Parent phone numbers updated successfully for Zalo testing");
            response.put("results", results);
            response.put("timestamp", LocalDateTime.now());
            
            log.info("✅ Successfully updated parent phone numbers for {} students", results.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error setting up parent phone numbers: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Get current parent phone setup status
     */
    @GetMapping("/parent-phones-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'TEACHER')")
    public ResponseEntity<Map<String, Object>> getParentPhonesStatus() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            // Find students with the target phone numbers
            List<Parent> parentsWithTargetPhones = parentRepository.findAll().stream()
                .filter(p -> "0971335989".equals(p.getPhone()) || "0859326040".equals(p.getPhone()))
                .collect(Collectors.toList());
            
            for (Parent parent : parentsWithTargetPhones) {
                List<StudentParent> relationships = studentParentRepository.findAll().stream()
                    .filter(sp -> sp.getParentId().equals(parent.getId()))
                    .collect(Collectors.toList());
                
                for (StudentParent relationship : relationships) {
                    Optional<User> studentOpt = userRepository.findById(relationship.getStudentId());
                    if (studentOpt.isPresent()) {
                        User student = studentOpt.get();
                        ParentNotificationPrefs prefs = notificationPrefsRepository.findByParentId(parent.getId())
                                .orElse(null);
                        
                        Map<String, Object> result = new HashMap<>();
                        result.put("studentId", student.getId());
                        result.put("studentUsername", student.getUsername());
                        result.put("studentName", student.getFullName());
                        result.put("parentId", parent.getId());
                        result.put("parentName", parent.getName());
                        result.put("parentPhone", parent.getPhone());
                        result.put("notificationEnabled", prefs != null && prefs.getChannels() != null ?
                            prefs.getChannels().getOrDefault("inapp", false) : false);
                        result.put("zaloEnabled", prefs != null && prefs.getChannels() != null ?
                            prefs.getChannels().getOrDefault("zalo", false) : false);
                        
                        results.add(result);
                    }
                }
            }
            
            response.put("success", true);
            response.put("results", results);
            response.put("totalFound", results.size());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error getting parent phones status: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * Find suitable test students
     */
    private List<User> findTestStudents() {
        // First try to find students with preferred usernames
        List<String> preferredUsernames = Arrays.asList("student", "student123", "teststudent1", "teststudent2");
        
        List<User> students = new ArrayList<>();
        
        for (String username : preferredUsernames) {
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isPresent() && userOpt.get().getRoleId() == 1) { // STUDENT role
                students.add(userOpt.get());
                if (students.size() >= 2) break;
            }
        }
        
        // If not enough preferred students found, get any active students
        if (students.size() < 2) {
            List<User> allStudents = userRepository.findByRoleIdAndStatus(1, "active");
            for (User student : allStudents) {
                if (!students.contains(student)) {
                    students.add(student);
                    if (students.size() >= 2) break;
                }
            }
        }
        
        return students;
    }
    
    /**
     * Setup parent for a student with specific phone number
     */
    private Parent setupParentForStudent(User student, String phoneNumber, String parentName, String parentEmail) {
        // Check if student already has a primary parent
        Optional<Parent> existingParent = parentRepository.findPrimaryParentByStudentId(student.getId());
        
        Parent parent;
        if (existingParent.isPresent()) {
            // Update existing parent
            parent = existingParent.get();
            parent.setPhone(phoneNumber);
            parent.setName(parentName);
            parent.setEmail(parentEmail);
            parent.setUpdatedAt(LocalDateTime.now());
            parent = parentRepository.save(parent);
            log.info("📱 Updated existing parent phone for student {}: {}", student.getUsername(), phoneNumber);
        } else {
            // Create new parent
            parent = new Parent();
            parent.setUserId(student.getId() + 1000L); // Generate fake user_id
            parent.setName(parentName);
            parent.setPhone(phoneNumber);
            parent.setEmail(parentEmail);
            parent.setStatus(Parent.ParentStatus.ACTIVE);
            parent.setCreatedAt(LocalDateTime.now());
            parent.setUpdatedAt(LocalDateTime.now());
            parent = parentRepository.save(parent);
            
            // Create student-parent relationship
            StudentParent relationship = new StudentParent();
            relationship.setStudentId(student.getId());
            relationship.setParentId(parent.getId());
            relationship.setRelationType(StudentParent.RelationType.MOTHER);
            relationship.setIsPrimary(true);
            relationship.setLegalGuardian(true);
            relationship.setStartAt(LocalDate.now());
            relationship.setCreatedAt(LocalDateTime.now());
            relationship.setUpdatedAt(LocalDateTime.now());
            studentParentRepository.save(relationship);
            
            log.info("👨‍👩‍👧‍👦 Created new parent for student {}: {}", student.getUsername(), phoneNumber);
        }
        
        // Setup notification preferences
        setupNotificationPreferences(parent);
        
        return parent;
    }
    
    /**
     * Setup notification preferences for parent
     */
    private void setupNotificationPreferences(Parent parent) {
        Optional<ParentNotificationPrefs> existingPrefs = notificationPrefsRepository.findByParentId(parent.getId());
        
        ParentNotificationPrefs prefs;
        if (existingPrefs.isPresent()) {
            prefs = existingPrefs.get();
        } else {
            prefs = new ParentNotificationPrefs();
            prefs.setParentId(parent.getId());
        }

        // Set notification channels
        Map<String, Boolean> channels = new HashMap<>();
        channels.put("inapp", true);
        channels.put("zalo", true);
        channels.put("sms", true);
        channels.put("email", true);
        prefs.setChannels(channels);
        
        notificationPrefsRepository.save(prefs);
        log.info("🔔 Setup notification preferences for parent {}", parent.getId());
    }
    
    /**
     * Create result object for student-parent setup
     */
    private Map<String, Object> createStudentParentResult(User student, Parent parent, String phoneNumber) {
        Map<String, Object> result = new HashMap<>();
        result.put("studentId", student.getId());
        result.put("studentUsername", student.getUsername());
        result.put("studentName", student.getFullName());
        result.put("parentId", parent.getId());
        result.put("parentName", parent.getName());
        result.put("parentPhone", phoneNumber);
        result.put("notificationEnabled", true);
        result.put("zaloEnabled", true);
        return result;
    }
}
