package com.classroomapp.classroombackend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "attendance_explanations")
public class AttendanceExplanation {
    
    @ManyToOne
    @JoinColumn(name = "staff_id", nullable = false)
    private User staff;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submitter_name", nullable = false)
    private String submitterName;

    @Column(name = "absence_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate absenceDate;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "submitted_at", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submittedAt;
@Column(name = "violation_id")
private Long violationId;

    // ✅ THÊM FIELD UPDATED_AT - ĐÂY LÀ NGUYÊN NHÂN GÂY LỖI
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExplanationStatus status;

    @Column(name = "approver_name")
    private String approverName;

    @Column(name = "department")
    private String department;
    @Column(name = "explanation_text", columnDefinition = "TEXT")
    private String explanationText;

    // ✅ THÊM LIFECYCLE CALLBACKS ĐỂ TỰ ĐỘNG SET TIMESTAMPS
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (submittedAt == null) {
            submittedAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public AttendanceExplanation() {
    }

    public AttendanceExplanation(String submitterName, LocalDate absenceDate, String reason, 
                               LocalDateTime submittedAt, ExplanationStatus status, String department) {
        this.submitterName = submitterName;
        this.absenceDate = absenceDate;
        this.reason = reason;
        this.submittedAt = submittedAt;
        this.status = status;
        this.department = department;
        this.updatedAt = LocalDateTime.now(); // Set updatedAt in constructor
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubmitterName() {
        return submitterName;
    }

    public void setSubmitterName(String submitterName) {
        this.submitterName = submitterName;
    }

    public LocalDate getAbsenceDate() {
        return absenceDate;
    }

    public void setAbsenceDate(LocalDate absenceDate) {
        this.absenceDate = absenceDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    // ✅ THÊM GETTER/SETTER CHO UPDATED_AT
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public ExplanationStatus getStatus() {
        return status;
    }

    public void setStatus(ExplanationStatus status) {
        this.status = status;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getExplanationText() {
        return explanationText;
    }

    public void setExplanationText(String explanationText) {
        this.explanationText = explanationText;
    }

    // Getter & Setter for Staff
    public User getStaff() {
        return staff;
    }
    
public Long getViolationId() {
    return violationId;
}

public void setViolationId(Long violationId) {
    this.violationId = violationId;
}

    public void setStaff(User staff) {
        this.staff = staff;
    }
}