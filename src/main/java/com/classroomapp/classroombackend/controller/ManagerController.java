package com.classroomapp.classroombackend.controller;

import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

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
import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.repository.ContractRepository;

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3000", "http://localhost:5173"})
public class ManagerController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ContractRepository contractRepository;

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
            Optional<Contract> active = contractRepository.findActiveContractByUserId(currentUser.getId());
            Contract contract = active.orElseGet(() -> {
                List<Contract> list = contractRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
                return list.isEmpty() ? null : list.get(0);
            });

            // Build response compatible with frontend form fields
            return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
                put("fullName", currentUser.getFullName());
                put("email", currentUser.getEmail());
                put("phoneNumber", currentUser.getPhoneNumber());
                // Show username as managerId (display-only)
                put("managerId", currentUser.getUsername());
                put("department", currentUser.getDepartment());
                // Include position and birthDate from contract if available
                put("position", contract != null ? contract.getPosition() : "");
                put("birthDate", currentUser.getBirthDate());
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
            // Update birthDate and position on active contract if provided
            LocalDate birthDate = null;
            if (profileData.containsKey("birthDate") && profileData.get("birthDate") instanceof String) {
                try { birthDate = LocalDate.parse((String) profileData.get("birthDate")); } catch (Exception ignored) {}
            } else if (profileData.containsKey("birthYear")) {
                Object yearObj = profileData.get("birthYear");
                try {
                    int year = yearObj instanceof Number ? ((Number) yearObj).intValue() : Integer.parseInt(yearObj.toString());
                    if (year > 1900 && year < LocalDate.now().getYear() + 1) {
                        birthDate = LocalDate.of(year, 1, 1);
                    }
                } catch (Exception ignored) {}
            }

            Optional<Contract> active = contractRepository.findActiveContractByUserId(currentUser.getId());
            Contract contract = active.orElseGet(() -> {
                List<Contract> list = contractRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId());
                return list.isEmpty() ? null : list.get(0);
            });
            
            // Create minimal contract if none exists
            if (contract == null) {
                contract = new Contract();
                contract.setUserId(currentUser.getId());
                contract.setContractId("MGR-" + currentUser.getId() + "-" + System.currentTimeMillis());
                contract.setFullName(currentUser.getFullName() != null ? currentUser.getFullName() : "Quản lý");
                contract.setEmail(currentUser.getEmail());
                contract.setContractType("MANAGER");
                contract.setPosition("Quản lý");
                contract.setSalary(20000000.0); // Default salary
                contract.setStatus("ACTIVE");
                contract.setDepartment(currentUser.getDepartment());
                contract.setPhoneNumber(currentUser.getPhoneNumber());
            }
            
            if (birthDate != null) {
                currentUser.setBirthDate(birthDate);
            }
            if (profileData.get("position") instanceof String) {
                contract.setPosition((String) profileData.get("position"));
            }
            contractRepository.save(contract);

            userRepository.save(currentUser);

            return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
