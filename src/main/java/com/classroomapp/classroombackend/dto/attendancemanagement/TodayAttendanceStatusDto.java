package com.classroomapp.classroombackend.dto.attendancemanagement;

import lombok.Builder;
import lombok.Data;
import java.time.LocalTime;

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
}