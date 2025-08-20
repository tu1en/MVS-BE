package com.classroomapp.classroombackend.services;

import com.classroomapp.classroombackend.service.AttendanceVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho hàm kiểm tra IP thuộc CIDR range trong AttendanceVerificationService
 * Test hàm isIpInRange để xác minh IP có thuộc mạng được phép hay không
 */
@DisplayName("IP Range Validation Tests")
class IpRangeValidationTest {

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
    @DisplayName("Kiểm tra IP - CIDR wildcard cho phép tất cả (0.0.0.0/0)")
    void testIsIpInRange_WildcardAllowAll() {
        // Setup: CIDR cho phép tất cả IP
        String ip = "192.168.1.100";
        String cidr = "0.0.0.0/0";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertTrue(result, "IP 192.168.1.100 phải thuộc CIDR 0.0.0.0/0");
        System.out.println("✅ Test thành công: IP " + ip + " thuộc CIDR " + cidr + " (cho phép tất cả)");
    }

    @Test
    @DisplayName("Kiểm tra IP - CIDR /24 cho mạng nội bộ")
    void testIsIpInRange_ClassCNetwork() {
        // Setup: CIDR /24 cho mạng 192.168.1.0/24
        String ip = "192.168.1.100";
        String cidr = "192.168.1.0/24";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertTrue(result, "IP 192.168.1.100 phải thuộc CIDR 192.168.1.0/24");
        System.out.println("✅ Test thành công: IP " + ip + " thuộc CIDR " + cidr + " (mạng /24)");
    }

    @Test
    @DisplayName("Kiểm tra IP - CIDR /16 cho mạng lớn hơn")
    void testIsIpInRange_ClassBNetwork() {
        // Setup: CIDR /16 cho mạng 10.0.0.0/16
        String ip = "10.5.100.200";
        String cidr = "10.0.0.0/16";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertTrue(result, "IP 10.5.100.200 phải thuộc CIDR 10.0.0.0/16");
        System.out.println("✅ Test thành công: IP " + ip + " thuộc CIDR " + cidr + " (mạng /16)");
    }

    @Test
    @DisplayName("Kiểm tra IP - CIDR /8 cho mạng rất lớn")
    void testIsIpInRange_ClassANetwork() {
        // Setup: CIDR /8 cho mạng 172.0.0.0/8
        String ip = "172.16.25.50";
        String cidr = "172.0.0.0/8";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertTrue(result, "IP 172.16.25.50 phải thuộc CIDR 172.0.0.0/8");
        System.out.println("✅ Test thành công: IP " + ip + " thuộc CIDR " + cidr + " (mạng /8)");
    }

    @Test
    @DisplayName("Kiểm tra IP - CIDR /30 cho mạng nhỏ (4 IP)")
    void testIsIpInRange_SmallNetwork() {
        // Setup: CIDR /30 cho mạng 192.168.1.0/30 (chỉ 4 IP: 192.168.1.0-3)
        String ip = "192.168.1.2";
        String cidr = "192.168.1.0/30";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertTrue(result, "IP 192.168.1.2 phải thuộc CIDR 192.168.1.0/30");
        System.out.println("✅ Test thành công: IP " + ip + " thuộc CIDR " + cidr + " (mạng /30)");
    }

    @Test
    @DisplayName("Kiểm tra IP - CIDR /32 cho IP cụ thể")
    void testIsIpInRange_SingleIP() {
        // Setup: CIDR /32 cho IP cụ thể 203.162.0.1/32
        String ip = "203.162.0.1";
        String cidr = "203.162.0.1/32";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertTrue(result, "IP 203.162.0.1 phải thuộc CIDR 203.162.0.1/32");
        System.out.println("✅ Test thành công: IP " + ip + " thuộc CIDR " + cidr + " (IP cụ thể)");
    }

    @Test
    @DisplayName("Kiểm tra IP - CIDR với prefix length không chia hết cho 8")
    void testIsIpInRange_NonByteAlignedPrefix() {
        // Setup: CIDR /26 cho mạng 192.168.1.0/26 (64 IP)
        String ip = "192.168.1.65";
        String cidr = "192.168.1.0/26";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertTrue(result, "IP 192.168.1.65 phải thuộc CIDR 192.168.1.0/26");
        System.out.println("✅ Test thành công: IP " + ip + " thuộc CIDR " + cidr + " (prefix /26)");
    }

    @Test
    @DisplayName("Kiểm tra IP - CIDR với prefix length 25")
    void testIsIpInRange_Prefix25() {
        // Setup: CIDR /25 cho mạng 10.0.0.0/25 (128 IP)
        String ip = "10.0.0.127";
        String cidr = "10.0.0.0/25";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertTrue(result, "IP 10.0.0.127 phải thuộc CIDR 10.0.0.0/25");
        System.out.println("✅ Test thành công: IP " + ip + " thuộc CIDR " + cidr + " (prefix /25)");
    }

    @Test
    @DisplayName("Kiểm tra IP - IP không thuộc mạng /24")
    void testIsIpInRange_IPNotInClassC() {
        // Setup: CIDR /24 cho mạng 192.168.1.0/24, nhưng IP thuộc mạng khác
        String ip = "192.168.2.100";
        String cidr = "192.168.1.0/24";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertFalse(result, "IP 192.168.2.100 không được thuộc CIDR 192.168.1.0/24");
        System.out.println("✅ Test thành công: IP " + ip + " không thuộc CIDR " + cidr + " (IP ngoài mạng)");
    }

    @Test
    @DisplayName("Kiểm tra IP - IP không thuộc mạng /16")
    void testIsIpInRange_IPNotInClassB() {
        // Setup: CIDR /16 cho mạng 10.0.0.0/16, nhưng IP thuộc mạng khác
        String ip = "11.0.0.1";
        String cidr = "10.0.0.0/16";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertFalse(result, "IP 11.0.0.1 không được thuộc CIDR 10.0.0.0/16");
        System.out.println("✅ Test thành công: IP " + ip + " không thuộc CIDR " + cidr + " (IP ngoài mạng)");
    }

    @Test
    @DisplayName("Kiểm tra IP - IP không thuộc mạng /30")
    void testIsIpInRange_IPNotInSmallNetwork() {
        // Setup: CIDR /30 cho mạng 192.168.1.0/30, nhưng IP ngoài range
        String ip = "192.168.1.5";
        String cidr = "192.168.1.0/30";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertFalse(result, "IP 192.168.1.5 không được thuộc CIDR 192.168.1.0/30");
        System.out.println("✅ Test thành công: IP " + ip + " không thuộc CIDR " + cidr + " (IP ngoài range)");
    }

    @Test
    @DisplayName("Kiểm tra IP - IP không thuộc mạng /26")
    void testIsIpInRange_IPNotInPrefix26() {
        // Setup: CIDR /26 cho mạng 192.168.1.0/26, nhưng IP ngoài range
        String ip = "192.168.1.200";
        String cidr = "192.168.1.0/26";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertFalse(result, "IP 192.168.1.200 không được thuộc CIDR 192.168.1.0/26");
        System.out.println("✅ Test thành công: IP " + ip + " không thuộc CIDR " + cidr + " (IP ngoài range /26)");
    }

    @Test
    @DisplayName("Kiểm tra IP - IP không thuộc mạng /25")
    void testIsIpInRange_IPNotInPrefix25() {
        // Setup: CIDR /25 cho mạng 10.0.0.0/25, nhưng IP ngoài range
        String ip = "10.0.0.200";
        String cidr = "10.0.0.0/25";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertFalse(result, "IP 10.0.0.200 không được thuộc CIDR 10.0.0.0/25");
        System.out.println("✅ Test thành công: IP " + ip + " không thuộc CIDR " + cidr + " (IP ngoài range /25)");
    }

    @Test
    @DisplayName("Kiểm tra IP - IP không thuộc mạng /32")
    void testIsIpInRange_IPNotInSingleIP() {
        // Setup: CIDR /32 cho IP cụ thể, nhưng IP khác
        String ip = "203.162.0.2";
        String cidr = "203.162.0.1/32";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertFalse(result, "IP 203.162.0.2 không được thuộc CIDR 203.162.0.1/32");
        System.out.println("✅ Test thành công: IP " + ip + " không thuộc CIDR " + cidr + " (IP khác với /32)");
    }

    @Test
    @DisplayName("Kiểm tra IP - IP thuộc biên mạng /24")
    void testIsIpInRange_IPAtClassCBoundary() {
        // Setup: CIDR /24 cho mạng 192.168.1.0/24, IP ở biên
        String ip = "192.168.1.255";
        String cidr = "192.168.1.0/24";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertTrue(result, "IP 192.168.1.255 phải thuộc CIDR 192.168.1.0/24 (IP broadcast)");
        System.out.println("✅ Test thành công: IP " + ip + " thuộc CIDR " + cidr + " (IP ở biên mạng)");
    }

    @Test
    @DisplayName("Kiểm tra IP - IP thuộc biên mạng /16")
    void testIsIpInRange_IPAtClassBBoundary() {
        // Setup: CIDR /16 cho mạng 10.0.0.0/16, IP ở biên
        String ip = "10.0.255.255";
        String cidr = "10.0.0.0/16";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertTrue(result, "IP 10.0.255.255 phải thuộc CIDR 10.0.0.0/16 (IP broadcast)");
        System.out.println("✅ Test thành công: IP " + ip + " thuộc CIDR " + cidr + " (IP ở biên mạng)");
    }

    @Test
    @DisplayName("Kiểm tra IP - IP thuộc biên mạng /8")
    void testIsIpInRange_IPAtClassABoundary() {
        // Setup: CIDR /8 cho mạng 172.0.0.0/8, IP ở biên
        String ip = "172.255.255.255";
        String cidr = "172.0.0.0/8";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả
        assertTrue(result, "IP 172.255.255.255 phải thuộc CIDR 172.0.0.0/8 (IP broadcast)");
        System.out.println("✅ Test thành công: IP " + ip + " thuộc CIDR " + cidr + " (IP ở biên mạng)");
    }

    @Test
    @DisplayName("Kiểm tra IP - CIDR không hợp lệ (sai format)")
    void testIsIpInRange_InvalidCIDRFormat() {
        // Setup: CIDR không hợp lệ
        String ip = "192.168.1.100";
        String cidr = "192.168.1.0/33"; // Prefix length > 32

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả (phải trả về false khi có lỗi)
        assertFalse(result, "Hàm phải trả về false khi CIDR không hợp lệ");
        System.out.println("✅ Test thành công: CIDR " + cidr + " không hợp lệ → trả về false");
    }

    @Test
    @DisplayName("Kiểm tra IP - IP không hợp lệ")
    void testIsIpInRange_InvalidIP() {
        // Setup: IP không hợp lệ
        String ip = "999.999.999.999";
        String cidr = "192.168.1.0/24";

        // Run: Gọi hàm kiểm tra IP có thuộc CIDR không
        boolean result = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "isIpInRange", 
            ip, cidr
        );

        // Assert: Kiểm tra kết quả (phải trả về false khi có lỗi)
        assertFalse(result, "Hàm phải trả về false khi IP không hợp lệ");
        System.out.println("✅ Test thành công: IP " + ip + " không hợp lệ → trả về false");
    }
}
