package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "requests")
@Data
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = true, length = 255)
    private String email;

    @Column(name = "full_name", nullable = true, length = 255)
    private String fullName;

    @Column(name = "phone_number", nullable = true, length = 20)
    private String phoneNumber;

    @Column(name = "requested_role", nullable = true, length = 50)
    private String requestedRole = "STUDENT"; // "TEACHER" or "STUDENT" - default to STUDENT

    @Column(columnDefinition = "TEXT", length = 4000, nullable = true)
    private String formResponses; // JSON string containing form responses

    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, COMPLETED

    @Column(name = "result_status", length = 50)
    private String resultStatus; // APPROVED, REJECTED, null

    @Column(columnDefinition = "TEXT", length = 4000, nullable = true)
    private String rejectReason;

    @Column(name = "created_at", nullable = true)
    private LocalDateTime createdAt;

    @Column(nullable = true)
    private LocalDateTime processedAt;

    public Request() {
        this.createdAt = LocalDateTime.now();
        this.requestedRole = "STUDENT";
        this.status = "PENDING";
        this.phoneNumber = "";
        this.email = "";
        this.fullName = "";
    }

    public Request(Long id, String email, String fullName, String phoneNumber, String requestedRole, String formResponses, String status, String rejectReason, LocalDateTime createdAt, LocalDateTime processedAt) {
        this.id = id;
        this.email = (email != null) ? email : "";
        this.fullName = (fullName != null) ? fullName : "";
        this.phoneNumber = (phoneNumber != null) ? phoneNumber : "";
        this.requestedRole = (requestedRole != null) ? requestedRole : "STUDENT";
        this.formResponses = formResponses;
        this.status = (status != null) ? status : "PENDING";
        this.rejectReason = rejectReason;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
        this.processedAt = processedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }
}