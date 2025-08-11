package com.classroomapp.classroombackend.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonUpdateDto {
    private Long lessonId;
    private LocalDate newDate;
    private String newStartTime; // HH:mm
    private String newEndTime;   // HH:mm
}


