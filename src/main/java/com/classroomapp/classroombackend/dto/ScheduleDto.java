package com.classroomapp.classroombackend.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Consolidated ScheduleDto that combines features from both previous versions
 * Supports both legacy integer day format and modern DayOfWeek enum
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private Long classroomId;
    private String className;
    private String classroomName;

    // Legacy support - Integer day format (0=Monday, 1=Tuesday, etc.)
    private Integer day;
    private String start; // Formatted as "HH:MM"
    private String end; // Formatted as "HH:MM"

    // Modern format - DayOfWeek enum and LocalTime
    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;
    private String dayName;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Location is required")
    private String location;
    private String room;
    private String subject;
    private String materialsUrl;
    private String meetUrl;
    private Integer studentCount;
    private String notes;
    private boolean isRecurring;

    // Legacy constructor that takes LocalTime for start and end and formats them
    public ScheduleDto(Long id, Long teacherId, String teacherName, Long classroomId, String className,
                     Integer day, LocalTime startTime, LocalTime endTime, String room,
                     String subject, String materialsUrl, String meetUrl, Integer studentCount) {
        this.id = id;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.classroomId = classroomId;
        this.className = className;
        this.day = day;
        this.start = startTime != null ? startTime.toString().substring(0, 5) : null; // Format as "HH:MM"
        this.end = endTime != null ? endTime.toString().substring(0, 5) : null; // Format as "HH:MM"
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
        this.location = room; // Map room to location for compatibility
        this.subject = subject;
        this.materialsUrl = materialsUrl;
        this.meetUrl = meetUrl;
        this.studentCount = studentCount;

        // Convert integer day to DayOfWeek for modern format
        if (day != null) {
            this.dayOfWeek = convertIntegerToDayOfWeek(day);
        }
    }

    // Helper method to format the day and time for display (from classroommanagement version)
    public String getFormattedSchedule() {
        if (dayOfWeek != null && startTime != null && endTime != null && location != null) {
            return dayOfWeek + " " + startTime + " - " + endTime + " at " + location;
        }
        return "Schedule not fully configured";
    }

    // Helper method to convert integer day to DayOfWeek
    private DayOfWeek convertIntegerToDayOfWeek(Integer day) {
        if (day == null) return null;
        switch (day) {
            case 0: return DayOfWeek.MONDAY;
            case 1: return DayOfWeek.TUESDAY;
            case 2: return DayOfWeek.WEDNESDAY;
            case 3: return DayOfWeek.THURSDAY;
            case 4: return DayOfWeek.FRIDAY;
            case 5: return DayOfWeek.SATURDAY;
            case 6: return DayOfWeek.SUNDAY;
            default: return DayOfWeek.MONDAY;
        }
    }

    // Helper method to convert DayOfWeek to integer
    public Integer getDayAsInteger() {
        if (dayOfWeek == null) return day;
        switch (dayOfWeek) {
            case MONDAY: return 0;
            case TUESDAY: return 1;
            case WEDNESDAY: return 2;
            case THURSDAY: return 3;
            case FRIDAY: return 4;
            case SATURDAY: return 5;
            case SUNDAY: return 6;
            default: return 0;
        }
    }
}