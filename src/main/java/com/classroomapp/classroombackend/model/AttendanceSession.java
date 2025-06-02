package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attendance_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The classroom this session belongs to
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    // The teacher responsible for the session
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    // Session start and end times
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Type of session: "ONLINE" or "OFFLINE"
    private String sessionType;

    // Status of the session: "SCHEDULED", "ACTIVE", "COMPLETED"
    private String status;

    // Session title or topic
    private String title;

    // Whether teacher attendance is auto-marked
    private boolean autoMarkTeacherAttendance;

    // Whether the session is currently active
    private boolean isActive;

    // When the session was created
    private LocalDateTime createdAt;
} 