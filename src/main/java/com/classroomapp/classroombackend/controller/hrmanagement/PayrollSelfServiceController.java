package com.classroomapp.classroombackend.controller.hrmanagement;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.model.hrmanagement.PayrollResult;
import com.classroomapp.classroombackend.model.hrmanagement.PayrollViewConfirmation;
import com.classroomapp.classroombackend.repository.hrmanagement.PayrollViewConfirmationRepository;
import com.classroomapp.classroombackend.service.hrmanagement.PayrollGenerationService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/my/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll Self Service", description = "Employee endpoints to view payroll and confirm")
public class PayrollSelfServiceController {

    private final PayrollGenerationService payrollGenerationService;
    private final PayrollViewConfirmationRepository confirmationRepository;

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
}


