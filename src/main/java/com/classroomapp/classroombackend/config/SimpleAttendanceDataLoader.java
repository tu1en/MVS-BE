package com.classroomapp.classroombackend.config;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.repository.AttendanceLogRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Simple data loader for populating attendance data on application startup
 * This creates basic sample data without depending on existing users
 */
@Component
@Slf4j
@Order(1000) // Run very late to ensure other components are ready
public class SimpleAttendanceDataLoader implements CommandLineRunner {

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    @Override
    public void run(String... args) throws Exception {
        if (shouldLoadData()) {
            log.info("Loading simple attendance sample data...");
            loadAttendanceData();
            log.info("Simple attendance sample data loaded successfully!");
        } else {
            log.info("Attendance data already exists, skipping data load.");
        }
    }

    private boolean shouldLoadData() {
        return attendanceLogRepository.count() == 0;
    }

    private void loadAttendanceData() {
        // Create sample users data
        String[] userNames = {
            "Nguyễn Văn An", "Trần Thị Bình", "Lê Văn Cường", "Phạm Thị Dung",
            "Hoàng Văn Em", "Võ Thị Phương", "Đặng Văn Giang", "Bùi Thị Hoa",
            "Lý Văn Inh", "Ngô Thị Khánh"
        };
        
        String[] roles = {"TEACHER", "MANAGER", "ACCOUNTANT", "TEACHER", "TEACHER", 
                         "MANAGER", "TEACHER", "ACCOUNTANT", "TEACHER", "MANAGER"};
        
        String[] departments = {"IT", "HR", "Finance", "Education", "Marketing", 
                               "IT", "Education", "Finance", "HR", "IT"};
        
        String[] shifts = {"Ca sáng", "Ca chiều", "Ca tối"};

        // Create attendance data for last 7 days including today
        for (int dayOffset = 0; dayOffset <= 6; dayOffset++) {
            LocalDate date = LocalDate.now().minusDays(dayOffset);
            createAttendanceForDate(userNames, roles, departments, shifts, date);
        }
    }

    private void createAttendanceForDate(String[] userNames, String[] roles, 
                                       String[] departments, String[] shifts, LocalDate date) {
        for (int i = 0; i < userNames.length; i++) {
            // Create 1-2 shifts per user per day
            int shiftsToCreate = (i % 2) + 1;
            
            for (int shiftIndex = 0; shiftIndex < shiftsToCreate; shiftIndex++) {
                AttendanceLog log = new AttendanceLog();
                
                // Set basic info
                log.setUserId((long) (i + 1));
                log.setUserName(userNames[i]);
                log.setRole(roles[i]);
                log.setDepartment(departments[i]);
                log.setDate(date);
                log.setShift(shifts[shiftIndex % shifts.length]);

                // Generate attendance times based on shift
                boolean isPresent = (i + date.getDayOfMonth() + shiftIndex) % 8 != 0; // 87.5% attendance

                if (isPresent) {
                    LocalTime baseCheckIn, baseCheckOut;
                    
                    // Different times for different shifts
                    switch (shiftIndex % 3) {
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
                    
                    int checkInVariation = (i * 7 + date.getDayOfMonth() + shiftIndex) % 45; // 0-44 minutes
                    LocalTime checkIn = baseCheckIn.plusMinutes(checkInVariation);
                    log.setCheckIn(checkIn);

                    int checkOutVariation = (i * 11 + date.getDayOfMonth() + shiftIndex) % 60; // 0-59 minutes  
                    LocalTime checkOut = baseCheckOut.plusMinutes(checkOutVariation);
                    log.setCheckOut(checkOut);

                    // Determine status
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
}
