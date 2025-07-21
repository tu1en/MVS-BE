package com.classroomapp.classroombackend.dto.hrmanagement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftAssignmentDto {

    private Long id;

    private Long employeeId;

    private String employeeName;

    private String employeeEmail;

    private String employeeDepartment;

    private Long shiftId;

    private String shiftName;

    private LocalTime shiftStartTime;

    private LocalTime shiftEndTime;

    private Long shiftTemplateId;

    private String shiftTemplateName;

    private LocalDate workDate;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private String checkInLocation;

    private String checkOutLocation;

    private ShiftAssignment.AttendanceStatus attendanceStatus;

    private BigDecimal workingHours;

    private BigDecimal overtimeHours;

    private BigDecimal basicHours;

    private String notes;

    private String rejectionReason;

    private String approvedBy;

    private LocalDateTime approvedAt;

    private ShiftAssignment.AssignmentStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    /**
     * Calculate actual working duration in minutes
     */
    public Long getActualDurationMinutes() {
        if (checkInTime != null && checkOutTime != null) {
            return java.time.Duration.between(checkInTime, checkOutTime).toMinutes();
        }
        return 0L;
    }

    /**
     * Check if check-in is late
     */
    public Boolean isLateCheckIn() {
        if (checkInTime != null && shiftStartTime != null && workDate != null) {
            LocalDateTime expectedCheckIn = workDate.atTime(shiftStartTime);
            return checkInTime.isAfter(expectedCheckIn.plusMinutes(5));
        }
        return false;
    }

    /**
     * Check if check-out is early
     */
    public Boolean isEarlyCheckOut() {
        if (checkOutTime != null && shiftEndTime != null && workDate != null) {
            LocalDateTime expectedCheckOut = workDate.atTime(shiftEndTime);
            return checkOutTime.isBefore(expectedCheckOut.minusMinutes(5));
        }
        return false;
    }

    /**
     * Get formatted working hours
     */
    public String getFormattedWorkingHours() {
        return (workingHours != null) ? String.format("%.2f", workingHours) : "0.00";
    }

    /**
     * Get assignment status description
     */
    public String getStatusDescription() {
        if (status == null) return "N/A";

        return switch (status) {
            case SCHEDULED -> "Đã lên lịch";
            case IN_PROGRESS -> "Đang thực hiện";
            case COMPLETED -> "Hoàn thành";
            case CANCELLED -> "Đã hủy";
            case NO_SHOW -> "Không có mặt";
        };
    }

    /**
     * Get attendance status description
     */
    public String getAttendanceStatusDescription() {
        if (attendanceStatus == null) return "N/A";

        return switch (attendanceStatus) {
            case PENDING -> "Chờ xác nhận";
            case PRESENT -> "Có mặt";
            case ABSENT -> "Vắng mặt";
            case LATE -> "Đi muộn";
            case EARLY_LEAVE -> "Về sớm";
        };
    }
}
