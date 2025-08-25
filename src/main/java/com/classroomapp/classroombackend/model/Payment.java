package com.classroomapp.classroombackend.model;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity for billing system
 */
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"invoice", "student"})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50, columnDefinition = "NVARCHAR(50)")
    private PaymentMethod paymentMethod;

    @Column(name = "reference_number", length = 100, columnDefinition = "NVARCHAR(100)")
    private String referenceNumber;

    @Nationalized
    @Column(name = "note", length = 500, columnDefinition = "NVARCHAR(500)")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private PaymentStatus status = PaymentStatus.COMPLETED;

    @Column(name = "receipt_id")
    private Long receiptId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "receipt_path", columnDefinition = "NVARCHAR(500)")
    private String receiptPath; // Path to uploaded receipt PDF file

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", insertable = false, updatable = false)
    @JsonBackReference
    private Invoice invoice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    @JsonBackReference
    private User student;

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
    }

    // Enums
    public enum PaymentMethod {
        CASH,           // Tiền mặt
        BANK_TRANSFER,  // Chuyển khoản
        CARD,           // Thẻ
        ONLINE          // Thanh toán online
    }

    public enum PaymentStatus {
        PENDING,        // Đang xử lý
        COMPLETED,      // Hoàn thành
        FAILED,         // Thất bại
        REFUNDED        // Đã hoàn trả
    }

    // Business logic methods
    
    /**
     * Get Vietnamese payment method name
     */
    public String getPaymentMethodVietnamese() {
        return switch (paymentMethod) {
            case CASH -> "Tiền mặt";
            case BANK_TRANSFER -> "Chuyển khoản";
            case CARD -> "Thẻ";
            case ONLINE -> "Thanh toán online";
        };
    }

    /**
     * Get Vietnamese payment status name
     */
    public String getPaymentStatusVietnamese() {
        return switch (status) {
            case PENDING -> "Đang xử lý";
            case COMPLETED -> "Hoàn thành";
            case FAILED -> "Thất bại";
            case REFUNDED -> "Đã hoàn trả";
        };
    }
}