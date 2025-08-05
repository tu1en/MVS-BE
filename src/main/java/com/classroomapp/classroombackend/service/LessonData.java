package com.classroomapp.classroombackend.service;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonData {
    private Integer week;
    private String topicName;
    private String lessonType;
    private String objectives;
    private String requirements;
    private String preparations;
    private Integer durationMinutes;
    
    public boolean isValid() {
        return week != null && topicName != null && !topicName.trim().isEmpty();
    }
}