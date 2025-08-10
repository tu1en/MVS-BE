package com.classroomapp.classroombackend.model.hrmanagement;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.classroomapp.classroombackend.util.TopCVCalculation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

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
    
    // Original contract salary
    private BigDecimal contractSalary;
    private String contractOffer;
    
    // Calculated salary (prorated based on attendance)
    private BigDecimal proratedGrossSalary;
    private BigDecimal netSalary;
    
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
    }
}