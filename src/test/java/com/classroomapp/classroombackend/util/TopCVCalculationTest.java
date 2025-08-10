package com.classroomapp.classroombackend.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class TopCVCalculationTest {

    @Test
    @DisplayName("Test basic GROSS to NET calculation")
    public void testCalculateFromGrossToNet() {
        // Test với lương GROSS 85,000,000 VNĐ
        BigDecimal grossSalary = new BigDecimal("85000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        // Kiểm tra các giá trị cơ bản
        assertEquals(grossSalary, result.getGrossSalary());
        assertEquals("GROSS_TO_NET", result.getCalculationType());
        
        // Kiểm tra đóng góp bảo hiểm
        BigDecimal expectedEmployeeContribution = grossSalary.multiply(new BigDecimal("0.105"));
        assertEquals(expectedEmployeeContribution, result.getInsuranceDetails().getTotalEmployeeContribution());
        
        BigDecimal expectedEmployerContribution = grossSalary.multiply(new BigDecimal("0.215"));
        assertEquals(expectedEmployerContribution, result.getInsuranceDetails().getTotalEmployerContribution());
        
        BigDecimal expectedTotalInsurance = grossSalary.multiply(new BigDecimal("0.32"));
        assertEquals(expectedTotalInsurance, result.getInsuranceDetails().getTotalInsuranceContribution());
        
        // Kiểm tra lương NET phải nhỏ hơn GROSS
        assertTrue(result.getNetSalary().compareTo(grossSalary) < 0);
        
        // Kiểm tra thuế TNCN phải lớn hơn 0
        assertTrue(result.getPersonalIncomeTax().compareTo(BigDecimal.ZERO) > 0);
        
        System.out.println("Test passed for gross salary: " + grossSalary);
        System.out.println("Net salary: " + result.getNetSalary());
        System.out.println("Employee contribution: " + result.getInsuranceDetails().getTotalEmployeeContribution());
        System.out.println("Personal income tax: " + result.getPersonalIncomeTax());
    }

    @Test
    @DisplayName("Test GROSS to NET calculation with dependents")
    public void testCalculateFromGrossToNet_WithDependents() {
        BigDecimal grossSalary = new BigDecimal("20000000");
        
        // Test with 0 dependents
        TopCVCalculation.SalaryCalculationResult result0 = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        assertEquals(BigDecimal.ZERO, result0.getDependentDeductions());
        
        // Test with 1 dependent
        TopCVCalculation.SalaryCalculationResult result1 = TopCVCalculation.calculateFromGrossToNet(grossSalary, 1);
        BigDecimal expectedDeduction1 = new BigDecimal("4400000");
        assertEquals(expectedDeduction1, result1.getDependentDeductions());
        
        // Test with 2 dependents
        TopCVCalculation.SalaryCalculationResult result2 = TopCVCalculation.calculateFromGrossToNet(grossSalary, 2);
        BigDecimal expectedDeduction2 = new BigDecimal("8800000");
        assertEquals(expectedDeduction2, result2.getDependentDeductions());
        
        // Net salary with dependents should be higher
        assertTrue(result1.getNetSalary().compareTo(result0.getNetSalary()) > 0);
        assertTrue(result2.getNetSalary().compareTo(result1.getNetSalary()) > 0);
        
        System.out.println("Test with 0 dependents - Net: " + result0.getNetSalary());
        System.out.println("Test with 1 dependent - Net: " + result1.getNetSalary());
        System.out.println("Test with 2 dependents - Net: " + result2.getNetSalary());
    }

    @Test
    @DisplayName("Test NET to GROSS reverse calculation")
    public void testCalculateFromNetToGross_ReverseCalculation() {
        BigDecimal targetNetSalary = new BigDecimal("15000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromNetToGross(targetNetSalary, 0);
        
        assertNotNull(result.getGrossSalary());
        assertNotNull(result.getNetSalary());
        assertEquals("GROSS_TO_NET", result.getCalculationType());
        
        // The calculated net should be close to the target net
        BigDecimal difference = result.getNetSalary().subtract(targetNetSalary).abs();
        BigDecimal tolerance = new BigDecimal("1000"); // 1000 VND tolerance
        assertTrue(difference.compareTo(tolerance) <= 0, 
                  "Difference " + difference + " exceeds tolerance " + tolerance);
        
        System.out.println("Target NET: " + targetNetSalary);
        System.out.println("Calculated GROSS: " + result.getGrossSalary());
        System.out.println("Calculated NET: " + result.getNetSalary());
        System.out.println("Difference: " + difference);
    }

    @Test
    @DisplayName("Test employee insurance contribution rates")
    public void testInsuranceContributions_EmployeeRates() {
        BigDecimal grossSalary = new BigDecimal("20000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        TopCVCalculation.InsuranceDetails insurance = result.getInsuranceDetails();
        
        // Employee contributions (10.5% total)
        BigDecimal expectedSocialInsurance = grossSalary.multiply(new BigDecimal("0.08"));
        BigDecimal expectedHealthInsurance = grossSalary.multiply(new BigDecimal("0.015"));
        BigDecimal expectedUnemploymentInsurance = grossSalary.multiply(new BigDecimal("0.01"));
        BigDecimal expectedTotalEmployee = grossSalary.multiply(new BigDecimal("0.105"));
        
        assertEquals(expectedSocialInsurance.setScale(0, RoundingMode.HALF_UP), 
                    insurance.getSocialInsuranceEmployee().setScale(0, RoundingMode.HALF_UP));
        assertEquals(expectedHealthInsurance.setScale(0, RoundingMode.HALF_UP), 
                    insurance.getHealthInsuranceEmployee().setScale(0, RoundingMode.HALF_UP));
        assertEquals(expectedUnemploymentInsurance.setScale(0, RoundingMode.HALF_UP), 
                    insurance.getUnemploymentInsuranceEmployee().setScale(0, RoundingMode.HALF_UP));
        assertEquals(expectedTotalEmployee.setScale(0, RoundingMode.HALF_UP), 
                    insurance.getTotalEmployeeContribution().setScale(0, RoundingMode.HALF_UP));
        
        System.out.println("Social Insurance (8%): " + insurance.getSocialInsuranceEmployee());
        System.out.println("Health Insurance (1.5%): " + insurance.getHealthInsuranceEmployee());
        System.out.println("Unemployment Insurance (1%): " + insurance.getUnemploymentInsuranceEmployee());
        System.out.println("Total Employee Contribution: " + insurance.getTotalEmployeeContribution());
    }

    @Test
    @DisplayName("Test employer insurance contribution rates")
    public void testInsuranceContributions_EmployerRates() {
        BigDecimal grossSalary = new BigDecimal("20000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        TopCVCalculation.InsuranceDetails insurance = result.getInsuranceDetails();
        
        // Employer contributions (21.5% total)
        BigDecimal expectedEmployerSocial = grossSalary.multiply(new BigDecimal("0.17"));
        BigDecimal expectedEmployerHealth = grossSalary.multiply(new BigDecimal("0.03"));
        BigDecimal expectedEmployerUnemployment = grossSalary.multiply(new BigDecimal("0.01"));
        BigDecimal expectedWorkAccident = grossSalary.multiply(new BigDecimal("0.005"));
        BigDecimal expectedTotalEmployer = grossSalary.multiply(new BigDecimal("0.215"));
        
        assertEquals(expectedEmployerSocial.setScale(0, RoundingMode.HALF_UP), 
                    insurance.getSocialInsuranceEmployer().setScale(0, RoundingMode.HALF_UP));
        assertEquals(expectedEmployerHealth.setScale(0, RoundingMode.HALF_UP), 
                    insurance.getHealthInsuranceEmployer().setScale(0, RoundingMode.HALF_UP));
        assertEquals(expectedEmployerUnemployment.setScale(0, RoundingMode.HALF_UP), 
                    insurance.getUnemploymentInsuranceEmployer().setScale(0, RoundingMode.HALF_UP));
        assertEquals(expectedWorkAccident.setScale(0, RoundingMode.HALF_UP), 
                    insurance.getWorkAccidentInsurance().setScale(0, RoundingMode.HALF_UP));
        assertEquals(expectedTotalEmployer.setScale(0, RoundingMode.HALF_UP), 
                    insurance.getTotalEmployerContribution().setScale(0, RoundingMode.HALF_UP));
        
        System.out.println("Employer Social Insurance (17%): " + insurance.getSocialInsuranceEmployer());
        System.out.println("Employer Health Insurance (3%): " + insurance.getHealthInsuranceEmployer());
        System.out.println("Employer Unemployment Insurance (1%): " + insurance.getUnemploymentInsuranceEmployer());
        System.out.println("Work Accident Insurance (0.5%): " + insurance.getWorkAccidentInsurance());
        System.out.println("Total Employer Contribution: " + insurance.getTotalEmployerContribution());
    }

    @Test
    @DisplayName("Test 5% tax bracket calculation")
    public void testTaxBracket_5Percent() {
        BigDecimal grossSalary = new BigDecimal("15000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        // Taxable income should be 2,425,000 (15,000,000 - 1,575,000 - 11,000,000)
        BigDecimal expectedTaxableIncome = new BigDecimal("2425000");
        assertEquals(expectedTaxableIncome.setScale(0, RoundingMode.HALF_UP), 
                    result.getTaxableIncome().setScale(0, RoundingMode.HALF_UP));
        
        // Tax should be 5% of taxable income
        BigDecimal expectedTax = expectedTaxableIncome.multiply(new BigDecimal("0.05"));
        assertEquals(expectedTax.setScale(0, RoundingMode.HALF_UP), 
                    result.getPersonalIncomeTax().setScale(0, RoundingMode.HALF_UP));
        
        System.out.println("Taxable Income: " + result.getTaxableIncome());
        System.out.println("Expected Tax (5%): " + expectedTax);
        System.out.println("Actual Tax: " + result.getPersonalIncomeTax());
    }

    @Test
    @DisplayName("Test 10% tax bracket calculation")
    public void testTaxBracket_10Percent() {
        BigDecimal grossSalary = new BigDecimal("20000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        // Income before tax: 20,000,000 - 2,100,000 = 17,900,000
        // Taxable income: 17,900,000 - 11,000,000 = 6,900,000
        // Tax: 5% of 5,000,000 + 10% of 1,900,000 = 250,000 + 190,000 = 440,000
        
        BigDecimal expectedTax = new BigDecimal("440000");
        assertEquals(expectedTax.setScale(0, RoundingMode.HALF_UP), 
                    result.getPersonalIncomeTax().setScale(0, RoundingMode.HALF_UP));
        
        System.out.println("Taxable Income: " + result.getTaxableIncome());
        System.out.println("Expected Tax (5% + 10%): " + expectedTax);
        System.out.println("Actual Tax: " + result.getPersonalIncomeTax());
    }

    @Test
    @DisplayName("Test 15% tax bracket calculation")
    public void testTaxBracket_15Percent() {
        BigDecimal grossSalary = new BigDecimal("30000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        // Income before tax: 30,000,000 - 3,150,000 = 26,850,000
        // Taxable income: 26,850,000 - 11,000,000 = 15,850,000
        // Tax: 5% of 5,000,000 + 10% of 5,000,000 + 15% of 5,850,000 = 250,000 + 500,000 + 877,500 = 1,627,500
        
        BigDecimal expectedTax = new BigDecimal("1627500");
        assertEquals(expectedTax.setScale(0, RoundingMode.HALF_UP), 
                    result.getPersonalIncomeTax().setScale(0, RoundingMode.HALF_UP));
        
        System.out.println("Taxable Income: " + result.getTaxableIncome());
        System.out.println("Expected Tax (5% + 10% + 15%): " + expectedTax);
        System.out.println("Actual Tax: " + result.getPersonalIncomeTax());
    }

    @Test
    @DisplayName("Test 20% tax bracket calculation")
    public void testTaxBracket_20Percent() {
        BigDecimal grossSalary = new BigDecimal("50000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        // Income before tax: 50,000,000 - 5,250,000 = 44,750,000
        // Taxable income: 44,750,000 - 11,000,000 = 33,750,000
        // Tax: 5% of 5,000,000 + 10% of 5,000,000 + 15% of 8,000,000 + 20% of 15,750,000 = 250,000 + 500,000 + 1,200,000 + 3,150,000 = 5,100,000
        
        BigDecimal expectedTax = new BigDecimal("5100000");
        assertEquals(expectedTax.setScale(0, RoundingMode.HALF_UP), 
                    result.getPersonalIncomeTax().setScale(0, RoundingMode.HALF_UP));
        
        System.out.println("Taxable Income: " + result.getTaxableIncome());
        System.out.println("Expected Tax (5% + 10% + 15% + 20%): " + expectedTax);
        System.out.println("Actual Tax: " + result.getPersonalIncomeTax());
    }

    @Test
    @DisplayName("Test 25% tax bracket calculation")
    public void testTaxBracket_25Percent() {
        BigDecimal grossSalary = new BigDecimal("70000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        // Income before tax: 70,000,000 - 7,350,000 = 62,650,000
        // Taxable income: 62,650,000 - 11,000,000 = 51,650,000
        // Tax: 5% of 5,000,000 + 10% of 5,000,000 + 15% of 8,000,000 + 20% of 14,000,000 + 25% of 19,650,000 = 250,000 + 500,000 + 1,200,000 + 2,800,000 + 4,912,500 = 9,662,500
        
        BigDecimal expectedTax = new BigDecimal("9662500");
        assertEquals(expectedTax.setScale(0, RoundingMode.HALF_UP), 
                    result.getPersonalIncomeTax().setScale(0, RoundingMode.HALF_UP));
        
        System.out.println("Taxable Income: " + result.getTaxableIncome());
        System.out.println("Expected Tax (5% + 10% + 15% + 20% + 25%): " + expectedTax);
        System.out.println("Actual Tax: " + result.getPersonalIncomeTax());
    }

    @Test
    @DisplayName("Test 30% tax bracket calculation")
    public void testTaxBracket_30Percent() {
        BigDecimal grossSalary = new BigDecimal("100000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        // Income before tax: 100,000,000 - 10,500,000 = 89,500,000
        // Taxable income: 89,500,000 - 11,000,000 = 78,500,000
        // Tax: 5% of 5,000,000 + 10% of 5,000,000 + 15% of 8,000,000 + 20% of 14,000,000 + 25% of 20,000,000 + 30% of 26,500,000 = 250,000 + 500,000 + 1,200,000 + 2,800,000 + 5,000,000 + 7,950,000 = 17,700,000
        
        BigDecimal expectedTax = new BigDecimal("17700000");
        assertEquals(expectedTax.setScale(0, RoundingMode.HALF_UP), 
                    result.getPersonalIncomeTax().setScale(0, RoundingMode.HALF_UP));
        
        System.out.println("Taxable Income: " + result.getTaxableIncome());
        System.out.println("Expected Tax (5% + 10% + 15% + 20% + 25% + 30%): " + expectedTax);
        System.out.println("Actual Tax: " + result.getPersonalIncomeTax());
    }

    @Test
    @DisplayName("Test 35% tax bracket calculation")
    public void testTaxBracket_35Percent() {
        BigDecimal grossSalary = new BigDecimal("150000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        // Income before tax: 150,000,000 - 15,750,000 = 134,250,000
        // Taxable income: 134,250,000 - 11,000,000 = 123,250,000
        // Tax: 5% of 5,000,000 + 10% of 5,000,000 + 15% of 8,000,000 + 20% of 14,000,000 + 25% of 20,000,000 + 30% of 28,000,000 + 35% of 43,250,000 = 250,000 + 500,000 + 1,200,000 + 2,800,000 + 5,000,000 + 8,400,000 + 15,137,500 = 33,287,500
        
        BigDecimal expectedTax = new BigDecimal("33287500");
        assertEquals(expectedTax.setScale(0, RoundingMode.HALF_UP), 
                    result.getPersonalIncomeTax().setScale(0, RoundingMode.HALF_UP));
        
        System.out.println("Taxable Income: " + result.getTaxableIncome());
        System.out.println("Expected Tax (5% + 10% + 15% + 20% + 25% + 30% + 35%): " + expectedTax);
        System.out.println("Actual Tax: " + result.getPersonalIncomeTax());
    }

    @Test
    @DisplayName("Test tax bracket boundary conditions")
    public void testTaxBracket_BoundaryConditions() {
        // Test at 5 million boundary (5% bracket)
        BigDecimal salary5M = new BigDecimal("16000000"); // 16M gross ≈ 5M taxable
        TopCVCalculation.SalaryCalculationResult result5M = TopCVCalculation.calculateFromGrossToNet(salary5M, 0);
        
        // Test at 10 million boundary (10% bracket)
        BigDecimal salary10M = new BigDecimal("22000000"); // 22M gross ≈ 10M taxable
        TopCVCalculation.SalaryCalculationResult result10M = TopCVCalculation.calculateFromGrossToNet(salary10M, 0);
        
        // Both should have positive tax
        assertTrue(result5M.getPersonalIncomeTax().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(result10M.getPersonalIncomeTax().compareTo(BigDecimal.ZERO) > 0);
        
        // Higher salary should have higher tax
        assertTrue(result10M.getPersonalIncomeTax().compareTo(result5M.getPersonalIncomeTax()) > 0);
        
        System.out.println("5M boundary salary: " + salary5M + ", Tax: " + result5M.getPersonalIncomeTax());
        System.out.println("10M boundary salary: " + salary10M + ", Tax: " + result10M.getPersonalIncomeTax());
    }

    @Test
    @DisplayName("Test format currency method")
    public void testFormatCurrency() {
        BigDecimal amount = new BigDecimal("1234567");
        String formatted = TopCVCalculation.formatCurrency(amount);
        assertEquals("1234567 VNĐ", formatted);
        
        String formattedNull = TopCVCalculation.formatCurrency(null);
        assertEquals("0 VNĐ", formattedNull);
    }

    @Test
    public void testCalculateExample() {
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateExample();
        
        // Kiểm tra ví dụ với 85,000,000 VNĐ
        assertEquals(new BigDecimal("85000000"), result.getGrossSalary());
        assertNotNull(result.getNetSalary());
        assertNotNull(result.getPersonalIncomeTax());
        
        System.out.println("Example calculation:");
        System.out.println("Gross: " + result.getGrossSalary());
        System.out.println("Net: " + result.getNetSalary());
        System.out.println("Tax: " + result.getPersonalIncomeTax());
    }

    @Test
    public void testUserExample1() {
        // Test GROSS 15,000,000 -> NET 13,303,750
        BigDecimal grossSalary = new BigDecimal("15000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        System.out.println("=== Test GROSS 15,000,000 -> NET 13,303,750 ===");
        System.out.println("Expected NET: 13,303,750");
        System.out.println("Actual NET: " + result.getNetSalary());
        // Verify the calculation matches the user's example
        assertEquals(new BigDecimal("13303750"), result.getNetSalary().setScale(0, RoundingMode.HALF_UP));
    }
    
    @Test
    public void testUserExample2() {
        // Test GROSS 16,995,001 -> NET 15,000,000
        BigDecimal grossSalary = new BigDecimal("16995001");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        System.out.println("=== Test GROSS 16,995,001 -> NET 15,000,000 ===");
        System.out.println("Expected NET: 15,000,000");
        System.out.println("Actual NET: " + result.getNetSalary());
        System.out.println("Difference: " + result.getNetSalary().subtract(new BigDecimal("15000000")));
        
        // Verify the calculation matches the user's example
        assertEquals(new BigDecimal("15000000"), result.getNetSalary().setScale(0, RoundingMode.HALF_UP));
    }
    
    @Test
    public void testDetailedBreakdown() {
        // Test detailed breakdown for GROSS 15,000,000
        BigDecimal grossSalary = new BigDecimal("15000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        System.out.println("=== Detailed Breakdown for GROSS 15,000,000 ===");
        System.out.println("Gross Salary: " + result.getGrossSalary());
        System.out.println("Social Insurance (8%): " + result.getInsuranceDetails().getSocialInsuranceEmployee());
        System.out.println("Health Insurance (1.5%): " + result.getInsuranceDetails().getHealthInsuranceEmployee());
        System.out.println("Unemployment Insurance (1%): " + result.getInsuranceDetails().getUnemploymentInsuranceEmployee());
        System.out.println("Total Employee Contribution: " + result.getInsuranceDetails().getTotalEmployeeContribution());
        System.out.println("Taxable Income: " + result.getTaxableIncome());
        System.out.println("Personal Income Tax: " + result.getPersonalIncomeTax());
        System.out.println("Net Salary: " + result.getNetSalary());
        
        // Verify expected values from user's example
        assertEquals(new BigDecimal("1200000"), result.getInsuranceDetails().getSocialInsuranceEmployee().setScale(0, RoundingMode.HALF_UP)); // 8% of 15,000,000
        assertEquals(new BigDecimal("225000"), result.getInsuranceDetails().getHealthInsuranceEmployee().setScale(0, RoundingMode.HALF_UP)); // 1.5% of 15,000,000
        assertEquals(new BigDecimal("150000"), result.getInsuranceDetails().getUnemploymentInsuranceEmployee().setScale(0, RoundingMode.HALF_UP)); // 1% of 15,000,000
        assertEquals(new BigDecimal("1575000"), result.getInsuranceDetails().getTotalEmployeeContribution().setScale(0, RoundingMode.HALF_UP)); // 10.5% of 15,000,000
        assertEquals(new BigDecimal("13425000"), result.getIncomeBeforeTax().setScale(0, RoundingMode.HALF_UP)); // 15,000,000 - 1,575,000
        assertEquals(new BigDecimal("2425000"), result.getTaxableIncome().setScale(0, RoundingMode.HALF_UP)); // 13,425,000 - 11,000,000 - 0
        assertEquals(new BigDecimal("121250"), result.getPersonalIncomeTax().setScale(0, RoundingMode.HALF_UP)); // 5% of 2,425,000
        assertEquals(new BigDecimal("13303750"), result.getNetSalary().setScale(0, RoundingMode.HALF_UP)); // 15,000,000 - 1,575,000 - 121,250
    }
}
