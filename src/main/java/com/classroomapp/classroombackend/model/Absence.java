package com.classroomapp.classroombackend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "absences")
@Data
@AllArgsConstructor
public class Absence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId; // Reference to Teacher user
    
    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;
    
    @Column(name = "user_full_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String userFullName;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;
    
    @Column(name = "number_of_days", nullable = false)
    private Integer numberOfDays;
    
    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String description; // Reason for leave
    
    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED
    
    @Column(name = "result_status", length = 50)
    private String resultStatus; // APPROVED, REJECTED, null
    
    @Column(columnDefinition = "NVARCHAR(MAX)", nullable = true)
    private String rejectReason;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "processed_at", nullable = true)
    private LocalDateTime processedAt;
    
    @Column(name = "processed_by", nullable = true)
    private Long processedBy; // Manager user ID who processed this request
    
    @Column(name = "is_over_limit", nullable = false)
    private Boolean isOverLimit = false; // Flag to indicate if this exceeds annual leave limit
    
    public Absence() {
        this.createdAt = LocalDateTime.now();
        this.status = "PENDING";
        this.isOverLimit = false;
    }
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getUserFullName() { return userFullName; }
    public void setUserFullName(String userFullName) { this.userFullName = userFullName; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getNumberOfDays() { return numberOfDays; }
    public void setNumberOfDays(Integer numberOfDays) { this.numberOfDays = numberOfDays; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getIsOverLimit() { return isOverLimit; }
    public void setIsOverLimit(Boolean isOverLimit) { this.isOverLimit = isOverLimit; }
    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    public Long getId() { return id; }
} 