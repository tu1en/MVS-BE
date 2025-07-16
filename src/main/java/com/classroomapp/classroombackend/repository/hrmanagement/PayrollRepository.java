package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.Payroll;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Payroll entity
 */
@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    
    /**
     * Find payroll by user and period
     */
    @Query("SELECT p FROM Payroll p WHERE p.user.id = :userId " +
           "AND p.payrollYear = :year AND p.payrollMonth = :month")
    Optional<Payroll> findByUserIdAndPeriod(@Param("userId") Long userId,
                                           @Param("year") Integer year,
                                           @Param("month") Integer month);
    
    /**
     * Find payrolls by user
     */
    @Query("SELECT p FROM Payroll p WHERE p.user.id = :userId " +
           "ORDER BY p.payrollYear DESC, p.payrollMonth DESC")
    Page<Payroll> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find payrolls by period
     */
    @Query("SELECT p FROM Payroll p WHERE p.payrollYear = :year AND p.payrollMonth = :month " +
           "ORDER BY p.user.fullName")
    Page<Payroll> findByPeriod(@Param("year") Integer year, @Param("month") Integer month, 
                              Pageable pageable);
    
    /**
     * Find payrolls by status
     */
    @Query("SELECT p FROM Payroll p WHERE p.status = :status " +
           "ORDER BY p.payrollYear DESC, p.payrollMonth DESC, p.user.fullName")
    Page<Payroll> findByStatus(@Param("status") Payroll.PayrollStatus status, Pageable pageable);
    
    /**
     * Find payrolls by status and period
     */
    @Query("SELECT p FROM Payroll p WHERE p.status = :status " +
           "AND p.payrollYear = :year AND p.payrollMonth = :month " +
           "ORDER BY p.user.fullName")
    List<Payroll> findByStatusAndPeriod(@Param("status") Payroll.PayrollStatus status,
                                       @Param("year") Integer year,
                                       @Param("month") Integer month);
    
    /**
     * Find current month payrolls
     */
    @Query("SELECT p FROM Payroll p WHERE p.payrollYear = :year AND p.payrollMonth = :month " +
           "ORDER BY p.user.fullName")
    List<Payroll> findCurrentMonthPayrolls(@Param("year") Integer year, @Param("month") Integer month);
    
    /**
     * Find payrolls in date range
     */
    @Query("SELECT p FROM Payroll p WHERE p.payPeriodStart >= :startDate " +
           "AND p.payPeriodEnd <= :endDate " +
           "ORDER BY p.payPeriodStart DESC, p.user.fullName")
    Page<Payroll> findInDateRange(@Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 Pageable pageable);
    
    /**
     * Find payrolls by net salary range
     */
    @Query("SELECT p FROM Payroll p WHERE p.netSalary >= :minSalary " +
           "AND p.netSalary <= :maxSalary " +
           "ORDER BY p.netSalary DESC")
    Page<Payroll> findByNetSalaryRange(@Param("minSalary") BigDecimal minSalary,
                                      @Param("maxSalary") BigDecimal maxSalary,
                                      Pageable pageable);
    
    /**
     * Get payroll statistics for period
     */
    @Query("SELECT " +
           "COUNT(p) as totalPayrolls, " +
           "SUM(p.grossSalary) as totalGrossSalary, " +
           "SUM(p.netSalary) as totalNetSalary, " +
           "SUM(p.totalDeductions) as totalDeductions, " +
           "AVG(p.netSalary) as avgNetSalary " +
           "FROM Payroll p " +
           "WHERE p.payrollYear = :year AND p.payrollMonth = :month " +
           "AND p.status != 'CANCELLED'")
    Object[] getPayrollStatisticsForPeriod(@Param("year") Integer year, @Param("month") Integer month);
    
    /**
     * Get yearly payroll statistics for user
     */
    @Query("SELECT " +
           "p.payrollMonth, " +
           "p.grossSalary, " +
           "p.netSalary, " +
           "p.totalDeductions " +
           "FROM Payroll p " +
           "WHERE p.user.id = :userId AND p.payrollYear = :year " +
           "AND p.status != 'CANCELLED' " +
           "ORDER BY p.payrollMonth")
    List<Object[]> getYearlyPayrollForUser(@Param("userId") Long userId, @Param("year") Integer year);
    
    /**
     * Find top earners for period
     */
    @Query("SELECT p FROM Payroll p WHERE p.payrollYear = :year AND p.payrollMonth = :month " +
           "AND p.status != 'CANCELLED' " +
           "ORDER BY p.netSalary DESC")
    List<Payroll> findTopEarnersForPeriod(@Param("year") Integer year, @Param("month") Integer month,
                                         Pageable pageable);
    
    /**
     * Find payrolls with high overtime
     */
    @Query("SELECT p FROM Payroll p WHERE p.overtimeHours > :minOvertimeHours " +
           "AND p.payrollYear = :year AND p.payrollMonth = :month " +
           "ORDER BY p.overtimeHours DESC")
    List<Payroll> findWithHighOvertime(@Param("minOvertimeHours") BigDecimal minOvertimeHours,
                                      @Param("year") Integer year,
                                      @Param("month") Integer month);
    
    /**
     * Find payrolls with attendance issues
     */
    @Query("SELECT p FROM Payroll p WHERE (p.lateArrivals > :maxLateArrivals " +
           "OR p.absentDays > :maxAbsentDays) " +
           "AND p.payrollYear = :year AND p.payrollMonth = :month " +
           "ORDER BY (p.lateArrivals + p.absentDays) DESC")
    List<Payroll> findWithAttendanceIssues(@Param("maxLateArrivals") Integer maxLateArrivals,
                                          @Param("maxAbsentDays") BigDecimal maxAbsentDays,
                                          @Param("year") Integer year,
                                          @Param("month") Integer month);
    
    /**
     * Get department payroll summary
     */
    @Query("SELECT " +
           "u.department, " +
           "COUNT(p), " +
           "SUM(p.grossSalary), " +
           "SUM(p.netSalary), " +
           "AVG(p.netSalary) " +
           "FROM Payroll p " +
           "JOIN p.user u " +
           "WHERE p.payrollYear = :year AND p.payrollMonth = :month " +
           "AND p.status != 'CANCELLED' " +
           "GROUP BY u.department " +
           "ORDER BY SUM(p.netSalary) DESC")
    List<Object[]> getDepartmentPayrollSummary(@Param("year") Integer year, @Param("month") Integer month);
    
    /**
     * Find pending approval payrolls
     */
    @Query("SELECT p FROM Payroll p WHERE p.status = 'CALCULATED' " +
           "ORDER BY p.payrollYear DESC, p.payrollMonth DESC, p.user.fullName")
    Page<Payroll> findPendingApproval(Pageable pageable);
    
    /**
     * Find approved but unpaid payrolls
     */
    @Query("SELECT p FROM Payroll p WHERE p.status = 'APPROVED' " +
           "ORDER BY p.approvedAt ASC")
    List<Payroll> findApprovedButUnpaid();
    
    /**
     * Count payrolls by status for period
     */
    @Query("SELECT p.status, COUNT(p) FROM Payroll p " +
           "WHERE p.payrollYear = :year AND p.payrollMonth = :month " +
           "GROUP BY p.status")
    List<Object[]> countByStatusForPeriod(@Param("year") Integer year, @Param("month") Integer month);
    
    /**
     * Find payrolls created by user
     */
    @Query("SELECT p FROM Payroll p WHERE p.createdBy = :userId " +
           "ORDER BY p.createdAt DESC")
    Page<Payroll> findByCreatedBy(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find payrolls approved by user
     */
    @Query("SELECT p FROM Payroll p WHERE p.approvedBy = :userId " +
           "ORDER BY p.approvedAt DESC")
    Page<Payroll> findByApprovedBy(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Get monthly payroll trends (last 12 months)
     */
    @Query("SELECT " +
           "p.payrollYear, " +
           "p.payrollMonth, " +
           "COUNT(p), " +
           "SUM(p.grossSalary), " +
           "SUM(p.netSalary), " +
           "AVG(p.netSalary) " +
           "FROM Payroll p " +
           "WHERE p.status != 'CANCELLED' " +
           "AND (p.payrollYear > :startYear OR (p.payrollYear = :startYear AND p.payrollMonth >= :startMonth)) " +
           "GROUP BY p.payrollYear, p.payrollMonth " +
           "ORDER BY p.payrollYear DESC, p.payrollMonth DESC")
    List<Object[]> getMonthlyPayrollTrends(@Param("startYear") Integer startYear, 
                                          @Param("startMonth") Integer startMonth);
    
    /**
     * Find duplicate payrolls (same user and period)
     */
    @Query("SELECT p.user.id, p.payrollYear, p.payrollMonth, COUNT(p) " +
           "FROM Payroll p " +
           "GROUP BY p.user.id, p.payrollYear, p.payrollMonth " +
           "HAVING COUNT(p) > 1")
    List<Object[]> findDuplicatePayrolls();
    
    /**
     * Get total company payroll cost for period
     */
    @Query("SELECT SUM(p.grossSalary) FROM Payroll p " +
           "WHERE p.payrollYear = :year AND p.payrollMonth = :month " +
           "AND p.status != 'CANCELLED'")
    BigDecimal getTotalPayrollCostForPeriod(@Param("year") Integer year, @Param("month") Integer month);
    
    /**
     * Find payrolls with calculation errors (negative net salary)
     */
    @Query("SELECT p FROM Payroll p WHERE p.netSalary < 0 " +
           "ORDER BY p.netSalary ASC")
    List<Payroll> findWithCalculationErrors();
    
    /**
     * Check if payroll exists for user and period
     */
    @Query("SELECT COUNT(p) > 0 FROM Payroll p WHERE p.user.id = :userId " +
           "AND p.payrollYear = :year AND p.payrollMonth = :month")
    boolean existsByUserIdAndPeriod(@Param("userId") Long userId,
                                   @Param("year") Integer year,
                                   @Param("month") Integer month);
    
    /**
     * Find latest payroll for user
     */
    @Query("SELECT p FROM Payroll p WHERE p.user.id = :userId " +
           "ORDER BY p.payrollYear DESC, p.payrollMonth DESC")
    Optional<Payroll> findLatestByUserId(@Param("userId") Long userId);
    
    /**
     * Get payroll comparison between periods
     */
    @Query("SELECT " +
           "p1.user.id, " +
           "p1.user.fullName, " +
           "p1.netSalary as currentSalary, " +
           "p2.netSalary as previousSalary, " +
           "(p1.netSalary - p2.netSalary) as difference " +
           "FROM Payroll p1 " +
           "LEFT JOIN Payroll p2 ON p1.user.id = p2.user.id " +
           "WHERE p1.payrollYear = :currentYear AND p1.payrollMonth = :currentMonth " +
           "AND p2.payrollYear = :previousYear AND p2.payrollMonth = :previousMonth " +
           "AND p1.status != 'CANCELLED' AND p2.status != 'CANCELLED' " +
           "ORDER BY (p1.netSalary - p2.netSalary) DESC")
    List<Object[]> getPayrollComparison(@Param("currentYear") Integer currentYear,
                                       @Param("currentMonth") Integer currentMonth,
                                       @Param("previousYear") Integer previousYear,
                                       @Param("previousMonth") Integer previousMonth);
}
