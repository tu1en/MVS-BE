package com.classroomapp.classroombackend.service.hrmanagement.shift;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftSchedule;
import com.classroomapp.classroombackend.model.usermanagement.User;

/**
 * Service interface cho Shift Schedule management
 * Cung cấp business logic cho quản lý lịch làm việc
 */
public interface ShiftScheduleService {

    ShiftSchedule createSchedule(ShiftSchedule schedule);

    ShiftSchedule updateSchedule(Long id, ShiftSchedule schedule);

    void deleteSchedule(Long id);

    Optional<ShiftSchedule> findById(Long id);

    List<ShiftSchedule> findByStatus(ShiftSchedule.ScheduleStatus status);

    List<ShiftSchedule> findByType(ShiftSchedule.ScheduleType scheduleType);

    List<ShiftSchedule> findActiveSchedules();

    Optional<ShiftSchedule> findActiveScheduleForDate(LocalDate date);

    Page<ShiftSchedule> searchSchedules(ShiftSchedule.ScheduleStatus status,
                                       ShiftSchedule.ScheduleType scheduleType,
                                       Long createdById, LocalDate startDate, LocalDate endDate,
                                       String search, Pageable pageable);

    ShiftSchedule publishSchedule(Long id, User publisher);

    ShiftSchedule archiveSchedule(Long id);

    void cancelSchedule(Long id, String reason);

    void validateSchedule(ShiftSchedule schedule);

    List<ShiftSchedule> findOverlappingSchedules(LocalDate startDate, LocalDate endDate, Long excludeId);

    List<ShiftSchedule> findSchedulesNeedingArchive(int daysAfterEnd);

    List<ShiftSchedule> findUpcomingSchedules(int daysAhead);

    int autoArchiveOldSchedules(int daysAfterEnd);

    int cleanupOldDrafts(int daysOld);

    ShiftSchedule copySchedule(Long sourceScheduleId, LocalDate newStartDate, String newName);

    ShiftSchedule generateWeeklySchedule(LocalDate startDate, String name, User creator);

    ShiftSchedule generateMonthlySchedule(LocalDate startDate, String name, User creator);

    ScheduleStatistics getScheduleStatistics(LocalDate startDate, LocalDate endDate);

    List<ShiftSchedule> findSchedulesWithMostAssignments(int limit);

    void updateAssignmentCount(Long scheduleId);

    void bulkUpdateStatus(List<Long> scheduleIds, ShiftSchedule.ScheduleStatus status);

    byte[] exportSchedule(Long scheduleId, String format);

    void sendScheduleNotifications(ShiftSchedule schedule, ScheduleNotificationType type);

    ScheduleConflictResult validateScheduleConflicts(ShiftSchedule schedule);

    enum ScheduleNotificationType {
        SCHEDULE_CREATED("Lịch làm việc mới được tạo"),
        SCHEDULE_PUBLISHED("Lịch làm việc đã được xuất bản"),
        SCHEDULE_UPDATED("Lịch làm việc đã được cập nhật"),
        SCHEDULE_CANCELLED("Lịch làm việc đã bị hủy"),
        SCHEDULE_ARCHIVED("Lịch làm việc đã được lưu trữ"),
        SCHEDULE_STARTING_SOON("Lịch làm việc sắp bắt đầu");

        private final String displayName;

        ScheduleNotificationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    class ScheduleStatistics {
        private long totalSchedules;
        private long draftSchedules;
        private long publishedSchedules;
        private long archivedSchedules;
        private long totalAssignments;
        private double averageAssignmentsPerSchedule;

        public ScheduleStatistics() {}

        public ScheduleStatistics(long totalSchedules, long draftSchedules, long publishedSchedules,
                                  long archivedSchedules, long totalAssignments) {
            this.totalSchedules = totalSchedules;
            this.draftSchedules = draftSchedules;
            this.publishedSchedules = publishedSchedules;
            this.archivedSchedules = archivedSchedules;
            this.totalAssignments = totalAssignments;
            this.averageAssignmentsPerSchedule = totalSchedules > 0 ?
                    (double) totalAssignments / totalSchedules : 0;
        }

        public long getTotalSchedules() { return totalSchedules; }
        public void setTotalSchedules(long totalSchedules) { this.totalSchedules = totalSchedules; }

        public long getDraftSchedules() { return draftSchedules; }
        public void setDraftSchedules(long draftSchedules) { this.draftSchedules = draftSchedules; }

        public long getPublishedSchedules() { return publishedSchedules; }
        public void setPublishedSchedules(long publishedSchedules) { this.publishedSchedules = publishedSchedules; }

        public long getArchivedSchedules() { return archivedSchedules; }
        public void setArchivedSchedules(long archivedSchedules) { this.archivedSchedules = archivedSchedules; }

        public long getTotalAssignments() { return totalAssignments; }
        public void setTotalAssignments(long totalAssignments) { this.totalAssignments = totalAssignments; }

        public double getAverageAssignmentsPerSchedule() { return averageAssignmentsPerSchedule; }
        public void setAverageAssignmentsPerSchedule(double averageAssignmentsPerSchedule) {
            this.averageAssignmentsPerSchedule = averageAssignmentsPerSchedule;
        }
    }

    class ScheduleConflictResult {
        private boolean hasConflict;
        private List<ShiftSchedule> conflictingSchedules;
        private String message;
        private ConflictSeverity severity;

        public ScheduleConflictResult() {}

        public ScheduleConflictResult(boolean hasConflict, List<ShiftSchedule> conflictingSchedules,
                                      String message, ConflictSeverity severity) {
            this.hasConflict = hasConflict;
            this.conflictingSchedules = conflictingSchedules;
            this.message = message;
            this.severity = severity;
        }

        public boolean hasConflict() { return hasConflict; } // ✅ Thêm method này

        public boolean isValid() { return !hasConflict; }

        public List<ShiftSchedule> getConflictingSchedules() { return conflictingSchedules; }
        public void setConflictingSchedules(List<ShiftSchedule> conflictingSchedules) {
            this.conflictingSchedules = conflictingSchedules;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public ConflictSeverity getSeverity() { return severity; }
        public void setSeverity(ConflictSeverity severity) { this.severity = severity; }

        public enum ConflictSeverity {
            LOW, MEDIUM, HIGH, CRITICAL
        }
    }
}
