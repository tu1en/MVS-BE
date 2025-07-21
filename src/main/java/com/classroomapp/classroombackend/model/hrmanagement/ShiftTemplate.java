package com.classroomapp.classroombackend.model.hrmanagement;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Entity cho Shift Template - Mẫu ca làm việc
 * Định nghĩa các loại ca làm việc: Morning, Afternoon, Evening, Full Day, Custom
 */
@Entity
@Table(name = "shift_templates", indexes = {
    @Index(name = "IX_shift_templates_active", columnList = "is_active"),
    @Index(name = "IX_shift_templates_code", columnList = "template_code"),
    @Index(name = "IX_shift_templates_sort", columnList = "sort_order")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShiftTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "template_name", nullable = false, length = 255)
    @NotBlank(message = "Tên mẫu ca không được để trống")
    @Size(max = 255, message = "Tên mẫu ca không được vượt quá 255 ký tự")
    private String templateName;

    @Column(name = "template_code", nullable = false, length = 50, unique = true)
    @NotBlank(message = "Mã mẫu ca không được để trống")
    @Size(max = 50, message = "Mã mẫu ca không được vượt quá 50 ký tự")
    @Pattern(regexp = "^[A-Z]{2,10}$", message = "Mã mẫu ca phải là chữ hoa, 2-10 ký tự")
    private String templateCode;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    @Column(name = "start_time", nullable = false)
    @NotNull(message = "Thời gian bắt đầu không được để trống")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    @NotNull(message = "Thời gian kết thúc không được để trống")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @Column(name = "break_start_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime breakEndTime;

    @Column(name = "break_duration_minutes")
    @Min(value = 0, message = "Thời gian nghỉ không được âm")
    @Max(value = 480, message = "Thời gian nghỉ không được vượt quá 8 giờ")
    private Integer breakDurationMinutes = 0;

    @Column(name = "total_hours", nullable = false, precision = 4, scale = 2)
    @NotNull(message = "Tổng số giờ không được để trống")
    @DecimalMin(value = "0.25", message = "Tổng số giờ phải ít nhất 15 phút")
    @DecimalMax(value = "24.00", message = "Tổng số giờ không được vượt quá 24 giờ")
    private BigDecimal totalHours;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_overtime_eligible", nullable = false)
    private Boolean isOvertimeEligible = false;

    @Column(name = "color_code", length = 7)
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Mã màu phải có định dạng hex (#RRGGBB)")
    private String colorCode = "#1890ff";

    @Column(name = "sort_order")
    @Min(value = 0, message = "Thứ tự sắp xếp không được âm")
    private Integer sortOrder = 0;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false, foreignKey = @ForeignKey(name = "FK_shift_templates_created_by"))
    private User createdBy;

    @OneToMany(mappedBy = "shiftTemplate", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ShiftAssignment> assignments;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Enums
    public enum ShiftType {
        MORNING("MS", "Ca Sáng"),
        AFTERNOON("AS", "Ca Chiều"), 
        EVENING("ES", "Ca Tối"),
        FULL_DAY("FD", "Ca Cả Ngày"),
        OVERTIME("OT", "Ca Tăng Ca"),
        CUSTOM("CT", "Ca Tùy Chỉnh");

        private final String code;
        private final String displayName;

        ShiftType(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }

        public String getCode() { return code; }
        public String getDisplayName() { return displayName; }
    }

    // Business methods
    /**
     * Tính toán thời gian làm việc thực tế (trừ thời gian nghỉ)
     */
    public BigDecimal getActualWorkingHours() {
        if (breakDurationMinutes == null || breakDurationMinutes == 0) {
            return totalHours;
        }
        return totalHours.subtract(BigDecimal.valueOf(breakDurationMinutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP));
    }

    /**
     * Kiểm tra xem ca có hợp lệ không
     */
    public boolean isValidShift() {
        if (startTime == null || endTime == null) {
            return false;
        }
        
        if (!startTime.isBefore(endTime)) {
            return false;
        }

        if (breakStartTime != null && breakEndTime != null) {
            return breakStartTime.isAfter(startTime) && 
                   breakEndTime.isBefore(endTime) && 
                   breakStartTime.isBefore(breakEndTime);
        }

        return true;
    }

    /**
     * Lấy thời lượng ca làm việc tính bằng phút
     */
    public long getDurationInMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Kiểm tra xem có xung đột thời gian với ca khác không
     */
    public boolean hasTimeConflict(ShiftTemplate other) {
        if (other == null || other.startTime == null || other.endTime == null) {
            return false;
        }
        
        return this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime);
    }

    /**
     * Format thời gian hiển thị
     */
    public String getTimeRangeDisplay() {
        if (startTime == null || endTime == null) {
            return "Chưa xác định";
        }
        return String.format("%s - %s", startTime.toString(), endTime.toString());
    }

    /**
     * Lấy thông tin nghỉ giải lao
     */
    public String getBreakTimeDisplay() {
        if (breakStartTime == null || breakEndTime == null) {
            return "Không có nghỉ";
        }
        return String.format("%s - %s (%d phút)", 
                           breakStartTime.toString(), 
                           breakEndTime.toString(), 
                           breakDurationMinutes);
    }

    @PrePersist
    @PreUpdate
    private void validateEntity() {
        if (!isValidShift()) {
            throw new IllegalStateException("Thông tin ca làm việc không hợp lệ");
        }
    }
}
