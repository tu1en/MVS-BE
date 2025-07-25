package com.classroomapp.classroombackend.dto.hrmanagement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.hrmanagement.PayrollRecord;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for PayrollRecord entity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRecordDto {
    
    private Long id;
    
    private Long staffId;
    private String staffName;
    private String staffEmail;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate payPeriodStart;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate payPeriodEnd;
    
    private BigDecimal totalWorkingHours;
    private BigDecimal totalTeachingHours;
    private BigDecimal baseSalary;
    private BigDecimal hourlyRate;
    private BigDecimal totalDeductions;
    private BigDecimal taxDeduction;
    private BigDecimal insuranceDeduction;
    private BigDecimal grossPay;
    private BigDecimal netPay;
    
    private PayrollRecord.PayrollStatus status;
    
    private Long generatedBy;
    private String generatedByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime generatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime processedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paidAt;
    
    // // Additional fields for display purposes
    // private Integer violationCount;
    // private String periodDescription;
    // private BigDecimal deductionPercentage;
    // private String statusDisplayName;
    
    // Helper methods
    public String getPeriodDescription() {
        if (payPeriodStart != null && payPeriodEnd != null) {
            return payPeriodStart.toString() + " to " + payPeriodEnd.toString();
        }
        return "";
    }
    
    public BigDecimal getDeductionPercentage() {
        if (grossPay != null && grossPay.compareTo(BigDecimal.ZERO) > 0 && totalDeductions != null) {
            return totalDeductions.divide(grossPay, 4, BigDecimal.ROUND_HALF_UP)
                   .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }
    
    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "";
    }
}