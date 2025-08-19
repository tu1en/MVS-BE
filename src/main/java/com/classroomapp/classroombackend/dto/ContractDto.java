package com.classroomapp.classroombackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractDto {
    private Long id;
    private Long userId;
    private String contractId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String contractType; // "TEACHER", "ACCOUNTANT"
    private String position;
    private String department;
    private Double salary;
    private String workingHours;
    private String status; // "ACTIVE", "EXPIRED", "TERMINATED"
    private String contractTerms;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String offer; // Thông tin offer từ Quản Lý Offer
    
    // --- OFFER MANAGEMENT FIELDS ---
    private String comments; // Changed from evaluation to comments (Nhận xét từ Quản lý Offer)
    private Long grossSalary; // Lương GROSS từ Quản lý Offer
    private Long netSalary; // Lương NET từ Quản lý Offer  
    private Long hourlySalary; // Lương theo giờ từ Quản lý Offer

    // --- CUSTOM FIELDS FOR VIETNAMESE CONTRACT ---
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;
    private String citizenId; // Số CCCD
    private String address;
    private String qualification;
    private String subject;
    private String classLevel; // Changed from educationLevel to classLevel (Lớp học)
    
    // --- NEW WORKING SCHEDULE FIELDS ---
    private String workSchedule; // Thời gian làm việc (combined schedule description)
    private String workShifts; // Ca làm việc (morning, afternoon, evening)
    private String workDays; // Ngày trong tuần (Monday, Tuesday, etc.)

    // --- CONTRACT DURATION FIELDS ---
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate; // Contract start date (persistent field)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate; // Contract expiry date (persistent field)
    
    // --- COMPUTED FIELD ---
    // Ngày bắt đầu hợp đồng (tự động tính theo buổi dạy đầu tiên của giáo viên)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate contractStartDate;
}
