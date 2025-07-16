package com.classroomapp.classroombackend.model.hrmanagement;

import com.classroomapp.classroombackend.model.usermanagement.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Entity representing monthly payroll for employees
 */
@Entity
@Table(name = "payrolls", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "payroll_year", "payroll_month"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payroll {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salary_structure_id", nullable = false)
    private SalaryStructure salaryStructure;
    
    @Column(name = "payroll_year", nullable = false)
    private Integer payrollYear;
    
    @Column(name = "payroll_month", nullable = false)
    private Integer payrollMonth;
    
    @Column(name = "pay_period_start", nullable = false)
    private LocalDate payPeriodStart;
    
    @Column(name = "pay_period_end", nullable = false)
    private LocalDate payPeriodEnd;
    
    // Working hours summary
    @Column(name = "total_working_days", columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private BigDecimal totalWorkingDays = BigDecimal.ZERO;
    
    @Column(name = "actual_working_days", columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private BigDecimal actualWorkingDays = BigDecimal.ZERO;
    
    @Column(name = "total_working_hours", columnDefinition = "DECIMAL(8,2) DEFAULT 0")
    private BigDecimal totalWorkingHours = BigDecimal.ZERO;
    
    @Column(name = "regular_hours", columnDefinition = "DECIMAL(8,2) DEFAULT 0")
    private BigDecimal regularHours = BigDecimal.ZERO;
    
    @Column(name = "overtime_hours", columnDefinition = "DECIMAL(8,2) DEFAULT 0")
    private BigDecimal overtimeHours = BigDecimal.ZERO;
    
    @Column(name = "holiday_hours", columnDefinition = "DECIMAL(8,2) DEFAULT 0")
    private BigDecimal holidayHours = BigDecimal.ZERO;
    
    @Column(name = "weekend_hours", columnDefinition = "DECIMAL(8,2) DEFAULT 0")
    private BigDecimal weekendHours = BigDecimal.ZERO;
    
    // Attendance summary
    @Column(name = "late_arrivals", columnDefinition = "INT DEFAULT 0")
    private Integer lateArrivals = 0;
    
    @Column(name = "early_departures", columnDefinition = "INT DEFAULT 0")
    private Integer earlyDepartures = 0;
    
    @Column(name = "absent_days", columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private BigDecimal absentDays = BigDecimal.ZERO;
    
    @Column(name = "leave_days", columnDefinition = "DECIMAL(5,2) DEFAULT 0")
    private BigDecimal leaveDays = BigDecimal.ZERO;
    
    // Salary calculations
    @Column(name = "base_salary", precision = 15, scale = 2, nullable = false)
    private BigDecimal baseSalary;
    
    @Column(name = "regular_pay", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal regularPay = BigDecimal.ZERO;
    
    @Column(name = "overtime_pay", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal overtimePay = BigDecimal.ZERO;
    
    @Column(name = "holiday_pay", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal holidayPay = BigDecimal.ZERO;
    
    @Column(name = "weekend_pay", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal weekendPay = BigDecimal.ZERO;
    
    // Allowances
    @Column(name = "position_allowance", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    private BigDecimal positionAllowance = BigDecimal.ZERO;
    
    @Column(name = "transport_allowance", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    private BigDecimal transportAllowance = BigDecimal.ZERO;
    
    @Column(name = "meal_allowance", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    private BigDecimal mealAllowance = BigDecimal.ZERO;
    
    @Column(name = "phone_allowance", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    private BigDecimal phoneAllowance = BigDecimal.ZERO;
    
    @Column(name = "other_allowances", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    private BigDecimal otherAllowances = BigDecimal.ZERO;
    
    @Column(name = "total_allowances", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal totalAllowances = BigDecimal.ZERO;
    
    // Deductions
    @Column(name = "social_insurance", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    private BigDecimal socialInsurance = BigDecimal.ZERO;
    
    @Column(name = "health_insurance", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    private BigDecimal healthInsurance = BigDecimal.ZERO;
    
    @Column(name = "unemployment_insurance", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    private BigDecimal unemploymentInsurance = BigDecimal.ZERO;
    
    @Column(name = "personal_income_tax", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    private BigDecimal personalIncomeTax = BigDecimal.ZERO;
    
    @Column(name = "late_penalty", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    private BigDecimal latePenalty = BigDecimal.ZERO;
    
    @Column(name = "absent_penalty", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    private BigDecimal absentPenalty = BigDecimal.ZERO;
    
    @Column(name = "other_deductions", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    private BigDecimal otherDeductions = BigDecimal.ZERO;
    
    @Column(name = "total_deductions", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal totalDeductions = BigDecimal.ZERO;
    
    // Final amounts
    @Column(name = "gross_salary", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal grossSalary = BigDecimal.ZERO;
    
    @Column(name = "net_salary", precision = 15, scale = 2, columnDefinition = "DECIMAL(15,2) DEFAULT 0")
    private BigDecimal netSalary = BigDecimal.ZERO;
    
    // Status and processing
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PayrollStatus status = PayrollStatus.DRAFT;
    
    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approved_by")
    private Long approvedBy;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "paid_by")
    private Long paidBy;
    
    @Column(name = "notes", length = 2000)
    private String notes;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    /**
     * Payroll status enumeration
     */
    public enum PayrollStatus {
        DRAFT("Bản nháp"),
        CALCULATED("Đã tính toán"),
        APPROVED("Đã phê duyệt"),
        PAID("Đã thanh toán"),
        CANCELLED("Đã hủy");
        
        private final String description;
        
        PayrollStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Business logic methods
    
    /**
     * Get payroll period as YearMonth
     */
    public YearMonth getPayrollPeriod() {
        return YearMonth.of(payrollYear, payrollMonth);
    }
    
    /**
     * Calculate total earnings (regular + overtime + holiday + weekend)
     */
    public BigDecimal getTotalEarnings() {
        return regularPay
            .add(overtimePay)
            .add(holidayPay)
            .add(weekendPay);
    }
    
    /**
     * Calculate attendance rate
     */
    public BigDecimal getAttendanceRate() {
        if (totalWorkingDays.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return actualWorkingDays
            .divide(totalWorkingDays, 4, java.math.RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));
    }
    
    /**
     * Check if payroll can be edited
     */
    public boolean canBeEdited() {
        return status == PayrollStatus.DRAFT || status == PayrollStatus.CALCULATED;
    }
    
    /**
     * Check if payroll can be approved
     */
    public boolean canBeApproved() {
        return status == PayrollStatus.CALCULATED;
    }
    
    /**
     * Check if payroll can be paid
     */
    public boolean canBePaid() {
        return status == PayrollStatus.APPROVED;
    }
    
    /**
     * Approve payroll
     */
    public void approve(Long approvedBy) {
        if (!canBeApproved()) {
            throw new IllegalStateException("Bảng lương không thể phê duyệt ở trạng thái hiện tại");
        }
        
        this.status = PayrollStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = approvedBy;
    }
    
    /**
     * Mark payroll as paid
     */
    public void markAsPaid(Long paidBy) {
        if (!canBePaid()) {
            throw new IllegalStateException("Bảng lương không thể thanh toán ở trạng thái hiện tại");
        }
        
        this.status = PayrollStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.paidBy = paidBy;
    }
    
    /**
     * Cancel payroll
     */
    public void cancel() {
        if (status == PayrollStatus.PAID) {
            throw new IllegalStateException("Không thể hủy bảng lương đã thanh toán");
        }
        
        this.status = PayrollStatus.CANCELLED;
    }
    
    /**
     * Recalculate all totals
     */
    public void recalculateTotals() {
        // Calculate total allowances
        this.totalAllowances = positionAllowance
            .add(transportAllowance)
            .add(mealAllowance)
            .add(phoneAllowance)
            .add(otherAllowances);
        
        // Calculate total deductions
        this.totalDeductions = socialInsurance
            .add(healthInsurance)
            .add(unemploymentInsurance)
            .add(personalIncomeTax)
            .add(latePenalty)
            .add(absentPenalty)
            .add(otherDeductions);
        
        // Calculate gross salary
        this.grossSalary = getTotalEarnings().add(totalAllowances);
        
        // Calculate net salary
        this.netSalary = grossSalary.subtract(totalDeductions);
    }
    
    /**
     * Get formatted payroll period
     */
    public String getFormattedPayrollPeriod() {
        return String.format("%02d/%d", payrollMonth, payrollYear);
    }
    
    /**
     * Check if this is current month payroll
     */
    public boolean isCurrentMonth() {
        YearMonth current = YearMonth.now();
        return getPayrollPeriod().equals(current);
    }
    
    /**
     * Get working days efficiency
     */
    public BigDecimal getWorkingDaysEfficiency() {
        if (totalWorkingDays.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return actualWorkingDays.divide(totalWorkingDays, 4, java.math.RoundingMode.HALF_UP);
    }
}
