package com.classroomapp.classroombackend.service;

import java.net.InetAddress;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceCheckInResponseDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceVerificationDto;
import com.classroomapp.classroombackend.dto.attendancemanagement.TodayAttendanceStatusDto;
import com.classroomapp.classroombackend.model.hrmanagement.AllowedNetwork;
import com.classroomapp.classroombackend.model.hrmanagement.AttendanceVerificationLog;
import com.classroomapp.classroombackend.model.hrmanagement.CompanyLocation;
import com.classroomapp.classroombackend.model.hrmanagement.StaffAttendanceLog;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.hrmanagement.AllowedNetworkRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.AttendanceVerificationLogRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.CompanyLocationRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.StaffAttendanceLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceVerificationService {
    
    private final StaffAttendanceLogRepository attendanceLogRepository;
    private final CompanyLocationRepository locationRepository;
    private final AllowedNetworkRepository networkRepository;
    private final AttendanceVerificationLogRepository verificationLogRepository;
    
    @Transactional
    public AttendanceCheckInResponseDto processCheckIn(User user, AttendanceVerificationDto dto) {
        log.info("Processing check-in for user: {}", user.getUsername());
        
        // 1. Verify location
        LocationVerificationResult locationResult = verifyLocation(
            dto.getLatitude(), 
            dto.getLongitude(), 
            dto.getAccuracy()
        );
        
        // 2. Verify network/IP
        NetworkVerificationResult networkResult = verifyNetwork(dto.getPublicIp());
        
        // 3. Check if both verifications pass
        boolean canCheckIn = locationResult.isValid() && networkResult.isValid();
        
        if (!canCheckIn) {
            // Log failed attempt
            logVerificationAttempt(user, dto, locationResult, networkResult, 
                AttendanceVerificationLog.VerificationType.CHECK_IN,
                AttendanceVerificationLog.VerificationStatus.FAILED);
            
            return AttendanceCheckInResponseDto.builder()
                .success(false)
                .message("Xác thực thất bại. Vui lòng kiểm tra vị trí và mạng.")
                .locationVerification(AttendanceCheckInResponseDto.LocationVerification.builder()
                    .verified(locationResult.isValid())
                    .status(locationResult.getMessage())
                    .distance(locationResult.getDistance())
                    .build())
                .networkVerification(AttendanceCheckInResponseDto.NetworkVerification.builder()
                    .verified(networkResult.isValid())
                    .status(networkResult.getMessage())
                    .ipAddress(dto.getPublicIp())
                    .build())
                .build();
        }
        
        // 4. Create attendance log
        StaffAttendanceLog attendanceLog = createOrUpdateAttendanceLog(user, true);
        
        // 5. Log successful verification
        logVerificationAttempt(user, dto, locationResult, networkResult,
            AttendanceVerificationLog.VerificationType.CHECK_IN,
            AttendanceVerificationLog.VerificationStatus.SUCCESS);
        
        return AttendanceCheckInResponseDto.builder()
            .success(true)
            .message("Check-in thành công")
            .attendanceLogId(attendanceLog.getId())
            .checkInTime(attendanceLog.getCheckInTime())
            .locationVerification(AttendanceCheckInResponseDto.LocationVerification.builder()
                .verified(true)
                .locationName(locationResult.getLocationName())
                .distance(locationResult.getDistance())
                .status("Trong phạm vi cho phép")
                .build())
            .networkVerification(AttendanceCheckInResponseDto.NetworkVerification.builder()
                .verified(true)
                .networkName(networkResult.getNetworkName())
                .ipAddress(dto.getPublicIp())
                .status("Mạng công ty")
                .build())
            .build();
    }
    
    @Transactional
    public AttendanceCheckInResponseDto processCheckOut(User user, AttendanceVerificationDto dto) {
        log.info("Processing check-out for user: {}", user.getUsername());
        
        // Similar to check-in but update check-out time
        LocationVerificationResult locationResult = verifyLocation(
            dto.getLatitude(), 
            dto.getLongitude(), 
            dto.getAccuracy()
        );
        
        NetworkVerificationResult networkResult = verifyNetwork(dto.getPublicIp());
        
        boolean canCheckOut = locationResult.isValid() && networkResult.isValid();
        
        if (!canCheckOut) {
            logVerificationAttempt(user, dto, locationResult, networkResult,
                AttendanceVerificationLog.VerificationType.CHECK_OUT,
                AttendanceVerificationLog.VerificationStatus.FAILED);
            
            return AttendanceCheckInResponseDto.builder()
                .success(false)
                .message("Xác thực thất bại")
                .locationVerification(AttendanceCheckInResponseDto.LocationVerification.builder()
                    .verified(locationResult.isValid())
                    .status(locationResult.getMessage())
                    .build())
                .networkVerification(AttendanceCheckInResponseDto.NetworkVerification.builder()
                    .verified(networkResult.isValid())
                    .status(networkResult.getMessage())
                    .build())
                .build();
        }
        
        // Update existing attendance log
        StaffAttendanceLog attendanceLog = createOrUpdateAttendanceLog(user, false);
        
        // Log successful verification
        logVerificationAttempt(user, dto, locationResult, networkResult,
            AttendanceVerificationLog.VerificationType.CHECK_OUT,
            AttendanceVerificationLog.VerificationStatus.SUCCESS);
        
        return AttendanceCheckInResponseDto.builder()
            .success(true)
            .message("Check-out thành công")
            .attendanceLogId(attendanceLog.getId())
            .checkOutTime(attendanceLog.getCheckOutTime())
            .locationVerification(AttendanceCheckInResponseDto.LocationVerification.builder()
                .verified(true)
                .locationName(locationResult.getLocationName())
                .distance(locationResult.getDistance())
                .status("Trong phạm vi cho phép")
                .build())
            .networkVerification(AttendanceCheckInResponseDto.NetworkVerification.builder()
                .verified(true)
                .networkName(networkResult.getNetworkName())
                .ipAddress(dto.getPublicIp())
                .status("Mạng công ty")
                .build())
            .build();
    }
    
    public TodayAttendanceStatusDto getTodayStatus(User user) {
        LocalDate today = LocalDate.now();
        
        Optional<StaffAttendanceLog> todayLog = attendanceLogRepository
            .findByUserAndAttendanceDate(user, today);
        
        if (todayLog.isEmpty()) {
            return TodayAttendanceStatusDto.builder()
                .hasCheckedIn(false)
                .hasCheckedOut(false)
                .status("Chưa chấm công")
                .workingHours(0.0)
                .build();
        }
        
        StaffAttendanceLog log = todayLog.get();
        
        return TodayAttendanceStatusDto.builder()
            .hasCheckedIn(log.getCheckInTime() != null)
            .hasCheckedOut(log.getCheckOutTime() != null)
            .checkInTime(log.getCheckInTime())
            .checkOutTime(log.getCheckOutTime())
            .workingHours(log.getWorkingHours())
            .status(log.getAttendanceStatus())
            .build();
    }
    
    public List<CompanyLocation> getActiveLocations() {
        return locationRepository.findByActiveTrue();
    }
    
    public Object getWeeklyStats(User user) {
        LocalDate startOfWeek = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        List<StaffAttendanceLog> weekLogs = attendanceLogRepository.findByUserAndAttendanceDateBetween(user, startOfWeek, endOfWeek);
        
        double totalHours = weekLogs.stream()
            .filter(log -> log.getWorkingHours() > 0)
            .mapToDouble(StaffAttendanceLog::getWorkingHours)
            .sum();
        
        int daysWorked = (int) weekLogs.stream()
            .filter(log -> log.getCheckInTime() != null && log.getCheckOutTime() != null)
            .count();
        
        // Count late arrivals (after 8:30 AM)
        int lateCount = (int) weekLogs.stream()
            .filter(log -> log.getCheckInTime() != null && log.getCheckInTime().isAfter(LocalTime.of(8, 30)))
            .count();
        
        return Map.of(
            "totalHours", totalHours,
            "daysWorked", daysWorked,
            "lateCount", lateCount,
            "remainingLeaves", 12 // Mock data - should come from HR system
        );
    }
    
    public Object getAttendanceHistory(User user, String startDate, String endDate) {
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        
        List<StaffAttendanceLog> logs = attendanceLogRepository.findByUserAndAttendanceDateBetween(user, start, end);
        
        return logs.stream()
            .map(log -> Map.of(
                "date", log.getAttendanceDate().toString(),
                "checkInTime", log.getCheckInTime() != null ? log.getCheckInTime().toString() : null,
                "checkOutTime", log.getCheckOutTime() != null ? log.getCheckOutTime().toString() : null,
                "workingHours", log.getWorkingHours(),
                "status", log.getAttendanceStatus() != null ? log.getAttendanceStatus() : "Unknown"
            ))
            .toList();
    }
    
    public Object verifyLocationOnly(Double latitude, Double longitude) {
        LocationVerificationResult result = verifyLocation(latitude, longitude, null);
        
        return Map.of(
            "success", result.isValid(),
            "verified", result.isValid(),
            "locationName", result.getLocationName() != null ? result.getLocationName() : "Unknown",
            "distance", result.getDistance() != null ? result.getDistance() : -1,
            "message", result.getMessage()
        );
    }
    
    public Object getVerificationLogs(User user, String date) {
        LocalDate logDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        LocalDateTime startOfDay = logDate.atStartOfDay();
        LocalDateTime endOfDay = logDate.atTime(LocalTime.MAX);
        
        List<AttendanceVerificationLog> logs = verificationLogRepository.findByUserAndCreatedAtBetween(user, startOfDay, endOfDay);
        
        return logs.stream()
            .map(log -> {
                Map<String, Object> result = new HashMap<>();
                result.put("id", log.getId());
                result.put("verificationType", log.getVerificationType().toString());
                result.put("verificationStatus", log.getVerificationStatus().toString());
                result.put("createdAt", log.getCreatedAt().toString());
                result.put("latitude", log.getGpsLatitude());
                result.put("longitude", log.getGpsLongitude());
                result.put("accuracy", log.getGpsAccuracy() != null ? log.getGpsAccuracy() : 0);
                result.put("publicIp", log.getPublicIp() != null ? log.getPublicIp() : "Unknown");
                result.put("locationVerified", log.isLocationVerified());
                result.put("networkVerified", log.isNetworkVerified());
                result.put("failureReason", log.getFailureReason());
                return result;
            })
            .toList();
    }
    
    private LocationVerificationResult verifyLocation(Double latitude, Double longitude, Double accuracy) {
        List<CompanyLocation> locations = locationRepository.findByActiveTrue();
        
        for (CompanyLocation location : locations) {
            double distance = calculateDistance(
                latitude, longitude,
                location.getLatitude(), location.getLongitude()
            );
            
            // Add accuracy buffer
            double allowedRadius = location.getAllowedRadius() + (accuracy != null ? accuracy : 0);
            
            if (distance <= allowedRadius) {
                return LocationVerificationResult.builder()
                    .valid(true)
                    .distance((int) Math.round(distance))
                    .locationName(location.getName())
                    .message("Trong phạm vi cho phép")
                    .build();
            }
        }
        
        return LocationVerificationResult.builder()
            .valid(false)
            .message("Ngoài phạm vi văn phòng")
            .build();
    }
    
    private NetworkVerificationResult verifyNetwork(String publicIp) {
        if (publicIp == null) {
            return NetworkVerificationResult.builder()
                .valid(false)
                .message("Không thể xác định IP")
                .build();
        }
        
        List<AllowedNetwork> networks = networkRepository.findByActiveTrue();
        
        for (AllowedNetwork network : networks) {
            if (isIpInRange(publicIp, network.getIpRange())) {
                return NetworkVerificationResult.builder()
                    .valid(true)
                    .networkName(network.getName())
                    .message("Mạng được phép")
                    .build();
            }
        }
        
        return NetworkVerificationResult.builder()
            .valid(false)
            .message("IP không thuộc mạng công ty")
            .build();
    }
    
    private StaffAttendanceLog createOrUpdateAttendanceLog(User user, boolean isCheckIn) {
        LocalDate today = LocalDate.now();
        
        // Find today's log or create new
        StaffAttendanceLog log = attendanceLogRepository
            .findByUserAndAttendanceDate(user, today)
            .orElse(new StaffAttendanceLog());
        
        if (log.getId() == null) {
            log.setUser(user);
            log.setAttendanceDate(today);
            log.setAttendanceType(StaffAttendanceLog.AttendanceType.NORMAL);
        }
        
        if (isCheckIn) {
            log.setCheckInTime(LocalTime.now());
        } else {
            log.setCheckOutTime(LocalTime.now());
        }
        
        return attendanceLogRepository.save(log);
    }
    
    private void logVerificationAttempt(User user, AttendanceVerificationDto dto,
                                       LocationVerificationResult locationResult,
                                       NetworkVerificationResult networkResult,
                                       AttendanceVerificationLog.VerificationType type,
                                       AttendanceVerificationLog.VerificationStatus status) {
        
        AttendanceVerificationLog log = new AttendanceVerificationLog();
        log.setUser(user);
        log.setVerificationType(type);
        log.setGpsLatitude(dto.getLatitude());
        log.setGpsLongitude(dto.getLongitude());
        log.setGpsAccuracy(dto.getAccuracy() != null ? dto.getAccuracy().intValue() : null);
        log.setLocationVerified(locationResult.isValid());
        log.setLocationDistance(locationResult.getDistance());
        log.setPublicIp(dto.getPublicIp());
        log.setNetworkVerified(networkResult.isValid());
        log.setNetworkName(networkResult.getNetworkName());
        log.setUserAgent(dto.getUserAgent());
        log.setDeviceFingerprint(dto.getDeviceFingerprint());
        log.setVerificationStatus(status);
        
        if (status == AttendanceVerificationLog.VerificationStatus.FAILED) {
            String reason = "";
            if (!locationResult.isValid()) reason += locationResult.getMessage() + ". ";
            if (!networkResult.isValid()) reason += networkResult.getMessage() + ". ";
            log.setFailureReason(reason);
        }
        
        verificationLogRepository.save(log);
    }
    
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Earth radius in meters
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    private boolean isIpInRange(String ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            InetAddress targetAddr = InetAddress.getByName(ip);
            InetAddress rangeAddr = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);
            
            byte[] targetBytes = targetAddr.getAddress();
            byte[] rangeBytes = rangeAddr.getAddress();
            
            int bytesToCheck = prefixLength / 8;
            int bitsToCheck = prefixLength % 8;
            
            for (int i = 0; i < bytesToCheck; i++) {
                if (targetBytes[i] != rangeBytes[i]) {
                    return false;
                }
            }
            
            if (bitsToCheck > 0 && bytesToCheck < targetBytes.length) {
                int mask = 0xFF << (8 - bitsToCheck);
                return (targetBytes[bytesToCheck] & mask) == (rangeBytes[bytesToCheck] & mask);
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error checking IP range", e);
            return false;
        }
    }
    
    // Inner classes for results
    @lombok.Data
    @lombok.Builder
    private static class LocationVerificationResult {
        private boolean valid;
        private String locationName;
        private Integer distance;
        private String message;
    }
    
    @lombok.Data
    @lombok.Builder
    private static class NetworkVerificationResult {
        private boolean valid;
        private String networkName;
        private String message;
    }
}