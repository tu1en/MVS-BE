package com.classroomapp.classroombackend.model.usermanagement;

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
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String password;

    @NotBlank
    @Email
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)", unique = true)
    private String email;

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String department;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "NVARCHAR(10) default 'active'")
    private String status = "active";
    
    // Leave management fields for Teachers
    @Column(name = "annual_leave_balance", nullable = true)
    private Integer annualLeaveBalance = 12; // Default 12 days per year for teachers
    
    @Column(name = "leave_reset_date", nullable = true)
    private LocalDate leaveResetDate; // Date when annual leave resets (hire anniversary)
    
    /**
     * Get the role name as String based on the roleId
     *
     * @return String representation of the user's role
     */
    public String getRole() {
        if (roleId == null) return "USER";

        switch (roleId) {
            case 1: return "STUDENT";
            case 2: return "TEACHER";
            case 3: return "MANAGER";
            case 4: return "ADMIN";
            case 5: return "ACCOUNTANT";
            default: return "USER";
        }
    }

    /**
     * Check if user is eligible for shift assignment
     * Users are eligible if they are staff members (TEACHER, MANAGER, ADMIN) and have active status
     *
     * @return true if user can be assigned to shifts, false otherwise
     */
    public boolean isEligibleForShiftAssignment() {
        // Check if user has active status
        if (!"active".equalsIgnoreCase(status)) {
            return false;
        }

        // Check if user is staff (not student)
        if (roleId == null) {
            return false;
        }

        // Only TEACHER (2), MANAGER (3), and ADMIN (4) can be assigned shifts
        return roleId >= 2 && roleId <= 4;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        // Set leave reset date cho giáo viên hoặc kế toán viên (1 năm kể từ bây giờ)
        if (roleId != null && (roleId == 2 || roleId == 5) && leaveResetDate == null) { // TEACHER hoặc ACCOUNTANT
            leaveResetDate = LocalDate.now().plusYears(1);
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Explicit getters to resolve compilation issues
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public Integer getRoleId() { return roleId; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public LocalDate getHireDate() { return hireDate; }
    public String getDepartment() { return department; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getStatus() { return status; }
    
    // Explicit setters to resolve compilation issues
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setRoleId(Integer roleId) { this.roleId = roleId; }
    public void setRoleId(int roleId) { this.roleId = roleId; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public void setDepartment(String department) { this.department = department; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAnnualLeaveBalance() { return annualLeaveBalance; }
    public void setAnnualLeaveBalance(Integer annualLeaveBalance) { this.annualLeaveBalance = annualLeaveBalance; }
    public LocalDate getLeaveResetDate() { return leaveResetDate; }
    public void setLeaveResetDate(LocalDate leaveResetDate) { this.leaveResetDate = leaveResetDate; }
}
