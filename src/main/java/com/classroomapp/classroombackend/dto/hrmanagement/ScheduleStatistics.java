package com.classroomapp.classroombackend.dto.hrmanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho Schedule Statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleStatistics {
    private long totalSchedules;
    private long draftCount;
    private long publishedCount;
    private long archivedCount;
    private long cancelledCount;
    private long totalAssignments;
    private double averageAssignmentsPerSchedule;
    
}