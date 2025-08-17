package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.repository.absencemanagement.AbsenceRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/accountant")
@PreAuthorize("hasRole('ACCOUNTANT')")
@RequiredArgsConstructor
public class AccountantController {
    private final UserRepository userRepository;
    private final AbsenceRepository absenceRepository;
    private final ContractRepository contractRepository;

    @GetMapping("/dashboard-stats")
    public ResponseEntity<Map<String, Object>> getAccountantDashboardStats(Authentication authentication) {
        String email = authentication.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByUsername(email).orElse(null));
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không tìm thấy người dùng"));
        }
        Long userId = currentUser.getId();
        int totalAbsences = absenceRepository.findByUserId(userId).size();
        int pendingAbsences = (int) absenceRepository.findByUserId(userId).stream().filter(a -> "PENDING".equals(a.getStatus())).count();
        int approvedAbsences = (int) absenceRepository.findByUserId(userId).stream().filter(a -> "APPROVED".equals(a.getStatus())).count();
        Integer annualLeaveBalance = currentUser.getAnnualLeaveBalance();

        // Tạo object leaveStats
        Map<String, Object> leaveStats = new HashMap<>();
        leaveStats.put("totalAbsences", totalAbsences);
        leaveStats.put("pendingAbsences", pendingAbsences);
        leaveStats.put("approvedAbsences", approvedAbsences);
        leaveStats.put("annualLeaveBalance", annualLeaveBalance);

        // Tạo object financialStats (placeholder)
        Map<String, Object> financialStats = new HashMap<>();
        financialStats.put("totalInvoices", 0);
        financialStats.put("paidInvoices", 0);
        financialStats.put("pendingPayments", 0);
        financialStats.put("overduePayments", 0);

        // Tạo object messageStats (placeholder)
        Map<String, Object> messageStats = new HashMap<>();
        messageStats.put("unreadMessages", 0);

        // Gộp tất cả vào object trả về
        Map<String, Object> stats = new HashMap<>();
        stats.put("leaveStats", leaveStats);
        stats.put("financialStats", financialStats);
        stats.put("messageStats", messageStats);

        return ResponseEntity.ok(stats);
    }

    /**
     * Get current accountant profile
     * Endpoint: GET /api/accountant/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getAccountantProfile(Authentication authentication) {
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
                // Show username as accountantId (display-only)
                put("accountantId", currentUser.getUsername());
                put("department", currentUser.getDepartment());
                // Include position from contract if available; birthDate from user
                put("position", contract != null ? contract.getPosition() : "");
                put("birthDate", currentUser.getBirthDate());
            }});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Update current accountant profile
     * Endpoint: PUT /api/accountant/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateAccountantProfile(@RequestBody Map<String, Object> profileData, Authentication authentication) {
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
            // Update birthDate on user if provided
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
                contract.setContractId("ACC-" + currentUser.getId() + "-" + System.currentTimeMillis());
                contract.setFullName(currentUser.getFullName() != null ? currentUser.getFullName() : "Kế toán");
                contract.setEmail(currentUser.getEmail());
                contract.setContractType("ACCOUNTANT");
                contract.setPosition("Kế toán");
                contract.setSalary(15000000.0); // Default salary
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