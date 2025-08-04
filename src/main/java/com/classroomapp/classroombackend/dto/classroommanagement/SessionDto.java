package com.classroomapp.classroombackend.dto.classroommanagement;

import com.classroomapp.classroombackend.model.classroommanagement.Session;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO cho Session (Session)
 * Dùng trong Firebase sync để đồng bộ dữ liệu session
 */
@Data
public class SessionDto {
    private Long id;
    private Long classroomId;
    private String sessionDate;
    private String description;
    private Session.SessionStatus status;
}

/**
 * Các enum cho status của Session
 */
enum SessionStatus {
    SCHEDULED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}