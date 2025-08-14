package com.classroomapp.classroombackend.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.model.hrmanagement.StaffAttendanceLog;
import com.classroomapp.classroombackend.model.hrmanagement.UserShiftAssignment;
import com.classroomapp.classroombackend.model.hrmanagement.WorkShift;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.StaffAttendanceLogRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.UserShiftAssignmentRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.WorkShiftRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

/**
 * Data loader for violation detection testing
 * Creates sample data to test the attendance violation workflow
 */
@Component
@Order(999) // Run after main DataLoader
public class ViolationTestDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(ViolationTestDataLoader.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkShiftRepository workShiftRepository;

    @Autowired
    private UserShiftAssignmentRepository userShiftAssignmentRepository;

    @Autowired
    private StaffAttendanceLogRepository staffAttendanceLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ContractRepository contractRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("Starting ViolationTestDataLoader...");

        // Always ensure test users and contracts exist
        logger.info("Ensuring test users and contracts exist...");

        try {
            createTestUsers();
            ensureContractsExist();
            createWorkShifts();
            createShiftAssignments();
            createAttendanceLogs();
            logger.info("ViolationTestDataLoader completed successfully!");
        } catch (Exception e) {
            logger.error("Error in ViolationTestDataLoader: {}", e.getMessage(), e);
        }
    }

    private void createTestUsers() {
        logger.info("Creating test users for violation testing...");

        // Create additional test users if they don't exist
        if (userRepository.findByEmail("john.teacher@mvs.edu").isEmpty()) {
            User teacher1 = new User();
            teacher1.setUsername("john.teacher");
            teacher1.setEmail("john.teacher@mvs.edu");
            teacher1.setPassword(passwordEncoder.encode("password123"));
            teacher1.setFullName("John Smith");
            teacher1.setRoleId(2); // TEACHER
            teacher1.setDepartmentId(1L);
            teacher1.setDepartment("Mathematics");
            teacher1.setStatus("active");
            teacher1.setEligibleForShiftAssignment(true);
            userRepository.save(teacher1);
            logger.info("Created teacher: {}", teacher1.getFullName());
            
            // Create contract for John Smith
            createTeacherContract(teacher1, "Mathematics");
            // Seed representative teaching hours in target period for payroll demo
            seedTeacherHourlyAttendance(teacher1, 2025, 8, 24 * 2, 6 * 2); // 24h weekday, 6h weekend
        }

        if (userRepository.findByEmail("jane.teacher@mvs.edu").isEmpty()) {
            User teacher2 = new User();
            teacher2.setUsername("jane.teacher");
            teacher2.setEmail("jane.teacher@mvs.edu");
            teacher2.setPassword(passwordEncoder.encode("password123"));
            teacher2.setFullName("Jane Doe");
            teacher2.setRoleId(2); // TEACHER
            teacher2.setDepartmentId(1L);
            teacher2.setDepartment("Mathematics");
            teacher2.setStatus("active");
            teacher2.setEligibleForShiftAssignment(true);
            userRepository.save(teacher2);
            logger.info("Created teacher: {}", teacher2.getFullName());
            
            // Create contract for Jane Doe
            createTeacherContract(teacher2, "Mathematics");
            seedTeacherHourlyAttendance(teacher2, 2025, 8, 32 * 2, 0); // 32h weekday, 0 weekend
        }
        if (userRepository.findByEmail("ank89353@gmail.com").isEmpty()) {
            User accountant = new User();
            accountant.setUsername("ank89353");
            accountant.setEmail("ank89353@gmail.com");
            accountant.setPassword(passwordEncoder.encode("password123"));
            accountant.setFullName("Lê Long Vũ");
            accountant.setRoleId(5); // ACCOUNTANT
            accountant.setDepartmentId(2L);
            accountant.setDepartment("Finance");
            accountant.setStatus("active");
            accountant.setEligibleForShiftAssignment(true);
            userRepository.save(accountant);
            logger.info("Created accountant: {}", accountant.getFullName());
            
            // Create contract for accountant
            createStaffContract(accountant, "Finance", "Accountant");
            // Seed attendance for August 2025 (Mon-Fri 8h/day)
            seedMonthlyAttendanceForStaff(accountant, 2025, 8);
        }
    }

    private void ensureContractsExist() {
        logger.info("Ensuring contracts exist for all test users...");
        
        // Check and create contracts for existing users
        User johnTeacher = userRepository.findByEmail("john.teacher@mvs.edu").orElse(null);
        if (johnTeacher != null) {
            boolean hasContract = contractRepository.findActiveContractByUserId(johnTeacher.getId()).isPresent();
            if (!hasContract) {
                logger.info("Creating missing contract for: {}", johnTeacher.getFullName());
                createTeacherContract(johnTeacher, "Mathematics");
            }
        }
        
        User janeTeacher = userRepository.findByEmail("jane.teacher@mvs.edu").orElse(null);
        if (janeTeacher != null) {
            boolean hasContract = contractRepository.findActiveContractByUserId(janeTeacher.getId()).isPresent();
            if (!hasContract) {
                logger.info("Creating missing contract for: {}", janeTeacher.getFullName());
                createTeacherContract(janeTeacher, "Mathematics");
            }
        }
        
        User bobAccountant = userRepository.findByEmail("bob.accountant@mvs.edu").orElse(null);
        if (bobAccountant != null) {
            boolean hasContract = contractRepository.findActiveContractByUserId(bobAccountant.getId()).isPresent();
            if (!hasContract) {
                logger.info("Creating missing contract for: {}", bobAccountant.getFullName());
                createStaffContract(bobAccountant, "Finance", "Accountant");
            }
        }
        
        logger.info("Contract verification completed.");
    }

    private void createWorkShifts() {
        logger.info("Creating work shifts...");

        // Morning Teaching Shift
        if (!workShiftRepository.existsByNameIgnoreCase("Morning Teaching Shift")) {
            WorkShift morningShift = new WorkShift();
            morningShift.setName("Morning Teaching Shift");
            morningShift.setStartTime(LocalTime.of(7, 30)); // 7:30 AM
            morningShift.setEndTime(LocalTime.of(11, 30)); // 11:30 AM
            morningShift.setBreakHours(0.5); // 30 minute break
            morningShift.setDescription("Morning classes for primary students");
            morningShift.setIsActive(true);
            morningShift.setCreatedBy(1L);
            workShiftRepository.save(morningShift);
            logger.info("Created work shift: {}", morningShift.getName());
        } else {
            logger.info("Work shift already exists: Morning Teaching Shift");
        }

        // Afternoon Teaching Shift
        if (!workShiftRepository.existsByNameIgnoreCase("Afternoon Teaching Shift")) {
            WorkShift afternoonShift = new WorkShift();
            afternoonShift.setName("Afternoon Teaching Shift");
            afternoonShift.setStartTime(LocalTime.of(13, 0)); // 1:00 PM
            afternoonShift.setEndTime(LocalTime.of(17, 0)); // 5:00 PM
            afternoonShift.setBreakHours(0.5); // 30 minute break
            afternoonShift.setDescription("Afternoon classes for secondary students");
            afternoonShift.setIsActive(true);
            afternoonShift.setCreatedBy(1L);
            workShiftRepository.save(afternoonShift);
            logger.info("Created work shift: {}", afternoonShift.getName());
        } else {
            logger.info("Work shift already exists: Afternoon Teaching Shift");
        }

        // Office Hours
        if (!workShiftRepository.existsByNameIgnoreCase("Administrative Office Hours")) {
            WorkShift officeShift = new WorkShift();
            officeShift.setName("Administrative Office Hours");
            officeShift.setStartTime(LocalTime.of(8, 0)); // 8:00 AM
            officeShift.setEndTime(LocalTime.of(16, 0)); // 4:00 PM
            officeShift.setBreakHours(1.0); // 1 hour lunch break
            officeShift.setDescription("Regular office hours for administrative staff");
            officeShift.setIsActive(true);
            officeShift.setCreatedBy(1L);
            workShiftRepository.save(officeShift);
            logger.info("Created work shift: {}", officeShift.getName());
        } else {
            logger.info("Work shift already exists: Administrative Office Hours");
        }
    }

    private void createShiftAssignments() {
        logger.info("Creating shift assignments...");

        // Get users and shifts
        User johnTeacher = userRepository.findByEmail("john.teacher@mvs.edu").orElse(null);
        User janeTeacher = userRepository.findByEmail("jane.teacher@mvs.edu").orElse(null);
        User bobAccountant = userRepository.findByEmail("bob.accountant@mvs.edu").orElse(null);

        List<WorkShift> shifts = workShiftRepository.findAll();
        WorkShift morningShift = shifts.stream()
            .filter(s -> s.getName().contains("Morning"))
            .findFirst().orElse(null);
        WorkShift afternoonShift = shifts.stream()
            .filter(s -> s.getName().contains("Afternoon"))
            .findFirst().orElse(null);
        WorkShift officeShift = shifts.stream()
            .filter(s -> s.getName().contains("Administrative"))
            .findFirst().orElse(null);

        LocalDate testDate = LocalDate.of(2025, 8, 4);
        LocalDate endDate = testDate.plusDays(30); // 30-day assignment

        // Assign John to morning shift
        if (johnTeacher != null && morningShift != null) {
            boolean exists = userShiftAssignmentRepository
                .existsByUser_IdAndWorkShift_IdAndStartDateAndEndDateAndIsActiveTrue(
                    johnTeacher.getId(), morningShift.getId(), testDate, endDate);
            if (!exists) {
                UserShiftAssignment assignment1 = new UserShiftAssignment();
                assignment1.setUser(johnTeacher);
                assignment1.setWorkShift(morningShift);
                assignment1.setStartDate(testDate);
                assignment1.setEndDate(endDate);
                assignment1.setNotes("Test assignment for violation detection");
                assignment1.setIsActive(true);
                assignment1.setCreatedBy(1L);
                userShiftAssignmentRepository.save(assignment1);
                logger.info("Assigned {} to {}", johnTeacher.getFullName(), morningShift.getName());
            } else {
                logger.info("Assignment already exists for {} on {}-{} to shift {}", johnTeacher.getFullName(), testDate, endDate, morningShift.getName());
            }
        }

        // Assign Jane to afternoon shift
        if (janeTeacher != null && afternoonShift != null) {
            boolean exists = userShiftAssignmentRepository
                .existsByUser_IdAndWorkShift_IdAndStartDateAndEndDateAndIsActiveTrue(
                    janeTeacher.getId(), afternoonShift.getId(), testDate, endDate);
            if (!exists) {
                UserShiftAssignment assignment2 = new UserShiftAssignment();
                assignment2.setUser(janeTeacher);
                assignment2.setWorkShift(afternoonShift);
                assignment2.setStartDate(testDate);
                assignment2.setEndDate(endDate);
                assignment2.setNotes("Test assignment for violation detection");
                assignment2.setIsActive(true);
                assignment2.setCreatedBy(1L);
                userShiftAssignmentRepository.save(assignment2);
                logger.info("Assigned {} to {}", janeTeacher.getFullName(), afternoonShift.getName());
            } else {
                logger.info("Assignment already exists for {} on {}-{} to shift {}", janeTeacher.getFullName(), testDate, endDate, afternoonShift.getName());
            }
        }

        // Assign Bob to office hours
        if (bobAccountant != null && officeShift != null) {
            boolean exists = userShiftAssignmentRepository
                .existsByUser_IdAndWorkShift_IdAndStartDateAndEndDateAndIsActiveTrue(
                    bobAccountant.getId(), officeShift.getId(), testDate, endDate);
            if (!exists) {
                UserShiftAssignment assignment3 = new UserShiftAssignment();
                assignment3.setUser(bobAccountant);
                assignment3.setWorkShift(officeShift);
                assignment3.setStartDate(testDate);
                assignment3.setEndDate(endDate);
                assignment3.setNotes("Test assignment for violation detection");
                assignment3.setIsActive(true);
                assignment3.setCreatedBy(1L);
                userShiftAssignmentRepository.save(assignment3);
                logger.info("Assigned {} to {}", bobAccountant.getFullName(), officeShift.getName());
            } else {
                logger.info("Assignment already exists for {} on {}-{} to shift {}", bobAccountant.getFullName(), testDate, endDate, officeShift.getName());
            }
        }
    }

    private void createAttendanceLogs() {
        logger.info("Creating attendance logs that will trigger violations...");

        User johnTeacher = userRepository.findByEmail("john.teacher@mvs.edu").orElse(null);
        User janeTeacher = userRepository.findByEmail("jane.teacher@mvs.edu").orElse(null);
        User bobAccountant = userRepository.findByEmail("bob.accountant@mvs.edu").orElse(null);

        LocalDate testDate = LocalDate.of(2025, 8, 4);

        // Case 1: John arrives 20 minutes late (Expected: 7:30, Actual: 7:50)
        if (johnTeacher != null) {
            StaffAttendanceLog lateArrival = new StaffAttendanceLog();
            lateArrival.setUser(johnTeacher);
            lateArrival.setAttendanceDate(testDate);
            lateArrival.setCheckInTime(LocalTime.of(7, 50)); // 20 minutes late
            lateArrival.setCheckOutTime(LocalTime.of(11, 30)); // On time checkout
            lateArrival.setAttendanceType(StaffAttendanceLog.AttendanceType.NORMAL);
            lateArrival.setNotes("Late arrival test case");
            lateArrival.setCreatedBy(johnTeacher.getId());
            staffAttendanceLogRepository.save(lateArrival);
            logger.info("Created LATE ARRIVAL log for {}: expected 07:30, actual 07:50", johnTeacher.getFullName());
        }

        // Case 2: Jane leaves 30 minutes early (Expected: 17:00, Actual: 16:30)
        if (janeTeacher != null) {
            StaffAttendanceLog earlyDeparture = new StaffAttendanceLog();
            earlyDeparture.setUser(janeTeacher);
            earlyDeparture.setAttendanceDate(testDate);
            earlyDeparture.setCheckInTime(LocalTime.of(13, 0)); // On time check-in
            earlyDeparture.setCheckOutTime(LocalTime.of(16, 30)); // 30 minutes early
            earlyDeparture.setAttendanceType(StaffAttendanceLog.AttendanceType.NORMAL);
            earlyDeparture.setNotes("Early departure test case");
            earlyDeparture.setCreatedBy(janeTeacher.getId());
            staffAttendanceLogRepository.save(earlyDeparture);
            logger.info("Created EARLY DEPARTURE log for {}: expected 17:00, actual 16:30", janeTeacher.getFullName());
        }

        // Case 3: Bob forgets to check out (Missing check-out)
        if (bobAccountant != null) {
            StaffAttendanceLog missingCheckOut = new StaffAttendanceLog();
            missingCheckOut.setUser(bobAccountant);
            missingCheckOut.setAttendanceDate(testDate);
            missingCheckOut.setCheckInTime(LocalTime.of(8, 5)); // Slightly late but within tolerance
            missingCheckOut.setCheckOutTime(null); // Missing check-out
            missingCheckOut.setAttendanceType(StaffAttendanceLog.AttendanceType.NORMAL);
            missingCheckOut.setNotes("Missing check-out test case");
            missingCheckOut.setCreatedBy(bobAccountant.getId());
            staffAttendanceLogRepository.save(missingCheckOut);
            logger.info("Created MISSING CHECK-OUT log for {}: checked in 08:05, no check-out", bobAccountant.getFullName());
        }

        logger.info("Test attendance logs created successfully!");
        logger.info("Expected violations for {}:", testDate);
        logger.info("1. LATE_ARRIVAL - John Smith (20 minutes late)");
        logger.info("2. EARLY_DEPARTURE - Jane Doe (30 minutes early)");
        logger.info("3. MISSING_CHECK_OUT - Bob Wilson (no check-out)");
        logger.info("");
        logger.info("To test the violation detection, call:");
        logger.info("POST /api/admin/detect-violations?date=2025-08-04");
        logger.info("Or use the service method:");
        logger.info("violationDetectionService.detectDailyViolations(LocalDate.of(2025, 8, 4))");
    }

    // Generate 6-digit contract ID: 2-digit monthly sequence + MMYY
    private String generateSeedContractId() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1);
        LocalDateTime startOfMonthDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endOfMonthDateTime = endOfMonth.atStartOfDay();
        Long contractsThisMonth = contractRepository.countByCreatedAtBetween(startOfMonthDateTime, endOfMonthDateTime);
        String sequence = String.format("%02d", contractsThisMonth + 1);
        String dateFormat = String.format("%02d%02d", today.getMonthValue(), today.getYear() % 100);
        return sequence + dateFormat;
    }

    /**
     * Create a contract for a teacher
     */
    private void createTeacherContract(User teacher, String subject) {
        try {
            Contract contract = new Contract();
            contract.setContractId(generateSeedContractId()); // 6-digit SSMMYY
            contract.setUserId(teacher.getId());
            contract.setFullName(teacher.getFullName());
            contract.setEmail(teacher.getEmail());
            contract.setPhoneNumber("09" + (int)(10000000 + Math.random()*89999999));
            contract.setContractType("TEACHER");
            contract.setPosition("Giáo viên " + subject);
            contract.setDepartment(subject);
            contract.setSalary(15000000.0 + (int)(Math.random()*6000000)); // 15-21 million VND (không dùng cho giáo viên)
            // Giáo viên: lương theo giờ
            contract.setHourlySalary(150_000L + (long)(Math.random()*80_000L)); // 150k - 230k VND/giờ
            contract.setWorkingHours("ca sáng (07:30-11:30)");
            contract.setStatus("ACTIVE");
            contract.setSubject(subject);
            contract.setClassLevel("10,11,12");
            contractRepository.save(contract);
            logger.info("Created contract for teacher: {} - Salary: {} VND", teacher.getFullName(), contract.getSalary());
        } catch (Exception e) {
            logger.warn("Could not create contract for teacher {}: {}", teacher.getFullName(), e.getMessage());
        }
    }

    /**
     * Create a contract for staff member
     */
    private void createStaffContract(User staff, String department, String position) {
        try {
            Contract contract = new Contract();
            contract.setContractId(generateSeedContractId()); // 6-digit SSMMYY
            contract.setUserId(staff.getId());
            contract.setFullName(staff.getFullName());
            contract.setEmail(staff.getEmail());
            contract.setPhoneNumber("09" + (int)(10000000 + Math.random()*89999999));
            contract.setContractType("STAFF");
            contract.setPosition(position);
            contract.setDepartment(department);
            contract.setSalary(12000000.0 + (int)(Math.random()*5000000)); // 12-17 million VND
            contract.setWorkingHours("ca hành chính (08:00-16:00)");
            contract.setStatus("ACTIVE");
            contract.setSubject(null); // Staff don't have subjects
            contract.setClassLevel(null);
            contractRepository.save(contract);
            logger.info("Created contract for staff {}: {} VND", staff.getFullName(), contract.getSalary());
        } catch (Exception e) {
            logger.warn("Could not create contract for staff {}: {}", staff.getFullName(), e.getMessage());
        }
    }

    /**
     * Seed attendance logs for a specific user and month with Mon-Fri 8h/day
     */
    private void seedMonthlyAttendanceForStaff(User staff, int year, int month) {
        java.time.YearMonth ym = java.time.YearMonth.of(year, month);
        java.time.LocalDate start = ym.atDay(1);
        java.time.LocalDate end = ym.atEndOfMonth();
        java.time.LocalTime in = java.time.LocalTime.of(8, 0);
        java.time.LocalTime out = java.time.LocalTime.of(16, 0);
        for (java.time.LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            java.time.DayOfWeek dow = d.getDayOfWeek();
            if (dow.getValue() >= 6) continue; // skip weekend
            StaffAttendanceLog log = new StaffAttendanceLog();
            log.setUser(staff);
            log.setAttendanceDate(d);
            log.setCheckInTime(in);
            log.setCheckOutTime(out);
            log.setAttendanceType(StaffAttendanceLog.AttendanceType.NORMAL);
            staffAttendanceLogRepository.save(log);
        }
        logger.info("Seeded monthly attendance for {}: {}/{} (Mon-Fri 8h/day)", staff.getFullName(), month, year);
    }

    /**
     * Seed hourly attendance summary for teacher payroll demo
     * Creates simplified logs representing weekday vs weekend teaching hours.
     */
    private void seedTeacherHourlyAttendance(User teacher, int year, int month, int weekdayHours, int weekendHours) {
        java.time.YearMonth ym = java.time.YearMonth.of(year, month);
        java.time.LocalDate start = ym.atDay(1);
        java.time.LocalDate end = ym.atEndOfMonth();
        int remainingWeekday = Math.max(0, weekdayHours);
        int remainingWeekend = Math.max(0, weekendHours);

        for (java.time.LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            boolean isWeekend = d.getDayOfWeek().getValue() >= 6;
            if (isWeekend && remainingWeekend <= 0) continue;
            if (!isWeekend && remainingWeekday <= 0) continue;

            int hours = Math.min(3, isWeekend ? remainingWeekend : remainingWeekday);
            java.time.LocalTime in = isWeekend ? java.time.LocalTime.of(8, 0) : java.time.LocalTime.of(7, 30);
            java.time.LocalTime out = in.plusHours(hours);

            StaffAttendanceLog log = new StaffAttendanceLog();
            log.setUser(teacher);
            log.setAttendanceDate(d);
            log.setCheckInTime(in);
            log.setCheckOutTime(out);
            log.setAttendanceType(StaffAttendanceLog.AttendanceType.NORMAL);
            staffAttendanceLogRepository.save(log);

            if (isWeekend) remainingWeekend -= hours; else remainingWeekday -= hours;

            if (remainingWeekend <= 0 && remainingWeekday <= 0) break;
        }

        logger.info("Seeded teacher hourly attendance for {}: {} weekday hours, {} weekend hours in {}/{}",
                teacher.getFullName(), weekdayHours, weekendHours, month, year);
    }
}