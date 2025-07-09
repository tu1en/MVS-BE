package com.classroomapp.classroombackend.model.attendancemanagement;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
@Builder
public class AttendanceSession {
    public enum SessionStatus {
        OPEN,
        CLOSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Column(name = "created_at", nullable = true)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = true)
    private LocalDateTime expiresAt;

    @Column(nullable = true)
    @Builder.Default
    private Boolean isOpen = true;

    private String qrCodeData; // Can store a unique identifier for the QR code

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> records;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SessionStatus status;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(nullable = false)
    private LocalDate sessionDate;

    @Column(name = "auto_mark_teacher_attendance", nullable = false)
    @Builder.Default
    private boolean autoMarkTeacherAttendance = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
    
    @Column(name = "teacher_clock_in_time", nullable = true)
    private LocalDateTime teacherClockInTime;

    public void setTeacherClockInTime(LocalDateTime teacherClockInTime) {
        this.teacherClockInTime = teacherClockInTime;
    }

    public SessionStatus getStatus() {
        return status;
    }

    public void setStatus(SessionStatus status) {
        this.status = status;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }
} 