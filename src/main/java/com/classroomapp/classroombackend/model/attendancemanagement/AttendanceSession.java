package com.classroomapp.classroombackend.model.attendancemanagement;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession.SessionStatus;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attendance_sessions")
@Data
@Builder
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

    // Session name
    private String sessionName;
    
    // Session date
    private LocalDateTime sessionDate;
    
    // Session start and end times
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Type of session: "ONLINE" or "OFFLINE"
    private String sessionType;

    // Status of the session
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    // Session title or topic
    private String title;
    
    // Session description
    private String description;

    // Whether teacher attendance is auto-marked
    private boolean autoMarkTeacherAttendance;

    // Whether the session is currently active
    private boolean isActive;
    
    // Location data for attendance
    private Double locationLatitude;
    private Double locationLongitude;
    private Integer locationRadiusMeters;

    // When the session was created
    private LocalDateTime createdAt;
    
    // Session status enum
    public enum SessionStatus {
        SCHEDULED,
        ACTIVE,
        COMPLETED
    }
}
