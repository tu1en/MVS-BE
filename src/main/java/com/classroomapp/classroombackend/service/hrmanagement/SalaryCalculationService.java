package com.classroomapp.classroombackend.service.hrmanagement;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.model.hrmanagement.Payroll;
import com.classroomapp.classroombackend.model.hrmanagement.SalaryStructure;

/**
 * Service interface for salary calculation operations
 */
public interface SalaryCalculationService {
    
    /**
     * Calculate payroll for specific user and period
     */
    Payroll calculatePayrollForUser(Long userId, YearMonth period);
    
    /**
     * Calculate payroll for all users in period
     */
    List<Payroll> calculatePayrollForPeriod(YearMonth period);
    
    /**
     * Recalculate existing payroll
     */
    Payroll recalculatePayroll(Long payrollId);
    
    /**
     * Get payroll by ID
     */
    Payroll getPayrollById(Long payrollId);
    
    /**
     * Get payroll for user and period
     */
    Payroll getPayrollForUserAndPeriod(Long userId, YearMonth period);
    
    /**
     * Get payrolls for user
     */
    Page<Payroll> getPayrollsForUser(Long userId, Pageable pageable);
    
    /**
     * Get payrolls for period
     */
    Page<Payroll> getPayrollsForPeriod(YearMonth period, Pageable pageable);
    
    /**
     * Get payrolls by status
     */
    Page<Payroll> getPayrollsByStatus(Payroll.PayrollStatus status, Pageable pageable);
    
    /**
     * Approve payroll
     */
    Payroll approvePayroll(Long payrollId, Long approvedBy);
    
    /**
     * Approve multiple payrolls
     */
    List<Payroll> approvePayrolls(List<Long> payrollIds, Long approvedBy);
    
    /**
     * Mark payroll as paid
     */
    Payroll markPayrollAsPaid(Long payrollId, Long paidBy);
    
    /**
     * Cancel payroll
     */
    void cancelPayroll(Long payrollId);
    
    /**
     * Get payroll statistics for period
     */
    PayrollStatistics getPayrollStatisticsForPeriod(YearMonth period);
    
    /**
     * Get yearly payroll summary for user
     */
    List<MonthlyPayrollSummary> getYearlyPayrollSummary(Long userId, Integer year);
    
    /**
     * Get department payroll summary
     */
    List<DepartmentPayrollSummary> getDepartmentPayrollSummary(YearMonth period);
    
    /**
     * Get top earners for period
     */
    List<Payroll> getTopEarners(YearMonth period, int limit);
    
    /**
     * Get payroll comparison between periods
     */
    List<PayrollComparison> getPayrollComparison(YearMonth currentPeriod, YearMonth previousPeriod);
    
    /**
     * Get monthly payroll trends
     */
    List<MonthlyPayrollTrend> getMonthlyPayrollTrends(int months);
    
    /**
     * Validate payroll calculation
     */
    PayrollValidationResult validatePayroll(Long payrollId);
    
    /**
     * Get attendance summary for payroll calculation
     */
    AttendanceSummary getAttendanceSummaryForPayroll(Long userId, YearMonth period);
    
    /**
     * Calculate overtime pay
     */
    BigDecimal calculateOvertimePay(BigDecimal overtimeHours, SalaryStructure salaryStructure);
    
    /**
     * Calculate holiday pay
     */
    BigDecimal calculateHolidayPay(BigDecimal holidayHours, SalaryStructure salaryStructure);
    
    /**
     * Calculate attendance penalties
     */
    AttendancePenalties calculateAttendancePenalties(AttendanceSummary attendanceSummary, 
                                                   SalaryStructure salaryStructure);
    
    /**
     * Get payroll processing status
     */
    PayrollProcessingStatus getPayrollProcessingStatus(YearMonth period);
    
    /**
     * Process bulk payroll calculation
     */
    BulkPayrollResult processBulkPayrollCalculation(YearMonth period, List<Long> userIds);
    
    // Inner classes for DTOs
    
    class PayrollStatistics {
        private Long totalPayrolls;
        private BigDecimal totalGrossSalary;
        private BigDecimal totalNetSalary;
        private BigDecimal totalDeductions;
        private BigDecimal averageNetSalary;
        private YearMonth period;
        
        // Constructors, getters, setters
        public PayrollStatistics() {}
        
        public PayrollStatistics(Long totalPayrolls, BigDecimal totalGrossSalary, 
                               BigDecimal totalNetSalary, BigDecimal totalDeductions, 
                               BigDecimal averageNetSalary, YearMonth period) {
            this.totalPayrolls = totalPayrolls;
            this.totalGrossSalary = totalGrossSalary;
            this.totalNetSalary = totalNetSalary;
            this.totalDeductions = totalDeductions;
            this.averageNetSalary = averageNetSalary;
            this.period = period;
        }
        
        // Getters and setters
        public Long getTotalPayrolls() { return totalPayrolls; }
        public void setTotalPayrolls(Long totalPayrolls) { this.totalPayrolls = totalPayrolls; }
        
        public BigDecimal getTotalGrossSalary() { return totalGrossSalary; }
        public void setTotalGrossSalary(BigDecimal totalGrossSalary) { this.totalGrossSalary = totalGrossSalary; }
        
        public BigDecimal getTotalNetSalary() { return totalNetSalary; }
        public void setTotalNetSalary(BigDecimal totalNetSalary) { this.totalNetSalary = totalNetSalary; }
        
        public BigDecimal getTotalDeductions() { return totalDeductions; }
        public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }
        
        public BigDecimal getAverageNetSalary() { return averageNetSalary; }
        public void setAverageNetSalary(BigDecimal averageNetSalary) { this.averageNetSalary = averageNetSalary; }
        
        public YearMonth getPeriod() { return period; }
        public void setPeriod(YearMonth period) { this.period = period; }
    }
    
    class MonthlyPayrollSummary {
        private Integer month;
        private Integer year;
        private BigDecimal grossSalary;
        private BigDecimal netSalary;
        private BigDecimal totalDeductions;
        
        // Constructors, getters, setters
        public MonthlyPayrollSummary() {}
        
        public MonthlyPayrollSummary(Integer month, Integer year, BigDecimal grossSalary, 
                                   BigDecimal netSalary, BigDecimal totalDeductions) {
            this.month = month;
            this.year = year;
            this.grossSalary = grossSalary;
            this.netSalary = netSalary;
            this.totalDeductions = totalDeductions;
        }
        
        public Integer getMonth() { return month; }
        public void setMonth(Integer month) { this.month = month; }
        
        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }
        
        public BigDecimal getGrossSalary() { return grossSalary; }
        public void setGrossSalary(BigDecimal grossSalary) { this.grossSalary = grossSalary; }
        
        public BigDecimal getNetSalary() { return netSalary; }
        public void setNetSalary(BigDecimal netSalary) { this.netSalary = netSalary; }
        
        public BigDecimal getTotalDeductions() { return totalDeductions; }
        public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }
    }
    
    class DepartmentPayrollSummary {
        private String department;
        private Long employeeCount;
        private BigDecimal totalGrossSalary;
        private BigDecimal totalNetSalary;
        private BigDecimal averageNetSalary;
        
        // Constructors, getters, setters
        public DepartmentPayrollSummary() {}
        
        public DepartmentPayrollSummary(String department, Long employeeCount, 
                                      BigDecimal totalGrossSalary, BigDecimal totalNetSalary, 
                                      BigDecimal averageNetSalary) {
            this.department = department;
            this.employeeCount = employeeCount;
            this.totalGrossSalary = totalGrossSalary;
            this.totalNetSalary = totalNetSalary;
            this.averageNetSalary = averageNetSalary;
        }
        
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        
        public Long getEmployeeCount() { return employeeCount; }
        public void setEmployeeCount(Long employeeCount) { this.employeeCount = employeeCount; }
        
        public BigDecimal getTotalGrossSalary() { return totalGrossSalary; }
        public void setTotalGrossSalary(BigDecimal totalGrossSalary) { this.totalGrossSalary = totalGrossSalary; }
        
        public BigDecimal getTotalNetSalary() { return totalNetSalary; }
        public void setTotalNetSalary(BigDecimal totalNetSalary) { this.totalNetSalary = totalNetSalary; }
        
        public BigDecimal getAverageNetSalary() { return averageNetSalary; }
        public void setAverageNetSalary(BigDecimal averageNetSalary) { this.averageNetSalary = averageNetSalary; }
    }
    
    class PayrollComparison {
        private Long userId;
        private String userFullName;
        private BigDecimal currentSalary;
        private BigDecimal previousSalary;
        private BigDecimal difference;
        private BigDecimal percentageChange;
        
        // Constructors, getters, setters
        public PayrollComparison() {}
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public String getUserFullName() { return userFullName; }
        public void setUserFullName(String userFullName) { this.userFullName = userFullName; }
        
        public BigDecimal getCurrentSalary() { return currentSalary; }
        public void setCurrentSalary(BigDecimal currentSalary) { this.currentSalary = currentSalary; }
        
        public BigDecimal getPreviousSalary() { return previousSalary; }
        public void setPreviousSalary(BigDecimal previousSalary) { this.previousSalary = previousSalary; }
        
        public BigDecimal getDifference() { return difference; }
        public void setDifference(BigDecimal difference) { this.difference = difference; }
        
        public BigDecimal getPercentageChange() { return percentageChange; }
        public void setPercentageChange(BigDecimal percentageChange) { this.percentageChange = percentageChange; }
    }
    
    class MonthlyPayrollTrend {
        private Integer year;
        private Integer month;
        private Long payrollCount;
        private BigDecimal totalGrossSalary;
        private BigDecimal totalNetSalary;
        private BigDecimal averageNetSalary;
        
        // Constructors, getters, setters
        public MonthlyPayrollTrend() {}
        
        public Integer getYear() { return year; }
        public void setYear(Integer year) { this.year = year; }
        
        public Integer getMonth() { return month; }
        public void setMonth(Integer month) { this.month = month; }
        
        public Long getPayrollCount() { return payrollCount; }
        public void setPayrollCount(Long payrollCount) { this.payrollCount = payrollCount; }
        
        public BigDecimal getTotalGrossSalary() { return totalGrossSalary; }
        public void setTotalGrossSalary(BigDecimal totalGrossSalary) { this.totalGrossSalary = totalGrossSalary; }
        
        public BigDecimal getTotalNetSalary() { return totalNetSalary; }
        public void setTotalNetSalary(BigDecimal totalNetSalary) { this.totalNetSalary = totalNetSalary; }
        
        public BigDecimal getAverageNetSalary() { return averageNetSalary; }
        public void setAverageNetSalary(BigDecimal averageNetSalary) { this.averageNetSalary = averageNetSalary; }
    }
    
    class PayrollValidationResult {
        private boolean isValid;
        private List<String> errors;
        private List<String> warnings;
        
        public PayrollValidationResult() {
            this.errors = new java.util.ArrayList<>();
            this.warnings = new java.util.ArrayList<>();
        }
        
        public boolean isValid() { return isValid; }
        public void setValid(boolean valid) { isValid = valid; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
        
        public void addError(String error) { this.errors.add(error); }
        public void addWarning(String warning) { this.warnings.add(warning); }
    }
    
    class AttendanceSummary {
        private BigDecimal totalWorkingDays;
        private BigDecimal actualWorkingDays;
        private BigDecimal regularHours;
        private BigDecimal overtimeHours;
        private BigDecimal holidayHours;
        private BigDecimal weekendHours;
        private Integer lateArrivals;
        private Integer earlyDepartures;
        private BigDecimal absentDays;
        private BigDecimal leaveDays;
        
        // Constructors, getters, setters
        public AttendanceSummary() {}
        
        public BigDecimal getTotalWorkingDays() { return totalWorkingDays; }
        public void setTotalWorkingDays(BigDecimal totalWorkingDays) { this.totalWorkingDays = totalWorkingDays; }
        
        public BigDecimal getActualWorkingDays() { return actualWorkingDays; }
        public void setActualWorkingDays(BigDecimal actualWorkingDays) { this.actualWorkingDays = actualWorkingDays; }
        
        public BigDecimal getRegularHours() { return regularHours; }
        public void setRegularHours(BigDecimal regularHours) { this.regularHours = regularHours; }
        
        public BigDecimal getOvertimeHours() { return overtimeHours; }
        public void setOvertimeHours(BigDecimal overtimeHours) { this.overtimeHours = overtimeHours; }
        
        public BigDecimal getHolidayHours() { return holidayHours; }
        public void setHolidayHours(BigDecimal holidayHours) { this.holidayHours = holidayHours; }
        
        public BigDecimal getWeekendHours() { return weekendHours; }
        public void setWeekendHours(BigDecimal weekendHours) { this.weekendHours = weekendHours; }
        
        public Integer getLateArrivals() { return lateArrivals; }
        public void setLateArrivals(Integer lateArrivals) { this.lateArrivals = lateArrivals; }
        
        public Integer getEarlyDepartures() { return earlyDepartures; }
        public void setEarlyDepartures(Integer earlyDepartures) { this.earlyDepartures = earlyDepartures; }
        
        public BigDecimal getAbsentDays() { return absentDays; }
        public void setAbsentDays(BigDecimal absentDays) { this.absentDays = absentDays; }
        
        public BigDecimal getLeaveDays() { return leaveDays; }
        public void setLeaveDays(BigDecimal leaveDays) { this.leaveDays = leaveDays; }
    }
    
    class AttendancePenalties {
        private BigDecimal latePenalty;
        private BigDecimal absentPenalty;
        private BigDecimal totalPenalty;
        
        public AttendancePenalties() {}
        
        public AttendancePenalties(BigDecimal latePenalty, BigDecimal absentPenalty) {
            this.latePenalty = latePenalty;
            this.absentPenalty = absentPenalty;
            this.totalPenalty = latePenalty.add(absentPenalty);
        }
        
        public BigDecimal getLatePenalty() { return latePenalty; }
        public void setLatePenalty(BigDecimal latePenalty) { this.latePenalty = latePenalty; }
        
        public BigDecimal getAbsentPenalty() { return absentPenalty; }
        public void setAbsentPenalty(BigDecimal absentPenalty) { this.absentPenalty = absentPenalty; }
        
        public BigDecimal getTotalPenalty() { return totalPenalty; }
        public void setTotalPenalty(BigDecimal totalPenalty) { this.totalPenalty = totalPenalty; }
    }
    
    class PayrollProcessingStatus {
        private YearMonth period;
        private Integer totalEmployees;
        private Integer processedCount;
        private Integer pendingCount;
        private Integer errorCount;
        private boolean isCompleted;
        
        public PayrollProcessingStatus() {}
        
        public YearMonth getPeriod() { return period; }
        public void setPeriod(YearMonth period) { this.period = period; }
        
        public Integer getTotalEmployees() { return totalEmployees; }
        public void setTotalEmployees(Integer totalEmployees) { this.totalEmployees = totalEmployees; }
        
        public Integer getProcessedCount() { return processedCount; }
        public void setProcessedCount(Integer processedCount) { this.processedCount = processedCount; }
        
        public Integer getPendingCount() { return pendingCount; }
        public void setPendingCount(Integer pendingCount) { this.pendingCount = pendingCount; }
        
        public Integer getErrorCount() { return errorCount; }
        public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }
        
        public boolean isCompleted() { return isCompleted; }
        public void setCompleted(boolean completed) { isCompleted = completed; }
    }
    
    class BulkPayrollResult {
        private YearMonth period;
        private Integer totalRequested;
        private Integer successCount;
        private Integer errorCount;
        private List<String> errors;
        private List<Payroll> successfulPayrolls;
        
        public BulkPayrollResult() {
            this.errors = new java.util.ArrayList<>();
            this.successfulPayrolls = new java.util.ArrayList<>();
        }
        
        public YearMonth getPeriod() { return period; }
        public void setPeriod(YearMonth period) { this.period = period; }
        
        public Integer getTotalRequested() { return totalRequested; }
        public void setTotalRequested(Integer totalRequested) { this.totalRequested = totalRequested; }
        
        public Integer getSuccessCount() { return successCount; }
        public void setSuccessCount(Integer successCount) { this.successCount = successCount; }
        
        public Integer getErrorCount() { return errorCount; }
        public void setErrorCount(Integer errorCount) { this.errorCount = errorCount; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public List<Payroll> getSuccessfulPayrolls() { return successfulPayrolls; }
        public void setSuccessfulPayrolls(List<Payroll> successfulPayrolls) { this.successfulPayrolls = successfulPayrolls; }

        public void addError(String error) { this.errors.add(error); }
        public void addSuccessfulPayroll(Payroll payroll) { this.successfulPayrolls.add(payroll); }
    }
}
