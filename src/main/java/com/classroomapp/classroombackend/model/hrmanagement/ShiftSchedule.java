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
 * Entity cho Shift Schedule - Lá»‹ch lÃ m viá»‡c
 * Quáº£n lÃ½ cÃ¡c lá»‹ch lÃ m viá»‡c theo tuáº§n/thÃ¡ng
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
    @NotBlank(message = "TÃªn lá»‹ch lÃ m viá»‡c khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(max = 255, message = "TÃªn lá»‹ch lÃ m viá»‡c khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 255 kÃ½ tá»±")
    private String scheduleName;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    @Size(max = 1000, message = "MÃ´ táº£ khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 1000 kÃ½ tá»±")
    private String description;

    @Column(name = "start_date", nullable = false)
    @NotNull(message = "NgÃ y báº¯t Ä‘áº§u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Future(message = "NgÃ y báº¯t Ä‘áº§u pháº£i lÃ  ngÃ y trong tÆ°Æ¡ng lai")
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    @NotNull(message = "NgÃ y káº¿t thÃºc khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", length = 50, nullable = false)
    private ScheduleType scheduleType = ScheduleType.WEEKLY;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private ScheduleStatus status = ScheduleStatus.DRAFT;

    @Column(name = "total_assignments")
    @Min(value = 0, message = "Tá»•ng sá»‘ phÃ¢n cÃ´ng khÃ´ng Ä‘Æ°á»£c Ã¢m")
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
        WEEKLY("HÃ ng tuáº§n"),
        MONTHLY("HÃ ng thÃ¡ng"),
        CUSTOM("TÃ¹y chá»‰nh");

        private final String displayName;

        ScheduleType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    public enum ScheduleStatus {
        DRAFT("Báº£n nhÃ¡p"),
        PUBLISHED("ÄÃ£ xuáº¥t báº£n"),
        ARCHIVED("ÄÃ£ lÆ°u trá»¯"),
        CANCELLED("ÄÃ£ há»§y");

        private final String displayName;

        ScheduleStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    // Business methods
    /**
     * Kiá»ƒm tra xem lá»‹ch cÃ³ há»£p lá»‡ khÃ´ng
     */
    public boolean isValidSchedule() {
        return startDate != null && endDate != null && !startDate.isAfter(endDate);
    }

    /**
     * TÃ­nh sá»‘ ngÃ y cá»§a lá»‹ch
     */
    public long getDurationInDays() {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * Kiá»ƒm tra xem lá»‹ch cÃ³ Ä‘ang hoáº¡t Ä‘á»™ng khÃ´ng
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return status == ScheduleStatus.PUBLISHED && 
               !startDate.isAfter(today) && 
               !endDate.isBefore(today);
    }

    /**
     * Kiá»ƒm tra xem cÃ³ thá»ƒ chá»‰nh sá»­a lá»‹ch khÃ´ng
     */
    public boolean isEditable() {
        return status == ScheduleStatus.DRAFT;
    }

    /**
     * Kiá»ƒm tra xem cÃ³ thá»ƒ xuáº¥t báº£n lá»‹ch khÃ´ng
     */
    public boolean canPublish() {
        return status == ScheduleStatus.DRAFT && 
               totalAssignments > 0 && 
               isValidSchedule();
    }

    /**
     * Xuáº¥t báº£n lá»‹ch
     */
    public void publish(User publisher) {
        if (!canPublish()) {
            throw new IllegalStateException("KhÃ´ng thá»ƒ xuáº¥t báº£n lá»‹ch trong tráº¡ng thÃ¡i hiá»‡n táº¡i");
        }
        this.status = ScheduleStatus.PUBLISHED;
        this.publishedBy = publisher;
        this.publishedAt = LocalDateTime.now();
    }

    /**
     * LÆ°u trá»¯ lá»‹ch
     */
    public void archive() {
        if (status != ScheduleStatus.PUBLISHED) {
            throw new IllegalStateException("Chá»‰ cÃ³ thá»ƒ lÆ°u trá»¯ lá»‹ch Ä‘Ã£ xuáº¥t báº£n");
        }
        this.status = ScheduleStatus.ARCHIVED;
        this.archivedAt = LocalDateTime.now();
    }

    /**
     * Há»§y lá»‹ch
     */
    public void cancel() {
        if (status == ScheduleStatus.ARCHIVED) {
            throw new IllegalStateException("KhÃ´ng thá»ƒ há»§y lá»‹ch Ä‘Ã£ lÆ°u trá»¯");
        }
        this.status = ScheduleStatus.CANCELLED;
    }

    /**
     * Cáº­p nháº­t sá»‘ lÆ°á»£ng phÃ¢n cÃ´ng
     */
    public void updateAssignmentCount() {
        if (assignments != null) {
            this.totalAssignments = assignments.size();
        }
    }

    /**
     * Láº¥y thÃ´ng tin hiá»ƒn thá»‹ thá»i gian
     */
    public String getDateRangeDisplay() {
        if (startDate == null || endDate == null) {
            return "ChÆ°a xÃ¡c Ä‘á»‹nh";
        }
        return String.format("%s Ä‘áº¿n %s", startDate.toString(), endDate.toString());
    }

    /**
     * Láº¥y mÃ u hiá»ƒn thá»‹ theo tráº¡ng thÃ¡i
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
            throw new IllegalStateException("ThÃ´ng tin lá»‹ch lÃ m viá»‡c khÃ´ng há»£p lá»‡");
        }
        
        // Auto-generate schedule name if not provided
        if (scheduleName == null || scheduleName.trim().isEmpty()) {
            scheduleName = String.format("Lá»‹ch %s - %s", 
                                       scheduleType.getDisplayName(), 
                                       getDateRangeDisplay());
        }
    }
}
