package com.classroomapp.classroombackend.services;

import com.classroomapp.classroombackend.model.hrmanagement.StaffAttendanceLog;
import com.classroomapp.classroombackend.service.AttendanceVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho các hàm tính toán trong AttendanceVerificationService
 * Test các hàm tính toán thuần túy, không phụ thuộc vào database
 */
@DisplayName("Attendance Calculation Tests")
class AttendanceCalculationTest {

    @InjectMocks
    private AttendanceVerificationService attendanceVerificationService;

    @Mock
    private com.classroomapp.classroombackend.repository.hrmanagement.StaffAttendanceLogRepository attendanceLogRepository;

    @Mock
    private com.classroomapp.classroombackend.repository.hrmanagement.CompanyLocationRepository locationRepository;

    @Mock
    private com.classroomapp.classroombackend.repository.hrmanagement.AllowedNetworkRepository networkRepository;

    @Mock
    private com.classroomapp.classroombackend.repository.hrmanagement.AttendanceVerificationLogRepository verificationLogRepository;

    @Mock
    private com.classroomapp.classroombackend.repository.hrmanagement.UserShiftAssignmentRepository userShiftAssignmentRepository;

    @BeforeEach
    void setUp() {
        // Thiết lập Mockito
        MockitoAnnotations.openMocks(this);
        
        // Thiết lập các giá trị mặc định cho service
        ReflectionTestUtils.setField(attendanceVerificationService, "devMode", true);
        ReflectionTestUtils.setField(attendanceVerificationService, "skipLocationCheck", true);
        ReflectionTestUtils.setField(attendanceVerificationService, "skipNetworkCheck", true);
    }

    @Test
    @DisplayName("Tính số giờ làm việc - Trường hợp bình thường (8 giờ)")
    void testCalculateWorkingHours_NormalCase() {
        // Setup: Tạo dữ liệu test với giờ check-in 8:00 và check-out 17:00
        StaffAttendanceLog log = new StaffAttendanceLog();
        log.setCheckInTime(LocalTime.of(8, 0));  // 8:00 sáng
        log.setCheckOutTime(LocalTime.of(17, 0)); // 5:00 chiều

        // Run: Gọi hàm tính toán số giờ làm việc
        double workingHours = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateWorkingHours", 
            log
        );

        // Assert: Kiểm tra kết quả
        assertEquals(9.0, workingHours, 0.01, "Số giờ làm việc phải là 9.0 giờ");
        System.out.println("✅ Test thành công: 8:00 - 17:00 = " + workingHours + " giờ");
    }

    @Test
    @DisplayName("Tính số giờ làm việc - Trường hợp nửa ngày (4 giờ)")
    void testCalculateWorkingHours_HalfDay() {
        // Setup: Tạo dữ liệu test với giờ check-in 8:00 và check-out 12:00
        StaffAttendanceLog log = new StaffAttendanceLog();
        log.setCheckInTime(LocalTime.of(8, 0));  // 8:00 sáng
        log.setCheckOutTime(LocalTime.of(12, 0)); // 12:00 trưa

        // Run: Gọi hàm tính toán số giờ làm việc
        double workingHours = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateWorkingHours", 
            log
        );

        // Assert: Kiểm tra kết quả
        assertEquals(4.0, workingHours, 0.01, "Số giờ làm việc phải là 4.0 giờ");
        System.out.println("✅ Test thành công: 8:00 - 12:00 = " + workingHours + " giờ");
    }

    @Test
    @DisplayName("Tính số giờ làm việc - Trường hợp làm thêm giờ (10 giờ)")
    void testCalculateWorkingHours_Overtime() {
        // Setup: Tạo dữ liệu test với giờ check-in 8:00 và check-out 19:00
        StaffAttendanceLog log = new StaffAttendanceLog();
        log.setCheckInTime(LocalTime.of(8, 0));  // 8:00 sáng
        log.setCheckOutTime(LocalTime.of(19, 0)); // 7:00 tối

        // Run: Gọi hàm tính toán số giờ làm việc
        double workingHours = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateWorkingHours", 
            log
        );

        // Assert: Kiểm tra kết quả
        assertEquals(11.0, workingHours, 0.01, "Số giờ làm việc phải là 11.0 giờ");
        System.out.println("✅ Test thành công: 8:00 - 19:00 = " + workingHours + " giờ (làm thêm)");
    }

    @Test
    @DisplayName("Tính số giờ làm việc - Trường hợp check-in muộn (6 giờ)")
    void testCalculateWorkingHours_LateCheckIn() {
        // Setup: Tạo dữ liệu test với giờ check-in 10:00 và check-out 17:00
        StaffAttendanceLog log = new StaffAttendanceLog();
        log.setCheckInTime(LocalTime.of(10, 0)); // 10:00 sáng
        log.setCheckOutTime(LocalTime.of(17, 0)); // 5:00 chiều

        // Run: Gọi hàm tính toán số giờ làm việc
        double workingHours = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateWorkingHours", 
            log
        );

        // Assert: Kiểm tra kết quả
        assertEquals(7.0, workingHours, 0.01, "Số giờ làm việc phải là 7.0 giờ");
        System.out.println("✅ Test thành công: 10:00 - 17:00 = " + workingHours + " giờ (check-in muộn)");
    }

    @Test
    @DisplayName("Tính số giờ làm việc - Trường hợp check-out sớm (5 giờ)")
    void testCalculateWorkingHours_EarlyCheckOut() {
        // Setup: Tạo dữ liệu test với giờ check-in 8:00 và check-out 14:00
        StaffAttendanceLog log = new StaffAttendanceLog();
        log.setCheckInTime(LocalTime.of(8, 0));  // 8:00 sáng
        log.setCheckOutTime(LocalTime.of(14, 0)); // 2:00 chiều

        // Run: Gọi hàm tính toán số giờ làm việc
        double workingHours = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateWorkingHours", 
            log
        );

        // Assert: Kiểm tra kết quả
        assertEquals(6.0, workingHours, 0.01, "Số giờ làm việc phải là 6.0 giờ");
        System.out.println("✅ Test thành công: 8:00 - 14:00 = " + workingHours + " giờ (check-out sớm)");
    }

    @Test
    @DisplayName("Tính số giờ làm việc - Trường hợp làm việc qua đêm (16 giờ)")
    void testCalculateWorkingHours_Overnight() {
        // Setup: Tạo dữ liệu test với giờ check-in 20:00 và check-out 12:00 (ngày hôm sau)
        StaffAttendanceLog log = new StaffAttendanceLog();
        log.setCheckInTime(LocalTime.of(20, 0)); // 8:00 tối
        log.setCheckOutTime(LocalTime.of(12, 0)); // 12:00 trưa (ngày hôm sau)

        // Run: Gọi hàm tính toán số giờ làm việc
        double workingHours = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateWorkingHours", 
            log
        );

        // Assert: Kiểm tra kết quả (16 giờ từ 20:00 đến 12:00)
        assertEquals(16.0, workingHours, 0.01, "Số giờ làm việc phải là 16.0 giờ");
        System.out.println("✅ Test thành công: 20:00 - 12:00 (ngày hôm sau) = " + workingHours + " giờ (làm việc qua đêm)");
    }

    @Test
    @DisplayName("Tính số giờ làm việc - Trường hợp thiếu check-in (trả về 0)")
    void testCalculateWorkingHours_MissingCheckIn() {
        // Setup: Tạo dữ liệu test với check-in null và check-out 17:00
        StaffAttendanceLog log = new StaffAttendanceLog();
        log.setCheckInTime(null);                // Không có check-in
        log.setCheckOutTime(LocalTime.of(17, 0)); // 5:00 chiều

        // Run: Gọi hàm tính toán số giờ làm việc
        double workingHours = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateWorkingHours", 
            log
        );

        // Assert: Kiểm tra kết quả
        assertEquals(0.0, workingHours, 0.01, "Số giờ làm việc phải là 0.0 khi thiếu check-in");
        System.out.println("✅ Test thành công: Thiếu check-in → " + workingHours + " giờ");
    }

    @Test
    @DisplayName("Tính số giờ làm việc - Trường hợp thiếu check-out (trả về 0)")
    void testCalculateWorkingHours_MissingCheckOut() {
        // Setup: Tạo dữ liệu test với check-in 8:00 và check-out null
        StaffAttendanceLog log = new StaffAttendanceLog();
        log.setCheckInTime(LocalTime.of(8, 0)); // 8:00 sáng
        log.setCheckOutTime(null);               // Không có check-out

        // Run: Gọi hàm tính toán số giờ làm việc
        double workingHours = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateWorkingHours", 
            log
        );

        // Assert: Kiểm tra kết quả
        assertEquals(0.0, workingHours, 0.01, "Số giờ làm việc phải là 0.0 khi thiếu check-out");
        System.out.println("✅ Test thành công: Thiếu check-out → " + workingHours + " giờ");
    }

    @Test
    @DisplayName("Tính số giờ làm việc - Trường hợp cả check-in và check-out đều null (trả về 0)")
    void testCalculateWorkingHours_BothNull() {
        // Setup: Tạo dữ liệu test với cả check-in và check-out đều null
        StaffAttendanceLog log = new StaffAttendanceLog();
        log.setCheckInTime(null);  // Không có check-in
        log.setCheckOutTime(null); // Không có check-out

        // Run: Gọi hàm tính toán số giờ làm việc
        double workingHours = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateWorkingHours", 
            log
        );

        // Assert: Kiểm tra kết quả
        assertEquals(0.0, workingHours, 0.01, "Số giờ làm việc phải là 0.0 khi cả hai đều null");
        System.out.println("✅ Test thành công: Cả check-in và check-out đều null → " + workingHours + " giờ");
    }

    @Test
    @DisplayName("Tính số giờ làm việc - Trường hợp làm việc ít hơn 1 giờ (30 phút)")
    void testCalculateWorkingHours_LessThanOneHour() {
        // Setup: Tạo dữ liệu test với giờ check-in 8:00 và check-out 8:30
        StaffAttendanceLog log = new StaffAttendanceLog();
        log.setCheckInTime(LocalTime.of(8, 0));  // 8:00 sáng
        log.setCheckOutTime(LocalTime.of(8, 30)); // 8:30 sáng

        // Run: Gọi hàm tính toán số giờ làm việc
        double workingHours = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateWorkingHours", 
            log
        );

        // Assert: Kiểm tra kết quả
        assertEquals(0.5, workingHours, 0.01, "Số giờ làm việc phải là 0.5 giờ (30 phút)");
        System.out.println("✅ Test thành công: 8:00 - 8:30 = " + workingHours + " giờ (30 phút)");
    }

    @Test
    @DisplayName("Tính số giờ làm việc - Trường hợp làm việc chính xác 1 giờ")
    void testCalculateWorkingHours_ExactlyOneHour() {
        // Setup: Tạo dữ liệu test với giờ check-in 9:00 và check-out 10:00
        StaffAttendanceLog log = new StaffAttendanceLog();
        log.setCheckInTime(LocalTime.of(9, 0));  // 9:00 sáng
        log.setCheckOutTime(LocalTime.of(10, 0)); // 10:00 sáng

        // Run: Gọi hàm tính toán số giờ làm việc
        double workingHours = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateWorkingHours", 
            log
        );

        // Assert: Kiểm tra kết quả
        assertEquals(1.0, workingHours, 0.01, "Số giờ làm việc phải là 1.0 giờ chính xác");
        System.out.println("✅ Test thành công: 9:00 - 10:00 = " + workingHours + " giờ (chính xác 1 giờ)");
    }

    @Test
    @DisplayName("Tính số giờ làm việc - Trường hợp làm việc với phút lẻ (7 giờ 45 phút)")
    void testCalculateWorkingHours_OddMinutes() {
        // Setup: Tạo dữ liệu test với giờ check-in 8:15 và check-out 16:00
        StaffAttendanceLog log = new StaffAttendanceLog();
        log.setCheckInTime(LocalTime.of(8, 15)); // 8:15 sáng
        log.setCheckOutTime(LocalTime.of(16, 0)); // 4:00 chiều

        // Run: Gọi hàm tính toán số giờ làm việc
        double workingHours = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateWorkingHours", 
            log
        );

        // Assert: Kiểm tra kết quả (7 giờ 45 phút = 7.75 giờ)
        assertEquals(7.75, workingHours, 0.01, "Số giờ làm việc phải là 7.75 giờ (7 giờ 45 phút)");
        System.out.println("✅ Test thành công: 8:15 - 16:00 = " + workingHours + " giờ (7 giờ 45 phút)");
    }
}
