package com.classroomapp.classroombackend.dto.hrmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollSummaryDto {
    private Long totalRecords;
    private BigDecimal totalGrossPay;
    private BigDecimal totalNetPay;
    private BigDecimal totalDeductions;
    private BigDecimal totalWorkingHours;
    private Long draftRecords;
    private Long processedRecords;
    private Long paidRecords;
}