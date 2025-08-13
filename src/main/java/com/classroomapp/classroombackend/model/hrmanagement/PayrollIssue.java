package com.classroomapp.classroombackend.model.hrmanagement;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "payroll_issues")
@Data
public class PayrollIssue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String period; // YYYY-MM
    private String subject;
    private String description;
    private String attachmentUrl; // optional
    private String status; // OPEN, IN_PROGRESS, RESOLVED

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


