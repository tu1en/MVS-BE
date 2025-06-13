package com.classroomapp.classroombackend.controller;

<<<<<<< HEAD
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
=======
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
>>>>>>> master
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

<<<<<<< HEAD
import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceDto;
import com.classroomapp.classroombackend.dto.LocationDataDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AttendanceService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST Controller xử lý các API liên quan đến điểm danh
 */
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);
    
    private final AttendanceService attendanceService;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final UserRepository userRepository;
    
    /**
     * Constructor với tham số
     * 
     * @param attendanceService Service xử lý logic điểm danh
     * @param attendanceRepository Repository cho bản ghi điểm danh
     * @param sessionRepository Repository cho phiên điểm danh
     * @param userRepository Repository cho người dùng
     */
    @Autowired
    public AttendanceController(
            AttendanceService attendanceService,
            AttendanceRepository attendanceRepository,
            AttendanceSessionRepository sessionRepository,
            UserRepository userRepository) {
        this.attendanceService = attendanceService;
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Xử lý yêu cầu điểm danh
     * 
     * @param locationData Dữ liệu vị trí từ client
     * @param request HttpServletRequest để lấy IP client
     * @return ResponseEntity chứa kết quả điểm danh
     */
    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse> HandleCheckInRequest(
            @RequestBody LocationDataDto locationData,
            HttpServletRequest request) {
        
        // Mô phỏng thông tin người dùng (trong ứng dụng thực tế sẽ lấy từ context bảo mật)
        String demoUsername = "teacher_demo_user";
        logger.info("Nhận được yêu cầu điểm danh cho người dùng (mô phỏng): {}", demoUsername);
        
        // Lấy địa chỉ IP client
        String clientIpAddress = ExtractClientIpAddress(request);
        logger.info("Địa chỉ IP của client: {}", clientIpAddress);
        
        // Xác thực dữ liệu đầu vào
        if (locationData == null ||
            locationData.getLatitude() < -90 || locationData.getLatitude() > 90 ||
            locationData.getLongitude() < -180 || locationData.getLongitude() > 180 ||
            locationData.getAccuracy() <= 0) {
            logger.warn("Dữ liệu vị trí không hợp lệ: {}", locationData);
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Dữ liệu vị trí không hợp lệ."));
        }
        
        try {
            // Gọi service xử lý logic điểm danh
            ApiResponse serviceResponse = attendanceService.PerformCheckInLogic(demoUsername, locationData, clientIpAddress);
            
            // Trả về phản hồi phù hợp
            if (serviceResponse.isSuccess()) {
                return ResponseEntity.ok(serviceResponse);
            } else {
                return ResponseEntity.badRequest().body(serviceResponse);
            }
        } catch (Exception e) {
            // Xử lý ngoại lệ
            logger.error("Lỗi máy chủ khi xử lý điểm danh: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Đã xảy ra lỗi máy chủ nội bộ. Vui lòng thử lại sau."));
        }
    }
    
    /**
     * Lấy danh sách sinh viên để điểm danh
     * 
     * @param sessionId ID của phiên điểm danh
     * @param teacherId ID của giáo viên thực hiện điểm danh
     * @return Danh sách thông tin sinh viên cần điểm danh
     */
    @GetMapping("/students/{sessionId}")
    public ResponseEntity<List<AttendanceDto>> GetStudentsForAttendance(
            @PathVariable Long sessionId,
            @RequestParam Long teacherId) {
        
        logger.info("Nhận được yêu cầu lấy danh sách sinh viên để điểm danh cho phiên {}, từ giáo viên {}", 
                sessionId, teacherId);
        
        try {
            List<AttendanceDto> students = attendanceService.GetStudentsForAttendance(sessionId, teacherId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách sinh viên để điểm danh: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Đánh dấu điểm danh cho sinh viên
     * 
     * @param attendanceDto Thông tin điểm danh
     * @param teacherId ID của giáo viên thực hiện điểm danh
     * @return Kết quả điểm danh
     */
    @PostMapping("/mark-student")
    public ResponseEntity<ApiResponse> MarkStudentAttendance(
            @RequestBody AttendanceDto attendanceDto,
            @RequestParam Long teacherId) {
        
        logger.info("Nhận được yêu cầu đánh dấu điểm danh cho sinh viên {} từ giáo viên {}", 
                attendanceDto.getUserId(), teacherId);
        
        try {
            ApiResponse response = attendanceService.MarkStudentAttendance(attendanceDto, teacherId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Lỗi khi đánh dấu điểm danh cho sinh viên: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Đã xảy ra lỗi máy chủ nội bộ. Vui lòng thử lại sau."));
        }
    }
    
    /**
     * Trích xuất địa chỉ IP client từ request
     * 
     * @param request HttpServletRequest
     * @return Địa chỉ IP client
     */
    private String ExtractClientIpAddress(HttpServletRequest request) {
        // Kiểm tra header X-Forwarded-For (cho proxy, load balancer)
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        
        if (xForwardedForHeader != null && !xForwardedForHeader.isEmpty()) {
            // Lấy địa chỉ IP đầu tiên (thường là IP client gốc)
            logger.debug("X-Forwarded-For header: {}", xForwardedForHeader);
            return xForwardedForHeader.split(",")[0].trim();
        }
        
        // Sử dụng getRemoteAddr() nếu không có X-Forwarded-For
        logger.debug("Sử dụng request.getRemoteAddr(): {}", request.getRemoteAddr());
        return request.getRemoteAddr();
    }

    /**
     * Endpoint test để giả lập điểm danh với các IP khác nhau
     * @param locationData Dữ liệu vị trí
     * @param simulatedIP IP giả lập để test
     * @return Kết quả điểm danh
     */
    @PostMapping("/test-with-ip")
    public ResponseEntity<ApiResponse> TestAttendanceWithSimulatedIp(
            @RequestBody LocationDataDto locationData,
            @RequestParam String simulatedIP) {
        
        logger.info("Yêu cầu điểm danh test với IP giả lập: {}", simulatedIP);
        
        // Giả định user là "test_user"
        String username = "test_user";
        
        // Gọi service để xử lý logic điểm danh với IP giả lập
        ApiResponse result = attendanceService.PerformCheckInLogic(username, locationData, simulatedIP);
        
        // Trả về kết quả
        return ResponseEntity.ok(result);
    }
    
    /**
     * Endpoint test cho IP được phép (test case IP hợp lệ)
     * @param locationData Dữ liệu vị trí
     * @return Kết quả điểm danh
     */
    @PostMapping("/test-allowed-ip")
    public ResponseEntity<ApiResponse> TestWithAllowedIP(@RequestBody LocationDataDto locationData) {
        // Sử dụng IP đã có trong whitelist
        String allowedIP = "123.16.226.86";
        logger.info("Yêu cầu điểm danh test với IP ĐƯỢC PHÉP: {}", allowedIP);
        
        // Giả định user là "test_user"
        String username = "test_user_allowed";
        
        // Gọi service để xử lý logic điểm danh
        ApiResponse result = attendanceService.PerformCheckInLogic(username, locationData, allowedIP);
        
        // Trả về kết quả
        return ResponseEntity.ok(result);
    }
    
    /**
     * Endpoint test cho IP không được phép (test case IP không hợp lệ)
     * @param locationData Dữ liệu vị trí
     * @return Kết quả điểm danh
     */
    @PostMapping("/test-denied-ip")
    public ResponseEntity<ApiResponse> TestWithDeniedIP(@RequestBody LocationDataDto locationData) {
        // Sử dụng IP không có trong whitelist
        String deniedIP = "8.8.8.8";
        logger.info("Yêu cầu điểm danh test với IP KHÔNG ĐƯỢC PHÉP: {}", deniedIP);
        
        try {
            // Giả định user là "test_user"
            String username = "test_user_denied";
            
            // Gọi service để xử lý logic điểm danh
            ApiResponse result = attendanceService.PerformCheckInLogic(username, locationData, deniedIP);
            
            // Trả về kết quả
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // Xử lý ngoại lệ
            logger.error("Lỗi khi thực hiện test với IP không được phép: {}", e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.OK)  // Vẫn trả về 200 OK thay vì 500 để frontend dễ xử lý
                    .body(new ApiResponse(false, "IP không được phép (8.8.8.8): " + e.getMessage()));
        }
    }

    /**
     * Endpoint để kiểm tra trạng thái điểm danh của giáo viên
     * 
     * @param sessionId ID của phiên học
     * @param teacherId ID của giáo viên
     * @return Trạng thái điểm danh của giáo viên
     */
    @GetMapping("/teacher-status")
    public ResponseEntity<?> CheckTeacherAttendanceStatus(
            @RequestParam Long sessionId,
            @RequestParam Long teacherId) {
        
        logger.info("Kiểm tra trạng thái điểm danh của giáo viên {} cho phiên {}", teacherId, sessionId);
        
        try {
            // Lấy phiên học từ database
            AttendanceSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", sessionId));
            
            // Lấy giáo viên từ database
            User teacher = userRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", teacherId));
            
            // Kiểm tra xem giáo viên đã được điểm danh cho phiên này chưa
            Optional<Attendance> teacherAttendance = attendanceRepository.findByUserAndClassroomAndSessionDateBetweenAndIsTeacherRecordTrue(
                    teacher, session.getClassroom(), session.getStartTime(), session.getEndTime());
            
            // Tạo response
            Map<String, Object> response = new HashMap<>();
            response.put("isPresent", teacherAttendance.isPresent());
            
            if (teacherAttendance.isPresent()) {
                Attendance attendance = teacherAttendance.get();
                response.put("attendanceId", attendance.getId());
                response.put("attendanceTime", attendance.getCreatedAt());
                response.put("attendanceType", attendance.getAttendanceType());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Lỗi khi kiểm tra trạng thái điểm danh của giáo viên: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
} 
=======
import com.classroomapp.classroombackend.dto.AttendanceDto;
import com.classroomapp.classroombackend.dto.AttendanceSessionDto;
import com.classroomapp.classroombackend.model.Attendance.AttendanceStatus;
import com.classroomapp.classroombackend.service.AttendanceService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {

    private final AttendanceService attendanceService;

    // Attendance Session Management
    @PostMapping("/sessions")
    public ResponseEntity<AttendanceSessionDto> createAttendanceSession(
            @RequestParam Long classroomId,
            @RequestParam Long teacherId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false, defaultValue = "100.0") Double locationRadius) {
        
        log.info("Creating attendance session for classroom {} by teacher {}", classroomId, teacherId);
        
        AttendanceSessionDto session = attendanceService.createAttendanceSession(
                classroomId, teacherId, title, description, startTime, endTime, 
                latitude, longitude, locationRadius);
        
        return new ResponseEntity<>(session, HttpStatus.CREATED);
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<AttendanceSessionDto> getAttendanceSession(@PathVariable Long sessionId) {
        AttendanceSessionDto session = attendanceService.getAttendanceSessionById(sessionId);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/sessions/classroom/{classroomId}")
    public ResponseEntity<List<AttendanceSessionDto>> getAttendanceSessionsByClassroom(@PathVariable Long classroomId) {
        List<AttendanceSessionDto> sessions = attendanceService.getAttendanceSessionsByClassroom(classroomId);
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/sessions/teacher/{teacherId}")
    public ResponseEntity<List<AttendanceSessionDto>> getAttendanceSessionsByTeacher(@PathVariable Long teacherId) {
        List<AttendanceSessionDto> sessions = attendanceService.getAttendanceSessionsByTeacher(teacherId);
        return ResponseEntity.ok(sessions);
    }

    @PutMapping("/sessions/{sessionId}/start")
    public ResponseEntity<AttendanceSessionDto> startAttendanceSession(@PathVariable Long sessionId) {
        log.info("Starting attendance session: {}", sessionId);
        AttendanceSessionDto session = attendanceService.startAttendanceSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @PutMapping("/sessions/{sessionId}/end")
    public ResponseEntity<AttendanceSessionDto> endAttendanceSession(@PathVariable Long sessionId) {
        log.info("Ending attendance session: {}", sessionId);
        AttendanceSessionDto session = attendanceService.endAttendanceSession(sessionId);
        return ResponseEntity.ok(session);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteAttendanceSession(@PathVariable Long sessionId) {
        log.info("Deleting attendance session: {}", sessionId);
        attendanceService.deleteAttendanceSession(sessionId);
        return ResponseEntity.noContent().build();
    }

    // Attendance Marking
    @PostMapping("/mark")
    public ResponseEntity<AttendanceDto> markAttendance(
            @RequestParam Long sessionId,
            @RequestParam Long userId,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {
        
        log.info("Marking attendance for user {} in session {}", userId, sessionId);
        
        AttendanceDto attendance = attendanceService.markAttendance(sessionId, userId, latitude, longitude);
        return new ResponseEntity<>(attendance, HttpStatus.CREATED);
    }

    @PutMapping("/{attendanceId}/status")
    public ResponseEntity<AttendanceDto> updateAttendanceStatus(
            @PathVariable Long attendanceId,
            @RequestParam AttendanceStatus status) {
        
        log.info("Updating attendance {} to status {}", attendanceId, status);
        
        AttendanceDto attendance = attendanceService.updateAttendanceStatus(attendanceId, status);
        return ResponseEntity.ok(attendance);
    }

    // Attendance Retrieval
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<AttendanceDto>> getAttendanceBySession(@PathVariable Long sessionId) {
        List<AttendanceDto> attendanceList = attendanceService.getAttendanceBySession(sessionId);
        return ResponseEntity.ok(attendanceList);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByUser(@PathVariable Long userId) {
        List<AttendanceDto> attendanceList = attendanceService.getAttendanceByUser(userId);
        return ResponseEntity.ok(attendanceList);
    }

    @GetMapping("/user/{userId}/daterange")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByUserAndDateRange(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<AttendanceDto> attendanceList = attendanceService.getAttendanceByUserAndDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(attendanceList);
    }

    @GetMapping("/classroom/{classroomId}/daterange")
    public ResponseEntity<List<AttendanceDto>> getAttendanceByClassroomAndDateRange(
            @PathVariable Long classroomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<AttendanceDto> attendanceList = attendanceService.getAttendanceByClassroomAndDateRange(classroomId, startDate, endDate);
        return ResponseEntity.ok(attendanceList);
    }

    // Attendance Reports and Analytics
    @GetMapping("/session/{sessionId}/summary")
    public ResponseEntity<Map<AttendanceStatus, Long>> getAttendanceSummaryBySession(@PathVariable Long sessionId) {
        Map<AttendanceStatus, Long> summary = attendanceService.getAttendanceSummaryBySession(sessionId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/classroom/{classroomId}/percentage")
    public ResponseEntity<Map<Long, Double>> getStudentAttendancePercentageByClassroom(@PathVariable Long classroomId) {
        Map<Long, Double> percentages = attendanceService.getStudentAttendancePercentageByClassroom(classroomId);
        return ResponseEntity.ok(percentages);
    }

    @GetMapping("/session/{sessionId}/absent")
    public ResponseEntity<List<AttendanceDto>> getAbsentStudents(@PathVariable Long sessionId) {
        List<AttendanceDto> absentStudents = attendanceService.getAbsentStudents(sessionId);
        return ResponseEntity.ok(absentStudents);
    }

    @GetMapping("/session/{sessionId}/late")
    public ResponseEntity<List<AttendanceDto>> getLateStudents(@PathVariable Long sessionId) {
        List<AttendanceDto> lateStudents = attendanceService.getLateStudents(sessionId);
        return ResponseEntity.ok(lateStudents);
    }

    // Validation Endpoints
    @GetMapping("/validate/user/{userId}/classroom/{classroomId}")
    public ResponseEntity<Boolean> isUserInClassroom(@PathVariable Long userId, @PathVariable Long classroomId) {
        boolean isInClassroom = attendanceService.isUserInClassroom(userId, classroomId);
        return ResponseEntity.ok(isInClassroom);
    }

    @GetMapping("/validate/location")
    public ResponseEntity<Boolean> isWithinLocationRadius(
            @RequestParam Double userLat,
            @RequestParam Double userLon,
            @RequestParam Double sessionLat,
            @RequestParam Double sessionLon,
            @RequestParam Double radius) {
        
        boolean isWithinRadius = attendanceService.isWithinLocationRadius(userLat, userLon, sessionLat, sessionLon, radius);
        return ResponseEntity.ok(isWithinRadius);
    }

    @GetMapping("/validate/mark/{sessionId}/user/{userId}")
    public ResponseEntity<Boolean> canMarkAttendance(@PathVariable Long sessionId, @PathVariable Long userId) {
        boolean canMark = attendanceService.canMarkAttendance(sessionId, userId);
        return ResponseEntity.ok(canMark);
    }
    
    // Add a new endpoint to get attendance records for the current student
    @GetMapping("/student")
    public ResponseEntity<Map<String, Object>> getStudentAttendance() {
        log.info("Getting attendance records for current student");
        
        // In a real implementation, you would get the student ID from the security context
        // For demonstration, we'll create some sample attendance records
        List<AttendanceDto> attendanceRecords = new ArrayList<>();
        
        // Create sample data
        for (int i = 1; i <= 10; i++) {
            // Create a new instance of AttendanceDto using builder pattern instead of setters
            AttendanceDto record = AttendanceDto.builder()
                .id((long) i)
                .sessionId((long) (i % 3) + 1)
                .userId(4L) // Assuming student ID 4 is the current user
                
                // Vary the status for demonstration
                .status(i % 4 == 0 ? AttendanceStatus.ABSENT : 
                       (i % 4 == 1 ? AttendanceStatus.LATE : AttendanceStatus.PRESENT))
                
                // Set dates
                .createdAt(LocalDateTime.now().minusDays(i))
                .markedAt(LocalDateTime.now().minusDays(i))
                .build();
            
            attendanceRecords.add(record);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Retrieved student attendance records successfully");
        response.put("data", attendanceRecords);
        
        return ResponseEntity.ok(response);
    }
}
>>>>>>> master
