package com.classroomapp.classroombackend.dto;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDto {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private Long classroomId;
    private String className;
    private Integer day; // 0=Monday, 1=Tuesday, 2=Wednesday, 3=Thursday, 4=Friday, 5=Saturday, 6=Sunday
    private String start; // Formatted as "HH:MM"
    private String end; // Formatted as "HH:MM"
    private String room;
    private String subject;
    private String materialsUrl;
    private String meetUrl;
    private Integer studentCount;
    
    // Constructor that takes LocalTime for start and end and formats them
    public ScheduleDto(Long id, Long teacherId, String teacherName, Long classroomId, String className, 
                     Integer day, LocalTime startTime, LocalTime endTime, String room, 
                     String subject, String materialsUrl, String meetUrl, Integer studentCount) {
        this.id = id;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.classroomId = classroomId;
        this.className = className;
        this.day = day;
        this.start = startTime.toString().substring(0, 5); // Format as "HH:MM"
        this.end = endTime.toString().substring(0, 5); // Format as "HH:MM"
        this.room = room;
        this.subject = subject;
        this.materialsUrl = materialsUrl;
        this.meetUrl = meetUrl;
        this.studentCount = studentCount;
    }
} 