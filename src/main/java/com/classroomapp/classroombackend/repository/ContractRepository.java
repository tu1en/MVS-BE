package com.classroomapp.classroombackend.repository;

import com.classroomapp.classroombackend.model.Contract;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    
    // Find contracts by user ID
    List<Contract> findByUserId(Long userId);
    
    // Find active contract by user ID
    Optional<Contract> findByUserIdAndStatus(Long userId, String status);
    
    // Find contracts by status
    List<Contract> findByStatus(String status);
    
    // Find contracts by status with pagination
    Page<Contract> findByStatus(String status, Pageable pageable);
    
    // Find contracts by contract type
    List<Contract> findByContractType(String contractType);
    
    // Find contracts by department
    List<Contract> findByDepartment(String department);
    
    // Find contracts expiring within a date range
    @Query("SELECT c FROM Contract c WHERE c.endDate BETWEEN :startDate AND :endDate AND c.status = 'ACTIVE'")
    List<Contract> findContractsExpiringBetween(@Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
    
    // Find contracts by salary range
    @Query("SELECT c FROM Contract c WHERE c.salary BETWEEN :minSalary AND :maxSalary")
    List<Contract> findBySalaryBetween(@Param("minSalary") Double minSalary, 
                                      @Param("maxSalary") Double maxSalary);
    
    // Search contracts with filters
    @Query("SELECT c FROM Contract c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:contractType IS NULL OR c.contractType = :contractType) AND " +
           "(:department IS NULL OR c.department = :department) AND " +
           "(:startDate IS NULL OR c.startDate >= :startDate) AND " +
           "(:endDate IS NULL OR c.endDate <= :endDate)")
    Page<Contract> findContractsWithFilters(@Param("status") String status,
                                           @Param("contractType") String contractType,
                                           @Param("department") String department,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           Pageable pageable);
    
    // Count contracts by status
    long countByStatus(String status);
    
    // Count contracts by department
    long countByDepartment(String department);
    
    // Find contracts created by specific user
    List<Contract> findByCreatedBy(String createdBy);

    // Find contract by user, type, and status
    Optional<Contract> findByUserIdAndContractTypeAndStatus(Long userId, String contractType, String status);
}
