package com.classroomapp.classroombackend.model;

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
@Table(name = "requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = true, length = 255)
    private String fullName;

    @Column(nullable = true, length = 20)
    private String phoneNumber;

    @Column(length = 50)
    private String requestedRole = "STUDENT"; // "TEACHER" or "STUDENT" - default to STUDENT
    
    @Column(columnDefinition = "TEXT", length = 4000)
    private String formResponses; // JSON string containing form responses

    @Column(nullable = false, length = 50)    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(columnDefinition = "TEXT", length = 4000)
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