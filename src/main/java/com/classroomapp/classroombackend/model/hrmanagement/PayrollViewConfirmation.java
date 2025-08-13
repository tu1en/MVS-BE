package com.classroomapp.classroombackend.model.hrmanagement;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "payroll_view_confirmations")
@Data
public class PayrollViewConfirmation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    // Store YearMonth as string "YYYY-MM" for simplicity
    private String period;

    private LocalDateTime confirmedAt;
}


