package com.classroomapp.classroombackend.model.usermanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.Nationalized;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Nationalized
    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "gender", length = 16)
    private String gender; // MALE, FEMALE, OTHER (nullable)

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Nationalized
    @Column(columnDefinition = "NVARCHAR(100)")
    private String department;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "eligible_for_shift_assignment")
    private boolean eligibleForShiftAssignment = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Nationalized
    @Column(columnDefinition = "NVARCHAR(32) default 'active'")
    private String status = "active";

    @Column(name = "annual_leave_balance", nullable = true)
    private Integer annualLeaveBalance = 12;

    @Column(name = "leave_reset_date", nullable = true)
    private LocalDate leaveResetDate;

    // Parent information (for students)
    @Column(name = "parent_phone", length = 20)
    private String parentPhone;

    @Nationalized
    @Column(name = "parent_name", columnDefinition = "NVARCHAR(255)")
    private String parentName;

    // Student-specific fields
    @Column(name = "birth_date")
    private LocalDate birthDate; // For students, stored directly in User entity

    @Nationalized
    @Column(name = "school", columnDefinition = "NVARCHAR(255)")
    private String school;

    // Additional personal information fields
    @Column(name = "citizen_id", length = 12)
    private String citizenId; // 12-digit CCCD number

    @Nationalized
    @Column(name = "address", columnDefinition = "NVARCHAR(500)")
    private String address;

    /**
     * Get the role name as String based on the roleId
     */
    public String getRole() {
        if (roleId == null) return "USER";
        return switch (roleId) {
            case 1 -> "STUDENT";
            case 2 -> "TEACHER";
            case 3 -> "MANAGER";
            case 4 -> "ADMIN";
            case 5 -> "ACCOUNTANT";
            case 6 -> "TEACHING_ASSISTANT";
            case 7 -> "PARENT";
            default -> "USER";
        };
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
        if (roleId != null && (roleId == 2 || roleId == 5) && leaveResetDate == null) {
            leaveResetDate = LocalDate.now().plusYears(1);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Nếu IDE hoặc framework yêu cầu explicit getter/setter, bạn có thể giữ lại bên dưới:
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public boolean isEligibleForShiftAssignment() { return eligibleForShiftAssignment; }
    public void setEligibleForShiftAssignment(boolean eligible) { this.eligibleForShiftAssignment = eligible; }
}
