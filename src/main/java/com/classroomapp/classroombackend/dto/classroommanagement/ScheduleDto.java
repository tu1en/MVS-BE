package com.classroomapp.classroombackend.dto.classroommanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * DTO for schedule response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {

    private Long id;
    private Long classroomId;
    private String classroomName;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private String location;
    private String notes;
    private boolean isRecurring;

    /**
     * Get duration in minutes
     */
    public long getDurationInMinutes() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).toMinutes();
        }
        return 0;
    }

    /**
     * Get formatted time range
     */
    public String getTimeRange() {
        if (startTime != null && endTime != null) {
            return String.format("%s - %s", startTime.toString(), endTime.toString());
        }
        return "";
    }

    /**
     * Get day name in Vietnamese
     */
    public String getDayNameVi() {
        if (dayOfWeek == null) return "";
        
        switch (dayOfWeek) {
            case MONDAY: return "Thứ 2";
            case TUESDAY: return "Thứ 3";
            case WEDNESDAY: return "Thứ 4";
            case THURSDAY: return "Thứ 5";
            case FRIDAY: return "Thứ 6";
            case SATURDAY: return "Thứ 7";
            case SUNDAY: return "Chủ nhật";
            default: return dayOfWeek.toString();
        }
    }
}