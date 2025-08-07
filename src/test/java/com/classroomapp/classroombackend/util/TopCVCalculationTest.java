package com.classroomapp.classroombackend.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

public class TopCVCalculationTest {

    @Test
    public void testCalculateFromGrossToNet() {
        // Test với lương GROSS 85,000,000 VNĐ
        BigDecimal grossSalary = new BigDecimal("85000000");
        TopCVCalculation.SalaryCalculationResult result = TopCVCalculation.calculateFromGrossToNet(grossSalary, 0);
        
        // Kiểm tra các giá trị cơ bản
        assertEquals(grossSalary, result.getGrossSalary());
        assertEquals("GROSS_TO_NET", result.getCalculationType());
        
        // Kiểm tra đóng góp bảo hiểm
        BigDecimal expectedEmployeeContribution = grossSalary.multiply(new BigDecimal("0.105"));
        assertEquals(expectedEmployeeContribution, result.getEmployeeContribution());
        
        BigDecimal expectedEmployerContribution = grossSalary.multiply(new BigDecimal("0.215"));
        assertEquals(expectedEmployerContribution, result.getEmployerContribution());
        
        BigDecimal expectedTotalInsurance = grossSalary.multiply(new BigDecimal("0.32"));
        assertEquals(expectedTotalInsurance, result.getTotalInsuranceContribution());
        
        // Kiểm tra lương NET phải nhỏ hơn GROSS
        assertTrue(result.getNetSalary().compareTo(grossSalary) < 0);
        
        // Kiểm tra thuế TNCN phải lớn hơn 0
        assertTrue(result.getPersonalIncomeTax().compareTo(BigDecimal.ZERO) > 0);
        
        System.out.println("Test passed for gross salary: " + grossSalary);
        System.out.println("Net salary: " + result.getNetSalary());
        System.out.println("Employee contribution: " + result.getEmployeeContribution());
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
}
