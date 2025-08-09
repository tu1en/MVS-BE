package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "contract_id", unique = true, nullable = false)
    private String contractId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "contract_type", nullable = false)
    private String contractType; // "TEACHER", "ACCOUNTANT"

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "department")
    private String department;

    @Column(name = "salary", nullable = false)
    private Double salary;

    // Separate salary fields from Offer Management
    @Column(name = "gross_salary")
    private Long grossSalary; // Lương GROSS từ Quản lý Offer

    @Column(name = "net_salary")
    private Long netSalary; // Lương NET từ Quản lý Offer

    @Column(name = "hourly_salary")
    private Long hourlySalary; // Lương theo giờ từ Quản lý Offer

    @Column(name = "working_hours")
    private String workingHours;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", nullable = false)
    private String status; // "ACTIVE", "EXPIRED", "TERMINATED"

    @Column(name = "contract_terms", columnDefinition = "TEXT")
    private String contractTerms;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // --- CUSTOM FIELDS FOR VIETNAMESE CONTRACT ---
    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "citizen_id", length = 20)
    private String citizenId; // Số CCCD

    @Column(name = "address", columnDefinition = "NVARCHAR(255)")
    private String address;

    @Column(name = "qualification", columnDefinition = "NVARCHAR(255)")
    private String qualification;

    @Column(name = "subject", columnDefinition = "NVARCHAR(255)")
    private String subject;

    @Column(name = "education_level", columnDefinition = "NVARCHAR(255)")
    private String educationLevel;

    @Column(name = "offer", columnDefinition = "NVARCHAR(255)")
    private String offer;

    public String getOffer() {
        return offer;
    }

    public void setOffer(String offer) {
        this.offer = offer;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
