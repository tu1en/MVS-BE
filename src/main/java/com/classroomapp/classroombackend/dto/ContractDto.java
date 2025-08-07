package com.classroomapp.classroombackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String contractType; // "TEACHER", "ACCOUNTANT"
    private String position;
    private String department;
    private Double salary;
    private String workingHours;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // "ACTIVE", "EXPIRED", "TERMINATED"
    private String contractTerms;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String offer; // Thông tin offer từ Quản Lý Offer

    // --- CUSTOM FIELDS FOR VIETNAMESE CONTRACT ---
    private LocalDate birthDate;
    private String citizenId; // Số CCCD
    private String address;
    private String qualification;
    private String subject;
    private String educationLevel;
}
