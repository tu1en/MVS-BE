package com.classroomapp.classroombackend.dto.hrmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollReportDto {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<PayrollRecordDto> payrollRecords;
    private PayrollSummaryDto summary;
    private List<ViolationImpactDto> violationImpacts;
    private LocalDate generatedDate;
}