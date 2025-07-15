package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.absencemanagement.AbsenceRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/accountant")
@PreAuthorize("hasRole('ACCOUNTANT')")
@RequiredArgsConstructor
public class AccountantController {
    private final UserRepository userRepository;
    private final AbsenceRepository absenceRepository;

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
} 