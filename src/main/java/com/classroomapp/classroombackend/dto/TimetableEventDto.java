package com.classroomapp.classroombackend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimetableEventDto {
    private Long id;
    private String title;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDatetime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDatetime;
    private String eventType;
    private Long classroomId;
    private String classroomName;
    private Long lectureId; // Add lectureId field for attendance navigation
    private Long createdBy;
    private String location;
    private Boolean isAllDay;
    private Integer reminderMinutes;
    private String color;
    private String recurringRule;
    private Long parentEventId;
    private Boolean isCancelled;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // Additional fields for teacher schedule frontend compatibility
    private Integer day; // 0=Monday, 1=Tuesday, etc.
    private String dayName;
    private String start; // "HH:MM" format
    private String end; // "HH:MM" format
    private String className; // Classroom/class name
    private String subject; // Subject/course name
    private String teacherName;
    private Long teacherId;
    private String room;
    private String materialsUrl;
    private String meetUrl;
    private LocalDate date; // Calculated date for this occurrence
    private Integer studentCount;
}
