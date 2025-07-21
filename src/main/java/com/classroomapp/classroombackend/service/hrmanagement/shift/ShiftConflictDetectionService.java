package com.classroomapp.classroombackend.service.hrmanagement.shift;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftAssignment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Service cho Shift Conflict Detection
 * Xử lý logic phát hiện xung đột ca làm việc
 */
public interface ShiftConflictDetectionService {

    /**
     * Kiểm tra xung đột thời gian cho assignment mới
     */
    ConflictCheckResult checkTimeConflicts(Long employeeId, LocalDate date,
                                          LocalTime startTime, LocalTime endTime,
                                          Long excludeAssignmentId);

    /**
     * Kiểm tra vi phạm thời gian nghỉ tối thiểu (8 giờ giữa các ca)
     */
    ConflictCheckResult checkRestTimeViolations(Long employeeId, LocalDate date,
                                               LocalTime startTime, LocalTime endTime,
                                               Long excludeAssignmentId);

    /**
     * Kiểm tra vi phạm giới hạn giờ làm việc hàng tuần
     */
    ConflictCheckResult checkWeeklyHourLimits(Long employeeId, LocalDate date,
                                             BigDecimal additionalHours);

    /**
     * Kiểm tra tất cả các loại xung đột
     */
    ConflictCheckResult checkAllConflicts(Long employeeId, LocalDate date,
                                         LocalTime startTime, LocalTime endTime,
                                         BigDecimal hours,
                                         Long excludeAssignmentId);

    /**
     * Kiểm tra xung đột cho swap request
     */
    ConflictCheckResult checkSwapConflicts(Long requesterId, Long targetEmployeeId,
                                          ShiftAssignment requesterAssignment,
                                          ShiftAssignment targetAssignment);

    /**
     * Tìm các slot thời gian available cho employee
     */
    List<AvailableTimeSlot> findAvailableTimeSlots(Long employeeId, LocalDate date);

    /**
     * Kiểm tra employee availability
     */
    boolean isEmployeeAvailable(Long employeeId, LocalDate date, LocalTime startTime, LocalTime endTime);

    /**
     * Lấy tổng giờ làm việc trong tuần
     */
    BigDecimal getWeeklyWorkingHours(Long employeeId, LocalDate weekStartDate);

    /**
     * Lấy assignments xung đột
     */
    List<ShiftAssignment> getConflictingAssignments(Long employeeId, LocalDate date,
                                                   LocalTime startTime, LocalTime endTime,
                                                   Long excludeAssignmentId);

    /**
     * Validate assignment trước khi tạo
     */
    void validateAssignmentCreation(ShiftAssignment assignment);

    /**
     * Suggest alternative time slots
     */
    List<AvailableTimeSlot> suggestAlternativeTimeSlots(Long employeeId, LocalDate date,
                                                       BigDecimal requiredHours);

    // ======================= INNER CLASSES & ENUMS =======================

    /**
     * DTO cho kết quả kiểm tra xung đột
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ConflictCheckResult {
        private boolean hasConflict;
        private List<ConflictDetail> conflicts;
        private String summary;
        private ConflictSeverity severity;

        public boolean isValid() {
            return !hasConflict;
        }

        public boolean hasConflict() {
            return hasConflict;
        }

        public boolean hasTimeConflicts() {
            return conflicts != null && conflicts.stream()
                    .anyMatch(c -> c.getType() == ConflictType.TIME_OVERLAP);
        }

        public boolean hasRestTimeViolations() {
            return conflicts != null && conflicts.stream()
                    .anyMatch(c -> c.getType() == ConflictType.INSUFFICIENT_REST);
        }

        public boolean hasWeeklyHourViolations() {
            return conflicts != null && conflicts.stream()
                    .anyMatch(c -> c.getType() == ConflictType.WEEKLY_HOUR_LIMIT);
        }
    }

    /**
     * DTO cho chi tiết xung đột
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class ConflictDetail {
        private ConflictType type;
        private String message;
        private ShiftAssignment conflictingAssignment;
        private ConflictSeverity severity;
        private String suggestion;

        // ✅ Constructor 4 tham số để sửa lỗi
        public ConflictDetail(ConflictType type, String message, ConflictSeverity severity, String suggestion) {
            this.type = type;
            this.message = message;
            this.severity = severity;
            this.suggestion = suggestion;
        }

        // ✅ Constructor 3 tham số (trường hợp không cần suggestion & assignment)
        public ConflictDetail(ConflictType type, String message, ConflictSeverity severity) {
            this.type = type;
            this.message = message;
            this.severity = severity;
        }
    }

    /**
     * DTO cho time slot available
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    class AvailableTimeSlot {
        private LocalTime startTime;
        private LocalTime endTime;
        private BigDecimal maxHours;
        private String description;
        private boolean isPreferred;

        public long getDurationMinutes() {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }

        public BigDecimal getDurationHours() {
            return BigDecimal.valueOf(getDurationMinutes())
                    .divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
        }
    }

    /**
     * Enum cho loại xung đột
     */
    enum ConflictType {
        TIME_OVERLAP("Xung đột thời gian"),
        INSUFFICIENT_REST("Không đủ thời gian nghỉ"),
        WEEKLY_HOUR_LIMIT("Vượt quá giới hạn giờ làm việc hàng tuần"),
        EMPLOYEE_UNAVAILABLE("Nhân viên không có sẵn"),
        DUPLICATE_ASSIGNMENT("Trùng lặp phân công"),
        INVALID_TIME_RANGE("Khoảng thời gian không hợp lệ");

        private final String displayName;

        ConflictType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    /**
     * Enum cho mức độ nghiêm trọng
     */
    enum ConflictSeverity {
        LOW("Thấp", "#52c41a"),
        MEDIUM("Trung bình", "#faad14"),
        HIGH("Cao", "#fa8c16"),
        CRITICAL("Nghiêm trọng", "#ff4d4f");

        private final String displayName;
        private final String color;

        ConflictSeverity(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }
}
