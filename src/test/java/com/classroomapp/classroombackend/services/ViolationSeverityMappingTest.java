package com.classroomapp.classroombackend.services;

import com.classroomapp.classroombackend.model.hrmanagement.AttendanceViolation;
import com.classroomapp.classroombackend.repository.hrmanagement.AttendanceViolationRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.StaffAttendanceLogRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.UserShiftAssignmentRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.ViolationExplanationRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.impl.hrmanagement.ViolationDetectionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for mapping deviation/type to violation severity in ViolationDetectionServiceImpl.
 * Method under test: calculateSeverity(AttendanceViolation.ViolationType, int)
 */
@DisplayName("Violation Severity Mapping Tests")
class ViolationSeverityMappingTest {

	@InjectMocks
	private ViolationDetectionServiceImpl violationDetectionService;

	@Mock private AttendanceViolationRepository violationRepository;
	@Mock private StaffAttendanceLogRepository attendanceLogRepository;
	@Mock private UserShiftAssignmentRepository shiftAssignmentRepository;
	@Mock private UserRepository userRepository;
	@Mock private ViolationExplanationRepository violationExplanationRepository;

	@BeforeEach
	void setUp() {
		// Setup: Initialize Mockito
		MockitoAnnotations.openMocks(this);
	}

	@Test
	@DisplayName("LATE_ARRIVAL: <=5 → MINOR; 6-15 → MODERATE; >15 → MAJOR")
	void testLateArrivalThresholds() {
		// Setup: Violation type LATE_ARRIVAL, test boundary conditions
		AttendanceViolation.ViolationType type = AttendanceViolation.ViolationType.LATE_ARRIVAL;

		// Run: Call calculateSeverity for multiple deviations
		AttendanceViolation.ViolationSeverity s0 = violationDetectionService.calculateSeverity(type, 0);
		AttendanceViolation.ViolationSeverity s5 = violationDetectionService.calculateSeverity(type, 5);
		AttendanceViolation.ViolationSeverity s6 = violationDetectionService.calculateSeverity(type, 6);
		AttendanceViolation.ViolationSeverity s15 = violationDetectionService.calculateSeverity(type, 15);
		AttendanceViolation.ViolationSeverity s16 = violationDetectionService.calculateSeverity(type, 16);
		AttendanceViolation.ViolationSeverity sNeg = violationDetectionService.calculateSeverity(type, -3);

		// Assert: Verify mapping
		assertEquals(AttendanceViolation.ViolationSeverity.MINOR, s0, "0 minutes should be MINOR");
		assertEquals(AttendanceViolation.ViolationSeverity.MINOR, s5, "5 minutes should be MINOR");
		assertEquals(AttendanceViolation.ViolationSeverity.MODERATE, s6, "6 minutes should be MODERATE");
		assertEquals(AttendanceViolation.ViolationSeverity.MODERATE, s15, "15 minutes should be MODERATE");
		assertEquals(AttendanceViolation.ViolationSeverity.MAJOR, s16, ">15 minutes should be MAJOR");
		assertEquals(AttendanceViolation.ViolationSeverity.MINOR, sNeg, "Negative minutes should be MINOR");
		System.out.println("✅ LATE_ARRIVAL thresholds mapped correctly (MINOR/MODERATE/MAJOR)");
	}

	@Test
	@DisplayName("EARLY_DEPARTURE: >10 → MODERATE; else → MINOR")
	void testEarlyDepartureThresholds() {
		// Setup: Violation type EARLY_DEPARTURE
		AttendanceViolation.ViolationType type = AttendanceViolation.ViolationType.EARLY_DEPARTURE;

		// Run: Call calculateSeverity for multiple deviations
		AttendanceViolation.ViolationSeverity s0 = violationDetectionService.calculateSeverity(type, 0);
		AttendanceViolation.ViolationSeverity s10 = violationDetectionService.calculateSeverity(type, 10);
		AttendanceViolation.ViolationSeverity s11 = violationDetectionService.calculateSeverity(type, 11);

		// Assert: Verify mapping
		assertEquals(AttendanceViolation.ViolationSeverity.MINOR, s0, "0 minutes should be MINOR");
		assertEquals(AttendanceViolation.ViolationSeverity.MINOR, s10, "10 minutes should be MINOR");
		assertEquals(AttendanceViolation.ViolationSeverity.MODERATE, s11, ">10 minutes should be MODERATE");
		System.out.println("✅ EARLY_DEPARTURE thresholds mapped correctly (MINOR/MODERATE)");
	}

	@Test
	@DisplayName("ABSENT_WITHOUT_LEAVE → CRITICAL")
	void testAbsentWithoutLeave() {
		// Setup: Violation type ABSENT_WITHOUT_LEAVE
		AttendanceViolation.ViolationType type = AttendanceViolation.ViolationType.ABSENT_WITHOUT_LEAVE;

		// Run: Any deviation should yield CRITICAL
		AttendanceViolation.ViolationSeverity s = violationDetectionService.calculateSeverity(type, 0);

		// Assert
		assertEquals(AttendanceViolation.ViolationSeverity.CRITICAL, s, "Absent without leave should be CRITICAL");
		System.out.println("✅ ABSENT_WITHOUT_LEAVE mapped to CRITICAL");
	}

	@Test
	@DisplayName("MISSING_CHECK_IN / MISSING_CHECK_OUT → MODERATE")
	void testMissingCheckInOut() {
		// Setup: Violation types MISSING_CHECK_IN and MISSING_CHECK_OUT
		AttendanceViolation.ViolationType typeIn = AttendanceViolation.ViolationType.MISSING_CHECK_IN;
		AttendanceViolation.ViolationType typeOut = AttendanceViolation.ViolationType.MISSING_CHECK_OUT;

		// Run
		AttendanceViolation.ViolationSeverity sIn = violationDetectionService.calculateSeverity(typeIn, 0);
		AttendanceViolation.ViolationSeverity sOut = violationDetectionService.calculateSeverity(typeOut, 0);

		// Assert: Both should be MODERATE
		assertEquals(AttendanceViolation.ViolationSeverity.MODERATE, sIn, "Missing check-in should be MODERATE");
		assertEquals(AttendanceViolation.ViolationSeverity.MODERATE, sOut, "Missing check-out should be MODERATE");
		System.out.println("✅ MISSING_CHECK_IN and MISSING_CHECK_OUT mapped to MODERATE");
	}

	@Test
	@DisplayName("Null type → MINOR (default)")
	void testNullTypeDefaultsToMinor() {
		// Setup: Null type
		AttendanceViolation.ViolationType type = null;

		// Run
		AttendanceViolation.ViolationSeverity s = violationDetectionService.calculateSeverity(type, 100);

		// Assert: Default branch should be MINOR
		assertEquals(AttendanceViolation.ViolationSeverity.MINOR, s, "Null type should default to MINOR");
		System.out.println("✅ Null type defaults to MINOR");
	}
}
