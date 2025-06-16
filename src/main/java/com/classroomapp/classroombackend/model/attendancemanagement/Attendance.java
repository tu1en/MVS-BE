package com.classroomapp.classroombackend.model.attendancemanagement;

import java.time.LocalDateTime;

import com.classroomapp.classroombackend.model.usermanagement.User;

import jakarta.persistence.Column;
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
@Table(name = "attendances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user (student or teacher) this attendance record belongs to
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User student;

    // The session this attendance belongs to
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "session_id")
    private AttendanceSession session;

    // Attendance status
    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    // The check-in time
    private LocalDateTime checkInTime;

    // The check-out time (optional)
    private LocalDateTime checkOutTime;

    // Geolocation data (if applicable for offline sessions)
    private Double latitude;
    private Double longitude;
    
    // Notes or comments
    @Column(length = 500)
    private String notes;
    
    // When the attendance was recorded
    private LocalDateTime createdAt;
    
    // When the attendance was updated
    private LocalDateTime updatedAt;

    // Attendance status enum
    public enum AttendanceStatus {
        PRESENT,
        ABSENT,
        LATE,
        EXCUSED
    }
}
