package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomScheduleDto {
    
    private Long id;
    
    private Long roomId;
    
    private Long classId;
    
    private String className;
    
    private String title; // Event title
    
    private String date; // YYYY-MM-DD format
    
    private String startTime; // HH:MM format
    
    private String endTime; // HH:MM format
    
    private String status; // scheduled, in_progress, completed, cancelled
    
    private String teacherName;
    
    private Integer studentCount;
    
    private String description;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime scheduledDate; // Full datetime for the scheduled event
    
    // Additional fields for compatibility
    private String subject;
    
    private String type; // lecture, exam, seminar, etc.
}