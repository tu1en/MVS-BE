package com.classroomapp.classroombackend.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "attendance_explanations")
public class AttendanceExplanation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "submitter_name", nullable = false)
    private String submitterName;

    @Column(name = "absence_date", nullable = false)
    private LocalDate absenceDate;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExplanationStatus status;

    @Column(name = "approver_name")
    private String approverName;

    @Column(name = "department")
    private String department;

    // ✅ ADD missing field that database expects
    @Column(name = "explanation_text", nullable = false)
    private String explanationText;

    // ✅ ADD violation_id field to match database schema
    @Column(name = "violation_id", nullable = false)
    private Long violationId = 1L; // Default value to satisfy NOT NULL constraint

    // Constructors
    public AttendanceExplanation() {
        this.violationId = 1L; // Ensure default value
        this.explanationText = ""; // Default empty string
    }

    public AttendanceExplanation(String submitterName, LocalDate absenceDate, String reason, LocalDateTime submittedAt, ExplanationStatus status, String department) {
        this.submitterName = submitterName;
        this.absenceDate = absenceDate;
        this.reason = reason;
        this.submittedAt = submittedAt;
        this.status = status;
        this.department = department;
        this.violationId = 1L; // Default value
        this.explanationText = reason; // Use reason as explanation text by default
    }

    // ✅ Constructor with explanationText
    public AttendanceExplanation(String submitterName, LocalDate absenceDate, String reason, String explanationText, LocalDateTime submittedAt, ExplanationStatus status, String department) {
        this.submitterName = submitterName;
        this.absenceDate = absenceDate;
        this.reason = reason;
        this.explanationText = explanationText;
        this.submittedAt = submittedAt;
        this.status = status;
        this.department = department;
        this.violationId = 1L; // Default value
    }

    // Existing getters and setters...
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
        // ✅ Auto-update explanationText when reason changes if explanationText is empty
        if (this.explanationText == null || this.explanationText.isEmpty()) {
            this.explanationText = reason;
        }
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
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

    // ✅ NEW getter and setter for explanationText
    public String getExplanationText() {
        return explanationText;
    }

    public void setExplanationText(String explanationText) {
        this.explanationText = explanationText;
    }

    // Thêm 2 field quản lý thời gian
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    // ✅ NEW getter and setter for violationId
    public Long getViolationId() {
        return violationId;
    }

    public void setViolationId(Long violationId) {
        this.violationId = violationId;
    }
        // Hibernate callback
        @PrePersist
        protected void onCreate() {
            LocalDateTime now = LocalDateTime.now();
            this.createdAt = now;
            this.updatedAt = now;
        }
    
        @PreUpdate
        protected void onUpdate() {
            this.updatedAt = LocalDateTime.now();
        }
}