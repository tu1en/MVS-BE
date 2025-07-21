package com.classroomapp.classroombackend.service.hrmanagement.shift;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface cho Shift Assignment management
 * Cung cấp business logic cho quản lý phân công ca làm việc
 */
public interface ShiftAssignmentService {

    /**
     * Tạo assignment mới
     */
    ShiftAssignment createAssignment(ShiftAssignment assignment);

    /**
     * Tạo multiple assignments (bulk)
     */
    List<ShiftAssignment> createBulkAssignments(List<ShiftAssignment> assignments);

    /**
     * Cập nhật assignment
     */
    ShiftAssignment updateAssignment(Long id, ShiftAssignment assignment);

    /**
     * Xóa assignment
     */
    void deleteAssignment(Long id);

    /**
     * Hủy assignment
     */
    void cancelAssignment(Long id, String reason);

    /**
     * Tìm assignment theo ID
     */
    Optional<ShiftAssignment> findById(Long id);

    /**
     * Tìm assignments theo employee và ngày
     */
    List<ShiftAssignment> findByEmployeeAndDate(Long employeeId, LocalDate date);

    /**
     * Tìm assignments theo employee trong khoảng thời gian
     */
    List<ShiftAssignment> findByEmployeeAndDateRange(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * Tìm assignments theo ngày
     */
    List<ShiftAssignment> findByDate(LocalDate date);

    /**
     * Tìm assignments theo tuần
     */
    List<ShiftAssignment> findByWeek(LocalDate weekStart, Long employeeId);

    /**
     * Tìm assignments theo tháng
     */
    List<ShiftAssignment> findByMonth(int year, int month, Long employeeId);

    /**
     * Search assignments với filters
     */
    Page<ShiftAssignment> searchAssignments(Long employeeId, LocalDate startDate, LocalDate endDate,
                                           ShiftAssignment.AssignmentStatus status,
                                           ShiftAssignment.AttendanceStatus attendanceStatus,
                                           String search, Pageable pageable);

    /**
     * Check-in cho assignment
     */
    ShiftAssignment checkIn(Long assignmentId, String location);

    /**
     * Check-out cho assignment
     */
    ShiftAssignment checkOut(Long assignmentId, String location);

    /**
     * Tìm assignments cần check-in
     */
    List<ShiftAssignment> findPendingCheckIns();

    /**
     * Tìm assignments cần check-out
     */
    List<ShiftAssignment> findPendingCheckOuts();

    /**
     * Validate assignment trước khi tạo
     */
    void validateAssignment(ShiftAssignment assignment);

    /**
     * Kiểm tra xung đột cho assignment
     */
    ShiftConflictDetectionService.ConflictCheckResult checkConflicts(ShiftAssignment assignment);

    /**
     * Tính tổng giờ làm việc của employee
     */
    WorkingHoursSummary calculateWorkingHours(Long employeeId, LocalDate startDate, LocalDate endDate);

    /**
     * Lấy assignments có overtime
     */
    List<ShiftAssignment> findOvertimeAssignments(LocalDate startDate, LocalDate endDate);

    /**
     * Lấy assignments có vấn đề attendance
     */
    List<ShiftAssignment> findAttendanceIssues(LocalDate startDate, LocalDate endDate);

    /**
     * Tìm assignments có thể swap
     */
    List<ShiftAssignment> findSwappableAssignments(Long employeeId, LocalDate date, Long shiftTemplateId);

    /**
     * Auto-assign shifts cho employees dựa trên availability
     */
    List<ShiftAssignment> autoAssignShifts(List<Long> employeeIds, LocalDate startDate, LocalDate endDate);

    /**
     * Copy assignments từ tuần/tháng khác
     */
    List<ShiftAssignment> copyAssignments(LocalDate sourceStart, LocalDate sourceEnd, 
                                         LocalDate targetStart, List<Long> employeeIds);

    /**
     * Lấy thống kê assignments
     */
    AssignmentStatistics getAssignmentStatistics(LocalDate startDate, LocalDate endDate);

    /**
     * Export assignments to CSV/Excel
     */
    byte[] exportAssignments(LocalDate startDate, LocalDate endDate, String format);

    /**
     * Gửi reminder notifications
     */
    void sendShiftReminders(LocalDate date);

    /**
     * Update assignment status
     */
    void updateAssignmentStatus(Long id, ShiftAssignment.AssignmentStatus status);

    /**
     * Tìm assignments của employee trong tuần hiện tại
     */
    List<ShiftAssignment> findCurrentWeekAssignments(Long employeeId);

    /**
     * DTO cho working hours summary
     */
    class WorkingHoursSummary {
        private java.math.BigDecimal totalPlannedHours;
        private java.math.BigDecimal totalActualHours;
        private java.math.BigDecimal totalOvertimeHours;
        private int totalAssignments;
        private int completedAssignments;
        private double attendanceRate;

        // Constructors, getters, setters
        public WorkingHoursSummary() {}

        public WorkingHoursSummary(java.math.BigDecimal totalPlannedHours, 
                                  java.math.BigDecimal totalActualHours,
                                  java.math.BigDecimal totalOvertimeHours,
                                  int totalAssignments, int completedAssignments) {
            this.totalPlannedHours = totalPlannedHours;
            this.totalActualHours = totalActualHours;
            this.totalOvertimeHours = totalOvertimeHours;
            this.totalAssignments = totalAssignments;
            this.completedAssignments = completedAssignments;
            this.attendanceRate = totalAssignments > 0 ? 
                (double) completedAssignments / totalAssignments * 100 : 0;
        }

        // Getters and setters
        public java.math.BigDecimal getTotalPlannedHours() { return totalPlannedHours; }
        public void setTotalPlannedHours(java.math.BigDecimal totalPlannedHours) { this.totalPlannedHours = totalPlannedHours; }

        public java.math.BigDecimal getTotalActualHours() { return totalActualHours; }
        public void setTotalActualHours(java.math.BigDecimal totalActualHours) { this.totalActualHours = totalActualHours; }

        public java.math.BigDecimal getTotalOvertimeHours() { return totalOvertimeHours; }
        public void setTotalOvertimeHours(java.math.BigDecimal totalOvertimeHours) { this.totalOvertimeHours = totalOvertimeHours; }

        public int getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(int totalAssignments) { this.totalAssignments = totalAssignments; }

        public int getCompletedAssignments() { return completedAssignments; }
        public void setCompletedAssignments(int completedAssignments) { this.completedAssignments = completedAssignments; }

        public double getAttendanceRate() { return attendanceRate; }
        public void setAttendanceRate(double attendanceRate) { this.attendanceRate = attendanceRate; }
    }

    /**
     * DTO cho assignment statistics
     */
    class AssignmentStatistics {
        private long totalAssignments;
        private long scheduledAssignments;
        private long completedAssignments;
        private long cancelledAssignments;
        private java.math.BigDecimal totalHours;
        private java.math.BigDecimal totalOvertimeHours;
        private double completionRate;

        // Constructors, getters, setters
        public AssignmentStatistics() {}

        public AssignmentStatistics(long totalAssignments, long scheduledAssignments,
                                   long completedAssignments, long cancelledAssignments,
                                   java.math.BigDecimal totalHours, java.math.BigDecimal totalOvertimeHours) {
            this.totalAssignments = totalAssignments;
            this.scheduledAssignments = scheduledAssignments;
            this.completedAssignments = completedAssignments;
            this.cancelledAssignments = cancelledAssignments;
            this.totalHours = totalHours;
            this.totalOvertimeHours = totalOvertimeHours;
            this.completionRate = totalAssignments > 0 ? 
                (double) completedAssignments / totalAssignments * 100 : 0;
        }

        // Getters and setters
        public long getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(long totalAssignments) { this.totalAssignments = totalAssignments; }

        public long getScheduledAssignments() { return scheduledAssignments; }
        public void setScheduledAssignments(long scheduledAssignments) { this.scheduledAssignments = scheduledAssignments; }

        public long getCompletedAssignments() { return completedAssignments; }
        public void setCompletedAssignments(long completedAssignments) { this.completedAssignments = completedAssignments; }

        public long getCancelledAssignments() { return cancelledAssignments; }
        public void setCancelledAssignments(long cancelledAssignments) { this.cancelledAssignments = cancelledAssignments; }

        public java.math.BigDecimal getTotalHours() { return totalHours; }
        public void setTotalHours(java.math.BigDecimal totalHours) { this.totalHours = totalHours; }

        public java.math.BigDecimal getTotalOvertimeHours() { return totalOvertimeHours; }
        public void setTotalOvertimeHours(java.math.BigDecimal totalOvertimeHours) { this.totalOvertimeHours = totalOvertimeHours; }

        public double getCompletionRate() { return completionRate; }
        public void setCompletionRate(double completionRate) { this.completionRate = completionRate; }
    }
}
