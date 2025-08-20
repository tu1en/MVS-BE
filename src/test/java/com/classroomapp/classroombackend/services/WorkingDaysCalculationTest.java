package com.classroomapp.classroombackend.services;

import com.classroomapp.classroombackend.repository.absencemanagement.AbsenceRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.impl.AbsenceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test cho hàm đếm số ngày làm việc (loại trừ cuối tuần) trong AbsenceServiceImpl.
 * Test private method: calculateWorkingDays(LocalDate startDate, LocalDate endDate)
 */
@DisplayName("Absence Working Days Calculation Tests")
class WorkingDaysCalculationTest {

	@InjectMocks
	private AbsenceServiceImpl absenceService;

	@Mock
	private AbsenceRepository absenceRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ModelMapper modelMapper;

	@BeforeEach
	void setUp() {
		// Setup: Khởi tạo Mockito và service
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("Một ngày làm việc (thứ 2) → 1 ngày")
	void testSingleWeekday() {
		// Setup: 2025-03-03 là thứ 2
		LocalDate start = LocalDate.of(2025, 3, 3);
		LocalDate end = LocalDate.of(2025, 3, 3);

		// Run: Gọi hàm tính số ngày làm việc
		long days = ReflectionTestUtils.invokeMethod(absenceService, "calculateWorkingDays", start, end);

		// Assert: Kết quả phải là 1
		assertEquals(1L, days, "Thứ 2 đơn lẻ phải tính 1 ngày làm việc");
		System.out.println("✅ Một ngày làm việc (thứ 2) → " + days + " ngày");
	}

	@Test
	@DisplayName("Một ngày cuối tuần (thứ 7) → 0 ngày")
	void testSingleWeekendSaturday() {
		// Setup: 2025-03-08 là thứ 7
		LocalDate start = LocalDate.of(2025, 3, 8);
		LocalDate end = LocalDate.of(2025, 3, 8);

		// Run
		long days = ReflectionTestUtils.invokeMethod(absenceService, "calculateWorkingDays", start, end);

		// Assert
		assertEquals(0L, days, "Thứ 7 đơn lẻ phải tính 0 ngày làm việc");
		System.out.println("✅ Một ngày cuối tuần (thứ 7) → " + days + " ngày");
	}

	@Test
	@DisplayName("Hai ngày cuối tuần (thứ 7 - CN) → 0 ngày")
	void testWeekendOnlyTwoDays() {
		// Setup: 2025-03-08 (thứ 7) đến 2025-03-09 (CN)
		LocalDate start = LocalDate.of(2025, 3, 8);
		LocalDate end = LocalDate.of(2025, 3, 9);

		// Run
		long days = ReflectionTestUtils.invokeMethod(absenceService, "calculateWorkingDays", start, end);

		// Assert
		assertEquals(0L, days, "Thứ 7 & CN phải tính 0 ngày làm việc");
		System.out.println("✅ Hai ngày cuối tuần (thứ 7 - CN) → " + days + " ngày");
	}

	@Test
	@DisplayName("Một tuần đầy đủ (thứ 2 → CN) → 5 ngày")
	void testFullWeekMonToSun() {
		// Setup: 2025-03-03 (T2) → 2025-03-09 (CN)
		LocalDate start = LocalDate.of(2025, 3, 3);
		LocalDate end = LocalDate.of(2025, 3, 9);

		// Run
		long days = ReflectionTestUtils.invokeMethod(absenceService, "calculateWorkingDays", start, end);

		// Assert: 5 ngày làm việc
		assertEquals(5L, days, "Một tuần đủ phải có 5 ngày làm việc");
		System.out.println("✅ Một tuần đầy đủ (T2→CN) → " + days + " ngày");
	}

	@Test
	@DisplayName("Trong tuần (thứ 2 → thứ 6) → 5 ngày")
	void testMonToFri() {
		// Setup: 2025-03-03 (T2) → 2025-03-07 (T6)
		LocalDate start = LocalDate.of(2025, 3, 3);
		LocalDate end = LocalDate.of(2025, 3, 7);

		// Run
		long days = ReflectionTestUtils.invokeMethod(absenceService, "calculateWorkingDays", start, end);

		// Assert
		assertEquals(5L, days, "Thứ 2 đến thứ 6 phải là 5 ngày làm việc");
		System.out.println("✅ Trong tuần (T2→T6) → " + days + " ngày");
	}

	@Test
	@DisplayName("Vượt qua cuối tuần (thứ 6 → thứ 2) → 2 ngày")
	void testFriToMonAcrossWeekend() {
		// Setup: 2025-03-07 (T6) → 2025-03-10 (T2)
		LocalDate start = LocalDate.of(2025, 3, 7);
		LocalDate end = LocalDate.of(2025, 3, 10);

		// Run
		long days = ReflectionTestUtils.invokeMethod(absenceService, "calculateWorkingDays", start, end);

		// Assert: T6 và T2 = 2 ngày
		assertEquals(2L, days, "Thứ 6 đến thứ 2 chỉ tính 2 ngày làm việc");
		System.out.println("✅ Vượt qua cuối tuần (T6→T2) → " + days + " ngày");
	}

	@Test
	@DisplayName("Hai tuần kế tiếp (T2 tuần 1 → T6 tuần 2) → 10 ngày")
	void testTwoWeeksMonToNextFri() {
		// Setup: 2025-03-03 (T2) → 2025-03-14 (T6)
		LocalDate start = LocalDate.of(2025, 3, 3);
		LocalDate end = LocalDate.of(2025, 3, 14);

		// Run
		long days = ReflectionTestUtils.invokeMethod(absenceService, "calculateWorkingDays", start, end);

		// Assert: 10 ngày làm việc (5 + 5)
		assertEquals(10L, days, "Hai tuần làm việc liên tiếp phải là 10 ngày");
		System.out.println("✅ Hai tuần kế tiếp (T2→T6) → " + days + " ngày");
	}

	@Test
	@DisplayName("Cuối năm → đầu năm (30/12/2024 → 05/01/2025) → 5 ngày")
	void testYearBoundary() {
		// Setup: 2024-12-30 (T2) → 2025-01-05 (CN)
		LocalDate start = LocalDate.of(2024, 12, 30);
		LocalDate end = LocalDate.of(2025, 1, 5);

		// Run
		long days = ReflectionTestUtils.invokeMethod(absenceService, "calculateWorkingDays", start, end);

		// Assert: 5 ngày làm việc
		assertEquals(5L, days, "Tuần giao năm vẫn phải có 5 ngày làm việc");
		System.out.println("✅ Cuối năm → đầu năm (30/12/2024→05/01/2025) → " + days + " ngày");
	}

	@Test
	@DisplayName("Cả tháng 2/2024 (năm nhuận) → 21 ngày làm")
	void testLeapYearFebruary2024() {
		// Setup: Tháng 2 năm 2024 có 29 ngày; có 4 cuối tuần (8 ngày nghỉ) → 29-8=21
		LocalDate start = LocalDate.of(2024, 2, 1);
		LocalDate end = LocalDate.of(2024, 2, 29);

		// Run
		long days = ReflectionTestUtils.invokeMethod(absenceService, "calculateWorkingDays", start, end);

		// Assert: 21 ngày làm việc
		assertEquals(21L, days, "Tháng 2/2024 phải có 21 ngày làm việc");
		System.out.println("✅ Cả tháng 2/2024 (năm nhuận) → " + days + " ngày");
	}
}
