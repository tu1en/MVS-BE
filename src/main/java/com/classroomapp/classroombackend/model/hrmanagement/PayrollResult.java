package com.classroomapp.classroombackend.model.hrmanagement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import com.classroomapp.classroombackend.util.TopCVCalculation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Simple payroll result model using TopCV calculations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollResult {
    
    private Long userId;
    private String userName;
    private String userEmail;
    private String contractType;
    private YearMonth payrollPeriod;
    
    // Attendance data
    private Integer totalWorkingDays;
    private Integer actualWorkingDays;
    private Integer absentDays;
    private Integer actualWorkingHours; // tổng giờ làm thực tế (ước lượng)
    private Integer standardMonthlyHours; // tổng giờ công chuẩn trong tháng
    private Integer weekendWorkingDays; // số ngày làm cuối tuần
    private Integer weekendWorkingHours; // tổng giờ làm cuối tuần
    private Integer weekdayWorkingHours; // tổng giờ làm ngày thường

    // Teaching data (for teachers)
    private Double totalTeachingHours; // tổng giờ dạy thực tế
    private Double totalTeachingSlots; // tổng số tiết dạy (1 slot = 1.5 giờ)
    
    // Original contract salary
    private BigDecimal contractSalary;
    private String contractOffer;
    
    // Calculated salary (prorated based on attendance)
    private BigDecimal proratedGrossSalary;
    private BigDecimal netSalary;
    private BigDecimal hourlySalary; // đơn giá theo giờ (cho giáo viên)
    private BigDecimal weekendPay; // tiền cộng thêm do làm cuối tuần (2x)
    private BigDecimal overtimePay; // dự phòng: tăng ca (1.5x)
    private BigDecimal holidayPay; // dự phòng: ngày lễ (3x)
    
    // Contract period (để FE hiển thị ngày bắt đầu/kết thúc HĐ)
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;
    
    // TopCV calculation details
    private TopCVCalculation.SalaryCalculationResult topCVResult;
    
    // Status
    private String status; // "CALCULATED", "APPROVED", "PAID"
    private LocalDate calculatedAt;
    private Long calculatedBy; // Accountant user ID
    private Boolean insuranceApplicable; // đủ công để DN đóng 21.5%
    
    public PayrollResult(Long userId, String userName, YearMonth period,
                        Integer totalDays, Integer actualDays,
                        BigDecimal contractSalary, BigDecimal proratedGross, BigDecimal netSalary,
                        TopCVCalculation.SalaryCalculationResult details) {
        this.userId = userId;
        this.userName = userName;
        this.payrollPeriod = period;
        this.totalWorkingDays = totalDays;
        this.actualWorkingDays = actualDays;
        this.absentDays = totalDays - actualDays;
        this.contractSalary = contractSalary;
        this.proratedGrossSalary = proratedGross;
        this.netSalary = netSalary;
        this.topCVResult = details;
        this.status = "CALCULATED";
        this.calculatedAt = LocalDate.now();
        // Giờ công mặc định (8h/ngày) - sẽ được override cho teacher
        this.actualWorkingHours = actualDays != null ? actualDays * 8 : null;
        this.standardMonthlyHours = totalDays != null ? totalDays * 8 : null;
    }

    /**
     * Constructor cho teacher với giờ dạy thực tế
     */
    public PayrollResult(Long userId, String userName, YearMonth period,
                        Integer totalDays, Integer actualDays,
                        BigDecimal contractSalary, BigDecimal proratedGross, BigDecimal netSalary,
                        TopCVCalculation.SalaryCalculationResult details, Double actualTeachingHours) {
        this.userId = userId;
        this.userName = userName;
        this.payrollPeriod = period;
        this.totalWorkingDays = totalDays;
        this.actualWorkingDays = actualDays;
        this.absentDays = totalDays - actualDays;
        this.contractSalary = contractSalary;
        this.proratedGrossSalary = proratedGross;
        this.netSalary = netSalary;
        this.topCVResult = details;
        this.status = "CALCULATED";
        this.calculatedAt = LocalDate.now();
        // Dùng giờ dạy thực tế cho teacher
        this.actualWorkingHours = actualTeachingHours != null ? (int) Math.round(actualTeachingHours) : null;
        this.standardMonthlyHours = actualTeachingHours != null ? (int) Math.round(actualTeachingHours) : null;
        this.totalTeachingHours = actualTeachingHours;
        this.totalTeachingSlots = actualTeachingHours != null ? actualTeachingHours / 1.5 : null; // 1 slot = 1.5 giờ
    }

    /**
     * Constructor cho teacher với cả teaching hours và teaching slots
     */
    public PayrollResult(Long userId, String userName, YearMonth period,
                        Integer totalDays, Integer actualDays,
                        BigDecimal contractSalary, BigDecimal proratedGross, BigDecimal netSalary,
                        TopCVCalculation.SalaryCalculationResult details,
                        Double actualTeachingHours, Double totalTeachingSlots) {
        this.userId = userId;
        this.userName = userName;
        this.payrollPeriod = period;
        this.totalWorkingDays = totalDays;
        this.actualWorkingDays = actualDays;
        this.absentDays = totalDays - actualDays;
        this.contractSalary = contractSalary;
        this.proratedGrossSalary = proratedGross;
        this.netSalary = netSalary;
        this.topCVResult = details;
        this.status = "CALCULATED";
        this.calculatedAt = LocalDate.now();
        // Dùng giờ dạy thực tế cho teacher
        this.actualWorkingHours = actualTeachingHours != null ? (int) Math.round(actualTeachingHours) : null;
        this.standardMonthlyHours = actualTeachingHours != null ? (int) Math.round(actualTeachingHours) : null;
        this.totalTeachingHours = actualTeachingHours;
        this.totalTeachingSlots = totalTeachingSlots;
    }
}