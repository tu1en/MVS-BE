package com.classroomapp.classroombackend.model;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Invoice entity for billing system
 */
@Entity
@Table(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"student", "items", "payments"})
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.PENDING;

    @Nationalized
    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "document_path")
    private String documentPath; // Path to uploaded invoice PDF file

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    @JsonBackReference
    private User student;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<InvoiceItem> items;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Payment> payments;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic methods
    
    /**
     * Calculate remaining amount to be paid
     */
    public BigDecimal getRemainingAmount() {
        return totalAmount.subtract(paidAmount);
    }

    /**
     * Check if invoice is fully paid
     */
    public boolean isFullyPaid() {
        return paidAmount.compareTo(totalAmount) >= 0;
    }

    /**
     * Check if invoice is overdue
     */
    public boolean isOverdue() {
        return dueDate.isBefore(LocalDate.now()) && !isFullyPaid();
    }

    /**
     * Update invoice status based on payment amount
     */
    public void updateStatus() {
        if (isFullyPaid()) {
            this.status = InvoiceStatus.PAID;
        } else if (paidAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.status = InvoiceStatus.PARTIAL;
        } else if (isOverdue()) {
            this.status = InvoiceStatus.OVERDUE;
        } else {
            this.status = InvoiceStatus.PENDING;
        }
    }

    public enum InvoiceStatus {
        PENDING,    // Chờ thanh toán
        PAID,       // Đã thanh toán
        PARTIAL,    // Thanh toán một phần
        OVERDUE,    // Quá hạn
        CANCELLED   // Đã hủy
    }
}