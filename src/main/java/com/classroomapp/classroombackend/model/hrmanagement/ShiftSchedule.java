package com.classroomapp.classroombackend.model.hrmanagement;

import com.classroomapp.classroombackend.model.usermanagement.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity cho Shift Schedule - Lịch làm việc
 * Quản lý các lịch làm việc theo tuần/tháng
 */
@Entity
@Table(name = "shift_schedules", indexes = {
    @Index(name = "IX_shift_schedules_dates", columnList = "start_date, end_date"),
    @Index(name = "IX_shift_schedules_status", columnList = "status"),
    @Index(name = "IX_shift_schedules_type", columnList = "schedule_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "schedule_name", nullable = false, length = 255)
    @NotBlank(message = "Tên lịch làm việc không được để trống")
    @Size(max = 255, message = "Tên lịch làm việc không được vượt quá 255 ký tự")
    private String scheduleName;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Future(message = "Ngày bắt đầu phải là ngày trong tương lai")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", length = 50, nullable = false)
    private ScheduleType scheduleType = ScheduleType.WEEKLY;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private ScheduleStatus status = ScheduleStatus.DRAFT;

    @Column(name = "total_assignments")
    @Min(value = 0, message = "Tổng số phân công không được âm")
    private Integer totalAssignments = 0;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, foreignKey = @ForeignKey(name = "FK_shift_schedules_created_by"))
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by", foreignKey = @ForeignKey(name = "FK_shift_schedules_published_by"))
    private User publishedBy;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShiftAssignment> assignments;

    // Timestamps
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Enums
    public enum ScheduleType {
        WEEKLY("Hàng tuần"),
        MONTHLY("Hàng tháng"),
        CUSTOM("Tùy chỉnh");

        private final String displayName;

        ScheduleType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    public enum ScheduleStatus {
        DRAFT("Bản nháp"),
        PUBLISHED("Đã xuất bản"),
        ARCHIVED("Đã lưu trữ"),
        CANCELLED("Đã hủy");

        private final String displayName;

        ScheduleStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    // Business methods
    /**
     * Kiểm tra xem lịch có hợp lệ không
     */
    public boolean isValidSchedule() {
        return startDate != null && endDate != null && !startDate.isAfter(endDate);
    }

    /**
     * Tính số ngày của lịch
     */
    public long getDurationInDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Kiểm tra xem lịch có đang hoạt động không
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return status == ScheduleStatus.PUBLISHED && 
               !startDate.isAfter(today) && 
               !endDate.isBefore(today);
    }

    /**
     * Kiểm tra xem có thể chỉnh sửa lịch không
     */
    public boolean isEditable() {
        return status == ScheduleStatus.DRAFT;
    }

    /**
     * Kiểm tra xem có thể xuất bản lịch không
     */
    public boolean canPublish() {
        return status == ScheduleStatus.DRAFT && 
               totalAssignments > 0 && 
               isValidSchedule();
    }

    /**
     * Xuất bản lịch
     */
    public void publish(User publisher) {
        if (!canPublish()) {
            throw new IllegalStateException("Không thể xuất bản lịch trong trạng thái hiện tại");
        }
        this.status = ScheduleStatus.PUBLISHED;
        this.publishedBy = publisher;
        this.publishedAt = LocalDateTime.now();
    }

    /**
     * Lưu trữ lịch
     */
    public void archive() {
        if (status != ScheduleStatus.PUBLISHED) {
            throw new IllegalStateException("Chỉ có thể lưu trữ lịch đã xuất bản");
        }
        this.status = ScheduleStatus.ARCHIVED;
        this.archivedAt = LocalDateTime.now();
    }

    /**
     * Hủy lịch
     */
    public void cancel() {
        if (status == ScheduleStatus.ARCHIVED) {
            throw new IllegalStateException("Không thể hủy lịch đã lưu trữ");
        }
        this.status = ScheduleStatus.CANCELLED;
    }

    /**
     * Cập nhật số lượng phân công
     */
    public void updateAssignmentCount() {
        if (assignments != null) {
            this.totalAssignments = assignments.size();
        }
    }

    /**
     * Lấy thông tin hiển thị thời gian
     */
    public String getDateRangeDisplay() {
        if (startDate == null || endDate == null) {
            return "Chưa xác định";
        }
        return String.format("%s đến %s", startDate.toString(), endDate.toString());
    }

    /**
     * Lấy màu hiển thị theo trạng thái
     */
    public String getStatusColor() {
        switch (status) {
            case DRAFT: return "#faad14";
            case PUBLISHED: return "#52c41a";
            case ARCHIVED: return "#d9d9d9";
            case CANCELLED: return "#ff4d4f";
            default: return "#1890ff";
        }
    }

    @PrePersist
    @PreUpdate
    private void validateEntity() {
        if (!isValidSchedule()) {
            throw new IllegalStateException("Thông tin lịch làm việc không hợp lệ");
        }
        
        // Auto-generate schedule name if not provided
        if (scheduleName == null || scheduleName.trim().isEmpty()) {
            scheduleName = String.format("Lịch %s - %s", 
                                       scheduleType.getDisplayName(), 
                                       getDateRangeDisplay());
        }
    }
}
