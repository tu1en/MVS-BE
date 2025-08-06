package com.classroomapp.classroombackend.dto.attendancemanagement;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceVerificationDto {
    @NotNull
    private Double latitude;
    
    @NotNull
    private Double longitude;
    
    private Double accuracy;
    
    private String publicIp;
    private String userAgent;
    private String deviceFingerprint;
    private String notes;
}