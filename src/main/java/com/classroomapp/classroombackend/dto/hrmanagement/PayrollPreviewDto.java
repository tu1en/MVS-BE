package com.classroomapp.classroombackend.dto.hrmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollPreviewDto {
    private Long staffId;
    private String staffName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal baseSalary;
    private BigDecimal hourlyRate;
    private BigDecimal workingHours;
    private BigDecimal teachingHours;
    private BigDecimal grossPay;
    private BigDecimal deductions;
    private BigDecimal netPay;
    private List<DeductionDetailDto> deductionDetails;
    private List<WorkingHoursDetailDto> workingHoursDetails;
}