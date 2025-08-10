package com.classroomapp.classroombackend.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;

import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.model.hrmanagement.StaffAttendanceLog;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AttendanceLogRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.StaffAttendanceLogRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Data loader for populating attendance data on application startup
 * This runs after other data loaders to ensure users exist first
 */
// Temporarily disabled due to class loading issues
// @Component
@Slf4j
@Order(100) // Run after user data is loaded
public class AttendanceDataLoader implements CommandLineRunner {

    @Autowired
    private StaffAttendanceLogRepository staffAttendanceLogRepository;
    
    @Autowired
    private AttendanceLogRepository attendanceLogRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (shouldLoadData()) {
            log.info("Loading attendance sample data...");
            loadStaffAttendanceData();
            loadStudentAttendanceData();
            log.info("Attendance sample data loaded successfully!");
        } else {
            log.info("Attendance data already exists, skipping data load.");
        }
    }

    private boolean shouldLoadData() {
        // Check if data already exists
        return staffAttendanceLogRepository.count() == 0 && attendanceLogRepository.count() == 0;
    }

    private void loadStaffAttendanceData() {
        var allUsers = userRepository.findAll();
        if (allUsers.isEmpty()) {
            log.warn("No users found, cannot create attendance data");
            return;
        }

        // Filter staff users (teachers, managers, accountants)
        var staffUsers = allUsers.stream()
            .filter(user -> user.getRoleId() != null && (user.getRoleId() == 2 || user.getRoleId() == 3 || user.getRoleId() == 4))
            .toList();

        if (staffUsers.isEmpty()) {
            log.warn("No staff users found, creating sample data for all users");
            staffUsers = allUsers.stream().limit(8).toList(); // Use first 8 users
        }

        // Create attendance data for last 7 days including today
        for (int dayOffset = 0; dayOffset <= 6; dayOffset++) {
            LocalDate date = LocalDate.now().minusDays(dayOffset);
            createStaffAttendanceForDate(staffUsers, date);
        }
    }

    private void createStaffAttendanceForDate(List<User> staffUsers, LocalDate date) {
        for (User user : staffUsers) {
            StaffAttendanceLog log = new StaffAttendanceLog();
            log.setUser(user);
            log.setAttendanceDate(date);
            log.setAttendanceType(StaffAttendanceLog.AttendanceType.NORMAL);
            log.setCreatedAt(LocalDateTime.now());
            log.setUpdatedAt(LocalDateTime.now());

            // Generate realistic attendance times based on user ID for consistency
            long userId = user.getId();
            boolean isPresent = (userId + date.getDayOfMonth()) % 10 != 0; // 90% attendance rate

            if (isPresent) {
                // Check-in time: 8:00 AM + random variation
                int checkInMinutes = (int) ((userId * 7 + date.getDayOfMonth()) % 60); // 0-59 minutes
                LocalTime checkIn = LocalTime.of(8, 0).plusMinutes(checkInMinutes);
                log.setCheckInTime(checkIn);

                // Check-out time: 5:30 PM + random variation
                int checkOutMinutes = (int) ((userId * 11 + date.getDayOfMonth()) % 60); // 0-59 minutes
                LocalTime checkOut = LocalTime.of(17, 30).plusMinutes(checkOutMinutes);
                log.setCheckOutTime(checkOut);

                // Add some notes
                if (checkIn.isAfter(LocalTime.of(8, 30))) {
                    log.setNotes("Đến muộn");
                } else if (checkIn.isBefore(LocalTime.of(8, 5))) {
                    log.setNotes("Đến sớm");
                } else {
                    log.setNotes("Chấm công bình thường");
                }
            } else {
                // Absent
                log.setNotes("Vắng mặt");
            }

            staffAttendanceLogRepository.save(log);
        }
    }

    private void loadStudentAttendanceData() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return;
        }

        // Filter users to get different roles
        List<User> allUsers = users.stream().limit(10).toList();

        // Create attendance logs for the attendance_logs table (used by other endpoints)
        for (int dayOffset = 0; dayOffset <= 6; dayOffset++) {
            LocalDate date = LocalDate.now().minusDays(dayOffset);
            createAttendanceLogsForDate(allUsers, date);
        }
    }

    private void createAttendanceLogsForDate(List<User> users, LocalDate date) {
        String[] shifts = {"Ca sáng", "Ca chiều", "Ca tối"};
        String[] departments = {"IT", "HR", "Finance", "Education", "Marketing"};
        
        for (User user : users) {
            // Create multiple logs per user per day for different shifts to have more data
            int shiftsToCreate = (int)(user.getId() % 2) + 1; // 1-2 shifts per user
            
            for (int i = 0; i < shiftsToCreate; i++) {
                AttendanceLog log = new AttendanceLog();
                log.setUserId(user.getId());
                log.setUserName(user.getFullName() != null ? user.getFullName() : user.getUsername());
                log.setRole(getRoleName(user.getRoleId()));
                log.setDepartment(user.getDepartment() != null ? user.getDepartment() : departments[(int)(user.getId() % departments.length)]);
                log.setDate(date);
                log.setShift(shifts[i % shifts.length]);

                // Generate attendance times based on shift
                long userId = user.getId();
                boolean isPresent = (userId + date.getDayOfMonth() + i) % 8 != 0; // 87.5% attendance

                if (isPresent) {
                    LocalTime baseCheckIn, baseCheckOut;
                    
                    // Different times for different shifts
                    switch (i % 3) {
                        case 0: // Morning shift
                            baseCheckIn = LocalTime.of(8, 0);
                            baseCheckOut = LocalTime.of(12, 0);
                            break;
                        case 1: // Afternoon shift
                            baseCheckIn = LocalTime.of(13, 0);
                            baseCheckOut = LocalTime.of(17, 0);
                            break;
                        default: // Evening shift
                            baseCheckIn = LocalTime.of(18, 0);
                            baseCheckOut = LocalTime.of(22, 0);
                            break;
                    }
                    
                    int checkInMinutes = (int) ((userId * 7 + date.getDayOfMonth() + i) % 45); // 0-44 minutes
                    LocalTime checkIn = baseCheckIn.plusMinutes(checkInMinutes);
                    log.setCheckIn(checkIn);

                    int checkOutMinutes = (int) ((userId * 11 + date.getDayOfMonth() + i) % 60); // 0-59 minutes  
                    LocalTime checkOut = baseCheckOut.plusMinutes(checkOutMinutes);
                    log.setCheckOut(checkOut);

                    // Determine status - more lenient for afternoon/evening shifts
                    LocalTime lateThreshold = baseCheckIn.plusMinutes(30);
                    if (checkIn.isAfter(lateThreshold)) {
                        log.setStatus("LATE");
                    } else {
                        log.setStatus("PRESENT");
                    }
                } else {
                    log.setStatus("ABSENT");
                }

                attendanceLogRepository.save(log);
            }
        }
    }

    private String getRoleName(Integer roleId) {
        if (roleId == null) return "STAFF";
        return switch (roleId) {
            case 1 -> "STUDENT";
            case 2 -> "TEACHER";
            case 3 -> "MANAGER";
            case 4 -> "ADMIN";
            case 5 -> "ACCOUNTANT";
            default -> "STAFF";
        };
    }
}