package com.classroomapp.classroombackend.model.hrmanagement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import com.classroomapp.classroombackend.util.TopCVCalculation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple payroll result model using TopCV calculations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollResult {
    
    private Long userId;
    private String userName;
    private String userEmail;
    private String contractType;
    private YearMonth payrollPeriod;
    
    // Attendance data
    private Integer totalWorkingDays;
    private Integer actualWorkingDays;
    private Integer absentDays;
    private Integer actualWorkingHours; // tổng giờ làm thực tế (ước lượng)
    private Integer standardMonthlyHours; // tổng giờ công chuẩn trong tháng
    
    // Original contract salary
    private BigDecimal contractSalary;
    private String contractOffer;
    
    // Calculated salary (prorated based on attendance)
    private BigDecimal proratedGrossSalary;
    private BigDecimal netSalary;
    private BigDecimal hourlySalary; // đơn giá theo giờ (cho giáo viên)
    
    // TopCV calculation details
    private TopCVCalculation.SalaryCalculationResult topCVResult;
    
    // Status
    private String status; // "CALCULATED", "APPROVED", "PAID"
    private LocalDate calculatedAt;
    private Long calculatedBy; // Accountant user ID
    
    public PayrollResult(Long userId, String userName, YearMonth period, 
                        Integer totalDays, Integer actualDays,
                        BigDecimal contractSalary, BigDecimal proratedGross, BigDecimal netSalary,
                        TopCVCalculation.SalaryCalculationResult details) {
        this.userId = userId;
        this.userName = userName;
        this.payrollPeriod = period;
        this.totalWorkingDays = totalDays;
        this.actualWorkingDays = actualDays;
        this.absentDays = totalDays - actualDays;
        this.contractSalary = contractSalary;
        this.proratedGrossSalary = proratedGross;
        this.netSalary = netSalary;
        this.topCVResult = details;
        this.status = "CALCULATED";
        this.calculatedAt = LocalDate.now();
        // Giờ công mặc định (8h/ngày)
        this.actualWorkingHours = actualDays != null ? actualDays * 8 : null;
        this.standardMonthlyHours = totalDays != null ? totalDays * 8 : null;
    }
}