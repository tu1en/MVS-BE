package com.classroomapp.classroombackend.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.classroomapp.classroombackend.dto.ApiResponse;
import com.classroomapp.classroombackend.dto.LocationDataDto;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Lớp kiểm thử cho AttendanceService
 */
@ExtendWith(MockitoExtension.class)
public class AttendanceServiceTest {

    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private AttendanceRepository attendanceRepository;
    
    @Mock
    private AttendanceSessionRepository sessionRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ClassroomRepository classroomRepository;

    private AttendanceService attendanceService;

    /**
     * Thiết lập trước mỗi test case
     */
    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(
            attendanceRepository,
            sessionRepository,
            userRepository,
            classroomRepository
        );
    }

    /**
     * Kiểm tra trường hợp vị trí GPS độ chính xác thấp
     */
    @Test
    void TestLowAccuracyGpsShouldFail() {
        // Chuẩn bị dữ liệu với độ chính xác thấp (lớn hơn ngưỡng)
        LocationDataDto locationData = new LocationDataDto(21.028511, 105.804817, 300.0); // 300m accuracy
        
        // Thực thi
        ApiResponse response = attendanceService.PerformCheckInLogic("testUser", locationData, "127.0.0.1");
        
        // Kiểm tra kết quả
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Độ chính xác vị trí GPS quá thấp"));
    }

    /**
     * Kiểm tra trường hợp vị trí nằm ngoài phạm vi cho phép
     */
    @Test
    void TestLocationOutsideAllowedAreaShouldFail() {
        // Chuẩn bị dữ liệu với vị trí xa trung tâm (ngoài bán kính cho phép)
        LocationDataDto locationData = new LocationDataDto(22.0, 106.0, 50.0); // Vị trí xa trung tâm
        
        // Thực thi
        ApiResponse response = attendanceService.PerformCheckInLogic("testUser", locationData, "127.0.0.1");
        
        // Kiểm tra kết quả
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("nằm ngoài phạm vi cho phép"));
    }

    /**
     * Kiểm tra trường hợp IP không nằm trong whitelist
     */
    @Test
    void TestNonWhitelistedIpShouldFail() {
        // Chuẩn bị dữ liệu với vị trí hợp lệ
        LocationDataDto locationData = new LocationDataDto(21.028511, 105.804817, 50.0); // Vị trí chính xác
        
        // Thực thi với IP không nằm trong whitelist
        ApiResponse response = attendanceService.PerformCheckInLogic("testUser", locationData, "1.2.3.4");
        
        // Kiểm tra kết quả
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("IP của bạn") && response.getMessage().contains("không nằm trong danh sách IP"));
    }

    /**
     * Kiểm tra trường hợp thành công
     */
    @Test
    void TestSuccessfulCheckIn() {
        // Chuẩn bị dữ liệu hợp lệ
        LocationDataDto locationData = new LocationDataDto(21.028511, 105.804817, 50.0); // Vị trí chính xác
        
        // Thực thi với IP localhost (được cho phép)
        ApiResponse response = attendanceService.PerformCheckInLogic("testUser", locationData, "127.0.0.1");
        
        // Kiểm tra kết quả
        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("Điểm danh thành công"));
    }
    
    /**
     * Kiểm tra phương thức tính khoảng cách Haversine
     */
    @Test
    void TestHaversineDistanceCalculation() throws Exception {
        // Tạo một instance mới để truy cập phương thức private thông qua reflection
        AttendanceService service = new AttendanceService(
            attendanceRepository,
            sessionRepository,
            userRepository,
            classroomRepository
        );
        
        // Lấy phương thức private bằng reflection
        java.lang.reflect.Method method = AttendanceService.class.getDeclaredMethod("CalculateHaversineDistance", 
                double.class, double.class, double.class, double.class);
        method.setAccessible(true);
        
        // Tọa độ Hà Nội
        double hanoi_lat = 21.028511;
        double hanoi_lon = 105.804817;
        
        // Tọa độ Hồ Chí Minh (cách xa Hà Nội)
        double hcm_lat = 10.823099;
        double hcm_lon = 106.629664;
        
        // Thực thi phương thức
        double distance = (double) method.invoke(service, hanoi_lat, hanoi_lon, hcm_lat, hcm_lon);
        
        // Kiểm tra khoảng cách (khoảng cách thực tế khoảng 1760km)
        assertTrue(distance > 1700000 && distance < 1800000);
    }
} 