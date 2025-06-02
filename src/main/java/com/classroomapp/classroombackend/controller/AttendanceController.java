package com.classroomapp.classroombackend.controller;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.AttendanceDto;
import com.classroomapp.classroombackend.dto.LocationDataDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.Attendance;
import com.classroomapp.classroombackend.model.AttendanceSession;
import com.classroomapp.classroombackend.model.User;
import com.classroomapp.classroombackend.repository.AttendanceRepository;
import com.classroomapp.classroombackend.repository.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.UserRepository;
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