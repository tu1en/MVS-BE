package com.classroomapp.classroombackend.dto.absencemanagement;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TeacherLeaveInfoDTO {
    private Long userId;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String department;
    private Integer annualLeaveBalance;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate leaveResetDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate hireDate;
    
    private Integer usedLeave; // Calculated field - total days used this year
    private Integer pendingLeave; // Calculated field - total days pending approval
    private Integer overLimitDays; // Calculated field - days over the 12-day annual limit
} 