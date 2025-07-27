package com.classroomapp.classroombackend.model.hrmanagement;

import com.classroomapp.classroombackend.model.usermanagement.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity for Payroll Records - Monthly salary calculations
 * Based on guild.md specifications
 */
@Entity
@Table(name = "payroll_records",
       indexes = {
           @Index(name = "idx_payroll_staff_period", columnList = "staff_id, pay_period_start, pay_period_end"),
           @Index(name = "idx_payroll_status", columnList = "status"),
           @Index(name = "idx_payroll_period", columnList = "pay_period_start, pay_period_end")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_payroll_unique", 
                           columnNames = {"staff_id", "pay_period_start", "pay_period_end"})
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_payroll_staff"))
    private User staff;
    
    @Column(name = "pay_period_start", nullable = false)
    private LocalDate payPeriodStart;
    
    @Column(name = "pay_period_end", nullable = false)
    private LocalDate payPeriodEnd;
    
    @Column(name = "total_working_hours", precision = 6, scale = 2)
    private BigDecimal totalWorkingHours = BigDecimal.ZERO;
    
    @Column(name = "total_teaching_hours", precision = 6, scale = 2)
    private BigDecimal totalTeachingHours = BigDecimal.ZERO;
    
    @Column(name = "base_salary", precision = 12, scale = 2)
    private BigDecimal baseSalary = BigDecimal.ZERO;
    
    @Column(name = "hourly_rate", precision = 8, scale = 2)
    private BigDecimal hourlyRate = BigDecimal.ZERO;
    
    @Column(name = "total_deductions", precision = 10, scale = 2)
    private BigDecimal totalDeductions = BigDecimal.ZERO;
    
    @Column(name = "gross_pay", precision = 12, scale = 2)
    private BigDecimal grossPay = BigDecimal.ZERO;
    
    @Column(name = "net_pay", precision = 12, scale = 2)
    private BigDecimal netPay = BigDecimal.ZERO;
    
    @Column(name = "tax_deduction", precision = 10, scale = 2)
    private BigDecimal taxDeduction = BigDecimal.ZERO;
    
    @Column(name = "insurance_deduction", precision = 10, scale = 2)
    private BigDecimal insuranceDeduction = BigDecimal.ZERO;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PayrollStatus status = PayrollStatus.DRAFT;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by", 
                foreignKey = @ForeignKey(name = "fk_payroll_generated_by"))
    private User generatedBy;
    
    @Column(name = "generated_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime generatedAt;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    public enum PayrollStatus {
        DRAFT("Draft", true),
        PROCESSED("Processed", false),
        APPROVED("Approved", false),
        PAID("Paid", false),
        CANCELLED("Cancelled", true);
        
        private final String displayName;
        private final boolean editable;
        
        PayrollStatus(String displayName, boolean editable) {
            this.displayName = displayName;
            this.editable = editable;
        }
        
        public String getDisplayName() { return displayName; }
        public boolean isEditable() { return editable; }
    }
    
    public boolean isEditable() {
        return status.isEditable();
    }
    
    public BigDecimal getHourlyPay() {
        if (totalWorkingHours.compareTo(BigDecimal.ZERO) > 0) {
            return grossPay.divide(totalWorkingHours, 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getEffectiveRate() {
        if (totalWorkingHours.compareTo(BigDecimal.ZERO) > 0) {
            return netPay.divide(totalWorkingHours, 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalDeductionsPercentage() {
        if (grossPay.compareTo(BigDecimal.ZERO) > 0) {
            return totalDeductions.divide(grossPay, 2, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }
    
    public boolean isCurrentPeriod() {
        LocalDate now = LocalDate.now();
        return !payPeriodStart.isAfter(now) && !payPeriodEnd.isBefore(now);
    }
    
    public long getDaysUntilDue() {
        LocalDate now = LocalDate.now();
        return java.time.Duration.between(now.atStartOfDay(), payPeriodEnd.atStartOfDay()).toDays();
    }
    
    @PrePersist
    @PreUpdate
    private void calculateTotals() {
        if (totalWorkingHours == null) totalWorkingHours = BigDecimal.ZERO;
        if (hourlyRate == null) hourlyRate = BigDecimal.ZERO;
        if (totalDeductions == null) totalDeductions = BigDecimal.ZERO;
        
        grossPay = baseSalary.add(hourlyRate.multiply(totalWorkingHours));
        netPay = grossPay.subtract(totalDeductions);
    }
}