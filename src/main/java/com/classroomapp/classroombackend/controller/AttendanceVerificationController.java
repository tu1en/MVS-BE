package com.classroomapp.classroombackend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceCheckInResponseDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceVerificationDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.TodayAttendanceStatusDto;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AttendanceVerificationService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/attendance/verification")
@RequiredArgsConstructor
public class AttendanceVerificationController {
    
    private final AttendanceVerificationService verificationService;
    private final UserRepository userRepository;
    
    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<?> checkIn(
            @Valid @RequestBody AttendanceVerificationDto dto,
            Authentication authentication,
            HttpServletRequest request) {
        
        // Get current user
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Add request info
        dto.setPublicIp(getClientIp(request));
        dto.setUserAgent(request.getHeader("User-Agent"));
        
        // Process check-in
        AttendanceCheckInResponseDto response = verificationService.processCheckIn(user, dto);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<?> checkOut(
            @Valid @RequestBody AttendanceVerificationDto dto,
            Authentication authentication,
            HttpServletRequest request) {
        
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        dto.setPublicIp(getClientIp(request));
        dto.setUserAgent(request.getHeader("User-Agent"));
        
        AttendanceCheckInResponseDto response = verificationService.processCheckOut(user, dto);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/today-status")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<?> getTodayStatus(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        TodayAttendanceStatusDto status = verificationService.getTodayStatus(user);
        
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/weekly-stats")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<?> getWeeklyStats(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(verificationService.getWeeklyStats(user));
    }
    
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<?> getAttendanceHistory(
            Authentication authentication,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(verificationService.getAttendanceHistory(user, startDate, endDate));
    }
    
    @PostMapping("/verify-location")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<?> verifyLocation(@RequestBody Map<String, Double> locationData) {
        Double latitude = locationData.get("latitude");
        Double longitude = locationData.get("longitude");
        
        if (latitude == null || longitude == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Latitude and longitude are required"
            ));
        }
        
        return ResponseEntity.ok(verificationService.verifyLocationOnly(latitude, longitude));
    }
    
    @GetMapping("/logs")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<?> getVerificationLogs(
            Authentication authentication,
            @RequestParam(required = false) String date) {
        
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ResponseEntity.ok(verificationService.getVerificationLogs(user, date));
    }
    
    @GetMapping("/locations")
    @PreAuthorize("hasAnyRole('MANAGER', 'ACCOUNTANT')")
    public ResponseEntity<?> getCompanyLocations() {
        return ResponseEntity.ok(Map.of(
            "data", verificationService.getActiveLocations(),
            "message", "Danh sách địa điểm công ty"
        ));
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}