package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entity representing parent billing access permissions
 * Based on PARENT_ROLE_SPEC.md requirements for financial transparency
 */
@Entity
@Table(name = "parent_billing_access")
@Data
@NoArgsConstructor
@ToString(exclude = {"parent", "student"})
public class ParentBillingAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "can_view_invoices")
    private Boolean canViewInvoices = true;

    @Column(name = "can_view_payments")
    private Boolean canViewPayments = true;

    @Column(name = "can_make_payments")
    private Boolean canMakePayments = false; // For future online payment integration

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Relationships

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @JsonBackReference
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    @JsonBackReference
    private User student;

    // Constructors

    public ParentBillingAccess(Long parentId, Long studentId) {
        this.parentId = parentId;
        this.studentId = studentId;
        this.canViewInvoices = true;
        this.canViewPayments = true;
        this.canMakePayments = false;
        this.createdAt = LocalDateTime.now();
    }

    public ParentBillingAccess(Long parentId, Long studentId, 
                              Boolean canViewInvoices, Boolean canViewPayments, Boolean canMakePayments) {
        this.parentId = parentId;
        this.studentId = studentId;
        this.canViewInvoices = canViewInvoices != null ? canViewInvoices : true;
        this.canViewPayments = canViewPayments != null ? canViewPayments : true;
        this.canMakePayments = canMakePayments != null ? canMakePayments : false;
        this.createdAt = LocalDateTime.now();
    }

    // Lifecycle callbacks

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Business logic methods

    /**
     * Check if parent can view invoices for this student
     */
    public boolean canViewInvoices() {
        return Boolean.TRUE.equals(this.canViewInvoices);
    }

    /**
     * Check if parent can view payments for this student
     */
    public boolean canViewPayments() {
        return Boolean.TRUE.equals(this.canViewPayments);
    }

    /**
     * Check if parent can make payments for this student
     */
    public boolean canMakePayments() {
        return Boolean.TRUE.equals(this.canMakePayments);
    }

    /**
     * Grant full billing access
     */
    public void grantFullAccess() {
        this.canViewInvoices = true;
        this.canViewPayments = true;
        this.canMakePayments = true;
    }

    /**
     * Grant view-only access
     */
    public void grantViewOnlyAccess() {
        this.canViewInvoices = true;
        this.canViewPayments = true;
        this.canMakePayments = false;
    }

    /**
     * Revoke all billing access
     */
    public void revokeAccess() {
        this.canViewInvoices = false;
        this.canViewPayments = false;
        this.canMakePayments = false;
    }

    /**
     * Check if has any billing access
     */
    public boolean hasAnyAccess() {
        return canViewInvoices() || canViewPayments() || canMakePayments();
    }

    /**
     * Get access level description
     */
    public String getAccessLevelDescription() {
        if (!hasAnyAccess()) {
            return "Không có quyền truy cập";
        }
        
        if (canMakePayments()) {
            return "Toàn quyền (xem và thanh toán)";
        }
        
        if (canViewInvoices() && canViewPayments()) {
            return "Chỉ xem (hóa đơn và thanh toán)";
        }
        
        if (canViewInvoices()) {
            return "Chỉ xem hóa đơn";
        }
        
        if (canViewPayments()) {
            return "Chỉ xem thanh toán";
        }
        
        return "Quyền hạn chế";
    }
}