package com.classroomapp.classroombackend.model.hrmanagement;

import com.classroomapp.classroombackend.model.usermanagement.User;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_verification_logs")
@Data
public class AttendanceVerificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_log_id")
    private StaffAttendanceLog attendanceLog;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type")
    private VerificationType verificationType;
    
    @Column(name = "gps_latitude")
    private Double gpsLatitude;
    
    @Column(name = "gps_longitude")
    private Double gpsLongitude;
    
    @Column(name = "gps_accuracy")
    private Integer gpsAccuracy;
    
    @Column(name = "location_verified")
    private boolean locationVerified;
    
    @Column(name = "location_distance")
    private Integer locationDistance;
    
    @Column(name = "public_ip")
    private String publicIp;
    
    @Column(name = "network_verified")
    private boolean networkVerified;
    
    @Column(name = "network_name")
    private String networkName;
    
    @Column(name = "user_agent", columnDefinition = "NVARCHAR(500)")
    private String userAgent;
    
    @Column(name = "device_fingerprint")
    private String deviceFingerprint;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus;
    
    @Column(name = "failure_reason", columnDefinition = "NVARCHAR(500)")
    private String failureReason;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum VerificationType {
        CHECK_IN, CHECK_OUT
    }
    
    public enum VerificationStatus {
        SUCCESS, FAILED, PARTIAL
    }
}