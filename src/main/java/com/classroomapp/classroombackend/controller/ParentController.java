package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.ChildDto;
import com.classroomapp.classroombackend.model.usermanagement.ParentChildRelationship;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ParentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parent")
@PreAuthorize("hasRole('PARENT')")
@RequiredArgsConstructor
public class ParentController {
    private final UserRepository userRepository;
    private final ParentService parentService;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getParentDashboardStats(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email).orElse(null));
        
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy người dùng"));
        }

        // Get children for current parent
        List<ChildDto> children = parentService.getChildrenForParent(currentUser.getId());

        // Create children stats
        Map<String, Object> childrenStats = new HashMap<>();
        childrenStats.put("totalChildren", children.size());
        childrenStats.put("activeChildren", children.size());
        childrenStats.put("childrenWithAssignments", children.size()); // Placeholder
        childrenStats.put("childrenWithAttendance", children.size()); // Placeholder

        // Create academic stats (placeholder data for now)
        Map<String, Object> academicStats = new HashMap<>();
        academicStats.put("averageGrade", 8.5);
        academicStats.put("completedAssignments", 15);
        academicStats.put("totalAssignments", 20);
        academicStats.put("attendanceRate", 95);

        // Create notification stats (placeholder data for now)
        Map<String, Object> notificationStats = new HashMap<>();
        notificationStats.put("unreadMessages", 3);
        notificationStats.put("pendingAnnouncements", 2);

        // Create response
        Map<String, Object> response = new HashMap<>();
        response.put("childrenStats", childrenStats);
        response.put("academicStats", academicStats);
        response.put("notificationStats", notificationStats);
        response.put("children", children);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/children")
    public ResponseEntity<List<ChildDto>> getMyChildren(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email).orElse(null));
        
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }

        List<ChildDto> children = parentService.getChildrenForParent(currentUser.getId());
        return ResponseEntity.ok(children);
    }

    @PostMapping("/children")
    public ResponseEntity<ParentChildRelationship> addChild(
            Authentication authentication,
            @RequestParam Long childId,
            @RequestParam ParentChildRelationship.RelationshipType relationshipType) {
        
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email).orElse(null));
        
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            ParentChildRelationship relationship = parentService.addChildToParent(currentUser.getId(), childId, relationshipType);
            return ResponseEntity.ok(relationship);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/children/{childId}")
    public ResponseEntity<Void> removeChild(
            Authentication authentication,
            @PathVariable Long childId) {
        
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email).orElse(null));
        
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            parentService.removeChildFromParent(currentUser.getId(), childId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/children/{childId}/academic-performance")
    public ResponseEntity<Map<String, Object>> getChildAcademicPerformance(
            Authentication authentication,
            @PathVariable Long childId) {
        
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email).orElse(null));
        
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }

        // Verify parent-child relationship
        if (!parentService.hasParentChildRelationship(currentUser.getId(), childId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Placeholder academic performance data
        Map<String, Object> academicData = new HashMap<>();
        academicData.put("childId", childId);
        academicData.put("averageGrade", 8.5);
        academicData.put("totalAssignments", 20);
        academicData.put("completedAssignments", 15);
        academicData.put("attendanceRate", 95);
        academicData.put("subjects", List.of("Mathematics", "Science", "English"));

        return ResponseEntity.ok(academicData);
    }

    @GetMapping("/children/{childId}/attendance")
    public ResponseEntity<Map<String, Object>> getChildAttendance(
            Authentication authentication,
            @PathVariable Long childId,
            @RequestParam(required = false) Long classroomId) {
        
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email).orElse(null));
        
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }

        // Verify parent-child relationship
        if (!parentService.hasParentChildRelationship(currentUser.getId(), childId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Placeholder attendance data
        Map<String, Object> attendanceData = new HashMap<>();
        attendanceData.put("childId", childId);
        attendanceData.put("totalSessions", 30);
        attendanceData.put("presentSessions", 28);
        attendanceData.put("absentSessions", 2);
        attendanceData.put("attendanceRate", 93.33);

        return ResponseEntity.ok(attendanceData);
    }

    @GetMapping("/children/{childId}/schedule")
    public ResponseEntity<Map<String, Object>> getChildSchedule(
            Authentication authentication,
            @PathVariable Long childId) {
        
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email).orElse(null));
        
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }

        // Verify parent-child relationship
        if (!parentService.hasParentChildRelationship(currentUser.getId(), childId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Placeholder schedule data
        Map<String, Object> scheduleData = new HashMap<>();
        scheduleData.put("childId", childId);
        scheduleData.put("classes", List.of(
            Map.of("subject", "Mathematics", "time", "08:00-09:30", "teacher", "Nguyễn Văn Minh"),
            Map.of("subject", "Science", "time", "09:45-11:15", "teacher", "Trần Thị Lan"),
            Map.of("subject", "English", "time", "13:30-15:00", "teacher", "Lê Văn Hùng")
        ));

        return ResponseEntity.ok(scheduleData);
    }

    @GetMapping("/children/{childId}/assignments")
    public ResponseEntity<Map<String, Object>> getChildAssignments(
            Authentication authentication,
            @PathVariable Long childId) {
        
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email).orElse(null));
        
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }

        // Verify parent-child relationship
        if (!parentService.hasParentChildRelationship(currentUser.getId(), childId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Placeholder assignments data
        Map<String, Object> assignmentsData = new HashMap<>();
        assignmentsData.put("childId", childId);
        assignmentsData.put("totalAssignments", 20);
        assignmentsData.put("completedAssignments", 15);
        assignmentsData.put("pendingAssignments", 5);
        assignmentsData.put("averageGrade", 8.5);

        return ResponseEntity.ok(assignmentsData);
    }

    @GetMapping("/relationship/{childId}")
    public ResponseEntity<ParentChildRelationship> getRelationship(
            Authentication authentication,
            @PathVariable Long childId) {
        
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email).orElse(null));
        
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            ParentChildRelationship relationship = parentService.getParentChildRelationship(currentUser.getId(), childId);
            return ResponseEntity.ok(relationship);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
} 