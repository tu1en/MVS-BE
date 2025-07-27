package com.classroomapp.classroombackend.dto.hrmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPayrollStatsDto {
    private Integer year;
    private Integer month;
    private Long totalStaff;
    private BigDecimal totalGrossPay;
    private BigDecimal totalNetPay;
    private BigDecimal totalDeductions;
    private BigDecimal averageGrossPay;
    private BigDecimal averageNetPay;
    private BigDecimal totalWorkingHours;
    private BigDecimal averageWorkingHours;
}