package com.classroomapp.classroombackend.repository.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.SalaryStructure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for SalaryStructure entity
 */
@Repository
public interface SalaryStructureRepository extends JpaRepository<SalaryStructure, Long> {
    
    /**
     * Find active salary structure for user at specific date
     */
    @Query("SELECT s FROM SalaryStructure s WHERE s.user.id = :userId " +
           "AND s.isActive = true " +
           "AND s.effectiveDate <= :date " +
           "AND (s.endDate IS NULL OR s.endDate >= :date) " +
           "ORDER BY s.effectiveDate DESC")
    Optional<SalaryStructure> findActiveByUserIdAndDate(@Param("userId") Long userId, 
                                                       @Param("date") LocalDate date);
    
    /**
     * Find current active salary structure for user
     */
    @Query("SELECT s FROM SalaryStructure s WHERE s.user.id = :userId " +
           "AND s.isActive = true " +
           "AND s.effectiveDate <= CURRENT_DATE " +
           "AND (s.endDate IS NULL OR s.endDate >= CURRENT_DATE) " +
           "ORDER BY s.effectiveDate DESC")
    Optional<SalaryStructure> findCurrentActiveByUserId(@Param("userId") Long userId);
    
    /**
     * Find all salary structures for user
     */
    @Query("SELECT s FROM SalaryStructure s WHERE s.user.id = :userId " +
           "ORDER BY s.effectiveDate DESC")
    Page<SalaryStructure> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find all active salary structures
     */
    @Query("SELECT s FROM SalaryStructure s WHERE s.isActive = true " +
           "ORDER BY s.user.fullName, s.effectiveDate DESC")
    Page<SalaryStructure> findAllActive(Pageable pageable);
    
    /**
     * Find salary structures by salary type
     */
    @Query("SELECT s FROM SalaryStructure s WHERE s.salaryType = :salaryType " +
           "AND s.isActive = true " +
           "ORDER BY s.user.fullName")
    Page<SalaryStructure> findBySalaryType(@Param("salaryType") SalaryStructure.SalaryType salaryType, 
                                          Pageable pageable);
    
    /**
     * Find salary structures effective in date range
     */
    @Query("SELECT s FROM SalaryStructure s WHERE s.isActive = true " +
           "AND s.effectiveDate <= :endDate " +
           "AND (s.endDate IS NULL OR s.endDate >= :startDate) " +
           "ORDER BY s.user.fullName, s.effectiveDate DESC")
    List<SalaryStructure> findEffectiveInDateRange(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
    
    /**
     * Find overlapping salary structures for user
     */
    @Query("SELECT s FROM SalaryStructure s WHERE s.user.id = :userId " +
           "AND s.id != :excludeId " +
           "AND s.isActive = true " +
           "AND s.effectiveDate <= :endDate " +
           "AND (s.endDate IS NULL OR s.endDate >= :startDate)")
    List<SalaryStructure> findOverlappingForUser(@Param("userId") Long userId,
                                               @Param("excludeId") Long excludeId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    
    /**
     * Find salary structures by base salary range
     */
    @Query("SELECT s FROM SalaryStructure s WHERE s.isActive = true " +
           "AND s.baseSalary >= :minSalary " +
           "AND s.baseSalary <= :maxSalary " +
           "ORDER BY s.baseSalary DESC")
    Page<SalaryStructure> findBySalaryRange(@Param("minSalary") java.math.BigDecimal minSalary,
                                           @Param("maxSalary") java.math.BigDecimal maxSalary,
                                           Pageable pageable);
    
    /**
     * Find users without active salary structure
     */
    @Query("SELECT u.id FROM User u WHERE u.id NOT IN " +
           "(SELECT s.user.id FROM SalaryStructure s WHERE s.isActive = true " +
           "AND s.effectiveDate <= CURRENT_DATE " +
           "AND (s.endDate IS NULL OR s.endDate >= CURRENT_DATE))")
    List<Long> findUsersWithoutActiveSalaryStructure();
    
    /**
     * Count active salary structures by type
     */
    @Query("SELECT s.salaryType, COUNT(s) FROM SalaryStructure s " +
           "WHERE s.isActive = true " +
           "AND s.effectiveDate <= CURRENT_DATE " +
           "AND (s.endDate IS NULL OR s.endDate >= CURRENT_DATE) " +
           "GROUP BY s.salaryType")
    List<Object[]> countActiveBySalaryType();
    
    /**
     * Get salary statistics
     */
    @Query("SELECT " +
           "MIN(s.baseSalary) as minSalary, " +
           "MAX(s.baseSalary) as maxSalary, " +
           "AVG(s.baseSalary) as avgSalary, " +
           "COUNT(s) as totalCount " +
           "FROM SalaryStructure s " +
           "WHERE s.isActive = true " +
           "AND s.effectiveDate <= CURRENT_DATE " +
           "AND (s.endDate IS NULL OR s.endDate >= CURRENT_DATE)")
    Object[] getSalaryStatistics();
    
    /**
     * Find salary structures expiring soon
     */
    @Query("SELECT s FROM SalaryStructure s WHERE s.isActive = true " +
           "AND s.endDate IS NOT NULL " +
           "AND s.endDate BETWEEN CURRENT_DATE AND :futureDate " +
           "ORDER BY s.endDate ASC")
    List<SalaryStructure> findExpiringSoon(@Param("futureDate") LocalDate futureDate);
    
    /**
     * Find salary structures created by user
     */
    @Query("SELECT s FROM SalaryStructure s WHERE s.createdBy = :userId " +
           "ORDER BY s.createdAt DESC")
    Page<SalaryStructure> findByCreatedBy(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find salary structures updated in date range
     */
    @Query("SELECT s FROM SalaryStructure s WHERE s.updatedAt BETWEEN :startDate AND :endDate " +
           "ORDER BY s.updatedAt DESC")
    List<SalaryStructure> findUpdatedInDateRange(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);
    
    /**
     * Check if user has salary structure for date
     */
    @Query("SELECT COUNT(s) > 0 FROM SalaryStructure s WHERE s.user.id = :userId " +
           "AND s.isActive = true " +
           "AND s.effectiveDate <= :date " +
           "AND (s.endDate IS NULL OR s.endDate >= :date)")
    boolean existsByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    /**
     * Find salary structures by department (assuming user has department)
     */
    @Query("SELECT s FROM SalaryStructure s " +
           "JOIN s.user u " +
           "WHERE u.department = :department " +
           "AND s.isActive = true " +
           "ORDER BY s.baseSalary DESC")
    Page<SalaryStructure> findByDepartment(@Param("department") String department, Pageable pageable);
    
    /**
     * Get average salary by department
     */
    @Query("SELECT u.department, AVG(s.baseSalary) " +
           "FROM SalaryStructure s " +
           "JOIN s.user u " +
           "WHERE s.isActive = true " +
           "AND s.effectiveDate <= CURRENT_DATE " +
           "AND (s.endDate IS NULL OR s.endDate >= CURRENT_DATE) " +
           "GROUP BY u.department " +
           "ORDER BY AVG(s.baseSalary) DESC")
    List<Object[]> getAverageSalaryByDepartment();
    
    /**
     * Find salary structures with specific allowance types
     */
    @Query("SELECT s FROM SalaryStructure s WHERE s.isActive = true " +
           "AND (:hasPositionAllowance = false OR s.positionAllowance > 0) " +
           "AND (:hasTransportAllowance = false OR s.transportAllowance > 0) " +
           "AND (:hasMealAllowance = false OR s.mealAllowance > 0) " +
           "ORDER BY s.user.fullName")
    Page<SalaryStructure> findWithAllowances(@Param("hasPositionAllowance") boolean hasPositionAllowance,
                                           @Param("hasTransportAllowance") boolean hasTransportAllowance,
                                           @Param("hasMealAllowance") boolean hasMealAllowance,
                                           Pageable pageable);
    
    /**
     * Get total allowances statistics
     */
    @Query("SELECT " +
           "SUM(s.positionAllowance) as totalPositionAllowance, " +
           "SUM(s.transportAllowance) as totalTransportAllowance, " +
           "SUM(s.mealAllowance) as totalMealAllowance, " +
           "SUM(s.phoneAllowance) as totalPhoneAllowance, " +
           "SUM(s.otherAllowances) as totalOtherAllowances " +
           "FROM SalaryStructure s " +
           "WHERE s.isActive = true " +
           "AND s.effectiveDate <= CURRENT_DATE " +
           "AND (s.endDate IS NULL OR s.endDate >= CURRENT_DATE)")
    Object[] getAllowancesStatistics();
}
