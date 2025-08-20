package com.classroomapp.classroombackend.services;

import com.classroomapp.classroombackend.service.impl.hrmanagement.ViolationDetectionServiceImpl;
import com.classroomapp.classroombackend.repository.hrmanagement.AttendanceViolationRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.StaffAttendanceLogRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.UserShiftAssignmentRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.ViolationExplanationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho hàm tính chênh lệch phút giữa hai thời điểm trong ViolationDetectionServiceImpl.
 * Test private method: calculateDeviationMinutes(LocalTime expected, LocalTime actual)
 */
@DisplayName("Violation Deviation Minutes Tests")
class ViolationDeviationMinutesTest {

	@InjectMocks
	private ViolationDetectionServiceImpl violationDetectionService;

	@Mock private AttendanceViolationRepository violationRepository;
	@Mock private StaffAttendanceLogRepository attendanceLogRepository;
	@Mock private UserShiftAssignmentRepository shiftAssignmentRepository;
	@Mock private UserRepository userRepository;
	@Mock private ViolationExplanationRepository violationExplanationRepository;

	@BeforeEach
	void setUp() {
		// Setup: Khởi tạo Mockito
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Cùng thời điểm → 0 phút")
	void testSameTime() {
		// Setup: 08:00 và 08:00
		LocalTime expected = LocalTime.of(8, 0);
		LocalTime actual = LocalTime.of(8, 0);

		// Run: Gọi hàm tính chênh lệch phút
		int diff = ReflectionTestUtils.invokeMethod(violationDetectionService, "calculateDeviationMinutes", expected, actual);

		// Assert: Kết quả là 0 phút
		assertEquals(0, diff, "Cùng thời điểm phải chênh lệch 0 phút");
		System.out.println("✅ Cùng thời điểm → " + diff + " phút");
	}

	@Test
	@DisplayName("08:30 và 08:40 → 10 phút")
	void testTenMinutesDifference() {
		// Setup: 08:30 và 08:40
		LocalTime expected = LocalTime.of(8, 30);
		LocalTime actual = LocalTime.of(8, 40);

		// Run
		int diff = ReflectionTestUtils.invokeMethod(violationDetectionService, "calculateDeviationMinutes", expected, actual);

		// Assert
		assertEquals(10, diff, "Chênh lệch phải là 10 phút");
		System.out.println("✅ 08:30 và 08:40 → " + diff + " phút");
	}

	@Test
	@DisplayName("09:00 và 08:30 (đảo chiều) → 30 phút")
	void testReverseOrderAbsoluteDifference() {
		// Setup: 09:00 và 08:30
		LocalTime expected = LocalTime.of(9, 0);
		LocalTime actual = LocalTime.of(8, 30);

		// Run
		int diff = ReflectionTestUtils.invokeMethod(violationDetectionService, "calculateDeviationMinutes", expected, actual);

		// Assert: Tuyệt đối → 30 phút
		assertEquals(30, diff, "Chênh lệch tuyệt đối phải là 30 phút");
		System.out.println("✅ 09:00 và 08:30 → " + diff + " phút (tuyệt đối)");
	}

	@Test
	@DisplayName("08:00 và 18:30 → 630 phút")
	void testLargeDifference() {
		// Setup: 08:00 và 18:30
		LocalTime expected = LocalTime.of(8, 0);
		LocalTime actual = LocalTime.of(18, 30);

		// Run
		int diff = ReflectionTestUtils.invokeMethod(violationDetectionService, "calculateDeviationMinutes", expected, actual);

		// Assert
		assertEquals(630, diff, "Chênh lệch phải là 630 phút (10.5 giờ)");
		System.out.println("✅ 08:00 và 18:30 → " + diff + " phút");
	}

	@Test
	@DisplayName("Qua nửa đêm: 23:50 và 00:10 → 1420 phút (theo triển khai hiện tại)")
	void testAcrossMidnightAbsoluteDifference() {
		// Setup: 23:50 và 00:10 (cùng ngày logic toSecondOfDay, không wrap-around)
		LocalTime expected = LocalTime.of(23, 50);
		LocalTime actual = LocalTime.of(0, 10);

		// Run
		int diff = ReflectionTestUtils.invokeMethod(violationDetectionService, "calculateDeviationMinutes", expected, actual);

		// Assert: Theo triển khai hiện tại dùng |secondsA - secondsB| / 60 → 1420 phút
		assertEquals(1420, diff, "Theo logic hiện tại, chênh lệch tuyệt đối theo giây cho ra 1420 phút");
		System.out.println("✅ 23:50 và 00:10 → " + diff + " phút (logic tuyệt đối theo ngày)");
	}

	@Test
	@DisplayName("Có giây lẻ: 08:00:30 và 08:01:29 → 0 phút (làm tròn xuống)")
	void testOddSecondsFloorDivision() {
		// Setup: 08:00:30 và 08:01:29 (59 giây)
		LocalTime expected = LocalTime.of(8, 0, 30);
		LocalTime actual = LocalTime.of(8, 1, 29);

		// Run
		int diff = ReflectionTestUtils.invokeMethod(violationDetectionService, "calculateDeviationMinutes", expected, actual);

		// Assert: 59 giây / 60 → 0 (làm tròn xuống)
		assertEquals(0, diff, "59 giây chênh lệch phải tính 0 phút do chia nguyên");
		System.out.println("✅ 08:00:30 và 08:01:29 → " + diff + " phút (chia nguyên)");
	}

	@Test
	@DisplayName("Thiếu expected (null) → 0 phút")
	void testNullExpected() {
		// Setup: expected null, actual 08:00
		LocalTime expected = null;
		LocalTime actual = LocalTime.of(8, 0);

		// Run
		int diff = ReflectionTestUtils.invokeMethod(violationDetectionService, "calculateDeviationMinutes", expected, actual);

		// Assert
		assertEquals(0, diff, "Thiếu expected phải trả 0 phút");
		System.out.println("✅ expected=null → " + diff + " phút");
	}

	@Test
	@DisplayName("Thiếu actual (null) → 0 phút")
	void testNullActual() {
		// Setup: expected 08:00, actual null
		LocalTime expected = LocalTime.of(8, 0);
		LocalTime actual = null;

		// Run
		int diff = ReflectionTestUtils.invokeMethod(violationDetectionService, "calculateDeviationMinutes", expected, actual);

		// Assert
		assertEquals(0, diff, "Thiếu actual phải trả 0 phút");
		System.out.println("✅ actual=null → " + diff + " phút");
	}

	@Test
	@DisplayName("Cả hai null → 0 phút")
	void testBothNull() {
		// Setup: expected null, actual null
		LocalTime expected = null;
		LocalTime actual = null;

		// Run
		int diff = ReflectionTestUtils.invokeMethod(violationDetectionService, "calculateDeviationMinutes", expected, actual);

		// Assert
		assertEquals(0, diff, "Cả hai null phải trả 0 phút");
		System.out.println("✅ expected=null, actual=null → " + diff + " phút");
	}
}
