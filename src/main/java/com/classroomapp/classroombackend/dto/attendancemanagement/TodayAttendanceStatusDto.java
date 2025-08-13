package com.classroomapp.classroombackend.dto.attendancemanagement;

import java.time.LocalTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TodayAttendanceStatusDto {
    private boolean hasCheckedIn;
    private boolean hasCheckedOut;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String checkInLocation;
    private String checkOutLocation;
    private Double workingHours;
    private Double weeklyHours;
    private Integer lateCount;
    private Integer remainingLeaves;
    private String status;
    // Audit/notes for detailed log
    private String notes;
    private String attendanceType; // NORMAL/OVERTIME/WEEKEND/HOLIDAY...
    private Integer overtimeMinutes;
}