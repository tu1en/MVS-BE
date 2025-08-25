package com.classroomapp.classroombackend.controller.hrmanagement;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import com.classroomapp.classroombackend.model.hrmanagement.PayrollResult;
import com.classroomapp.classroombackend.model.hrmanagement.PayrollViewConfirmation;
import com.classroomapp.classroombackend.repository.AttendanceLogRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.PayrollViewConfirmationRepository;
import com.classroomapp.classroombackend.service.hrmanagement.PayrollGenerationService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Slf4j
@RestController
@RequestMapping("/api/my/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll Self Service", description = "Employee endpoints to view payroll and confirm")
public class PayrollSelfServiceController {

    private final PayrollGenerationService payrollGenerationService;
    private final PayrollViewConfirmationRepository confirmationRepository;
    private final AttendanceLogRepository attendanceLogRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('STAFF','TEACHER','ACCOUNTANT','MANAGER','ADMIN')")
    public ResponseEntity<PayrollResult> viewMyPayroll(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period) {
        try {
            PayrollResult result = payrollGenerationService.generatePayrollForUser(userId, period);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            // Không có hợp đồng hoặc user không tồn tại → trả 204 để FE hiển thị "Không có dữ liệu"
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            // Lỗi khác → tránh 500 trống, trả 400 để FE có thể hiển thị thông báo thân thiện
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/test")
    public ResponseEntity<PayrollResult> testPayroll(
            @RequestParam(defaultValue = "2") Long userId,
            @RequestParam(defaultValue = "2025-08") @DateTimeFormat(pattern = "yyyy-MM") YearMonth period) {
        try {
            PayrollResult result = payrollGenerationService.generatePayrollForUser(userId, period);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException ex) {
            // Không có hợp đồng hoặc user không tồn tại → trả 204 để FE hiển thị "Không có dữ liệu"
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            // Lỗi khác → tránh 500 trống, trả 400 để FE có thể hiển thị thông báo thân thiện
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/test-all-teachers")
    public ResponseEntity<List<PayrollResult>> testAllTeachersPayroll() {
        try {
            // Test với tất cả giáo viên có dữ liệu
            List<Long> teacherIds = Arrays.asList(2L, 6L, 7L, 14L, 15L, 16L);
            List<PayrollResult> results = new ArrayList<>();

            for (Long teacherId : teacherIds) {
                try {
                    PayrollResult result = payrollGenerationService.generatePayrollForUser(teacherId, YearMonth.of(2025, 8));
                    results.add(result);
                } catch (Exception e) {
                    log.warn("Failed to generate payroll for teacher {}: {}", teacherId, e.getMessage());
                }
            }

            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("Error in test all teachers payroll: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('STAFF','TEACHER','ACCOUNTANT','MANAGER','ADMIN')")
    public ResponseEntity<Map<String, Object>> confirmViewed(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period) {
        String periodStr = period.toString();
        PayrollViewConfirmation c = confirmationRepository
                .findByUserIdAndPeriod(userId, periodStr)
                .orElseGet(PayrollViewConfirmation::new);
        c.setUserId(userId);
        c.setPeriod(periodStr);
        c.setConfirmedAt(java.time.LocalDateTime.now());
        confirmationRepository.save(c);
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "CONFIRMED");
        resp.put("confirmedAt", c.getConfirmedAt());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/debug-attendance")
    public ResponseEntity<Map<String, Object>> debugAttendance() {
        try {
            Map<String, Object> debug = new HashMap<>();

            // Lấy tất cả attendance logs
            List<com.classroomapp.classroombackend.model.AttendanceLog> allLogs =
                attendanceLogRepository.findAll();

            debug.put("totalAttendanceLogs", allLogs.size());
            debug.put("sampleLogs", allLogs.stream().limit(5).map(log -> {
                Map<String, Object> logInfo = new HashMap<>();
                logInfo.put("id", log.getId());
                logInfo.put("userId", log.getUserId());
                logInfo.put("userName", log.getUserName());
                logInfo.put("date", log.getDate());
                logInfo.put("checkIn", log.getCheckIn());
                logInfo.put("checkOut", log.getCheckOut());
                logInfo.put("status", log.getStatus());
                logInfo.put("role", log.getRole());
                return logInfo;
            }).collect(java.util.stream.Collectors.toList()));

            // Kiểm tra dữ liệu cho user 2 cụ thể
            List<com.classroomapp.classroombackend.model.AttendanceLog> user2Logs =
                attendanceLogRepository.findByUserIdAndDateBetween(2L,
                    java.time.LocalDate.of(2025, 8, 1),
                    java.time.LocalDate.of(2025, 8, 31));

            debug.put("user2LogsCount", user2Logs.size());
            debug.put("user2Logs", user2Logs.stream().map(log -> {
                Map<String, Object> logInfo = new HashMap<>();
                logInfo.put("id", log.getId());
                logInfo.put("userId", log.getUserId());
                logInfo.put("date", log.getDate());
                logInfo.put("checkIn", log.getCheckIn());
                logInfo.put("checkOut", log.getCheckOut());
                logInfo.put("status", log.getStatus());
                return logInfo;
            }).collect(java.util.stream.Collectors.toList()));

            return ResponseEntity.ok(debug);
        } catch (Exception e) {
            log.error("Error in debug attendance: {}", e.getMessage(), e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}


