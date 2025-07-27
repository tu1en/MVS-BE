package com.classroomapp.classroombackend.model.usermanagement;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostLoad;
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

    // Add ManyToOne relationship with Role entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_enum", length = 20)
    private RoleEnum roleEnum;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted = false;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String department;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "NVARCHAR(32) default 'active'")
    private String status = "active";
    
    // Leave management fields for Teachers
    @Column(name = "annual_leave_balance", nullable = true)
    private Integer annualLeaveBalance = 12; // Default 12 days per year for teachers
    
    @Column(name = "leave_reset_date", nullable = true)
    private LocalDate leaveResetDate; // Date when annual leave resets (hire anniversary)
    
    /**
     * Get the role name as String based on the roleId
     * This method works even if Role entity is not loaded
     * 
     * @return String representation of the user's role
     */
    public String getRole() {
        // First try to get from Role entity if loaded
        if (role != null && role.getName() != null) {
            return role.getName();
        }
        
        // Fallback to roleId mapping
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
     * Get Role entity (may be null if not loaded)
     */
    public Role getRoleEntity() {
        return role;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        
        if (isDeleted == null) {
            isDeleted = false;
        }
        
        // Sync role_enum from role_id if needed
        syncRoleEnum();
        
        // Set leave reset date cho giáo viên hoặc kế toán viên (1 năm kể từ bây giờ)
        if (roleId != null && (roleId == 2 || roleId == 5) && leaveResetDate == null) { // TEACHER hoặc ACCOUNTANT
            leaveResetDate = LocalDate.now().plusYears(1);
        }
    }

    @PostLoad
    public void syncRoleEnum() {
        if (roleEnum == null && roleId != null) {
            roleEnum = RoleEnum.fromRoleId(roleId);
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
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public void setDepartment(String department) { this.department = department; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAnnualLeaveBalance() { return annualLeaveBalance; }
    public void setAnnualLeaveBalance(Integer annualLeaveBalance) { this.annualLeaveBalance = annualLeaveBalance; }
    public LocalDate getLeaveResetDate() { return leaveResetDate; }
    public void setLeaveResetDate(LocalDate leaveResetDate) { this.leaveResetDate = leaveResetDate; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    public void setRoleEnum(RoleEnum roleEnum) { this.roleEnum = roleEnum; }
    public void setRole(Role role) { this.role = role; }

    public void setDeleted(boolean deleted) { 
        this.isDeleted = deleted; 
    }
    
    public boolean isDeleted() { 
        return isDeleted != null ? isDeleted : false; 
    }

    // Additional methods required by other services
    public enum RoleEnum {
        STUDENT(1, "STUDENT"),
        TEACHER(2, "TEACHER"), 
        MANAGER(3, "MANAGER"),
        ADMIN(4, "ADMIN"),
        ACCOUNTANT(5, "ACCOUNTANT");

        private final int id;
        private final String name;

        RoleEnum(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public int toRoleId() { return id; }
        
        public static RoleEnum fromRoleId(Integer roleId) {
            if (roleId == null) return STUDENT;
            
            switch (roleId) {
                case 1: return STUDENT;
                case 2: return TEACHER;
                case 3: return MANAGER;
                case 4: return ADMIN;
                case 5: return ACCOUNTANT;
                default: return STUDENT;
            }
        }
    }

    public RoleEnum getRoleEnum() {
        if (roleId == null) return null;
        
        switch (roleId) {
            case 1: return RoleEnum.STUDENT;
            case 2: return RoleEnum.TEACHER;
            case 3: return RoleEnum.MANAGER;
            case 4: return RoleEnum.ADMIN;
            case 5: return RoleEnum.ACCOUNTANT;
            default: return null;
        }
    }

    public boolean isEligibleForShiftAssignment() {
        return roleId != null && (roleId == 2 || roleId == 5); // TEACHER or ACCOUNTANT
    }

    public Long getDepartmentId() {
        // Return hardcoded department ID based on role for now
        if (roleId == null) return null;
        
        switch (roleId) {
            case 2: return 1L; // Teachers -> IT Department  
            case 5: return 2L; // Accountants -> Finance Department
            default: return 1L; // Default department
        }
    }
}