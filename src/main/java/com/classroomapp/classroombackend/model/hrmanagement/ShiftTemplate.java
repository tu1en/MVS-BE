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
 * Entity cho Shift Template - Máº«u ca lÃ m viá»‡c
 * Äá»‹nh nghÄ©a cÃ¡c loáº¡i ca lÃ m viá»‡c: Morning, Afternoon, Evening, Full Day, Custom
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
    @NotBlank(message = "TÃªn máº«u ca khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(max = 255, message = "TÃªn máº«u ca khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 255 kÃ½ tá»±")
    private String templateName;

    @Column(name = "template_code", nullable = false, length = 50, unique = true)
    @NotBlank(message = "MÃ£ máº«u ca khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(max = 50, message = "MÃ£ máº«u ca khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 50 kÃ½ tá»±")
    @Pattern(regexp = "^[A-Z]{2,10}$", message = "MÃ£ máº«u ca pháº£i lÃ  chá»¯ hoa, 2-10 kÃ½ tá»±")
    private String templateCode;

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    @Size(max = 1000, message = "MÃ´ táº£ khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 1000 kÃ½ tá»±")
    private String description;

    @Column(name = "start_time", nullable = false)
    @NotNull(message = "Thá»i gian báº¯t Ä‘áº§u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    @NotNull(message = "Thá»i gian káº¿t thÃºc khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;

    @Column(name = "break_start_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime breakStartTime;

    @Column(name = "break_end_time")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime breakEndTime;

    @Column(name = "break_duration_minutes")
    @Min(value = 0, message = "Thá»i gian nghá»‰ khÃ´ng Ä‘Æ°á»£c Ã¢m")
    @Max(value = 480, message = "Thá»i gian nghá»‰ khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 8 giá»")
    private Integer breakDurationMinutes = 0;

    @Column(name = "total_hours", nullable = false, precision = 4, scale = 2)
    @NotNull(message = "Tá»•ng sá»‘ giá» khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @DecimalMin(value = "0.25", message = "Tá»•ng sá»‘ giá» pháº£i Ã­t nháº¥t 15 phÃºt")
    @DecimalMax(value = "24.00", message = "Tá»•ng sá»‘ giá» khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 24 giá»")
    private BigDecimal totalHours;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_overtime_eligible", nullable = false)
    private Boolean isOvertimeEligible = false;

    @Column(name = "color_code", length = 7)
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "MÃ£ mÃ u pháº£i cÃ³ Ä‘á»‹nh dáº¡ng hex (#RRGGBB)")
    private String colorCode = "#1890ff";

    @Column(name = "sort_order")
    @Min(value = 0, message = "Thá»© tá»± sáº¯p xáº¿p khÃ´ng Ä‘Æ°á»£c Ã¢m")
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
        MORNING("MS", "Ca SÃ¡ng"),
        AFTERNOON("AS", "Ca Chiá»u"), 
        EVENING("ES", "Ca Tá»‘i"),
        FULL_DAY("FD", "Ca Cáº£ NgÃ y"),
        OVERTIME("OT", "Ca TÄƒng Ca"),
        CUSTOM("CT", "Ca TÃ¹y Chá»‰nh");

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
     * TÃ­nh toÃ¡n thá»i gian lÃ m viá»‡c thá»±c táº¿ (trá»« thá»i gian nghá»‰)
     */
    public BigDecimal getActualWorkingHours() {
        if (breakDurationMinutes == null || breakDurationMinutes == 0) {
            return totalHours;
        }
        return totalHours.subtract(BigDecimal.valueOf(breakDurationMinutes).divide(BigDecimal.valueOf(60), 2, BigDecimal.ROUND_HALF_UP));
    }

    /**
     * Kiá»ƒm tra xem ca cÃ³ há»£p lá»‡ khÃ´ng
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
     * Láº¥y thá»i lÆ°á»£ng ca lÃ m viá»‡c tÃ­nh báº±ng phÃºt
     */
    public long getDurationInMinutes() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }

    /**
     * Kiá»ƒm tra xem cÃ³ xung Ä‘á»™t thá»i gian vá»›i ca khÃ¡c khÃ´ng
     */
    public boolean hasTimeConflict(ShiftTemplate other) {
        if (other == null || other.startTime == null || other.endTime == null) {
            return false;
        }
        
        return this.startTime.isBefore(other.endTime) && this.endTime.isAfter(other.startTime);
    }

    /**
     * Format thá»i gian hiá»ƒn thá»‹
     */
    public String getTimeRangeDisplay() {
        if (startTime == null || endTime == null) {
            return "ChÆ°a xÃ¡c Ä‘á»‹nh";
        }
        return String.format("%s - %s", startTime.toString(), endTime.toString());
    }

    /**
     * Láº¥y thÃ´ng tin nghá»‰ giáº£i lao
     */
    public String getBreakTimeDisplay() {
        if (breakStartTime == null || breakEndTime == null) {
            return "KhÃ´ng cÃ³ nghá»‰";
        }
        return String.format("%s - %s (%d phÃºt)", 
                           breakStartTime.toString(), 
                           breakEndTime.toString(), 
                           breakDurationMinutes);
    }

    @PrePersist
    @PreUpdate
    private void validateEntity() {
        if (!isValidShift()) {
            throw new IllegalStateException("ThÃ´ng tin ca lÃ m viá»‡c khÃ´ng há»£p lá»‡");
        }
    }
}
