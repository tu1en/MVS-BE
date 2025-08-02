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
    private String contractType; // "TEACHER", "STAFF"
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
}
