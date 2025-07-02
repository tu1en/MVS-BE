package com.classroomapp.classroombackend.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.AttendanceDto;
import com.classroomapp.classroombackend.dto.AttendanceSessionDto;
import com.classroomapp.classroombackend.dto.LocationDataDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance.AttendanceStatus;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession.SessionStatus;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AttendanceService;
import com.classroomapp.classroombackend.util.ModelMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final ClassroomEnrollmentRepository enrollmentRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public AttendanceSessionDto createAttendanceSession(Long classroomId, Long teacherId, String title,
                                                      String description, LocalDateTime startTime,
                                                      LocalDateTime endTime, Double latitude,
                                                      Double longitude, Double locationRadius) {
        log.info("Creating attendance session for classroom {} by teacher {}", classroomId, teacherId);
        
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
        
        // Validate teacher role and classroom access
        if (!teacher.getRoleId().equals(2) && !teacher.getRoleId().equals(4)) {
            throw new IllegalArgumentException("Only teachers can create attendance sessions");
        }
        
        if (!classroom.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher can only create sessions for their own classrooms");
        }
        
        AttendanceSession session = AttendanceSession.builder()
                .classroom(classroom)
                .teacher(teacher)
                .sessionName(title)
                .sessionDate(startTime)
                .description(description)
                .startTime(startTime)
                .endTime(endTime)
                .status(SessionStatus.SCHEDULED)
                .locationLatitude(latitude)
                .locationLongitude(longitude)
                .locationRadiusMeters(locationRadius != null ? locationRadius.intValue() : 50)
                .build();
        
        session = attendanceSessionRepository.save(session);
        log.info("Created attendance session with ID: {}", session.getId());
        
        return modelMapper.MapToAttendanceSessionDto(session);
    }

    @Override
    public AttendanceSessionDto getAttendanceSessionById(Long sessionId) {
        AttendanceSession session = findAttendanceSessionById(sessionId);
        return modelMapper.MapToAttendanceSessionDto(session);
    }

    @Override
    public List<AttendanceSessionDto> getAttendanceSessionsByClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        
        List<AttendanceSession> sessions = attendanceSessionRepository.findByClassroom(classroom);
        return sessions.stream()
                .map(modelMapper::MapToAttendanceSessionDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceSessionDto> getAttendanceSessionsByTeacher(Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", "id", teacherId));
        
        List<AttendanceSession> sessions = attendanceSessionRepository.findByTeacher(teacher);
        return sessions.stream()
                .map(modelMapper::MapToAttendanceSessionDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AttendanceSessionDto startAttendanceSession(Long sessionId) {
        log.info("Starting attendance session with ID: {}", sessionId);
        
        AttendanceSession session = findAttendanceSessionById(sessionId);
        
        if (!session.getStatus().equals(SessionStatus.SCHEDULED)) {
            throw new IllegalStateException("Only scheduled sessions can be started");
        }
        
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartTime(LocalDateTime.now());
        
        // Auto-mark teacher as present
        markTeacherAttendance(session);
        
        session = attendanceSessionRepository.save(session);
        log.info("Started attendance session with ID: {}", sessionId);
        
        return modelMapper.MapToAttendanceSessionDto(session);
    }

    @Override
    @Transactional
    public AttendanceSessionDto endAttendanceSession(Long sessionId) {
        log.info("Ending attendance session with ID: {}", sessionId);
        
        AttendanceSession session = findAttendanceSessionById(sessionId);
        
        if (!session.getStatus().equals(SessionStatus.ACTIVE)) {
            throw new IllegalStateException("Only active sessions can be ended");
        }
        
        session.setStatus(SessionStatus.COMPLETED);
        session.setEndTime(LocalDateTime.now());
        
        // Mark absent students who haven't marked attendance
        markAbsentStudents(session);
        
        session = attendanceSessionRepository.save(session);
        log.info("Ended attendance session with ID: {}", sessionId);
        
        return modelMapper.MapToAttendanceSessionDto(session);
    }

    @Override
    @Transactional
    public void deleteAttendanceSession(Long sessionId) {
        log.info("Deleting attendance session with ID: {}", sessionId);
        
        AttendanceSession session = findAttendanceSessionById(sessionId);
        
        if (session.getStatus().equals(SessionStatus.ACTIVE)) {
            throw new IllegalStateException("Cannot delete an active attendance session");
        }
        
        attendanceSessionRepository.delete(session);
        log.info("Deleted attendance session with ID: {}", sessionId);
    }

    @Override
    @Transactional
    public AttendanceDto markAttendance(Long sessionId, Long userId, Double latitude, Double longitude) {
        log.info("Marking attendance for user {} in session {}", userId, sessionId);
        
        AttendanceSession session = findAttendanceSessionById(sessionId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Validate session is active
        if (!session.getStatus().equals(SessionStatus.ACTIVE)) {
            throw new IllegalStateException("Can only mark attendance for active sessions");
        }
        
        // Check if user can mark attendance
        if (!canMarkAttendance(sessionId, userId)) {
            throw new IllegalArgumentException("User cannot mark attendance for this session");
        }
        
        // Check if already marked
        if (attendanceRepository.existsByStudentAndSession(user, session)) {
            throw new IllegalArgumentException("Attendance already marked for this session");
        }
        
        // Validate location if required
        AttendanceStatus status = AttendanceStatus.PRESENT;
        if (session.getLocationLatitude() != null && session.getLocationLongitude() != null) {
            if (!isWithinLocationRadius(latitude, longitude, session.getLocationLatitude(), 
                                      session.getLocationLongitude(), session.getLocationRadiusMeters().doubleValue())) {
                throw new IllegalArgumentException("Not within required location radius");
            }
        }
        
        // Check if late
        if (LocalDateTime.now().isAfter(session.getStartTime().plusMinutes(15))) {
            status = AttendanceStatus.LATE;
        }
        
        Attendance attendance = Attendance.builder()
                .student(user)
                .session(session)
                .status(status)
                .checkInTime(LocalDateTime.now())
                .latitude(latitude)
                .longitude(longitude)
                .build();
        
        attendance = attendanceRepository.save(attendance);
        log.info("Marked attendance with ID: {} for user {} in session {}", attendance.getId(), userId, sessionId);
        
        return modelMapper.MapToAttendanceDto(attendance);
    }

    @Override
    @Transactional
    public AttendanceDto updateAttendanceStatus(Long attendanceId, AttendanceStatus status) {
        log.info("Updating attendance {} to status {}", attendanceId, status);
        
        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", attendanceId));
        
        attendance.setStatus(status);
        attendance = attendanceRepository.save(attendance);
        
        return modelMapper.MapToAttendanceDto(attendance);
    }

    @Override
    public List<AttendanceDto> getAttendanceBySession(Long sessionId) {
        AttendanceSession session = findAttendanceSessionById(sessionId);
        List<Attendance> attendanceList = attendanceRepository.findBySession(session);
        return attendanceList.stream()
                .map(modelMapper::MapToAttendanceDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDto> getAttendanceByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        List<Attendance> attendanceList = attendanceRepository.findByStudent(user);
        return attendanceList.stream()
                .map(modelMapper::MapToAttendanceDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDto> getAttendanceByUserAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        List<Attendance> attendanceList = attendanceRepository.findByStudentAndCheckInTimeBetween(user, startDate, endDate);
        return attendanceList.stream()
                .map(modelMapper::MapToAttendanceDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDto> getAttendanceByClassroomAndDateRange(Long classroomId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Attendance> attendanceList = attendanceRepository.findByClassroomAndDateRange(classroomId, startDate, endDate);
        return attendanceList.stream()
                .map(modelMapper::MapToAttendanceDto)
                .collect(Collectors.toList());
    }    @Override
    public Map<AttendanceStatus, Long> getAttendanceSummaryBySession(Long sessionId) {
        AttendanceSession session = findAttendanceSessionById(sessionId);
        List<Object[]> summary = attendanceRepository.getAttendanceSummaryBySession(session);
        
        Map<AttendanceStatus, Long> result = new HashMap<>();
        for (Object[] row : summary) {
            AttendanceStatus status = (AttendanceStatus) row[0];
            Long count = (Long) row[1];
            result.put(status, count);
        }
        
        return result;
    }

    @Override
    public Map<Long, Double> getStudentAttendancePercentageByClassroom(Long classroomId) {
        List<Object[]> percentages = attendanceRepository.getStudentAttendancePercentageByClassroom(classroomId);
        
        Map<Long, Double> result = new HashMap<>();
        for (Object[] row : percentages) {
            User user = (User) row[0];
            Double percentage = (Double) row[1];
            result.put(user.getId(), percentage);
        }
        
        return result;
    }

    @Override
    public List<AttendanceDto> getAbsentStudents(Long sessionId) {
        AttendanceSession session = findAttendanceSessionById(sessionId);
        List<Attendance> absentList = attendanceRepository.findBySession(session)
                .stream()
                .filter(a -> a.getStatus().equals(AttendanceStatus.ABSENT))
                .collect(Collectors.toList());
        
        return absentList.stream()
                .map(modelMapper::MapToAttendanceDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<AttendanceDto> getLateStudents(Long sessionId) {
        AttendanceSession session = findAttendanceSessionById(sessionId);
        List<Attendance> lateList = attendanceRepository.findBySession(session)
                .stream()
                .filter(a -> a.getStatus().equals(AttendanceStatus.LATE))
                .collect(Collectors.toList());
        
        return lateList.stream()
                .map(modelMapper::MapToAttendanceDto)
                .collect(Collectors.toList());
    }

    @Override
    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void processScheduledSessions() {
        log.debug("Processing scheduled sessions");
        
        LocalDateTime now = LocalDateTime.now();
        List<AttendanceSession> sessionsToActivate = attendanceSessionRepository.findByStatusAndStartTimeLessThanEqual(
            AttendanceSession.SessionStatus.SCHEDULED, now);
        
        for (AttendanceSession session : sessionsToActivate) {
            try {
                session.setStatus(SessionStatus.ACTIVE);
                markTeacherAttendance(session);
                attendanceSessionRepository.save(session);
                log.info("Auto-activated session: {}", session.getId());
            } catch (Exception e) {
                log.error("Failed to activate session {}: {}", session.getId(), e.getMessage());
            }
        }
    }

    @Override
    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    public void processActiveSessions() {
        log.debug("Processing active sessions");
        
        LocalDateTime now = LocalDateTime.now();
        List<AttendanceSession> sessionsToComplete = attendanceSessionRepository.findByStatusAndEndTimeLessThanEqual(
            AttendanceSession.SessionStatus.ACTIVE, now);
        
        for (AttendanceSession session : sessionsToComplete) {
            try {
                session.setStatus(SessionStatus.COMPLETED);
                markAbsentStudents(session);
                attendanceSessionRepository.save(session);
                log.info("Auto-completed session: {}", session.getId());
            } catch (Exception e) {
                log.error("Failed to complete session {}: {}", session.getId(), e.getMessage());
            }
        }
    }

    @Override
    public boolean isUserInClassroom(Long userId, Long classroomId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", classroomId));
        
        // Check if user is the teacher
        if (classroom.getTeacher().getId().equals(userId)) {
            return true;
        }
        
        // Check if user is enrolled as student
        return enrollmentRepository.findByUserAndClassroom(user, classroom).isPresent();
    }

    @Override
    public boolean isWithinLocationRadius(Double userLat, Double userLon, Double sessionLat, Double sessionLon, Double radius) {
        if (userLat == null || userLon == null || sessionLat == null || sessionLon == null || radius == null) {
            return true; // No location validation required
        }
        
        double distance = calculateDistance(userLat, userLon, sessionLat, sessionLon);
        return distance <= radius;
    }

    @Override
    public boolean canMarkAttendance(Long sessionId, Long userId) {
        AttendanceSession session = findAttendanceSessionById(sessionId);
        
        // Check if user is in the classroom
        if (!isUserInClassroom(userId, session.getClassroom().getId())) {
            return false;
        }
        
        // Check if session is active
        if (!session.getStatus().equals(SessionStatus.ACTIVE)) {
            return false;
        }
        
        // Check if already marked
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        return !attendanceRepository.existsByStudentAndSession(user, session);
    }

    // Helper Methods
    private AttendanceSession findAttendanceSessionById(Long sessionId) {
        return attendanceSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", sessionId));
    }

    private void markTeacherAttendance(AttendanceSession session) {
        // Check if teacher attendance is already marked
        if (!attendanceRepository.existsByStudentAndSession(session.getTeacher(), session)) {
            Attendance teacherAttendance = Attendance.builder()
                    .student(session.getTeacher())
                    .session(session)
                    .status(AttendanceStatus.PRESENT)
                    .checkInTime(LocalDateTime.now())
                    .latitude(session.getLocationLatitude())
                    .longitude(session.getLocationLongitude())
                    .build();
            
            attendanceRepository.save(teacherAttendance);
            log.info("Auto-marked teacher attendance for session: {}", session.getId());
        }
    }

    private void markAbsentStudents(AttendanceSession session) {
        // Get all enrolled students in the classroom
        List<ClassroomEnrollment> enrollments = enrollmentRepository.findByClassroom(session.getClassroom());
        
        for (ClassroomEnrollment enrollment : enrollments) {
            User student = enrollment.getUser();
            
            // Skip if attendance already marked
            if (!attendanceRepository.existsByStudentAndSession(student, session)) {
                Attendance absentAttendance = Attendance.builder()
                        .student(student)
                        .session(session)
                        .status(AttendanceStatus.ABSENT)
                        .checkInTime(LocalDateTime.now())
                        .build();
                
                attendanceRepository.save(absentAttendance);
                log.debug("Auto-marked student {} as absent for session: {}", student.getId(), session.getId());
            }
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        
        return distance;
    }
    
    // Implementation of missing methods required by AttendanceController
    
    @Override
    public ApiResponse PerformCheckInLogic(String username, LocationDataDto locationData, String clientIpAddress) {
        try {
            // Find user by username
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            // Basic GPS accuracy validation
            if (locationData.getAccuracy() > 100.0) {
                return new ApiResponse(false, "Độ chính xác vị trí GPS quá thấp. Vui lòng thử lại.");
            }
            
            // Location validation (example coordinates for classroom)
            double allowedLat = 21.028511;
            double allowedLon = 105.804817;
            double allowedRadius = 100.0; // 100 meters
            
            if (!isWithinLocationRadius(locationData.getLatitude(), locationData.getLongitude(),
                    allowedLat, allowedLon, allowedRadius)) {
                return new ApiResponse(false, "Bạn đang ở vị trí nằm ngoài phạm vi cho phép để điểm danh.");
            }
            
            // IP whitelist validation (example)
            List<String> allowedIPs = List.of("127.0.0.1", "192.168.1.0/24", "10.0.0.0/8");
            if (!isIpAllowed(clientIpAddress, allowedIPs)) {
                return new ApiResponse(false, "IP address không được phép truy cập hệ thống điểm danh.");
            }
            
            return new ApiResponse(true, "Điểm danh thành công. Vị trí và thông tin hợp lệ.");
            
        } catch (Exception e) {
            log.error("Error in PerformCheckInLogic: {}", e.getMessage(), e);
            return new ApiResponse(false, "Đã xảy ra lỗi trong quá trình xử lý điểm danh.");
        }
    }
    
    @Override
    public List<AttendanceDto> GetStudentsForAttendance(Long sessionId, Long teacherId) {
        log.info("Getting students for attendance session {} by teacher {}", sessionId, teacherId);
        
        // Validate session exists and teacher has access
        AttendanceSession session = findAttendanceSessionById(sessionId);
        if (!session.getTeacher().getId().equals(teacherId)) {
            throw new IllegalArgumentException("Teacher does not have access to this session");
        }
        
        // Get all students in the classroom
        List<ClassroomEnrollment> enrollments = enrollmentRepository.findByClassroom(session.getClassroom());
        
        return enrollments.stream()
                .map(enrollment -> {
                    User student = enrollment.getUser();
                    // Check if attendance already exists
                    boolean alreadyMarked = attendanceRepository.existsByStudentAndSession(student, session);
                    
                    AttendanceDto dto = new AttendanceDto();
                    dto.setUserId(student.getId());
                    dto.setUserName(student.getFullName() != null ? student.getFullName() : student.getUsername());
                    dto.setSessionId(sessionId);
                      if (alreadyMarked) {
                        Optional<Attendance> existingOpt = attendanceRepository.findByStudentAndSession(student, session);
                        if (existingOpt.isPresent()) {
                            Attendance existing = existingOpt.get();
                            dto.setStatus(existing.getStatus());
                            dto.setMarkedAt(existing.getCheckInTime());
                        }
                    } else {
                        dto.setStatus(AttendanceStatus.ABSENT); // Default status
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public ApiResponse MarkStudentAttendance(AttendanceDto attendanceDto, Long teacherId) {
        log.info("Marking attendance for student {} by teacher {}", attendanceDto.getUserId(), teacherId);
        
        try {
            // Validate session and teacher access
            AttendanceSession session = findAttendanceSessionById(attendanceDto.getSessionId());
            if (!session.getTeacher().getId().equals(teacherId)) {
                return new ApiResponse(false, "Teacher does not have access to this session");
            }
            
            User student = userRepository.findById(attendanceDto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", attendanceDto.getUserId()));
              // Check if attendance already exists
            Optional<Attendance> attendanceOpt = attendanceRepository.findByStudentAndSession(student, session);
            Attendance attendance;
            
            if (attendanceOpt.isPresent()) {
                // Update existing attendance
                attendance = attendanceOpt.get();
                attendance.setStatus(attendanceDto.getStatus());
                attendance.setCheckInTime(LocalDateTime.now());
            } else {
                // Create new attendance record
                attendance = Attendance.builder()
                        .student(student)
                        .session(session)
                        .status(attendanceDto.getStatus())
                        .checkInTime(LocalDateTime.now())
                        .build();
            }
            
            attendance = attendanceRepository.save(attendance);
            return new ApiResponse(true, "Attendance marked successfully");
        } catch (Exception e) {
            log.error("Error marking attendance: {}", e.getMessage(), e);
            return new ApiResponse(false, e.getMessage());
        }
    }
    
    // Helper method for IP validation
    private boolean isIpAllowed(String clientIp, List<String> allowedIPs) {
        // Simple IP validation - localhost is always allowed
        if ("127.0.0.1".equals(clientIp) || "localhost".equals(clientIp)) {
            return true;
        }
        
        // For more sophisticated IP range checking, you would implement CIDR notation parsing
        return allowedIPs.contains(clientIp);
    }
}
