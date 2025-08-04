package com.classroomapp.classroombackend.dto.hrmanagement;

import java.util.List;

import com.classroomapp.classroombackend.model.hrmanagement.ShiftSchedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho kết quả kiểm tra conflict của Schedule
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleConflictResult {
    private boolean hasConflict;
    private List<ShiftSchedule> conflictingSchedules;
    private String message;
    private ConflictSeverity severity;
    
    public enum ConflictSeverity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}