package com.classroomapp.classroombackend.dto.classroommanagement;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Session Statistics
 * Contains statistics and aggregated data for sessions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionStatisticsDto {
    
    private Long classroomId;
    private String classroomName;
    private int totalSessions;
    private int completedSessions;
    private int upcomingSessions;
    private int cancelledSessions;
    private LocalDate firstSessionDate;
    private LocalDate lastSessionDate;
    private double averageAttendanceRate;
    private int totalStudentsEnrolled;
    private List<SessionDto> recentSessions;
    private List<MonthlyStatistics> monthlyStatistics;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStatistics {
        private int month;
        private int year;
        private int sessionsCount;
        private double averageAttendance;
    }
}