package com.classroomapp.classroombackend.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:5173"})
public class ManagerController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        // Return all fields expected by the frontend
        Map<String, Object> stats = Map.of(
            "totalUsers", 100,
            "totalCourses", 20,
            "totalSchedules", 10,
            "totalMessages", 5
        );
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Get current manager profile
     * Endpoint: GET /api/manager/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getManagerProfile(Authentication authentication) {
        try {
            String identifier = authentication.getName();
            User currentUser = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User", "identifier", identifier));

            // Build response compatible with frontend form fields
            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
                put("fullName", currentUser.getFullName());
                put("email", currentUser.getEmail());
                put("phoneNumber", currentUser.getPhoneNumber());
                // Show username as managerId (display-only)
                put("managerId", currentUser.getUsername());
                put("department", currentUser.getDepartment());
                // Position is not persisted yet; return empty string for compatibility
                put("position", "");
            }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Update current manager profile
     * Endpoint: PUT /api/manager/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateManagerProfile(@RequestBody Map<String, Object> profileData, Authentication authentication) {
        try {
            String identifier = authentication.getName();
            User currentUser = userRepository.findByUsernameOrEmail(identifier, identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User", "identifier", identifier));

            // Update allowed fields only
            if (profileData.get("fullName") instanceof String) {
                currentUser.setFullName((String) profileData.get("fullName"));
            }
            if (profileData.get("email") instanceof String) {
                currentUser.setEmail((String) profileData.get("email"));
            }
            if (profileData.get("phoneNumber") instanceof String) {
                currentUser.setPhoneNumber((String) profileData.get("phoneNumber"));
            }
            if (profileData.get("department") instanceof String) {
                currentUser.setDepartment((String) profileData.get("department"));
            }
            // "managerId" and "position" are ignored (display-only / not persisted)

            userRepository.save(currentUser);

            return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
