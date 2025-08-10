package com.classroomapp.classroombackend.service;

import java.net.InetAddress;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${app.attendance.dev-mode:true}")
    private boolean devMode;
    
    @Value("${app.attendance.skip-location-check:true}")
    private boolean skipLocationCheck;
    
    @Value("${app.attendance.skip-network-check:true}")
    private boolean skipNetworkCheck;
    
    @Transactional
    public AttendanceCheckInResponseDto processCheckIn(User user, AttendanceVerificationDto dto) {
        log.info("Processing check-in for user: {} (DevMode: {}, SkipLocation: {}, SkipNetwork: {})", 
                 user.getUsername(), devMode, skipLocationCheck, skipNetworkCheck);
        
        // Development mode - always allow with mock verification
        if (devMode) {
            return processCheckInDevMode(user, dto, true);
        }
        
        // Production mode - full verification
        return processCheckInProduction(user, dto, true);
    }
    
    @Transactional
    public AttendanceCheckInResponseDto processCheckOut(User user, AttendanceVerificationDto dto) {
        log.info("Processing check-out for user: {} (DevMode: {}, SkipLocation: {}, SkipNetwork: {})", 
                 user.getUsername(), devMode, skipLocationCheck, skipNetworkCheck);
        
        if (devMode) {
            return processCheckInDevMode(user, dto, false);
        }
        
        return processCheckInProduction(user, dto, false);
    }
    
    private AttendanceCheckInResponseDto processCheckInDevMode(User user, AttendanceVerificationDto dto, boolean isCheckIn) {
        log.info("Development mode: Processing {} for user: {}", isCheckIn ? "check-in" : "check-out", user.getUsername());
        
        // Always create attendance log in dev mode
        StaffAttendanceLog attendanceLog = createOrUpdateAttendanceLog(user, isCheckIn);
        
        // Create mock verification results
        LocationVerificationResult locationResult = LocationVerificationResult.builder()
            .valid(true)
            .distance(10)
            .locationName("Văn phòng chính (Development)")
            .message("Development Mode - Bỏ qua xác thực vị trí")
            .build();
            
        NetworkVerificationResult networkResult = NetworkVerificationResult.builder()
            .valid(true)
            .networkName("Development Network")
            .message("Development Mode - Bỏ qua xác thực mạng")
            .build();
        
        // Log successful verification
        logVerificationAttempt(user, dto, locationResult, networkResult,
            isCheckIn ? AttendanceVerificationLog.VerificationType.CHECK_IN : AttendanceVerificationLog.VerificationType.CHECK_OUT,
            AttendanceVerificationLog.VerificationStatus.SUCCESS);
        
        return AttendanceCheckInResponseDto.builder()
            .success(true)
            .message(isCheckIn ? "Check-in thành công (Development Mode)" : "Check-out thành công (Development Mode)")
            .attendanceLogId(attendanceLog.getId())
            .checkInTime(isCheckIn ? attendanceLog.getCheckInTime() : null)
            .checkOutTime(!isCheckIn ? attendanceLog.getCheckOutTime() : null)
            .locationVerification(AttendanceCheckInResponseDto.LocationVerification.builder()
                .verified(true)
                .locationName("Văn phòng chính (Dev)")
                .distance(10)
                .status("Development Mode")
                .build())
            .networkVerification(AttendanceCheckInResponseDto.NetworkVerification.builder()
                .verified(true)
                .networkName("Dev Network")
                .ipAddress(dto.getPublicIp())
                .status("Development Mode")
                .build())
            .build();
    }
    
    private AttendanceCheckInResponseDto processCheckInProduction(User user, AttendanceVerificationDto dto, boolean isCheckIn) {
        log.info("Production mode: Processing {} for user: {}", isCheckIn ? "check-in" : "check-out", user.getUsername());
        
        // 1. Verify location (skip if configured)
        LocationVerificationResult locationResult;
        if (skipLocationCheck) {
            locationResult = LocationVerificationResult.builder()
                .valid(true)
                .distance(0)
                .locationName("Bỏ qua kiểm tra vị trí")
                .message("Location check disabled")
                .build();
        } else {
            locationResult = verifyLocation(dto.getLatitude(), dto.getLongitude(), dto.getAccuracy());
        }
        
        // 2. Verify network/IP (skip if configured)
        NetworkVerificationResult networkResult;
        if (skipNetworkCheck) {
            networkResult = NetworkVerificationResult.builder()
                .valid(true)
                .networkName("Bỏ qua kiểm tra mạng")
                .message("Network check disabled")
                .build();
        } else {
            networkResult = verifyNetwork(dto.getPublicIp());
        }
        
        // 3. Check if both verifications pass
        boolean canProceed = locationResult.isValid() && networkResult.isValid();
        
        if (!canProceed) {
            // Log failed attempt
            logVerificationAttempt(user, dto, locationResult, networkResult, 
                isCheckIn ? AttendanceVerificationLog.VerificationType.CHECK_IN : AttendanceVerificationLog.VerificationType.CHECK_OUT,
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
        StaffAttendanceLog attendanceLog = createOrUpdateAttendanceLog(user, isCheckIn);
        
        // 5. Log successful verification
        logVerificationAttempt(user, dto, locationResult, networkResult,
            isCheckIn ? AttendanceVerificationLog.VerificationType.CHECK_IN : AttendanceVerificationLog.VerificationType.CHECK_OUT,
            AttendanceVerificationLog.VerificationStatus.SUCCESS);
        
        return AttendanceCheckInResponseDto.builder()
            .success(true)
            .message(isCheckIn ? "Check-in thành công" : "Check-out thành công")
            .attendanceLogId(attendanceLog.getId())
            .checkInTime(isCheckIn ? attendanceLog.getCheckInTime() : null)
            .checkOutTime(!isCheckIn ? attendanceLog.getCheckOutTime() : null)
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
            .workingHours(calculateWorkingHours(log))
            .status(determineAttendanceStatus(log))
            .build();
    }
    
    private double calculateWorkingHours(StaffAttendanceLog log) {
        if (log.getCheckInTime() == null || log.getCheckOutTime() == null) {
            return 0.0;
        }
        
        LocalTime checkIn = log.getCheckInTime();
        LocalTime checkOut = log.getCheckOutTime();
        
        // Calculate hours between check-in and check-out
        long minutes = java.time.Duration.between(checkIn, checkOut).toMinutes();
        return minutes / 60.0;
    }
    
    private String determineAttendanceStatus(StaffAttendanceLog log) {
        if (log.getCheckInTime() == null) {
            return "Chưa check-in";
        }
        
        if (log.getCheckOutTime() == null) {
            return "Đang làm việc";
        }
        
        return "Hoàn thành";
    }
    
    public List<CompanyLocation> getActiveLocations() {
        List<CompanyLocation> locations = locationRepository.findByActiveTrue();
        
        // If no locations in production mode, create default
        if (locations.isEmpty() && !devMode) {
            log.warn("No active company locations found! Creating default location...");
            CompanyLocation defaultLocation = new CompanyLocation();
            defaultLocation.setName("Văn phòng chính (Tự động tạo)");
            defaultLocation.setAddress("Chưa cập nhật địa chỉ");
            defaultLocation.setLatitude(10.762622); // Default to HCM City
            defaultLocation.setLongitude(106.660172);
            defaultLocation.setAllowedRadius(1000); // 1km radius
            defaultLocation.setActive(true);
            defaultLocation = locationRepository.save(defaultLocation);
            locations = List.of(defaultLocation);
        }
        
        return locations;
    }
    
    public Object getWeeklyStats(User user) {
        LocalDate startOfWeek = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        
        List<StaffAttendanceLog> weekLogs = attendanceLogRepository.findByUserAndAttendanceDateBetween(user, startOfWeek, endOfWeek);
        
        double totalHours = weekLogs.stream()
            .mapToDouble(this::calculateWorkingHours)
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
                "workingHours", calculateWorkingHours(log),
                "status", determineAttendanceStatus(log)
            ))
            .toList();
    }
    
    public Object verifyLocationOnly(Double latitude, Double longitude) {
        if (devMode || skipLocationCheck) {
            return Map.of(
                "success", true,
                "verified", true,
                "locationName", "Development Mode",
                "distance", 10,
                "message", "Development mode - location check skipped"
            );
        }
        
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
        List<CompanyLocation> locations = getActiveLocations(); // Use method that creates default if needed
        
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
            .distance(-1)
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
        
        // If no networks configured, allow all in development
        if (networks.isEmpty()) {
            if (devMode) {
                log.warn("No allowed networks configured, allowing all IPs in development mode");
                return NetworkVerificationResult.builder()
                    .valid(true)
                    .networkName("Development - All Networks")
                    .message("Development mode - no network restrictions")
                    .build();
            } else {
                log.error("No allowed networks configured in production mode!");
                return NetworkVerificationResult.builder()
                    .valid(false)
                    .message("Không có mạng nào được cấu hình")
                    .build();
            }
        }
        
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
            .message("IP không thuộc mạng công ty: " + publicIp)
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
            log.setNotes("Check-in via attendance verification system");
        } else {
            log.setCheckOutTime(LocalTime.now());
            log.setNotes("Check-out via attendance verification system");
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
            // Handle simple wildcard case for development
            if ("0.0.0.0/0".equals(cidr)) {
                return true;
            }
            
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
            log.error("Error checking IP range for IP: {} against CIDR: {}", ip, cidr, e);
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