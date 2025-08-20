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
 * Unit test cho hàm tính khoảng cách Haversine trong AttendanceVerificationService
 * Test hàm calculateDistance sử dụng công thức Haversine để tính khoảng cách giữa 2 tọa độ
 */
@DisplayName("Haversine Distance Calculation Tests")
class HaversineDistanceTest {

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
    @DisplayName("Tính khoảng cách - Hai điểm trùng nhau (0 mét)")
    void testCalculateDistance_SamePoint() {
        // Setup: Tạo tọa độ của 2 điểm giống hệt nhau
        double lat1 = 10.762622;  // Tọa độ Hồ Chí Minh
        double lon1 = 106.660172;
        double lat2 = 10.762622;  // Cùng tọa độ
        double lon2 = 106.660172;

        // Run: Gọi hàm tính khoảng cách
        double distance = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateDistance", 
            lat1, lon1, lat2, lon2
        );

        // Assert: Kiểm tra kết quả
        assertEquals(0.0, distance, 1.0, "Khoảng cách giữa 2 điểm trùng nhau phải là 0 mét");
        System.out.println("✅ Test thành công: Khoảng cách giữa 2 điểm trùng nhau = " + distance + " mét");
    }

    @Test
    @DisplayName("Tính khoảng cách - Hai điểm rất gần nhau (vài mét)")
    void testCalculateDistance_VeryClosePoints() {
        // Setup: Tạo tọa độ của 2 điểm rất gần nhau (cách nhau khoảng 10 mét)
        double lat1 = 10.762622;  // Tọa độ Hồ Chí Minh
        double lon1 = 106.660172;
        double lat2 = 10.762632;  // Tọa độ cách đó khoảng 10 mét
        double lon2 = 106.660172;

        // Run: Gọi hàm tính khoảng cách
        double distance = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateDistance", 
            lat1, lon1, lat2, lon2
        );

        // Assert: Kiểm tra kết quả (khoảng 10-15 mét)
        assertTrue(distance > 0, "Khoảng cách phải lớn hơn 0");
        assertTrue(distance < 20, "Khoảng cách phải nhỏ hơn 20 mét");
        System.out.println("✅ Test thành công: Khoảng cách giữa 2 điểm gần nhau = " + distance + " mét");
    }

    @Test
    @DisplayName("Tính khoảng cách - Hai điểm trong cùng thành phố (vài km)")
    void testCalculateDistance_SameCity() {
        // Setup: Tạo tọa độ của 2 điểm trong Hồ Chí Minh
        double lat1 = 10.762622;  // Quận 1, Hồ Chí Minh
        double lon1 = 106.660172;
        double lat2 = 10.7769;    // Quận 3, Hồ Chí Minh
        double lon2 = 106.7009;

        // Run: Gọi hàm tính khoảng cách
        double distance = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateDistance", 
            lat1, lon1, lat2, lon2
        );

        // Assert: Kiểm tra kết quả (khoảng 5-8 km)
        assertTrue(distance > 4000, "Khoảng cách phải lớn hơn 4 km");
        assertTrue(distance < 10000, "Khoảng cách phải nhỏ hơn 10 km");
        System.out.println("✅ Test thành công: Khoảng cách trong cùng thành phố = " + distance + " mét (" + (distance/1000) + " km)");
    }

    @Test
    @DisplayName("Tính khoảng cách - Hai thành phố khác nhau (Hà Nội - Hồ Chí Minh)")
    void testCalculateDistance_DifferentCities() {
        // Setup: Tạo tọa độ của Hà Nội và Hồ Chí Minh
        double lat1 = 21.0285;    // Hà Nội
        double lon1 = 105.8542;
        double lat2 = 10.762622;  // Hồ Chí Minh
        double lon2 = 106.660172;

        // Run: Gọi hàm tính khoảng cách
        double distance = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateDistance", 
            lat1, lon1, lat2, lon2
        );

        // Assert: Kiểm tra kết quả (khoảng 1100-1200 km)
        assertTrue(distance > 1100000, "Khoảng cách phải lớn hơn 1100 km");
        assertTrue(distance < 1300000, "Khoảng cách phải nhỏ hơn 1300 km");
        System.out.println("✅ Test thành công: Khoảng cách Hà Nội - Hồ Chí Minh = " + distance + " mét (" + (distance/1000) + " km)");
    }

    @Test
    @DisplayName("Tính khoảng cách - Hai điểm trên đường xích đạo")
    void testCalculateDistance_Equator() {
        // Setup: Tạo tọa độ của 2 điểm trên đường xích đạo (lat = 0)
        double lat1 = 0.0;        // Điểm 1 trên xích đạo
        double lon1 = 0.0;        // Kinh tuyến gốc
        double lat2 = 0.0;        // Điểm 2 trên xích đạo
        double lon2 = 1.0;        // Kinh tuyến 1 độ

        // Run: Gọi hàm tính khoảng cách
        double distance = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateDistance", 
            lat1, lon1, lat2, lon2
        );

        // Assert: Kiểm tra kết quả (1 độ kinh tuyến trên xích đạo ≈ 111 km)
        assertTrue(distance > 110000, "Khoảng cách phải lớn hơn 110 km");
        assertTrue(distance < 112000, "Khoảng cách phải nhỏ hơn 112 km");
        System.out.println("✅ Test thành công: Khoảng cách 1 độ kinh tuyến trên xích đạo = " + distance + " mét (" + (distance/1000) + " km)");
    }

    @Test
    @DisplayName("Tính khoảng cách - Hai điểm trên cùng kinh tuyến")
    void testCalculateDistance_SameMeridian() {
        // Setup: Tạo tọa độ của 2 điểm trên cùng kinh tuyến (lon = 106.660172)
        double lat1 = 10.762622;  // Hồ Chí Minh
        double lon1 = 106.660172;
        double lat2 = 11.762622;  // Điểm cách đó 1 độ vĩ tuyến về phía bắc
        double lon2 = 106.660172; // Cùng kinh tuyến

        // Run: Gọi hàm tính khoảng cách
        double distance = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateDistance", 
            lat1, lon1, lat2, lon2
        );

        // Assert: Kiểm tra kết quả (1 độ vĩ tuyến ≈ 111 km)
        assertTrue(distance > 110000, "Khoảng cách phải lớn hơn 110 km");
        assertTrue(distance < 112000, "Khoảng cách phải nhỏ hơn 112 km");
        System.out.println("✅ Test thành công: Khoảng cách 1 độ vĩ tuyến = " + distance + " mét (" + (distance/1000) + " km)");
    }

    @Test
    @DisplayName("Tính khoảng cách - Hai điểm đối diện qua trái đất")
    void testCalculateDistance_AntipodalPoints() {
        // Setup: Tạo tọa độ của 2 điểm đối diện qua trái đất
        double lat1 = 10.762622;  // Hồ Chí Minh
        double lon1 = 106.660172;
        double lat2 = -10.762622; // Điểm đối diện (vĩ tuyến âm)
        double lon2 = 106.660172 + 180; // Kinh tuyến đối diện

        // Run: Gọi hàm tính khoảng cách
        double distance = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateDistance", 
            lat1, lon1, lat2, lon2
        );

        // Assert: Kiểm tra kết quả (khoảng 20000 km - nửa chu vi trái đất)
        assertTrue(distance > 19000000, "Khoảng cách phải lớn hơn 19000 km");
        assertTrue(distance < 21000000, "Khoảng cách phải nhỏ hơn 21000 km");
        System.out.println("✅ Test thành công: Khoảng cách giữa 2 điểm đối diện = " + distance + " mét (" + (distance/1000) + " km)");
    }

    @Test
    @DisplayName("Tính khoảng cách - Hai điểm với tọa độ âm")
    void testCalculateDistance_NegativeCoordinates() {
        // Setup: Tạo tọa độ của 2 điểm với vĩ tuyến âm
        double lat1 = -33.8688;   // Sydney, Australia
        double lon1 = 151.2093;
        double lat2 = -37.8136;   // Melbourne, Australia
        double lon2 = 144.9631;

        // Run: Gọi hàm tính khoảng cách
        double distance = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateDistance", 
            lat1, lon1, lat2, lon2
        );

        // Assert: Kiểm tra kết quả (khoảng 700-800 km)
        assertTrue(distance > 700000, "Khoảng cách phải lớn hơn 700 km");
        assertTrue(distance < 900000, "Khoảng cách phải nhỏ hơn 900 km");
        System.out.println("✅ Test thành công: Khoảng cách Sydney - Melbourne = " + distance + " mét (" + (distance/1000) + " km)");
    }

    @Test
    @DisplayName("Tính khoảng cách - Hai điểm với tọa độ rất lớn")
    void testCalculateDistance_LargeCoordinates() {
        // Setup: Tạo tọa độ của 2 điểm với vĩ tuyến rất lớn
        double lat1 = 89.0;       // Gần cực bắc
        double lon1 = 0.0;
        double lat2 = 89.1;       // Điểm cách đó 0.1 độ
        double lon2 = 0.0;

        // Run: Gọi hàm tính khoảng cách
        double distance = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateDistance", 
            lat1, lon1, lat2, lon2
        );

        // Assert: Kiểm tra kết quả (gần cực, 0.1 độ vĩ tuyến ≈ 11 km)
        assertTrue(distance > 10000, "Khoảng cách phải lớn hơn 10 km");
        assertTrue(distance < 12000, "Khoảng cách phải nhỏ hơn 12 km");
        System.out.println("✅ Test thành công: Khoảng cách gần cực bắc = " + distance + " mét (" + (distance/1000) + " km)");
    }

    @Test
    @DisplayName("Tính khoảng cách - Kiểm tra tính đối xứng")
    void testCalculateDistance_Symmetry() {
        // Setup: Tạo tọa độ của 2 điểm
        double lat1 = 10.762622;  // Hồ Chí Minh
        double lon1 = 106.660172;
        double lat2 = 21.0285;    // Hà Nội
        double lon2 = 105.8542;

        // Run: Gọi hàm tính khoảng cách theo 2 chiều
        double distance1 = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateDistance", 
            lat1, lon1, lat2, lon2
        );
        
        double distance2 = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateDistance", 
            lat2, lon2, lat1, lon1
        );

        // Assert: Kiểm tra tính đối xứng (khoảng cách A->B = B->A)
        assertEquals(distance1, distance2, 0.1, "Khoảng cách phải có tính đối xứng");
        System.out.println("✅ Test thành công: Tính đối xứng - A→B = " + distance1 + "m, B→A = " + distance2 + "m");
    }

    @Test
    @DisplayName("Tính khoảng cách - Kiểm tra tính bắc cầu")
    void testCalculateDistance_Transitivity() {
        // Setup: Tạo tọa độ của 3 điểm A, B, C
        double latA = 10.762622;  // Hồ Chí Minh
        double lonA = 106.660172;
        double latB = 15.8700;    // Đà Nẵng
        double lonB = 108.2200;
        double latC = 21.0285;    // Hà Nội
        double lonC = 105.8542;

        // Run: Gọi hàm tính khoảng cách giữa các cặp điểm
        double distanceAB = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateDistance", 
            latA, lonA, latB, lonB
        );
        
        double distanceBC = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateDistance", 
            latB, lonB, latC, lonC
        );
        
        double distanceAC = ReflectionTestUtils.invokeMethod(
            attendanceVerificationService, 
            "calculateDistance", 
            latA, lonA, latC, lonC
        );

        // Assert: Kiểm tra bất đẳng thức tam giác (AB + BC >= AC)
        assertTrue(distanceAB + distanceBC >= distanceAC, 
            "Tổng khoảng cách AB + BC phải lớn hơn hoặc bằng AC");
        System.out.println("✅ Test thành công: Bất đẳng thức tam giác - AB(" + (distanceAB/1000) + "km) + BC(" + (distanceBC/1000) + "km) >= AC(" + (distanceAC/1000) + "km)");
    }
}
