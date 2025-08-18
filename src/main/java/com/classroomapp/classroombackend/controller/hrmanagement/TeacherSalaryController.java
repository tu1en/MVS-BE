package com.classroomapp.classroombackend.controller.hrmanagement;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.service.impl.hrmanagement.TeacherSalaryCalculationService;
import com.classroomapp.classroombackend.service.impl.hrmanagement.TeacherSalaryCalculationService.TeacherSalaryResult;

import lombok.extern.slf4j.Slf4j;

/**
 * Controller để test service tính lương giáo viên từ lịch sử giảng dạy
 */
@RestController
@RequestMapping("/api/teacher-salary")
@Slf4j
public class TeacherSalaryController {

    @Autowired
    private TeacherSalaryCalculationService teacherSalaryCalculationService;
    
    @Autowired
    private ContractRepository contractRepository;

    /**
     * Tính lương giáo viên theo tháng
     * @param teacherId ID giáo viên
     * @param year Năm
     * @param month Tháng
     * @return Kết quả tính lương
     */
    @GetMapping("/calculate/{teacherId}")
    public ResponseEntity<TeacherSalaryResult> calculateTeacherSalary(
            @PathVariable Long teacherId,
            @RequestParam int year,
            @RequestParam int month) {
        
        try {
            log.info("🔄 Calculating salary for teacher {} for {}-{}", teacherId, year, month);
            
            // Tìm hợp đồng của giáo viên
            Contract contract = contractRepository.findActiveContractByUserId(teacherId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng active cho giáo viên: " + teacherId));
            
            // Xác định kỳ lương
            LocalDate periodStart = LocalDate.of(year, month, 1);
            LocalDate periodEnd = periodStart.plusMonths(1).minusDays(1);
            
            log.info("📅 Period: {} to {}", periodStart, periodEnd);
            
            // Tính lương
            TeacherSalaryResult result = teacherSalaryCalculationService.calculateSalaryFromTeachingHistory(
                    teacherId, periodStart, periodEnd, contract);
            
            log.info("✅ Salary calculated: {} VND for {} hours", 
                    result.getTotalSalary(), result.getTotalTeachingHours());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ Error calculating salary for teacher {}: {}", teacherId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Tính lương giáo viên theo khoảng thời gian tùy chỉnh
     * @param teacherId ID giáo viên
     * @param startDate Ngày bắt đầu (format: yyyy-MM-dd)
     * @param endDate Ngày kết thúc (format: yyyy-MM-dd)
     * @return Kết quả tính lương
     */
    @GetMapping("/calculate-custom/{teacherId}")
    public ResponseEntity<TeacherSalaryResult> calculateTeacherSalaryCustomPeriod(
            @PathVariable Long teacherId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        try {
            log.info("🔄 Calculating salary for teacher {} from {} to {}", teacherId, startDate, endDate);
            
            // Tìm hợp đồng của giáo viên
            Contract contract = contractRepository.findActiveContractByUserId(teacherId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hợp đồng active cho giáo viên: " + teacherId));
            
            // Tính lương
            TeacherSalaryResult result = teacherSalaryCalculationService.calculateSalaryFromTeachingHistory(
                    teacherId, startDate, endDate, contract);
            
            log.info("✅ Salary calculated: {} VND for {} hours", 
                    result.getTotalSalary(), result.getTotalTeachingHours());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("❌ Error calculating salary for teacher {}: {}", teacherId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Lấy thông tin hợp đồng của giáo viên
     * @param teacherId ID giáo viên
     * @return Thông tin hợp đồng
     */
    @GetMapping("/contract/{teacherId}")
    public ResponseEntity<Contract> getTeacherContract(@PathVariable Long teacherId) {
        try {
            Contract contract = contractRepository.findActiveContractByUserId(teacherId)
                    .orElse(null);
            
            if (contract == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(contract);
            
        } catch (Exception e) {
            log.error("❌ Error getting contract for teacher {}: {}", teacherId, e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
