package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    // Constructors
    public AttendanceExplanation() {
    }

    public AttendanceExplanation(String submitterName, LocalDate absenceDate, String reason, LocalDateTime submittedAt, ExplanationStatus status, String department) {
        this.submitterName = submitterName;
        this.absenceDate = absenceDate;
        this.reason = reason;
        this.submittedAt = submittedAt;
        this.status = status;
        this.department = department;
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
}
