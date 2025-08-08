package com.classroomapp.classroombackend.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class TopCVCalculationTest {

    @Test
    public void testCalculateFromGrossToNet() {
        // Test với lương GROSS 85,000,000 VNĐ
        BigDecimal grossSalary = new BigDecimal("85000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        // Kiểm tra các giá trị cơ bản
        assertEquals(grossSalary.setScale(0, RoundingMode.HALF_UP), result.getGrossSalary());
        assertEquals("GROSS_TO_NET", result.getCalculationType());
        
        // Kiểm tra đóng góp bảo hiểm (đã được làm tròn về số nguyên)
        BigDecimal expectedEmployeeContribution = grossSalary.multiply(new BigDecimal("0.105")).setScale(0, RoundingMode.HALF_UP);
        assertEquals(expectedEmployeeContribution, result.getInsuranceDetails().getTotalEmployeeContribution());
        
        BigDecimal expectedEmployerContribution = grossSalary.multiply(new BigDecimal("0.215")).setScale(0, RoundingMode.HALF_UP);
        assertEquals(expectedEmployerContribution, result.getInsuranceDetails().getTotalEmployerContribution());
        
        BigDecimal expectedTotalInsurance = grossSalary.multiply(new BigDecimal("0.32")).setScale(0, RoundingMode.HALF_UP);
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
    public void testFormatCurrency() {
        BigDecimal amount = new BigDecimal("1234567");
        String formatted = TopCVCalculation.formatCurrency(amount);
        assertEquals("1234567 VNĐ", formatted);
        
        String formattedNull = TopCVCalculation.formatCurrency(null);
        assertEquals("0 VNĐ", formattedNull);
    }

    @Test
    public void testUserExample1() {
        // Test GROSS 15,000,000 -> NET 13,303,750
        BigDecimal grossSalary = new BigDecimal("15000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        System.out.println("=== Test GROSS 15,000,000 -> NET 13,303,750 ===");
        System.out.println("Expected NET: 13,303,750");
        System.out.println("Actual NET: " + result.getNetSalary());
        System.out.println("Difference: " + result.getNetSalary().subtract(new BigDecimal("13303750")));
        
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
