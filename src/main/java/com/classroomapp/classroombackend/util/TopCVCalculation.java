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
 * Based on the latest calculation standards from TopCV
 */
public class TopCVCalculation {
    
    // Constants for Vietnamese labor law (updated 2024)
    // Employee contribution rates (10.5% total)
    private static final BigDecimal SOCIAL_INSURANCE_EMPLOYEE_RATE = new BigDecimal("0.08"); // 8% BHXH
    private static final BigDecimal HEALTH_INSURANCE_EMPLOYEE_RATE = new BigDecimal("0.015"); // 1.5% BHYT
    private static final BigDecimal UNEMPLOYMENT_INSURANCE_EMPLOYEE_RATE = new BigDecimal("0.01"); // 1% BHTN
    private static final BigDecimal TOTAL_EMPLOYEE_CONTRIBUTION_RATE = new BigDecimal("0.105"); // 10.5%
    
    // Employer contribution rates (21.5% total)
    private static final BigDecimal SOCIAL_INSURANCE_EMPLOYER_RATE = new BigDecimal("0.17"); // 17% BHXH
    private static final BigDecimal HEALTH_INSURANCE_EMPLOYER_RATE = new BigDecimal("0.03"); // 3% BHYT
    private static final BigDecimal UNEMPLOYMENT_INSURANCE_EMPLOYER_RATE = new BigDecimal("0.01"); // 1% BHTN
    private static final BigDecimal WORK_ACCIDENT_INSURANCE_RATE = new BigDecimal("0.005"); // 0.5% BHTNLĐ
    private static final BigDecimal TOTAL_EMPLOYER_CONTRIBUTION_RATE = new BigDecimal("0.215"); // 21.5%
    
    // Personal Income Tax constants
    private static final BigDecimal PERSONAL_DEDUCTION = new BigDecimal("11000000"); // 11 triệu VNĐ
    private static final BigDecimal DEPENDENT_DEDUCTION = new BigDecimal("4400000"); // 4.4 triệu VNĐ per dependent
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SalaryCalculationResult {
        private BigDecimal grossSalary;
        private BigDecimal netSalary;
        private BigDecimal incomeBeforeTax;
        private InsuranceDetails insuranceDetails;
        private BigDecimal personalIncomeTax;
        private BigDecimal dependentDeductions;
        private BigDecimal taxableIncome;
        private List<TaxBracket> taxBrackets;
        private String calculationType; // "GROSS_TO_NET" or "NET_TO_GROSS"
        private Integer numberOfDependents;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsuranceDetails {
        private BigDecimal socialInsuranceEmployee; // BHXH nhân viên
        private BigDecimal healthInsuranceEmployee; // BHYT nhân viên
        private BigDecimal unemploymentInsuranceEmployee; // BHTN nhân viên
        private BigDecimal totalEmployeeContribution; // Tổng đóng góp nhân viên
        
        private BigDecimal socialInsuranceEmployer; // BHXH công ty
        private BigDecimal healthInsuranceEmployer; // BHYT công ty
        private BigDecimal unemploymentInsuranceEmployer; // BHTN công ty
        private BigDecimal workAccidentInsurance; // BHTNLĐ
        private BigDecimal totalEmployerContribution; // Tổng đóng góp công ty
        
        private BigDecimal totalInsuranceContribution; // Tổng bảo hiểm
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
        result.setNumberOfDependents(numberOfDependents);
        
        // Calculate detailed insurance contributions
        InsuranceDetails insuranceDetails = calculateInsuranceDetails(grossSalary);
        result.setInsuranceDetails(insuranceDetails);
        
        // Calculate dependent deductions
        BigDecimal dependentDeductions = DEPENDENT_DEDUCTION.multiply(new BigDecimal(numberOfDependents));
        result.setDependentDeductions(dependentDeductions);
        
        // Calculate income before tax: Gross - Employee contributions
        BigDecimal incomeBeforeTax = grossSalary.subtract(insuranceDetails.getTotalEmployeeContribution());
        result.setIncomeBeforeTax(incomeBeforeTax);
        
        // Calculate taxable income: Income before tax - Personal deduction - Dependent deductions
        BigDecimal taxableIncome = incomeBeforeTax.subtract(PERSONAL_DEDUCTION).subtract(dependentDeductions);
        if (taxableIncome.compareTo(BigDecimal.ZERO) < 0) {
            taxableIncome = BigDecimal.ZERO;
        }
        result.setTaxableIncome(taxableIncome);
        
        // Calculate personal income tax
        BigDecimal personalIncomeTax = calculateProgressiveTax(taxableIncome);
        result.setPersonalIncomeTax(personalIncomeTax);
        
        // Calculate net salary: Gross - Employee contributions - Personal income tax
        BigDecimal netSalary = grossSalary.subtract(insuranceDetails.getTotalEmployeeContribution()).subtract(personalIncomeTax);
        result.setNetSalary(netSalary);
        
        // Round all monetary values to integers for consistency
        result.setGrossSalary(result.getGrossSalary().setScale(0, RoundingMode.HALF_UP));
        result.setNetSalary(result.getNetSalary().setScale(0, RoundingMode.HALF_UP));
        result.setIncomeBeforeTax(result.getIncomeBeforeTax().setScale(0, RoundingMode.HALF_UP));
        result.setPersonalIncomeTax(result.getPersonalIncomeTax().setScale(0, RoundingMode.HALF_UP));
        result.setDependentDeductions(result.getDependentDeductions().setScale(0, RoundingMode.HALF_UP));
        result.setTaxableIncome(result.getTaxableIncome().setScale(0, RoundingMode.HALF_UP));
        
        // Round insurance details
        if (result.getInsuranceDetails() != null) {
            InsuranceDetails insurance = result.getInsuranceDetails();
            insurance.setSocialInsuranceEmployee(insurance.getSocialInsuranceEmployee().setScale(0, RoundingMode.HALF_UP));
            insurance.setHealthInsuranceEmployee(insurance.getHealthInsuranceEmployee().setScale(0, RoundingMode.HALF_UP));
            insurance.setUnemploymentInsuranceEmployee(insurance.getUnemploymentInsuranceEmployee().setScale(0, RoundingMode.HALF_UP));
            insurance.setTotalEmployeeContribution(insurance.getTotalEmployeeContribution().setScale(0, RoundingMode.HALF_UP));
            insurance.setSocialInsuranceEmployer(insurance.getSocialInsuranceEmployer().setScale(0, RoundingMode.HALF_UP));
            insurance.setHealthInsuranceEmployer(insurance.getHealthInsuranceEmployer().setScale(0, RoundingMode.HALF_UP));
            insurance.setUnemploymentInsuranceEmployer(insurance.getUnemploymentInsuranceEmployer().setScale(0, RoundingMode.HALF_UP));
            insurance.setWorkAccidentInsurance(insurance.getWorkAccidentInsurance().setScale(0, RoundingMode.HALF_UP));
            insurance.setTotalEmployerContribution(insurance.getTotalEmployerContribution().setScale(0, RoundingMode.HALF_UP));
            insurance.setTotalInsuranceContribution(insurance.getTotalInsuranceContribution().setScale(0, RoundingMode.HALF_UP));
        }
        
        return result;
    }
    
    /**
     * Calculate detailed insurance contributions
     * @param grossSalary The gross salary amount
     * @return InsuranceDetails with all insurance breakdowns
     */
    private static InsuranceDetails calculateInsuranceDetails(BigDecimal grossSalary) {
        InsuranceDetails details = new InsuranceDetails();
        
        // Employee contributions (10.5% total)
        details.setSocialInsuranceEmployee(grossSalary.multiply(SOCIAL_INSURANCE_EMPLOYEE_RATE));
        details.setHealthInsuranceEmployee(grossSalary.multiply(HEALTH_INSURANCE_EMPLOYEE_RATE));
        details.setUnemploymentInsuranceEmployee(grossSalary.multiply(UNEMPLOYMENT_INSURANCE_EMPLOYEE_RATE));
        details.setTotalEmployeeContribution(grossSalary.multiply(TOTAL_EMPLOYEE_CONTRIBUTION_RATE));
        
        // Employer contributions (21.5% total)
        details.setSocialInsuranceEmployer(grossSalary.multiply(SOCIAL_INSURANCE_EMPLOYER_RATE));
        details.setHealthInsuranceEmployer(grossSalary.multiply(HEALTH_INSURANCE_EMPLOYER_RATE));
        details.setUnemploymentInsuranceEmployer(grossSalary.multiply(UNEMPLOYMENT_INSURANCE_EMPLOYER_RATE));
        details.setWorkAccidentInsurance(grossSalary.multiply(WORK_ACCIDENT_INSURANCE_RATE));
        details.setTotalEmployerContribution(grossSalary.multiply(TOTAL_EMPLOYER_CONTRIBUTION_RATE));
        
        // Total insurance contribution (32%)
        details.setTotalInsuranceContribution(grossSalary.multiply(new BigDecimal("0.32")));
        
        return details;
    }
    
    /**
     * Calculate salary from NET to GROSS (reverse calculation)
     * @param netSalary The net salary amount
     * @param numberOfDependents Number of dependents for tax deduction
     * @return SalaryCalculationResult with detailed breakdown
     */
    public static SalaryCalculationResult calculateFromNetToGross(BigDecimal netSalary, int numberOfDependents) {
        // Use a more direct calculation approach
        // Net = Gross - Employee Insurance - Tax
        // Net = Gross - (Gross * 0.105) - Tax
        // Net = Gross * (1 - 0.105) - Tax
        // Net = Gross * 0.895 - Tax
        
        // First, estimate gross without tax consideration
        BigDecimal estimatedGrossWithoutTax = netSalary.divide(new BigDecimal("0.895"), 0, RoundingMode.HALF_UP);
        
        // Calculate tax for this estimated gross
        SalaryCalculationResult tempResult = calculateFromGrossToNet(estimatedGrossWithoutTax, numberOfDependents);
        BigDecimal calculatedNet = tempResult.getNetSalary();
        
        // If the calculated net is close to target net, return the result
        BigDecimal difference = calculatedNet.subtract(netSalary).abs();
        if (difference.compareTo(new BigDecimal("10000")) <= 0) {
            return tempResult;
        }
        
        // Otherwise, use binary search for more accurate result
        BigDecimal low = netSalary;
        BigDecimal high = estimatedGrossWithoutTax.multiply(new BigDecimal("1.5"));
        BigDecimal bestGross = estimatedGrossWithoutTax;
        BigDecimal bestDifference = difference;
        
        for (int i = 0; i < 20; i++) {
            BigDecimal mid = low.add(high).divide(new BigDecimal("2"), 0, RoundingMode.HALF_UP);
            SalaryCalculationResult midResult = calculateFromGrossToNet(mid, numberOfDependents);
            BigDecimal midNet = midResult.getNetSalary();
            BigDecimal midDifference = midNet.subtract(netSalary).abs();
            
            if (midDifference.compareTo(bestDifference) < 0) {
                bestDifference = midDifference;
                bestGross = mid;
            }
            
            if (midNet.compareTo(netSalary) > 0) {
                high = mid;
            } else {
                low = mid;
            }
            
            // If we're close enough, break
            if (midDifference.compareTo(new BigDecimal("1000")) <= 0) {
                bestGross = mid;
                break;
            }
        }
        
        // Ensure the result has integer values for better user experience
        SalaryCalculationResult result = calculateFromGrossToNet(bestGross, numberOfDependents);
        
        // Round all monetary values to integers
        result.setGrossSalary(result.getGrossSalary().setScale(0, RoundingMode.HALF_UP));
        result.setNetSalary(result.getNetSalary().setScale(0, RoundingMode.HALF_UP));
        result.setIncomeBeforeTax(result.getIncomeBeforeTax().setScale(0, RoundingMode.HALF_UP));
        result.setPersonalIncomeTax(result.getPersonalIncomeTax().setScale(0, RoundingMode.HALF_UP));
        result.setDependentDeductions(result.getDependentDeductions().setScale(0, RoundingMode.HALF_UP));
        result.setTaxableIncome(result.getTaxableIncome().setScale(0, RoundingMode.HALF_UP));
        
        // Round insurance details
        if (result.getInsuranceDetails() != null) {
            InsuranceDetails insurance = result.getInsuranceDetails();
            insurance.setSocialInsuranceEmployee(insurance.getSocialInsuranceEmployee().setScale(0, RoundingMode.HALF_UP));
            insurance.setHealthInsuranceEmployee(insurance.getHealthInsuranceEmployee().setScale(0, RoundingMode.HALF_UP));
            insurance.setUnemploymentInsuranceEmployee(insurance.getUnemploymentInsuranceEmployee().setScale(0, RoundingMode.HALF_UP));
            insurance.setTotalEmployeeContribution(insurance.getTotalEmployeeContribution().setScale(0, RoundingMode.HALF_UP));
            insurance.setSocialInsuranceEmployer(insurance.getSocialInsuranceEmployer().setScale(0, RoundingMode.HALF_UP));
            insurance.setHealthInsuranceEmployer(insurance.getHealthInsuranceEmployer().setScale(0, RoundingMode.HALF_UP));
            insurance.setUnemploymentInsuranceEmployer(insurance.getUnemploymentInsuranceEmployer().setScale(0, RoundingMode.HALF_UP));
            insurance.setWorkAccidentInsurance(insurance.getWorkAccidentInsurance().setScale(0, RoundingMode.HALF_UP));
            insurance.setTotalEmployerContribution(insurance.getTotalEmployerContribution().setScale(0, RoundingMode.HALF_UP));
            insurance.setTotalInsuranceContribution(insurance.getTotalInsuranceContribution().setScale(0, RoundingMode.HALF_UP));
        }
        
        return result;
    }
    
    /**
     * Calculate progressive personal income tax according to Vietnamese law
     * Tax brackets: 5%, 10%, 15%, 20%, 25%, 30%, 35%
     * @param taxableIncome The taxable income amount
     * @return Total tax amount
     */
    private static BigDecimal calculateProgressiveTax(BigDecimal taxableIncome) {
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
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
