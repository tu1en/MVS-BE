package com.classroomapp.classroombackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String email;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @Column(nullable = false, columnDefinition = "NVARCHAR(20)")
    private String phoneNumber;

    @Column(nullable = false, columnDefinition = "NVARCHAR(50)")
    private String requestedRole; // "TEACHER" or "STUDENT"

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String formResponses; // JSON string containing form responses

    @Column(nullable = false, columnDefinition = "NVARCHAR(50)")
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String rejectReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 