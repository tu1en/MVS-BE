package com.classroomapp.classroombackend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.attendancemanagement.AttendanceDto;
import com.classroomapp.classroombackend.dto.LocationDataDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

/**
 * Service xử lý logic điểm danh
 */
@Service
public class AttendanceService {
    
    // Logger cho class này
    private static final Logger logger = LoggerFactory.getLogger(AttendanceService.class);
    
    // Cấu hình tọa độ trung tâm trường học
    private final double schoolCenterLatitude = 21.0478; // Bạch Kim, Hà Nội
    private final double schoolCenterLongitude = 105.7828; // Bạch Kim, Hà Nội
    
    // Bán kính cho phép điểm danh tính từ trung tâm trường (đơn vị: mét)
    private final double allowedRadiusMeters = 500.0;
    
    // Danh sách IP được phép của trường
    private final List<String> schoolWhitelistedIPs = Arrays.asList(
        "127.0.0.1", // Localhost cho phát triển
        "123.16.226.86", // IP công khai của bạn
        "0:0:0:0:0:0:0:1", // IPv6 localhost
        "118.70.211.230" // IP của bạn
    );
    
    // Danh sách IP test (chỉ dùng cho demo)
    private final List<String> testDeniedIPs = Arrays.asList(
        "8.8.8.8", // Google DNS - cố định là luôn bị từ chối
        "1.1.1.1", // Cloudflare DNS - cố định là luôn bị từ chối
        "208.67.222.222" // OpenDNS - cố định là luôn bị từ chối
    );
    
    // Các phụ thuộc
    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    
    /**
     * Constructor với tham số
     */
    @Autowired
    public AttendanceService(
            AttendanceRepository attendanceRepository,
            AttendanceSessionRepository sessionRepository,
            UserRepository userRepository,
            ClassroomRepository classroomRepository) {
        this.attendanceRepository = attendanceRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
    }
    
    /**
     * Thực hiện logic điểm danh
     * 
     * @param username Tên người dùng đang điểm danh
     * @param locationData Dữ liệu vị trí từ client
     * @param clientIp Địa chỉ IP của client
     * @return Phản hồi API chứa kết quả điểm danh
     */
    public ApiResponse PerformCheckInLogic(String username, LocationDataDto locationData, String clientIp) {
        logger.info("Đang xử lý điểm danh cho người dùng: {}, IP: {}, Vị trí: lat={}, lon={}, acc={}",
                username, clientIp, locationData.getLatitude(), locationData.getLongitude(), locationData.getAccuracy());
        
        // Tìm người dùng từ database
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        
        // Kiểm tra người dùng có phải là giáo viên không
        boolean isTeacher = user.getRoleId() == 2; // 2 = TEACHER
        
        // Tự động điểm danh cho giáo viên nếu có phiên học đang diễn ra
        if (isTeacher) {
            return ProcessTeacherAttendance(user, locationData, clientIp);
        } else {
            // Đối với sinh viên, chỉ xử lý điểm danh nếu giáo viên đã điểm danh
            return ProcessStudentAttendance(user, locationData, clientIp);
        }
    }
    
    /**
     * Xử lý điểm danh cho giáo viên
     */
    private ApiResponse ProcessTeacherAttendance(User teacher, LocationDataDto locationData, String clientIp) {
        logger.info("Xử lý điểm danh cho giáo viên: {}", teacher.getUsername());
        
        // Kiểm tra xem giáo viên có phiên dạy đang diễn ra không
        LocalDateTime currentTime = LocalDateTime.now();
        boolean hasActiveSession = sessionRepository.existsByTeacherAndIsActiveTrueAndStartTimeBeforeAndEndTimeAfter(
                teacher, currentTime, currentTime);
        
        if (!hasActiveSession) {
            logger.warn("Giáo viên {} không có phiên dạy nào đang diễn ra", teacher.getUsername());
            return new ApiResponse(false, "Không có phiên dạy nào đang diễn ra. Vui lòng kiểm tra lịch dạy của bạn.");
        }
        
        // Lấy danh sách các phiên dạy đang diễn ra của giáo viên
        List<AttendanceSession> activeSessions = sessionRepository.findByTeacherAndIsActiveTrue(teacher);
        if (activeSessions.isEmpty()) {
            logger.warn("Không tìm thấy phiên dạy nào cho giáo viên {}", teacher.getUsername());
            return new ApiResponse(false, "Không tìm thấy phiên dạy nào đang diễn ra.");
        }
        
        // Lấy phiên dạy đầu tiên từ danh sách (giả sử một giáo viên chỉ có một phiên dạy vào một thời điểm)
        AttendanceSession activeSession = activeSessions.get(0);
        
        // Kiểm tra xem giáo viên đã được điểm danh cho phiên này chưa
        Optional<Attendance> existingAttendance = attendanceRepository.findByUserAndClassroomAndSessionDateBetweenAndIsTeacherRecordTrue(
                teacher, activeSession.getClassroom(), activeSession.getStartTime(), activeSession.getEndTime());
        
        if (existingAttendance.isPresent()) {
            logger.info("Giáo viên {} đã được điểm danh cho phiên này", teacher.getUsername());
            return new ApiResponse(true, "Bạn đã được điểm danh cho phiên này.");
        }
        
        // Kiểm tra loại phiên học (online/offline)
        if ("ONLINE".equalsIgnoreCase(activeSession.getSessionType())) {
            // Đối với phiên online, tự động điểm danh cho giáo viên mà không cần kiểm tra vị trí
            return AutoMarkTeacherAttendance(teacher, activeSession, locationData, clientIp);
        } else {
            // Đối với phiên offline, kiểm tra vị trí và mạng trước khi điểm danh
            return ValidateAndMarkTeacherAttendance(teacher, activeSession, locationData, clientIp);
        }
    }
    
    /**
     * Tự động điểm danh cho giáo viên đối với phiên online
     */
    @Transactional
    public ApiResponse AutoMarkTeacherAttendance(User teacher, AttendanceSession session, 
            LocationDataDto locationData, String clientIp) {
        logger.info("Tự động điểm danh cho giáo viên {} trong phiên online", teacher.getUsername());
        
        // Tạo bản ghi điểm danh mới
        Attendance attendance = new Attendance();
        attendance.setUser(teacher);
        attendance.setClassroom(session.getClassroom());
        attendance.setSessionDate(LocalDateTime.now());
        attendance.setPresent(true);
        attendance.setAttendanceType("ONLINE");
        attendance.setIpAddress(clientIp);
        attendance.setMarkedBy(teacher); // Giáo viên tự điểm danh cho mình
        attendance.setCreatedAt(LocalDateTime.now());
        attendance.setTeacherRecord(true);
        
        // Lưu vào database
        attendanceRepository.save(attendance);
        
        logger.info("Đã điểm danh tự động cho giáo viên {}", teacher.getUsername());
        return new ApiResponse(true, "Điểm danh tự động thành công cho phiên dạy online.");
    }
    
    /**
     * Kiểm tra vị trí và mạng trước khi điểm danh cho giáo viên đối với phiên offline
     */
    public ApiResponse ValidateAndMarkTeacherAttendance(User teacher, AttendanceSession session, 
            LocationDataDto locationData, String clientIp) {
        logger.info("Kiểm tra vị trí và mạng cho giáo viên {} trong phiên offline", teacher.getUsername());
        
        // Kiểm tra độ chính xác GPS
        double gpsAccuracyThreshold = 200.0; // mét
        if (locationData.getAccuracy() > gpsAccuracyThreshold) {
            String message = String.format("Độ chính xác vị trí GPS quá thấp (%.0fm). Yêu cầu <= %.0fm. Vui lòng thử lại ở nơi thoáng hơn.",
                                          locationData.getAccuracy(), gpsAccuracyThreshold);
            logger.warn("Điểm danh thất bại cho {}: {}", teacher.getUsername(), message);
            return new ApiResponse(false, message);
        }
        
        // Kiểm tra khoảng cách (geofence)
        double distanceToSchoolCenter = CalculateHaversineDistance(
                locationData.getLatitude(), locationData.getLongitude(),
                schoolCenterLatitude, schoolCenterLongitude
        );
        
        if (distanceToSchoolCenter > allowedRadiusMeters) {
            String message = String.format("Vị trí của bạn (cách trung tâm trường %.0fm) nằm ngoài phạm vi cho phép (%.0fm).",
                                          distanceToSchoolCenter, allowedRadiusMeters);
            logger.warn("Điểm danh thất bại cho {}: {}", teacher.getUsername(), message);
            return new ApiResponse(false, message);
        }
        
        // Kiểm tra IP và VPN/Proxy
        boolean isIpWhitelisted = schoolWhitelistedIPs.contains(clientIp) ||
                                 clientIp.equals("127.0.0.1") || clientIp.equals("0:0:0:0:0:0:0:1");
        
        boolean isVpnOrProxy = DetectVpnOrProxy(clientIp);
        
        if (isVpnOrProxy) {
            String message = String.format("Kết nối từ IP của bạn (%s) có dấu hiệu sử dụng VPN/Proxy và không được chấp nhận.", clientIp);
            logger.warn("Điểm danh thất bại cho {}: {}", teacher.getUsername(), message);
            return new ApiResponse(false, message);
        }
        
        if (!isIpWhitelisted) {
            String message = String.format("Địa chỉ IP của bạn (%s) không nằm trong danh sách IP được phép của trường.", clientIp);
            logger.warn("Điểm danh thất bại cho {}: {}", teacher.getUsername(), message);
            return new ApiResponse(false, message);
        }
        
        // Tất cả kiểm tra đều thành công, ghi nhận điểm danh
        Attendance attendance = new Attendance();
        attendance.setUser(teacher);
        attendance.setClassroom(session.getClassroom());
        attendance.setSessionDate(LocalDateTime.now());
        attendance.setPresent(true);
        attendance.setAttendanceType("OFFLINE");
        attendance.setLatitude(locationData.getLatitude());
        attendance.setLongitude(locationData.getLongitude());
        attendance.setIpAddress(clientIp);
        attendance.setMarkedBy(teacher); // Giáo viên tự điểm danh cho mình
        attendance.setCreatedAt(LocalDateTime.now());
        attendance.setTeacherRecord(true);
        
        // Lưu vào database
        attendanceRepository.save(attendance);
        
        logger.info("Đã điểm danh thành công cho giáo viên {}", teacher.getUsername());
        return new ApiResponse(true, "Điểm danh thành công cho phiên dạy offline.");
    }
    
    /**
     * Xử lý điểm danh cho sinh viên
     */
    public ApiResponse ProcessStudentAttendance(User student, LocationDataDto locationData, String clientIp) {
        // Xử lý điểm danh cho sinh viên sẽ được thực hiện bởi giáo viên
        // Đây chỉ là dự phòng nếu sinh viên cố gắng tự điểm danh
        return new ApiResponse(false, "Sinh viên không thể tự điểm danh. Vui lòng liên hệ giáo viên của bạn.");
    }
    
    /**
     * Lấy danh sách sinh viên để điểm danh
     * 
     * @param sessionId ID của phiên điểm danh
     * @param teacherId ID của giáo viên thực hiện điểm danh
     * @return Danh sách thông tin sinh viên cần điểm danh
     */
    public List<AttendanceDto> GetStudentsForAttendance(Long sessionId, Long teacherId) {
        try {
            // Kiểm tra phiên điểm danh có tồn tại không
            AttendanceSession session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("AttendanceSession", "id", sessionId));
            
            // Kiểm tra giáo viên có quyền thực hiện điểm danh cho phiên này không
            User teacher = userRepository.findById(teacherId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", teacherId));
            
            if (!session.getTeacher().getId().equals(teacherId)) {
                logger.warn("Giáo viên {} không có quyền điểm danh cho phiên {}", teacher.getUsername(), sessionId);
                return new ArrayList<>();
            }
            
            // Lấy danh sách sinh viên trong lớp học
            Classroom classroom = session.getClassroom();
            Set<User> students = classroom.getStudents();
            
            if (students == null || students.isEmpty()) {
                logger.warn("Không có sinh viên nào trong lớp {}", classroom.getName());
                return new ArrayList<>();
            }
            
            // Chuyển đổi thành DTO để trả về
            return students.stream().map(student -> {
                AttendanceDto dto = new AttendanceDto();
                dto.setUserId(student.getId());
                dto.setUserName(student.getUsername());
                dto.setUserFullName(student.getFullName());
                // Thêm URL ảnh của sinh viên nếu có
                // dto.setUserPhotoUrl(student.getPhotoUrl());
                dto.setClassroomId(classroom.getId());
                dto.setClassroomName(classroom.getName());
                dto.setSessionDate(LocalDateTime.now());
                dto.setPresent(false); // Mặc định là vắng mặt
                dto.setAttendanceType(session.getSessionType());
                
                // Kiểm tra xem sinh viên đã được điểm danh chưa
                Optional<Attendance> existingAttendance = attendanceRepository.findByUserAndClassroomAndSessionDateBetween(
                        student, classroom, session.getStartTime(), session.getEndTime());
                
                if (existingAttendance.isPresent()) {
                    Attendance attendance = existingAttendance.get();
                    dto.setId(attendance.getId());
                    dto.setPresent(attendance.isPresent());
                    dto.setComment(attendance.getComment());
                    dto.setPhotoUrl(attendance.getPhotoUrl());
                }
                
                return dto;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Lỗi khi lấy danh sách sinh viên: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Đánh dấu điểm danh cho sinh viên bởi giáo viên
     * 
     * @param attendanceDto Thông tin điểm danh
     * @param teacherId ID của giáo viên thực hiện điểm danh
     * @return ApiResponse chứa kết quả điểm danh
     */
    @Transactional
    public ApiResponse MarkStudentAttendance(AttendanceDto attendanceDto, Long teacherId) {
        // Kiểm tra giáo viên có tồn tại không
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", teacherId));
        
        // Kiểm tra sinh viên có tồn tại không
        User student = userRepository.findById(attendanceDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", attendanceDto.getUserId()));
        
        // Kiểm tra lớp học có tồn tại không
        Classroom classroom = classroomRepository.findById(attendanceDto.getClassroomId())
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", "id", attendanceDto.getClassroomId()));
        
        // Kiểm tra xem sinh viên có trong lớp học không
        if (!classroom.getStudents().contains(student)) {
            logger.warn("Sinh viên {} không thuộc lớp học {}", student.getUsername(), classroom.getName());
            return new ApiResponse(false, "Sinh viên không thuộc lớp học này.");
        }
        
        // Kiểm tra xem giáo viên có quyền điểm danh cho lớp học này không
        if (!classroom.getTeacher().getId().equals(teacherId)) {
            logger.warn("Giáo viên {} không có quyền điểm danh cho lớp học {}", teacher.getUsername(), classroom.getName());
            return new ApiResponse(false, "Bạn không có quyền điểm danh cho lớp học này.");
        }
        
        // Tạo hoặc cập nhật bản ghi điểm danh
        Attendance attendance;
        if (attendanceDto.getId() != null) {
            // Cập nhật bản ghi điểm danh hiện có
            attendance = attendanceRepository.findById(attendanceDto.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", attendanceDto.getId()));
        } else {
            // Tạo bản ghi điểm danh mới
            attendance = new Attendance();
            attendance.setUser(student);
            attendance.setClassroom(classroom);
            attendance.setSessionDate(LocalDateTime.now());
            attendance.setCreatedAt(LocalDateTime.now());
            attendance.setTeacherRecord(false);
        }
        
        // Cập nhật thông tin điểm danh
        attendance.setPresent(attendanceDto.isPresent());
        attendance.setAttendanceType(attendanceDto.getAttendanceType());
        attendance.setComment(attendanceDto.getComment());
        attendance.setPhotoUrl(attendanceDto.getPhotoUrl());
        attendance.setMarkedBy(teacher);
        
        // Lưu vào database
        attendanceRepository.save(attendance);
        
        logger.info("Giáo viên {} đã điểm danh cho sinh viên {}: {}", 
                teacher.getUsername(), student.getUsername(), attendance.isPresent() ? "Có mặt" : "Vắng mặt");
                
        return new ApiResponse(true, "Điểm danh thành công cho sinh viên.");
    }
    
    /**
     * Tính khoảng cách giữa hai tọa độ GPS bằng công thức Haversine
     * 
     * @param lat1 Vĩ độ điểm 1
     * @param lon1 Kinh độ điểm 1
     * @param lat2 Vĩ độ điểm 2
     * @param lon2 Kinh độ điểm 2
     * @return Khoảng cách tính bằng mét
     */
    private double CalculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371; // Bán kính trung bình của Trái Đất (km)
        
        // Chuyển đổi từ độ sang radian
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);
        
        // Tính chênh lệch
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;
        
        // Công thức Haversine
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        // Khoảng cách theo mét
        double distance = EARTH_RADIUS_KM * c * 1000;
        return distance;
    }
    
    /**
     * Phát hiện VPN hoặc Proxy qua dịch vụ bên ngoài
     * 
     * @param clientIp Địa chỉ IP cần kiểm tra
     * @return true nếu phát hiện là VPN/Proxy, false nếu không
     */
    private boolean DetectVpnOrProxy(String clientIp) {
        // Bỏ qua IP private/local
        if (clientIp == null || clientIp.isEmpty() ||
            clientIp.equals("127.0.0.1") || clientIp.equals("0:0:0:0:0:0:0:1") ||
            clientIp.matches("^192\\.168\\..*") ||
            clientIp.matches("^10\\..*") ||
            clientIp.matches("^172\\.(1[6-9]|2[0-9]|3[0-1])\\..*")) {
            logger.debug("Bỏ qua kiểm tra VPN/Proxy cho IP private/local: {}", clientIp);
            return false;
        }
        
        // Kiểm tra IP test - Nếu là IP test đã định nghĩa là bị chặn thì luôn trả về true
        if (testDeniedIPs.contains(clientIp)) {
            logger.info("IP {} được cố định là IP không được phép để test", clientIp);
            return true;
        }
        
        // Trong môi trường demo/dev, không thực sự gọi API bên ngoài để tránh lỗi
        // Chú ý: Trong ứng dụng thực tế, đoạn này sẽ thực hiện gọi API
        try {
            logger.debug("Bỏ qua gọi API bên ngoài trong môi trường demo: {}", clientIp);
            // Mô phỏng logic phát hiện: IP ngoài whitelist có 20% xác suất bị coi là proxy
            if (!schoolWhitelistedIPs.contains(clientIp)) {
                boolean simulatedResult = Math.random() < 0.2; // 20% xác suất
                if (simulatedResult) {
                    logger.info("IP {} được MÔ PHỎNG là Proxy/VPN (chỉ để demo)", clientIp);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("Ngoại lệ khi kiểm tra VPN/Proxy cho IP {}: {}", clientIp, e.getMessage(), e);
        }
        
        // Mặc định không phải VPN/Proxy nếu có lỗi
        return false;
    }
} 