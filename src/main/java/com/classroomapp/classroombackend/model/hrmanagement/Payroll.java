package com.classroomapp.classroombackend.model.hrmanagement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private BigDecimal totalWorkingDays = BigDecimal.ZERO;
    private BigDecimal actualWorkingDays = BigDecimal.ZERO;
    private BigDecimal totalWorkingHours = BigDecimal.ZERO;
    private BigDecimal regularHours = BigDecimal.ZERO;
    private BigDecimal overtimeHours = BigDecimal.ZERO;
    private BigDecimal holidayHours = BigDecimal.ZERO;
    private BigDecimal weekendHours = BigDecimal.ZERO;

    // Attendance summary
    private Integer lateArrivals = 0;
    private Integer earlyDepartures = 0;
    private BigDecimal absentDays = BigDecimal.ZERO;
    private BigDecimal leaveDays = BigDecimal.ZERO;

    // Salary calculations
    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal baseSalary;

    private BigDecimal regularPay = BigDecimal.ZERO;
    private BigDecimal overtimePay = BigDecimal.ZERO;
    private BigDecimal holidayPay = BigDecimal.ZERO;
    private BigDecimal weekendPay = BigDecimal.ZERO;

    // Allowances
    private BigDecimal positionAllowance = BigDecimal.ZERO;
    private BigDecimal transportAllowance = BigDecimal.ZERO;
    private BigDecimal mealAllowance = BigDecimal.ZERO;
    private BigDecimal phoneAllowance = BigDecimal.ZERO;
    private BigDecimal otherAllowances = BigDecimal.ZERO;
    private BigDecimal totalAllowances = BigDecimal.ZERO;

    // Deductions
    private BigDecimal socialInsurance = BigDecimal.ZERO;
    private BigDecimal healthInsurance = BigDecimal.ZERO;
    private BigDecimal unemploymentInsurance = BigDecimal.ZERO;
    private BigDecimal personalIncomeTax = BigDecimal.ZERO;
    private BigDecimal latePenalty = BigDecimal.ZERO;
    private BigDecimal absentPenalty = BigDecimal.ZERO;
    private BigDecimal otherDeductions = BigDecimal.ZERO;
    private BigDecimal totalDeductions = BigDecimal.ZERO;

    // Final amounts
    private BigDecimal grossSalary = BigDecimal.ZERO;
    private BigDecimal netSalary = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PayrollStatus status = PayrollStatus.DRAFT;

    private LocalDateTime calculatedAt;
    private LocalDateTime approvedAt;
    private Long approvedBy;
    private LocalDateTime paidAt;
    private Long paidBy;
    @Column(length = 2000)
    private String notes;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private Long createdBy;
    private Long updatedBy;

    public enum PayrollStatus {
        DRAFT("Bản nháp"),
        CALCULATED("Đã tính toán"),
        APPROVED("Đã phê duyệt"),
        PAID("Đã thanh toán"),
        CANCELLED("Đã hủy");

        private final String description;
        PayrollStatus(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    /**
     * Kiểm tra xem payroll có thể chỉnh sửa hay không
     */
    public boolean canBeEdited() {
        return this.status == PayrollStatus.DRAFT || this.status == PayrollStatus.CALCULATED;
    }

    /**
     * Kiểm tra xem payroll có thể approve hay không
     */
    public boolean canBeApproved() {
        return this.status == PayrollStatus.CALCULATED;
    }

    /**
     * Kiểm tra xem payroll có thể mark as paid hay không
     */
    public boolean canBePaid() {
        return this.status == PayrollStatus.APPROVED;
    }

    /**
     * Phê duyệt payroll
     */
    public void approve(Long approverId) {
        if (!canBeApproved()) {
            throw new IllegalStateException("Không thể phê duyệt payroll với trạng thái: " + this.status);
        }
        this.status = PayrollStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
        this.approvedBy = approverId;
        this.updatedBy = approverId;
    }

    /**
     * Đánh dấu đã thanh toán
     */
    public void markAsPaid(Long paidById) {
        if (!canBePaid()) {
            throw new IllegalStateException("Không thể thanh toán payroll với trạng thái: " + this.status);
        }
        this.status = PayrollStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.paidBy = paidById;
        this.updatedBy = paidById;
    }

    /**
     * Hủy payroll
     */
    public void cancel() {
        if (this.status == PayrollStatus.PAID) {
            throw new IllegalStateException("Không thể hủy payroll đã được thanh toán");
        }
        this.status = PayrollStatus.CANCELLED;
    }

    /**
     * Tính lại tổng allowances
     */
    public void recalculateAllowances() {
        this.totalAllowances = BigDecimal.ZERO
            .add(this.positionAllowance != null ? this.positionAllowance : BigDecimal.ZERO)
            .add(this.transportAllowance != null ? this.transportAllowance : BigDecimal.ZERO)
            .add(this.mealAllowance != null ? this.mealAllowance : BigDecimal.ZERO)
            .add(this.phoneAllowance != null ? this.phoneAllowance : BigDecimal.ZERO)
            .add(this.otherAllowances != null ? this.otherAllowances : BigDecimal.ZERO);
    }

    /**
     * Tính lại tổng deductions
     */
    public void recalculateDeductions() {
        this.totalDeductions = BigDecimal.ZERO
            .add(this.socialInsurance != null ? this.socialInsurance : BigDecimal.ZERO)
            .add(this.healthInsurance != null ? this.healthInsurance : BigDecimal.ZERO)
            .add(this.unemploymentInsurance != null ? this.unemploymentInsurance : BigDecimal.ZERO)
            .add(this.personalIncomeTax != null ? this.personalIncomeTax : BigDecimal.ZERO)
            .add(this.latePenalty != null ? this.latePenalty : BigDecimal.ZERO)
            .add(this.absentPenalty != null ? this.absentPenalty : BigDecimal.ZERO)
            .add(this.otherDeductions != null ? this.otherDeductions : BigDecimal.ZERO);
    }

    /**
     * Tính lại tổng lương gross
     */
    public void recalculateGrossSalary() {
        this.grossSalary = BigDecimal.ZERO
            .add(this.regularPay != null ? this.regularPay : BigDecimal.ZERO)
            .add(this.overtimePay != null ? this.overtimePay : BigDecimal.ZERO)
            .add(this.holidayPay != null ? this.holidayPay : BigDecimal.ZERO)
            .add(this.weekendPay != null ? this.weekendPay : BigDecimal.ZERO)
            .add(this.totalAllowances != null ? this.totalAllowances : BigDecimal.ZERO);
    }

    /**
     * Tính lại tổng lương net
     */
    public void recalculateNetSalary() {
        this.netSalary = (this.grossSalary != null ? this.grossSalary : BigDecimal.ZERO)
            .subtract(this.totalDeductions != null ? this.totalDeductions : BigDecimal.ZERO);
    }

    /**
     * Tính lại tất cả các totals
     */
    public void recalculateTotals() {
        recalculateAllowances();
        recalculateDeductions();
        recalculateGrossSalary();
        recalculateNetSalary();
        this.calculatedAt = LocalDateTime.now();
        if (this.status == PayrollStatus.DRAFT) {
            this.status = PayrollStatus.CALCULATED;
        }
    }

    /**
     * Kiểm tra tính hợp lệ của payroll
     */
    public boolean isValid() {
        if (this.user == null || this.salaryStructure == null) {
            return false;
        }
        if (this.payrollYear == null || this.payrollMonth == null) {
            return false;
        }
        if (this.payPeriodStart == null || this.payPeriodEnd == null) {
            return false;
        }
        if (this.baseSalary == null || this.baseSalary.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }
        return true;
    }

    /**
     * Lấy tên tháng năm cho payroll
     */
    public String getPayrollPeriodDisplay() {
        return String.format("%02d/%d", this.payrollMonth, this.payrollYear);
    }

    /**
     * Tính tỷ lệ chuyên cần (%)
     */
    public BigDecimal getAttendanceRate() {
        if (this.totalWorkingDays == null || this.totalWorkingDays.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal actualDays = this.actualWorkingDays != null ? this.actualWorkingDays : BigDecimal.ZERO;
        return actualDays.divide(this.totalWorkingDays, 4, java.math.RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));
    }

    /**
     * Tính tổng số vi phạm chuyên cần
     */
    public Integer getTotalViolations() {
        return (this.lateArrivals != null ? this.lateArrivals : 0) + 
               (this.earlyDepartures != null ? this.earlyDepartures : 0);
    }

    /**
     * Reset payroll về trạng thái draft để tính lại
     */
    public void resetForRecalculation() {
        this.status = PayrollStatus.DRAFT;
        this.calculatedAt = null;
        this.approvedAt = null;
        this.approvedBy = null;
        this.paidAt = null;
        this.paidBy = null;
    }

}
