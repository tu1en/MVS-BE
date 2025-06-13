package com.classroomapp.classroombackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendeeDto {
    private Long id;
    private Long eventId;
    private Long userId;
    private String userName;
    private String attendanceStatus;
    private LocalDateTime responseDate;
}
