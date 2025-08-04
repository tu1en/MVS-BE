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
 * Xá»­ lÃ½ logic phÃ¡t hiá»‡n xung Ä‘á»™t ca lÃ m viá»‡c
 */
public interface ShiftConflictDetectionService {

    /**
     * Kiá»ƒm tra xung Ä‘á»™t thá»i gian cho assignment má»›i
     */
    ConflictCheckResult checkTimeConflicts(Long assignedUserId, LocalDate date,
                                          LocalTime startTime, LocalTime endTime,
                                          Long excludeAssignmentId);

    /**
     * Kiá»ƒm tra vi pháº¡m thá»i gian nghá»‰ tá»‘i thiá»ƒu (8 giá» giá»¯a cÃ¡c ca)
     */
    ConflictCheckResult checkRestTimeViolations(Long assignedUserId, LocalDate date,
                                               LocalTime startTime, LocalTime endTime,
                                               Long excludeAssignmentId);

    /**
     * Kiá»ƒm tra vi pháº¡m giá»›i háº¡n giá» lÃ m viá»‡c hÃ ng tuáº§n
     */
    ConflictCheckResult checkWeeklyHourLimits(Long assignedUserId, LocalDate date,
                                             BigDecimal additionalHours);

    /**
     * Kiá»ƒm tra táº¥t cáº£ cÃ¡c loáº¡i xung Ä‘á»™t
     */
    ConflictCheckResult checkAllConflicts(Long assignedUserId, LocalDate date,
                                         LocalTime startTime, LocalTime endTime,
                                         BigDecimal hours,
                                         Long excludeAssignmentId);

    /**
     * Kiá»ƒm tra xung Ä‘á»™t cho swap request
     */
    ConflictCheckResult checkSwapConflicts(Long requesterId, Long targetUserId,
                                          ShiftAssignment requesterAssignment,
                                          ShiftAssignment targetAssignment);

    /**
     * TÃ¬m cÃ¡c slot thá»i gian available cho employee
     */
    List<AvailableTimeSlot> findAvailableTimeSlots(Long assignedUserId, LocalDate date);

    /**
     * Kiá»ƒm tra employee availability
     */
    boolean isEmployeeAvailable(Long assignedUserId, LocalDate date, LocalTime startTime, LocalTime endTime);

    /**
     * Láº¥y tá»•ng giá» lÃ m viá»‡c trong tuáº§n
     */
    BigDecimal getWeeklyWorkingHours(Long assignedUserId, LocalDate weekStartDate);

    /**
     * Láº¥y assignments xung Ä‘á»™t
     */
    List<ShiftAssignment> getConflictingAssignments(Long assignedUserId, LocalDate date,
                                                   LocalTime startTime, LocalTime endTime,
                                                   Long excludeAssignmentId);

    /**
     * Validate assignment trÆ°á»›c khi táº¡o
     */
    void validateAssignmentCreation(ShiftAssignment assignment);

    /**
     * Suggest alternative time slots
     */
    List<AvailableTimeSlot> suggestAlternativeTimeSlots(Long assignedUserId, LocalDate date,
                                                       BigDecimal requiredHours);

    // ======================= INNER CLASSES & ENUMS =======================

    /**
     * DTO cho káº¿t quáº£ kiá»ƒm tra xung Ä‘á»™t
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
     * DTO cho chi tiáº¿t xung Ä‘á»™t
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

        // âœ… Constructor 4 tham sá»‘ Ä‘á»ƒ sá»­a lá»—i
        public ConflictDetail(ConflictType type, String message, ConflictSeverity severity, String suggestion) {
            this.type = type;
            this.message = message;
            this.severity = severity;
            this.suggestion = suggestion;
        }

        // âœ… Constructor 3 tham sá»‘ (trÆ°á»ng há»£p khÃ´ng cáº§n suggestion & assignment)
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
     * Enum cho loáº¡i xung Ä‘á»™t
     */
    enum ConflictType {
        TIME_OVERLAP("Xung Ä‘á»™t thá»i gian"),
        INSUFFICIENT_REST("KhÃ´ng Ä‘á»§ thá»i gian nghá»‰"),
        WEEKLY_HOUR_LIMIT("VÆ°á»£t quÃ¡ giá»›i háº¡n giá» lÃ m viá»‡c hÃ ng tuáº§n"),
        EMPLOYEE_UNAVAILABLE("NhÃ¢n viÃªn khÃ´ng cÃ³ sáºµn"),
        DUPLICATE_ASSIGNMENT("TrÃ¹ng láº·p phÃ¢n cÃ´ng"),
        INVALID_TIME_RANGE("Khoáº£ng thá»i gian khÃ´ng há»£p lá»‡");

        private final String displayName;

        ConflictType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    /**
     * Enum cho má»©c Ä‘á»™ nghiÃªm trá»ng
     */
    enum ConflictSeverity {
        LOW("Tháº¥p", "#52c41a"),
        MEDIUM("Trung bÃ¬nh", "#faad14"),
        HIGH("Cao", "#fa8c16"),
        CRITICAL("NghiÃªm trá»ng", "#ff4d4f");

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
