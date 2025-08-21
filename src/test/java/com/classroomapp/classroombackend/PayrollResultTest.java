package com.classroomapp.classroombackend;

import com.classroomapp.classroombackend.dto.payroll.PayrollResult;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PayrollResultTest {

    @Test
    public void testPayrollResultConstructorWithActualWorkingHours() {
        // Test the new constructor that includes actualWorkingHours parameter
        String userName = "Test Teacher";
        double actualWorkingHours = 12.5; // 12.5 hours worked
        double contractSalary = 5000000.0;
        double proratedGrossSalary = 4500000.0;
        double netSalary = 4000000.0;
        String status = "GENERATED";

        // Create PayrollResult using the new constructor
        PayrollResult payrollResult = new PayrollResult(
            userName,
            actualWorkingHours,
            contractSalary,
            proratedGrossSalary,
            netSalary,
            status
        );

        // Verify all fields are set correctly
        assertEquals(userName, payrollResult.getUserName());
        assertEquals(actualWorkingHours, payrollResult.getActualWorkingHours(), 0.01);
        assertEquals(contractSalary, payrollResult.getContractSalary(), 0.01);
        assertEquals(proratedGrossSalary, payrollResult.getProratedGrossSalary(), 0.01);
        assertEquals(netSalary, payrollResult.getNetSalary(), 0.01);
        assertEquals(status, payrollResult.getStatus());

        System.out.println("✅ PayrollResult Constructor Test PASSED!");
        System.out.println("👤 User: " + payrollResult.getUserName());
        System.out.println("⏰ Actual Working Hours: " + payrollResult.getActualWorkingHours());
        System.out.println("💰 Contract Salary: " + payrollResult.getContractSalary());
        System.out.println("💵 Prorated Gross Salary: " + payrollResult.getProratedGrossSalary());
        System.out.println("💸 Net Salary: " + payrollResult.getNetSalary());
        System.out.println("📊 Status: " + payrollResult.getStatus());
    }

    @Test
    public void testPayrollResultWithZeroWorkingHours() {
        // Test case where teacher has no working hours (like our current scenario)
        PayrollResult payrollResult = new PayrollResult(
            "Teacher No Hours",
            0.0, // No working hours
            5000000.0,
            0.0, // Should be 0 if no hours worked
            0.0, // Should be 0 if no hours worked
            "GENERATED"
        );

        assertEquals(0.0, payrollResult.getActualWorkingHours(), 0.01);
        assertEquals(0.0, payrollResult.getProratedGrossSalary(), 0.01);
        assertEquals(0.0, payrollResult.getNetSalary(), 0.01);

        System.out.println("✅ Zero Working Hours Test PASSED!");
        System.out.println("⚠️ actualWorkingHours = " + payrollResult.getActualWorkingHours() + " (expected for no attendance data)");
    }
}
