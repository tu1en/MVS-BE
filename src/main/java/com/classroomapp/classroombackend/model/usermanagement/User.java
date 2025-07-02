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
    private Long id;    @NotBlank
    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String username;

    @NotBlank
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String password;

    @NotBlank
    @Email
    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    @Column(name = "hire_date")
    private LocalDate hireDate;    @Column(columnDefinition = "NVARCHAR(100)")
    private String department;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "NVARCHAR(10) default 'active'")
    private String status = "active";
    
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
            default: return "USER";
        }
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
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
    public Integer getRoleId() { return roleId; }
    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public LocalDate getHireDate() { return hireDate; }
    public String getDepartment() { return department; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getStatus() { return status; }
}
