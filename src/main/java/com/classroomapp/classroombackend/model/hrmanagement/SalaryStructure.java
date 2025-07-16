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

/**
 * Entity representing salary structure for employees
 */
@Entity
@Table(name = "salary_structures")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryStructure {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    // Basic salary components
    @Column(name = "base_salary", precision = 15, scale = 2, nullable = false)
    private BigDecimal baseSalary;
    
    @Column(name = "hourly_rate", precision = 10, scale = 2)
    private BigDecimal hourlyRate;
    
    @Column(name = "overtime_rate", precision = 10, scale = 2)
    private BigDecimal overtimeRate;
    
    @Column(name = "holiday_rate", precision = 10, scale = 2)
    private BigDecimal holidayRate;
    
    @Column(name = "weekend_rate", precision = 10, scale = 2)
    private BigDecimal weekendRate;
    
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
    
    // Deductions
    @Column(name = "social_insurance_rate", precision = 5, scale = 4, columnDefinition = "DECIMAL(5,4) DEFAULT 0.08")
    private BigDecimal socialInsuranceRate = new BigDecimal("0.08"); // 8%
    
    @Column(name = "health_insurance_rate", precision = 5, scale = 4, columnDefinition = "DECIMAL(5,4) DEFAULT 0.015")
    private BigDecimal healthInsuranceRate = new BigDecimal("0.015"); // 1.5%
    
    @Column(name = "unemployment_insurance_rate", precision = 5, scale = 4, columnDefinition = "DECIMAL(5,4) DEFAULT 0.01")
    private BigDecimal unemploymentInsuranceRate = new BigDecimal("0.01"); // 1%
    
    @Column(name = "personal_income_tax_rate", precision = 5, scale = 4, columnDefinition = "DECIMAL(5,4) DEFAULT 0.1")
    private BigDecimal personalIncomeTaxRate = new BigDecimal("0.1"); // 10%
    
    @Column(name = "other_deductions", precision = 12, scale = 2, columnDefinition = "DECIMAL(12,2) DEFAULT 0")
    private BigDecimal otherDeductions = BigDecimal.ZERO;
    
    // Salary type
    @Enumerated(EnumType.STRING)
    @Column(name = "salary_type", length = 20, nullable = false)
    private SalaryType salaryType = SalaryType.MONTHLY;
    
    @Column(name = "is_active", columnDefinition = "BIT DEFAULT 1")
    private Boolean isActive = true;
    
    @Column(name = "notes", length = 1000)
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
     * Salary type enumeration
     */
    public enum SalaryType {
        MONTHLY("Lương tháng"),
        HOURLY("Lương giờ"),
        DAILY("Lương ngày"),
        CONTRACT("Hợp đồng");
        
        private final String description;
        
        SalaryType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // Business logic methods
    
    /**
     * Calculate total allowances
     */
    public BigDecimal getTotalAllowances() {
        return positionAllowance
            .add(transportAllowance)
            .add(mealAllowance)
            .add(phoneAllowance)
            .add(otherAllowances);
    }
    
    /**
     * Calculate total insurance deduction rate
     */
    public BigDecimal getTotalInsuranceRate() {
        return socialInsuranceRate
            .add(healthInsuranceRate)
            .add(unemploymentInsuranceRate);
    }
    
    /**
     * Calculate gross salary (base + allowances)
     */
    public BigDecimal getGrossSalary() {
        return baseSalary.add(getTotalAllowances());
    }
    
    /**
     * Calculate insurance deduction amount
     */
    public BigDecimal getInsuranceDeduction() {
        return baseSalary.multiply(getTotalInsuranceRate());
    }
    
    /**
     * Calculate taxable income
     */
    public BigDecimal getTaxableIncome() {
        BigDecimal grossSalary = getGrossSalary();
        BigDecimal insuranceDeduction = getInsuranceDeduction();
        BigDecimal personalDeduction = new BigDecimal("11000000"); // 11M VND personal deduction
        
        BigDecimal taxableIncome = grossSalary
            .subtract(insuranceDeduction)
            .subtract(personalDeduction);
        
        return taxableIncome.compareTo(BigDecimal.ZERO) > 0 ? taxableIncome : BigDecimal.ZERO;
    }
    
    /**
     * Calculate personal income tax
     */
    public BigDecimal getPersonalIncomeTax() {
        BigDecimal taxableIncome = getTaxableIncome();
        
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        // Progressive tax calculation (simplified)
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal remaining = taxableIncome;
        
        // 5% for first 5M
        BigDecimal bracket1 = new BigDecimal("5000000");
        if (remaining.compareTo(bracket1) > 0) {
            tax = tax.add(bracket1.multiply(new BigDecimal("0.05")));
            remaining = remaining.subtract(bracket1);
        } else {
            return tax.add(remaining.multiply(new BigDecimal("0.05")));
        }
        
        // 10% for next 5M (5M-10M)
        BigDecimal bracket2 = new BigDecimal("5000000");
        if (remaining.compareTo(bracket2) > 0) {
            tax = tax.add(bracket2.multiply(new BigDecimal("0.10")));
            remaining = remaining.subtract(bracket2);
        } else {
            return tax.add(remaining.multiply(new BigDecimal("0.10")));
        }
        
        // 15% for next 8M (10M-18M)
        BigDecimal bracket3 = new BigDecimal("8000000");
        if (remaining.compareTo(bracket3) > 0) {
            tax = tax.add(bracket3.multiply(new BigDecimal("0.15")));
            remaining = remaining.subtract(bracket3);
        } else {
            return tax.add(remaining.multiply(new BigDecimal("0.15")));
        }
        
        // 20% for next 14M (18M-32M)
        BigDecimal bracket4 = new BigDecimal("14000000");
        if (remaining.compareTo(bracket4) > 0) {
            tax = tax.add(bracket4.multiply(new BigDecimal("0.20")));
            remaining = remaining.subtract(bracket4);
        } else {
            return tax.add(remaining.multiply(new BigDecimal("0.20")));
        }
        
        // 25% for next 20M (32M-52M)
        BigDecimal bracket5 = new BigDecimal("20000000");
        if (remaining.compareTo(bracket5) > 0) {
            tax = tax.add(bracket5.multiply(new BigDecimal("0.25")));
            remaining = remaining.subtract(bracket5);
        } else {
            return tax.add(remaining.multiply(new BigDecimal("0.25")));
        }
        
        // 30% for next 28M (52M-80M)
        BigDecimal bracket6 = new BigDecimal("28000000");
        if (remaining.compareTo(bracket6) > 0) {
            tax = tax.add(bracket6.multiply(new BigDecimal("0.30")));
            remaining = remaining.subtract(bracket6);
        } else {
            return tax.add(remaining.multiply(new BigDecimal("0.30")));
        }
        
        // 35% for above 80M
        tax = tax.add(remaining.multiply(new BigDecimal("0.35")));
        
        return tax;
    }
    
    /**
     * Calculate total deductions
     */
    public BigDecimal getTotalDeductions() {
        return getInsuranceDeduction()
            .add(getPersonalIncomeTax())
            .add(otherDeductions);
    }
    
    /**
     * Calculate net salary
     */
    public BigDecimal getNetSalary() {
        return getGrossSalary().subtract(getTotalDeductions());
    }
    
    /**
     * Check if salary structure is currently active
     */
    public boolean isCurrentlyActive() {
        if (!Boolean.TRUE.equals(isActive)) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        
        if (effectiveDate != null && today.isBefore(effectiveDate)) {
            return false;
        }
        
        if (endDate != null && today.isAfter(endDate)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Check if salary structure overlaps with another period
     */
    public boolean overlapsWith(LocalDate startDate, LocalDate endDate) {
        LocalDate thisStart = this.effectiveDate;
        LocalDate thisEnd = this.endDate != null ? this.endDate : LocalDate.of(9999, 12, 31);
        
        LocalDate otherStart = startDate;
        LocalDate otherEnd = endDate != null ? endDate : LocalDate.of(9999, 12, 31);
        
        return !thisEnd.isBefore(otherStart) && !otherEnd.isBefore(thisStart);
    }
}
