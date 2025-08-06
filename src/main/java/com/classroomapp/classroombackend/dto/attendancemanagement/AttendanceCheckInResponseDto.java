package com.classroomapp.classroombackend.dto.attendancemanagement;

import lombok.Builder;
import lombok.Data;
import java.time.LocalTime;

@Data
@Builder
public class AttendanceCheckInResponseDto {
    private boolean success;
    private String message;
    private Long attendanceLogId;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    
    private LocationVerification locationVerification;
    private NetworkVerification networkVerification;
    
    @Data
    @Builder
    public static class LocationVerification {
        private boolean verified;
        private String locationName;
        private Integer distance;
        private String status;
    }
    
    @Data
    @Builder
    public static class NetworkVerification {
        private boolean verified;
        private String networkName;
        private String ipAddress;
        private String status;
    }
}