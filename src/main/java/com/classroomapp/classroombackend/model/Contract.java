package com.classroomapp.classroombackend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "contract_id", unique = true, nullable = false, columnDefinition = "NVARCHAR(100)")
    private String contractId;

    @Column(name = "full_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(name = "email", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "phone_number", columnDefinition = "NVARCHAR(50)")
    private String phoneNumber;

    @Column(name = "contract_type", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String contractType; // "TEACHER", "ACCOUNTANT"

    @Column(name = "position", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String position;

    @Column(name = "department", columnDefinition = "NVARCHAR(255)")
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

    @Column(name = "working_hours", columnDefinition = "NVARCHAR(255)")
    private String workingHours;


    @Column(name = "status", nullable = false, columnDefinition = "NVARCHAR(50)")
    private String status; // "ACTIVE", "EXPIRED", "TERMINATED"

    @Column(name = "contract_terms", columnDefinition = "NVARCHAR(MAX)")
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

    @Column(name = "class_level", columnDefinition = "NVARCHAR(255)")
    private String classLevel; // Changed from educationLevel to classLevel (Lớp học)

    @Column(name = "comments", columnDefinition = "NVARCHAR(500)")
    private String comments; // Changed from evaluation to comments (Nhận xét)

    @Column(name = "work_schedule", columnDefinition = "NVARCHAR(500)")
    private String workSchedule; // New field: Thời gian làm việc (shifts and days)

    @Column(name = "work_shifts", columnDefinition = "NVARCHAR(255)")
    private String workShifts; // New field: Ca làm việc (morning, afternoon, evening)

    @Column(name = "work_days", columnDefinition = "NVARCHAR(255)")
    private String workDays; // New field: Ngày trong tuần (Monday, Tuesday, etc.)

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
