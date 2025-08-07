package com.classroomapp.classroombackend.util;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.ArrayList;

/**
 * Utility class for calculating salary components according to Vietnamese labor law
 * Based on the calculation standards provided in the image
 */
public class TopCVCalculation {
    
    // Constants for Vietnamese labor law
    private static final BigDecimal EMPLOYEE_CONTRIBUTION_RATE = new BigDecimal("0.105"); // 10.5%
    private static final BigDecimal EMPLOYER_CONTRIBUTION_RATE = new BigDecimal("0.215"); // 21.5%
    private static final BigDecimal TOTAL_CONTRIBUTION_RATE = new BigDecimal("0.32"); // 32%
    
    // Personal Income Tax brackets (progressive tax)
    private static final BigDecimal TAX_FREE_THRESHOLD = new BigDecimal("11000000"); // 11 million VND
    private static final BigDecimal TAX_RATE_10_PERCENT = new BigDecimal("0.10"); // 10%
    
    // Deduction for dependents (example values - should be researched from law)
    private static final BigDecimal DEPENDENT_DEDUCTION = new BigDecimal("4400000"); // 4.4 million per dependent
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalaryCalculationResult {
        private BigDecimal grossSalary;
        private BigDecimal netSalary;
        private BigDecimal employeeContribution;
        private BigDecimal employerContribution;
        private BigDecimal totalInsuranceContribution;
        private BigDecimal personalIncomeTax;
        private BigDecimal dependentDeductions;
        private BigDecimal taxableIncome;
        private List<TaxBracket> taxBrackets;
        private String calculationType; // "GROSS_TO_NET" or "NET_TO_GROSS"
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxBracket {
        private BigDecimal fromAmount;
        private BigDecimal toAmount;
        private BigDecimal taxRate;
        private BigDecimal taxableAmount;
        private BigDecimal taxAmount;
    }
    
    /**
     * Calculate salary from GROSS to NET
     * @param grossSalary The gross salary amount
     * @param numberOfDependents Number of dependents for tax deduction
     * @return SalaryCalculationResult with detailed breakdown
     */
    public static SalaryCalculationResult calculateFromGrossToNet(BigDecimal grossSalary, int numberOfDependents) {
        SalaryCalculationResult result = new SalaryCalculationResult();
        result.setGrossSalary(grossSalary);
        result.setCalculationType("GROSS_TO_NET");
        
        // Calculate insurance contributions
        BigDecimal employeeContribution = grossSalary.multiply(EMPLOYEE_CONTRIBUTION_RATE);
        BigDecimal employerContribution = grossSalary.multiply(EMPLOYER_CONTRIBUTION_RATE);
        BigDecimal totalInsuranceContribution = grossSalary.multiply(TOTAL_CONTRIBUTION_RATE);
        
        result.setEmployeeContribution(employeeContribution);
        result.setEmployerContribution(employerContribution);
        result.setTotalInsuranceContribution(totalInsuranceContribution);
        
        // Calculate dependent deductions
        BigDecimal dependentDeductions = DEPENDENT_DEDUCTION.multiply(new BigDecimal(numberOfDependents));
        result.setDependentDeductions(dependentDeductions);
        
        // Calculate taxable income
        BigDecimal taxableIncome = grossSalary.subtract(dependentDeductions);
        result.setTaxableIncome(taxableIncome);
        
        // Calculate personal income tax
        BigDecimal personalIncomeTax = calculateProgressiveTax(taxableIncome);
        result.setPersonalIncomeTax(personalIncomeTax);
        
        // Calculate net salary
        BigDecimal netSalary = grossSalary.subtract(employeeContribution).subtract(personalIncomeTax);
        result.setNetSalary(netSalary);
        
        return result;
    }
    
    /**
     * Calculate salary from NET to GROSS (reverse calculation)
     * @param netSalary The net salary amount
     * @param numberOfDependents Number of dependents for tax deduction
     * @return SalaryCalculationResult with detailed breakdown
     */
    public static SalaryCalculationResult calculateFromNetToGross(BigDecimal netSalary, int numberOfDependents) {
        // This is a more complex calculation that requires iteration
        // For simplicity, we'll use an approximation method
        
        BigDecimal estimatedGrossSalary = netSalary.multiply(new BigDecimal("1.5")); // Rough estimate
        BigDecimal tolerance = new BigDecimal("1000"); // 1000 VND tolerance
        BigDecimal maxIterations = new BigDecimal("100");
        BigDecimal iteration = BigDecimal.ZERO;
        
        while (iteration.compareTo(maxIterations) < 0) {
            SalaryCalculationResult tempResult = calculateFromGrossToNet(estimatedGrossSalary, numberOfDependents);
            BigDecimal calculatedNet = tempResult.getNetSalary();
            
            BigDecimal difference = calculatedNet.subtract(netSalary).abs();
            if (difference.compareTo(tolerance) <= 0) {
                return tempResult;
            }
            
            // Adjust gross salary based on difference
            if (calculatedNet.compareTo(netSalary) > 0) {
                estimatedGrossSalary = estimatedGrossSalary.subtract(difference.multiply(new BigDecimal("1.2")));
            } else {
                estimatedGrossSalary = estimatedGrossSalary.add(difference.multiply(new BigDecimal("1.2")));
            }
            
            iteration = iteration.add(BigDecimal.ONE);
        }
        
        // If iteration doesn't converge, return the last calculated result
        return calculateFromGrossToNet(estimatedGrossSalary, numberOfDependents);
    }
    
    /**
     * Calculate progressive personal income tax
     * @param taxableIncome The taxable income amount
     * @return Total tax amount
     */
    private static BigDecimal calculateProgressiveTax(BigDecimal taxableIncome) {
        List<TaxBracket> taxBrackets = new ArrayList<>();
        BigDecimal totalTax = BigDecimal.ZERO;
        
        // Tax bracket 1: 0 - 5 million (5%)
        BigDecimal bracket1Limit = new BigDecimal("5000000");
        BigDecimal bracket1Rate = new BigDecimal("0.05");
        
        if (taxableIncome.compareTo(bracket1Limit) <= 0) {
            BigDecimal taxAmount = taxableIncome.multiply(bracket1Rate);
            taxBrackets.add(new TaxBracket(BigDecimal.ZERO, bracket1Limit, bracket1Rate, taxableIncome, taxAmount));
            totalTax = totalTax.add(taxAmount);
        } else {
            BigDecimal taxAmount = bracket1Limit.multiply(bracket1Rate);
            taxBrackets.add(new TaxBracket(BigDecimal.ZERO, bracket1Limit, bracket1Rate, bracket1Limit, taxAmount));
            totalTax = totalTax.add(taxAmount);
            
            // Tax bracket 2: 5 million - 10 million (10%)
            BigDecimal bracket2Limit = new BigDecimal("10000000");
            BigDecimal bracket2Rate = new BigDecimal("0.10");
            
            if (taxableIncome.compareTo(bracket2Limit) <= 0) {
                BigDecimal taxableAmount2 = taxableIncome.subtract(bracket1Limit);
                BigDecimal taxAmount2 = taxableAmount2.multiply(bracket2Rate);
                taxBrackets.add(new TaxBracket(bracket1Limit, bracket2Limit, bracket2Rate, taxableAmount2, taxAmount2));
                totalTax = totalTax.add(taxAmount2);
            } else {
                BigDecimal taxableAmount2 = bracket2Limit.subtract(bracket1Limit);
                BigDecimal taxAmount2 = taxableAmount2.multiply(bracket2Rate);
                taxBrackets.add(new TaxBracket(bracket1Limit, bracket2Limit, bracket2Rate, taxableAmount2, taxAmount2));
                totalTax = totalTax.add(taxAmount2);
                
                // Tax bracket 3: 10 million - 18 million (15%)
                BigDecimal bracket3Limit = new BigDecimal("18000000");
                BigDecimal bracket3Rate = new BigDecimal("0.15");
                
                if (taxableIncome.compareTo(bracket3Limit) <= 0) {
                    BigDecimal taxableAmount3 = taxableIncome.subtract(bracket2Limit);
                    BigDecimal taxAmount3 = taxableAmount3.multiply(bracket3Rate);
                    taxBrackets.add(new TaxBracket(bracket2Limit, bracket3Limit, bracket3Rate, taxableAmount3, taxAmount3));
                    totalTax = totalTax.add(taxAmount3);
                } else {
                    BigDecimal taxableAmount3 = bracket3Limit.subtract(bracket2Limit);
                    BigDecimal taxAmount3 = taxableAmount3.multiply(bracket3Rate);
                    taxBrackets.add(new TaxBracket(bracket2Limit, bracket3Limit, bracket3Rate, taxableAmount3, taxAmount3));
                    totalTax = totalTax.add(taxAmount3);
                    
                    // Tax bracket 4: 18 million - 32 million (20%)
                    BigDecimal bracket4Limit = new BigDecimal("32000000");
                    BigDecimal bracket4Rate = new BigDecimal("0.20");
                    
                    if (taxableIncome.compareTo(bracket4Limit) <= 0) {
                        BigDecimal taxableAmount4 = taxableIncome.subtract(bracket3Limit);
                        BigDecimal taxAmount4 = taxableAmount4.multiply(bracket4Rate);
                        taxBrackets.add(new TaxBracket(bracket3Limit, bracket4Limit, bracket4Rate, taxableAmount4, taxAmount4));
                        totalTax = totalTax.add(taxAmount4);
                    } else {
                        BigDecimal taxableAmount4 = bracket4Limit.subtract(bracket3Limit);
                        BigDecimal taxAmount4 = taxableAmount4.multiply(bracket4Rate);
                        taxBrackets.add(new TaxBracket(bracket3Limit, bracket4Limit, bracket4Rate, taxableAmount4, taxAmount4));
                        totalTax = totalTax.add(taxAmount4);
                        
                        // Tax bracket 5: 32 million - 52 million (25%)
                        BigDecimal bracket5Limit = new BigDecimal("52000000");
                        BigDecimal bracket5Rate = new BigDecimal("0.25");
                        
                        if (taxableIncome.compareTo(bracket5Limit) <= 0) {
                            BigDecimal taxableAmount5 = taxableIncome.subtract(bracket4Limit);
                            BigDecimal taxAmount5 = taxableAmount5.multiply(bracket5Rate);
                            taxBrackets.add(new TaxBracket(bracket4Limit, bracket5Limit, bracket5Rate, taxableAmount5, taxAmount5));
                            totalTax = totalTax.add(taxAmount5);
                        } else {
                            BigDecimal taxableAmount5 = bracket5Limit.subtract(bracket4Limit);
                            BigDecimal taxAmount5 = taxableAmount5.multiply(bracket5Rate);
                            taxBrackets.add(new TaxBracket(bracket4Limit, bracket5Limit, bracket5Rate, taxableAmount5, taxAmount5));
                            totalTax = totalTax.add(taxAmount5);
                            
                            // Tax bracket 6: 52 million - 80 million (30%)
                            BigDecimal bracket6Limit = new BigDecimal("80000000");
                            BigDecimal bracket6Rate = new BigDecimal("0.30");
                            
                            if (taxableIncome.compareTo(bracket6Limit) <= 0) {
                                BigDecimal taxableAmount6 = taxableIncome.subtract(bracket5Limit);
                                BigDecimal taxAmount6 = taxableAmount6.multiply(bracket6Rate);
                                taxBrackets.add(new TaxBracket(bracket5Limit, bracket6Limit, bracket6Rate, taxableAmount6, taxAmount6));
                                totalTax = totalTax.add(taxAmount6);
                            } else {
                                BigDecimal taxableAmount6 = bracket6Limit.subtract(bracket5Limit);
                                BigDecimal taxAmount6 = taxableAmount6.multiply(bracket6Rate);
                                taxBrackets.add(new TaxBracket(bracket5Limit, bracket6Limit, bracket6Rate, taxableAmount6, taxAmount6));
                                totalTax = totalTax.add(taxAmount6);
                                
                                // Tax bracket 7: Above 80 million (35%)
                                BigDecimal bracket7Rate = new BigDecimal("0.35");
                                BigDecimal taxableAmount7 = taxableIncome.subtract(bracket6Limit);
                                BigDecimal taxAmount7 = taxableAmount7.multiply(bracket7Rate);
                                taxBrackets.add(new TaxBracket(bracket6Limit, null, bracket7Rate, taxableAmount7, taxAmount7));
                                totalTax = totalTax.add(taxAmount7);
                            }
                        }
                    }
                }
            }
        }
        
        return totalTax.setScale(0, RoundingMode.HALF_UP);
    }
    
    /**
     * Format currency for display
     * @param amount The amount to format
     * @return Formatted currency string
     */
    public static String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        return amount.setScale(0, RoundingMode.HALF_UP).toString() + " VNĐ";
    }
    
    /**
     * Calculate example with 85,000,000 VNĐ gross salary
     * @return Example calculation result
     */
    public static SalaryCalculationResult calculateExample() {
        BigDecimal grossSalary = new BigDecimal("85000000");
        return calculateFromGrossToNet(grossSalary, 0);
    }
}
