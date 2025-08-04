package com.classroomapp.classroombackend.dto.hrmanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHoursDetailDto {
    private LocalDate date;
    private String shiftName;
    private BigDecimal expectedHours;
    private BigDecimal actualHours;
    private String attendanceStatus;
}