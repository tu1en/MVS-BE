package com.classroomapp.classroombackend.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.AttendanceDto;
import com.classroomapp.classroombackend.dto.AttendanceSessionDto;
import com.classroomapp.classroombackend.dto.LocationDataDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance.AttendanceStatus;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AttendanceService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller xử lý các API liên quan đến điểm danh
 */
@RestController
@RequestMapping("/api/attendance")
@Slf4j
public class AttendanceController {
    private final com.classroomapp.classroombackend.accountant.repository.AttendanceExplanationRepository attendanceExplanationRepository;
    
    private final AttendanceService attendanceService;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    
    /**
     * Constructor với tham số
     * 
     * @param attendanceService Service xử lý logic điểm danh
     * @param attendanceRepository Repository cho bản ghi điểm danh
     * @param sessionRepository Repository cho phiên điểm danh
     * @param userRepository Repository cho người dùng
     * @param classroomRepository Repository cho lớp học
     */
    @Autowired
    public AttendanceController(
            AttendanceService attendanceService,
            AttendanceRepository attendanceRepository,
            AttendanceSessionRepository sessionRepository,
            UserRepository userRepository,
            ClassroomRepository classroomRepository,
            com.classroomapp.classroombackend.accountant.repository.AttendanceExplanationRepository attendanceExplanationRepository) {
        this.attendanceService = attendanceService;
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
        this.attendanceExplanationRepository = attendanceExplanationRepository;
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
        log.info("Nhận được yêu cầu điểm danh cho người dùng (mô phỏng): {}", demoUsername);
        
        // Lấy địa chỉ IP client
        String clientIpAddress = ExtractClientIpAddress(request);
        log.info("Địa chỉ IP của client: {}", clientIpAddress);
        
        // Xác thực dữ liệu đầu vào
        if (locationData == null ||
            locationData.getLatitude() < -90 || locationData.getLatitude() > 90 ||
            locationData.getLongitude() < -180 || locationData.getLongitude() > 180 ||
            locationData.getAccuracy() <= 0) {
            log.warn("Dữ liệu vị trí không hợp lệ: {}", locationData);
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
            log.error("Lỗi máy chủ khi xử lý điểm danh: {}", e.getMessage(), e);
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
        
        log.info("Nhận được yêu cầu lấy danh sách sinh viên để điểm danh cho phiên {}, từ giáo viên {}", 
                sessionId, teacherId);
        
        try {
            List<AttendanceDto> students = attendanceService.GetStudentsForAttendance(sessionId, teacherId);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách sinh viên để điểm danh: {}", e.getMessage(), e);
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
        
        log.info("Marking attendance for student {} by teacher {}", attendanceDto.getUserId(), teacherId);
        
        ApiResponse response = attendanceService.MarkStudentAttendance(attendanceDto, teacherId);
        return ResponseEntity.ok(response);
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
            // Lấy địa chỉ IP đầu tiên trong chuỗi (IP của client ban đầu)
            return xForwardedForHeader.split(",")[0].trim();
        }
        
        // Kiểm tra các header khác liên quan đến proxy
        String proxyClientIp = request.getHeader("Proxy-Client-IP");
        if (proxyClientIp != null && !proxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(proxyClientIp)) {
            return proxyClientIp;
        }
        
        String wlProxyClientIp = request.getHeader("WL-Proxy-Client-IP");
        if (wlProxyClientIp != null && !wlProxyClientIp.isEmpty() && !"unknown".equalsIgnoreCase(wlProxyClientIp)) {
            return wlProxyClientIp;
        }
        
        // Sử dụng địa chỉ Remote của request (giải pháp cuối cùng)
        return request.getRemoteAddr();
    }
    
    /**
     * Kiểm tra API với địa chỉ IP được chỉ định
     * 
     * @param locationData Dữ liệu vị trí
     * @param simulatedIP IP giả lập
     * @return Kết quả kiểm tra
     */
    @PostMapping("/test-with-ip")
    public ResponseEntity<ApiResponse> TestAttendanceWithSimulatedIp(
            @RequestBody LocationDataDto locationData,
            @RequestParam String simulatedIP) {
        
        log.info("Thử nghiệm điểm danh với IP giả lập: {}", simulatedIP);
        
        // Giả lập người dùng
        String testUsername = "test_user_ip_simulation";
        
        // Gọi service với IP đã chỉ định
        ApiResponse response = attendanceService.PerformCheckInLogic(testUsername, locationData, simulatedIP);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Kiểm tra API với địa chỉ IP được cho phép
     * 
     * @param locationData Dữ liệu vị trí
     * @return Kết quả kiểm tra
     */
    @PostMapping("/test-allowed-ip")
    public ResponseEntity<ApiResponse> TestWithAllowedIP(@RequestBody LocationDataDto locationData) {
        
        log.info("Thử nghiệm điểm danh với IP được cho phép");
        
        // IP được cho phép để kiểm tra
        String allowedTestIp = "192.168.1.100";
        String testUsername = "test_user_allowed_ip";
        
        // Gọi service với IP đã chỉ định
        ApiResponse response = attendanceService.PerformCheckInLogic(testUsername, locationData, allowedTestIp);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Kiểm tra API với địa chỉ IP không được cho phép
     * 
     * @param locationData Dữ liệu vị trí
     * @return Kết quả kiểm tra
     */
    @PostMapping("/test-denied-ip")
    public ResponseEntity<ApiResponse> TestWithDeniedIP(@RequestBody LocationDataDto locationData) {
        
        log.info("Thử nghiệm điểm danh với IP không được cho phép");
        
        // IP không được cho phép để kiểm tra
        String deniedTestIp = "10.0.0.50";
        String testUsername = "test_user_denied_ip";
        
        // Gọi service với IP đã chỉ định
        ApiResponse response = attendanceService.PerformCheckInLogic(testUsername, locationData, deniedTestIp);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Kiểm tra trạng thái điểm danh của giáo viên
     * 
     * @param sessionId ID phiên điểm danh
     * @param teacherId ID giáo viên
     * @return Trạng thái điểm danh của giáo viên
     */
    @GetMapping("/teacher-status")
    public ResponseEntity<?> CheckTeacherAttendanceStatus(
            @RequestParam Long sessionId,
            @RequestParam Long teacherId) {
        
        log.info("Kiểm tra trạng thái điểm danh của giáo viên {} cho phiên {}", teacherId, sessionId);
        
        try {
            AttendanceSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", sessionId));
            
            // Lấy giáo viên từ database
            User teacher = userRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", teacherId));
            
            // Just check if teacher attendance exists
            Optional<Attendance> teacherAttendance = attendanceRepository.findByStudentAndSession(teacher, session);
            
            // Tạo response
            Map<String, Object> response = new HashMap<>();
            response.put("isPresent", teacherAttendance.isPresent());
            
            if (teacherAttendance.isPresent()) {
                Attendance attendance = teacherAttendance.get();
                response.put("attendanceId", attendance.getId());
                response.put("attendanceTime", attendance.getCheckInTime());
                response.put("attendanceStatus", attendance.getStatus().toString());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra trạng thái điểm danh của giáo viên: {}", e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

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

    /**
     * Get all attendance sessions
     * @return List of all attendance sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<AttendanceSessionDto>> getAllAttendanceSessions(Authentication authentication) {
        try {
            log.info("Getting all attendance sessions");
            
            // Get current user from authentication
            String username = authentication != null ? authentication.getName() : null;
            if (username == null) {
                log.warn("No authentication provided when getting all attendance sessions");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(List.of());
            }
            
            User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            List<AttendanceSessionDto> sessions;
            
            // If user is a teacher, get their sessions
            if (currentUser.getRoleId() == 2) { // Teacher role
                sessions = attendanceService.getAttendanceSessionsByTeacher(currentUser.getId());
            } 
            // If user is a student, get sessions from their enrolled classrooms
            else if (currentUser.getRoleId() == 3) { // Student role
                // Get all classrooms where the student is enrolled
                List<Classroom> enrolledClassrooms = classroomRepository.findClassroomsByStudentId(currentUser.getId());
                
                // Get sessions for each enrolled classroom
                sessions = enrolledClassrooms.stream()
                    .flatMap(classroom -> attendanceService.getAttendanceSessionsByClassroom(classroom.getId()).stream())
                    .collect(Collectors.toList());
            } 
            // If user is admin or manager, get all sessions
            else {
                // Get all sessions from all classrooms
                List<Classroom> classrooms = classroomRepository.findAll();
                sessions = classrooms.stream()
                    .flatMap(classroom -> attendanceService.getAttendanceSessionsByClassroom(classroom.getId()).stream())
                    .collect(Collectors.toList());
            }
            
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            log.error("Error getting all attendance sessions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(List.of());
        }
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
    
    /**
     * API endpoint để sinh viên xem điểm danh của mình
     * @param authentication Thông tin xác thực của người dùng
     * @return Danh sách điểm danh của sinh viên
     */
    @GetMapping("/student/view")
    public ResponseEntity<?> getStudentAttendanceView(Authentication authentication) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Không có quyền truy cập. Vui lòng đăng nhập."));
            }

            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
            
            // Lấy danh sách điểm danh của sinh viên
            List<AttendanceDto> attendanceRecords = attendanceService.getAttendanceByUser(currentUser.getId());
            
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("success", true);
                put("records", attendanceRecords);
            }});
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin điểm danh của sinh viên: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Đã xảy ra lỗi khi lấy thông tin điểm danh."));
        }
    }

    /**
     * API endpoint để đếm số buổi đi học, vắng học của sinh viên
     * @param authentication Thông tin xác thực của người dùng
     * @return Thống kê điểm danh của sinh viên
     */
    /**
     * API: Báo cáo chấm công theo ca, trả về lý do đi trễ nếu có giải trình
     * Params: from, to, shift
     */
    @GetMapping("/shift-report")
    public ResponseEntity<?> getShiftAttendanceReport(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam(required = false) String shift) {
        try {
            java.time.LocalDateTime fromDate = java.time.LocalDateTime.parse(from + "T00:00:00");
            java.time.LocalDateTime toDate = java.time.LocalDateTime.parse(to + "T23:59:59");
            // Lấy danh sách Attendance theo ngày (không lọc shift ở DB)
            java.util.List<Attendance> attendances = attendanceRepository.findByDateRange(fromDate, toDate);
            // Nếu FE truyền shift, lọc ở Java (giả sử shift lưu ở sessionName hoặc title)
            if (shift != null && !shift.isEmpty()) {
                attendances = attendances.stream().filter(a -> {
                    String sessionShift = null;
                    if (a.getSession().getSessionName() != null) sessionShift = a.getSession().getSessionName();
                    else if (a.getSession().getTitle() != null) sessionShift = a.getSession().getTitle();
                    if (sessionShift == null) return false;
                    return sessionShift.equalsIgnoreCase(shift);
                }).toList();
            }
            // Chuẩn bị map để lấy lý do đi trễ
            java.util.List<Long> userIds = attendances.stream()
                .filter(a -> a.getStatus() == Attendance.AttendanceStatus.LATE)
                .map(a -> a.getStudent().getId())
                .distinct().toList();
            java.util.Map<Long, String> lateReasons = new java.util.HashMap<>();
            if (!userIds.isEmpty()) {
                // Lấy giải trình đã được duyệt cho các user này trong khoảng ngày
                java.util.List<com.classroomapp.classroombackend.accountant.model.AttendanceExplanation> explanations =
                    attendanceExplanationRepository.findByEmployeeIdInAndExplanationTypeAndStatus(
                        userIds, "LATE", "APPROVED");
                for (var exp : explanations) {
                    lateReasons.put(exp.getEmployeeId(), exp.getContent());
                }
            }
            // Trả về dữ liệu cho FE
            java.util.List<java.util.Map<String, Object>> result = attendances.stream().map(a -> {
                java.util.Map<String, Object> row = new java.util.HashMap<>();
                row.put("id", a.getId());
                row.put("employeeName", a.getStudent().getFullName());
                // shift lấy từ sessionName hoặc title nếu có
                String sessionShift = null;
                if (a.getSession().getSessionName() != null) sessionShift = a.getSession().getSessionName();
                else if (a.getSession().getTitle() != null) sessionShift = a.getSession().getTitle();
                row.put("shift", sessionShift);
                row.put("date", a.getCheckInTime().toLocalDate());
                row.put("checkIn", a.getCheckInTime());
                row.put("checkOut", a.getCheckOutTime());
                boolean isLate = a.getStatus() == Attendance.AttendanceStatus.LATE;
                row.put("isLate", isLate);
                row.put("lateReason", isLate ? lateReasons.getOrDefault(a.getStudent().getId(), null) : null);
                return row;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Lỗi lấy báo cáo chấm công theo ca: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Lỗi lấy báo cáo chấm công theo ca."));
        }
    }


    @GetMapping("/validate/location")
    public ResponseEntity<Boolean> isWithinLocationRadius(
            @RequestParam Double userLat,
            @RequestParam Double userLon,
            @RequestParam Double sessionLat,
            @RequestParam Double sessionLon,
            @RequestParam Double radius) {
        
        boolean isWithinRadius = attendanceService.isWithinLocationRadius(
            userLat, userLon, sessionLat, sessionLon, radius);
        return ResponseEntity.ok(isWithinRadius);
    }

    @GetMapping("/validate/mark/{sessionId}/user/{userId}")
    public ResponseEntity<Boolean> canMarkAttendance(@PathVariable Long sessionId, @PathVariable Long userId) {
        boolean canMark = attendanceService.canMarkAttendance(sessionId, userId);
        return ResponseEntity.ok(canMark);
    }


}

