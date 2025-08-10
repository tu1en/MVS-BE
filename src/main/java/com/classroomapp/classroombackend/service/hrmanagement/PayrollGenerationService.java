package com.classroomapp.classroombackend.service.hrmanagement;

import com.classroomapp.classroombackend.model.hrmanagement.PayrollResult;
import java.time.YearMonth;
import java.util.List;

/**
 * Service for generating payroll using TopCV calculations and contract data
 */
public interface PayrollGenerationService {
    
    /**
     * Generate payroll for a specific user and month
     * @param userId The user ID
     * @param period The payroll period (year-month)
     * @return Payroll result with TopCV calculations
     */
    PayrollResult generatePayrollForUser(Long userId, YearMonth period);
    
    /**
     * Generate payroll for all active employees in a month
     * @param period The payroll period (year-month)
     * @return List of payroll results
     */
    List<PayrollResult> generatePayrollForAllEmployees(YearMonth period);
    
    /**
     * Generate payroll for employees by contract type
     * @param contractType The contract type ("TEACHER", "ACCOUNTANT", etc.)
     * @param period The payroll period (year-month)
     * @return List of payroll results
     */
    List<PayrollResult> generatePayrollByContractType(String contractType, YearMonth period);
    
    /**
     * Get payroll history for a user
     * @param userId The user ID
     * @param fromPeriod Start period
     * @param toPeriod End period
     * @return List of historical payroll results
     */
    List<PayrollResult> getPayrollHistory(Long userId, YearMonth fromPeriod, YearMonth toPeriod);
}