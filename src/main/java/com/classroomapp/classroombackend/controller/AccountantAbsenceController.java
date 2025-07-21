package com.classroomapp.classroombackend.controller;

import com.classroomapp.classroombackend.dto.absencemanagement.AbsenceDTO;
import com.classroomapp.classroombackend.dto.absencemanagement.CreateAbsenceDTO;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AbsenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/accountant/absences")
@PreAuthorize("hasRole('ACCOUNTANT')")
@RequiredArgsConstructor
@Slf4j
public class AccountantAbsenceController {

    private final AbsenceService absenceService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<AbsenceDTO> createAbsenceRequest(
            @Valid @RequestBody CreateAbsenceDTO createDto,
            Authentication authentication) {
        try {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Long userId = getUserIdFromAuthentication(authentication);
            AbsenceDTO createdAbsence = absenceService.createAbsenceRequest(createDto, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAbsence);
        } catch (Exception e) {
            log.error("Error creating absence request: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping
    public ResponseEntity<List<AbsenceDTO>> getMyAbsenceRequests(Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        List<AbsenceDTO> absences = absenceService.getMyAbsenceRequests(userId);
        return ResponseEntity.ok(absences);
    }

    @GetMapping("/{absenceId}")
    public ResponseEntity<AbsenceDTO> getAbsenceById(
            @PathVariable Long absenceId,
            Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        AbsenceDTO absence = absenceService.getAbsenceById(absenceId, userId);
        return ResponseEntity.ok(absence);
    }

    // Helper method to extract user ID from authentication
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            throw new RuntimeException("Người dùng chưa xác thực hoặc không có thông tin user details.");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String principal = userDetails.getUsername();
        User user = userRepository.findByEmail(principal)
                .orElseGet(() -> userRepository.findByUsername(principal).orElse(null));
        if (user == null) {
            throw new RuntimeException("Không tìm thấy người dùng với thông tin xác thực hiện tại");
        }
        return user.getId();
    }
} 