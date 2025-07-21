package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "contracts")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "contract_type", nullable = false)
    private String contractType;

    @Column(name = "position", nullable = false)
    private String position;

    @Column(name = "department")
    private String department;

    @Column(name = "salary", nullable = false)
    private Double salary;

    @Column(name = "working_hours")
    private String workingHours;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, TERMINATED, EXPIRED

    @Column(name = "termination_reason")
    private String terminationReason;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "who_approved")
    private String whoApproved;

    @Column(name = "settlement_info", columnDefinition = "TEXT")
    private String settlementInfo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "attachment_path")
    private String attachmentPath;

    // Constructors
    public Contract() {
        this.createdAt = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    public Contract(Long userId, String contractType, String position, String department, 
                   Double salary, LocalDate startDate, String createdBy) {
        this();
        this.userId = userId;
        this.contractType = contractType;
        this.position = position;
        this.department = department;
        this.salary = salary;
        this.startDate = startDate;
        this.createdBy = createdBy;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public String getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTerminationReason() {
        return terminationReason;
    }

    public void setTerminationReason(String terminationReason) {
        this.terminationReason = terminationReason;
    }

    public LocalDate getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(LocalDate terminationDate) {
        this.terminationDate = terminationDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getAttachmentPath() {
        return attachmentPath;
    }

    public void setAttachmentPath(String attachmentPath) {
        this.attachmentPath = attachmentPath;
    }

    public String getWhoApproved() {
        return whoApproved;
    }

    public void setWhoApproved(String whoApproved) {
        this.whoApproved = whoApproved;
    }

    public String getSettlementInfo() {
        return settlementInfo;
    }

    public void setSettlementInfo(String settlementInfo) {
        this.settlementInfo = settlementInfo;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
