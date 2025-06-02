package com.classroomapp.classroombackend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
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
@Table(name = "attendances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user (student or teacher) this attendance record belongs to
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    // The classroom this attendance belongs to
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    // The session date for this attendance record
    private LocalDateTime sessionDate;

    // Whether the user is present or absent
    private boolean isPresent;

    // Type of attendance: "ONLINE" or "OFFLINE"
    private String attendanceType;

    // Optional comment for the attendance record
    @Column(length = 500)
    private String comment;

    // URL to student's photo (if applicable)
    private String photoUrl;

    // Geolocation data (if applicable for offline sessions)
    private Double latitude;
    private Double longitude;
    
    // IP address used for attendance (if applicable for online sessions)
    private String ipAddress;
    
    // The teacher who marked this attendance
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "marked_by_id")
    private User markedBy;
    
    // When the attendance was recorded
    private LocalDateTime createdAt;
    
    // Whether this is a teacher or student attendance record
    private boolean isTeacherRecord;
} 