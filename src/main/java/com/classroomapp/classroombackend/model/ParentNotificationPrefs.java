package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entity representing parent notification preferences
 * Based on PARENT_ROLE_SPEC.md requirements
 */
@Entity
@Table(name = "parent_notification_prefs")
@Data
@NoArgsConstructor
@ToString(exclude = {"parent"})
public class ParentNotificationPrefs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id", nullable = false)
    private Long parentId;

    @Column(name = "channels", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Boolean> channels; // {"inapp": true, "email": false, "sms": false}

    @Column(name = "quiet_hours", columnDefinition = "NVARCHAR(MAX)")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> quietHours; // {"from": "22:00", "to": "07:00"}

    @Column(name = "event_toggles", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Boolean> eventToggles; // Per-event notification settings

    @Enumerated(EnumType.STRING)
    @Column(name = "digest_frequency")
    private DigestFrequency digestFrequency = DigestFrequency.DAILY;

    @Column(name = "language_preference", length = 10)
    private String languagePreference = "vi";

    @Column(name = "timezone", length = 50)
    private String timezone = "Asia/Ho_Chi_Minh";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    @JsonBackReference
    private Parent parent;

    // Constructors

    public ParentNotificationPrefs(Long parentId) {
        this.parentId = parentId;
        this.channels = getDefaultChannels();
        this.quietHours = getDefaultQuietHours();
        this.eventToggles = getDefaultEventToggles();
        this.digestFrequency = DigestFrequency.DAILY;
        this.languagePreference = "vi";
        this.timezone = "Asia/Ho_Chi_Minh";
        this.updatedAt = LocalDateTime.now();
    }

    // Lifecycle callbacks

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods

    /**
     * Get default channel settings
     */
    private Map<String, Boolean> getDefaultChannels() {
        return Map.of(
            "inapp", true,
            "email", false,
            "sms", false
        );
    }

    /**
     * Get default quiet hours
     */
    private Map<String, String> getDefaultQuietHours() {
        return Map.of(
            "from", "22:00",
            "to", "07:00"
        );
    }

    /**
     * Get default event toggle settings
     */
    private Map<String, Boolean> getDefaultEventToggles() {
        return Map.of(
            "leave_notice_ack", true,      // Leave notice acknowledged
            "new_grade", true,             // New grade posted
            "assignment_due", true,        // Assignment due soon
            "invoice_issued", false,       // Invoice issued
            "attendance_flagged", true,    // Attendance flagged
            "teacher_message", true,       // Teacher sent message
            "schedule_change", true,       // Schedule change
            "announcement", false          // General announcements
        );
    }

    /**
     * Check if notification channel is enabled
     */
    public boolean isChannelEnabled(String channel) {
        return channels != null && Boolean.TRUE.equals(channels.get(channel));
    }

    /**
     * Check if event notifications are enabled
     */
    public boolean isEventEnabled(String eventType) {
        return eventToggles != null && Boolean.TRUE.equals(eventToggles.get(eventType));
    }

    /**
     * Enable/disable a notification channel
     */
    public void setChannelEnabled(String channel, boolean enabled) {
        if (channels != null) {
            channels.put(channel, enabled);
        }
    }

    /**
     * Enable/disable event notifications
     */
    public void setEventEnabled(String eventType, boolean enabled) {
        if (eventToggles != null) {
            eventToggles.put(eventType, enabled);
        }
    }

    /**
     * Check if in-app notifications are enabled
     */
    public boolean isInAppEnabled() {
        return isChannelEnabled("inapp");
    }

    /**
     * Check if email notifications are enabled
     */
    public boolean isEmailEnabled() {
        return isChannelEnabled("email");
    }

    /**
     * Check if SMS notifications are enabled
     */
    public boolean isSmsEnabled() {
        return isChannelEnabled("sms");
    }

    /**
     * Check if digest is enabled
     */
    public boolean isDigestEnabled() {
        return digestFrequency != null && !DigestFrequency.NONE.equals(digestFrequency);
    }

    /**
     * Get quiet hours start time
     */
    public String getQuietHoursStart() {
        return quietHours != null ? quietHours.get("from") : "22:00";
    }

    /**
     * Get quiet hours end time
     */
    public String getQuietHoursEnd() {
        return quietHours != null ? quietHours.get("to") : "07:00";
    }

    /**
     * Update quiet hours
     */
    public void setQuietHours(String from, String to) {
        if (quietHours != null) {
            quietHours.put("from", from);
            quietHours.put("to", to);
        }
    }

    /**
     * Reset to default settings
     */
    public void resetToDefaults() {
        this.channels = getDefaultChannels();
        this.quietHours = getDefaultQuietHours();
        this.eventToggles = getDefaultEventToggles();
        this.digestFrequency = DigestFrequency.DAILY;
        this.languagePreference = "vi";
        this.timezone = "Asia/Ho_Chi_Minh";
        this.updatedAt = LocalDateTime.now();
    }

    // Enums

    public enum DigestFrequency {
        NONE,    // Không gửi digest
        DAILY,   // Hàng ngày
        WEEKLY   // Hàng tuần
    }
}