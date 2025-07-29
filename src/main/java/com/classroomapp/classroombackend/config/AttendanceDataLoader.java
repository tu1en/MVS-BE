package com.classroomapp.classroombackend.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.AttendanceExplanation;
import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.model.AttendanceStatus;
import com.classroomapp.classroombackend.model.ExplanationStatus;
import com.classroomapp.classroombackend.model.hrmanagement.Shift;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AttendanceExplanationRepository;
import com.classroomapp.classroombackend.repository.AttendanceLogRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.ShiftRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
@Order(100) // Run after other seeders
public class AttendanceDataLoader implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceDataLoader.class);

    @Autowired
    private AttendanceExplanationRepository explanationRepository;
    
    @Autowired
    private AttendanceLogRepository attendanceLogRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ShiftRepository shiftRepository;

    @Override
    public void run(String... args) throws Exception {
        try {
            logger.info("🔧 Starting AttendanceDataLoader...");
            
            // Only load data if tables are empty
            if (explanationRepository.count() == 0) {
                loadExplanationData();
            } else {
                logger.info("✅ Attendance explanations already exist, skipping...");
            }
            
            if (attendanceLogRepository.count() == 0) {
                loadAttendanceLogData();
            } else {
                logger.info("✅ Attendance logs already exist, skipping...");
            }
            
            logger.info("✅ AttendanceDataLoader completed successfully");
            
        } catch (Exception e) {
            logger.error("❌ Error in AttendanceDataLoader: ", e);
            logger.warn("⚠️ AttendanceDataLoader failed but application will continue running");
            // DO NOT throw exception - let app continue
        }
    }

    private void loadExplanationData() {
        try {
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                logger.warn("⚠️ No users found. Skipping explanation data loading.");
                return;
            }

            Random random = new Random();
            String[] reasons = {
                "Đi muộn do kẹt xe",
                "Vắng mặt do ốm",
                "Quên chấm công",
                "Sự cố gia đình",
                "Họp khẩn cấp",
                "Đi công tác",
                "Khám bệnh định kỳ",
                "Tham gia đào tạo",
                "Sự cố giao thông",
                "Lỗi hệ thống chấm công"
            };

            ExplanationStatus[] statuses = {ExplanationStatus.PENDING, ExplanationStatus.APPROVED, ExplanationStatus.REJECTED};

            logger.info("📝 Loading 10 sample explanation reports...");

            for (int i = 0; i < 10; i++) {
                try {
                    User user = users.get(random.nextInt(users.size()));
                    
                    AttendanceExplanation explanation = new AttendanceExplanation();
                    explanation.setSubmitterName(user.getFullName());
                    explanation.setDepartment(user.getDepartment() != null ? user.getDepartment() : "Phòng " + getRoleString(user.getRoleId()));
                    explanation.setReason(reasons[i]);
                    explanation.setExplanationText(reasons[i]); // Set explanation text
                    explanation.setAbsenceDate(LocalDate.now().minusDays(random.nextInt(30)));
                    explanation.setStatus(statuses[random.nextInt(statuses.length)]);
                    explanation.setSubmittedAt(LocalDateTime.now().minusDays(random.nextInt(7)));
                    
                    // Set required fields to prevent NULL constraint violations
                    explanation.setViolationId((long) (i + 1));
                    explanation.setStaffId(user.getId()); // Map user ID to staff_id (since no separate staff table)
                    
                    if (!explanation.getStatus().equals(ExplanationStatus.PENDING)) {
                        explanation.setApproverName("Manager " + (i % 3 + 1));
                    }

                    explanationRepository.save(explanation);
                    logger.debug("✅ Saved explanation {}/10", i + 1);
                    
                } catch (Exception e) {
                    logger.error("❌ Failed to save explanation {}: {}", i + 1, e.getMessage());
                    // Continue with next iteration
                }
            }

            logger.info("✅ Successfully loaded explanation reports.");
            
        } catch (Exception e) {
            logger.error("❌ Failed to load explanation data: ", e);
            logger.warn("⚠️ Skipping explanation data loading due to schema mismatch");
        }
    }

    private void loadAttendanceLogData() {
        try {
            List<User> users = userRepository.findAll();
            if (users.isEmpty()) {
                logger.warn("⚠️ No users found. Skipping attendance log data loading.");
                return;
            }

            Random random = new Random();
            AttendanceStatus[] statuses = {AttendanceStatus.PRESENT, AttendanceStatus.ABSENT, AttendanceStatus.LATE};
            List<Shift> shifts = shiftRepository.findAll();
            
            if (shifts.isEmpty()) {
                logger.warn("⚠️ No shifts found. Skipping attendance log data loading.");
                return;
            }

            logger.info("📊 Loading 20 sample attendance logs...");

            for (int i = 0; i < 20; i++) {
                try {
                    User user = users.get(random.nextInt(users.size()));
                    
                    AttendanceLog log = new AttendanceLog();
                    log.setStaff(user);
                    log.setAttendanceDate(LocalDate.now().minusDays(random.nextInt(7)));
                    log.setShift(shifts.get(random.nextInt(shifts.size())));
                    log.setStatus(statuses[random.nextInt(statuses.length)]);
                    
                    // Set check-in and check-out times based on shift and status
                    LocalTime baseCheckIn = getShiftStartTime(log.getShift());
                    LocalTime baseCheckOut = getShiftEndTime(log.getShift());
                    
                    if (log.getStatus() == AttendanceStatus.PRESENT) {
                        log.setCheckInTime(log.getAttendanceDate().atTime(baseCheckIn.plusMinutes(random.nextInt(30) - 15))); // ±15 minutes
                        log.setCheckOutTime(log.getAttendanceDate().atTime(baseCheckOut.plusMinutes(random.nextInt(60) - 30))); // ±30 minutes
                    } else if (log.getStatus() == AttendanceStatus.LATE) {
                        log.setCheckInTime(log.getAttendanceDate().atTime(baseCheckIn.plusMinutes(15 + random.nextInt(45)))); // 15-60 minutes late
                        log.setCheckOutTime(log.getAttendanceDate().atTime(baseCheckOut.plusMinutes(random.nextInt(60) - 30)));
                    }
                    // ABSENT status has null check-in/check-out times

                    attendanceLogRepository.save(log);
                    logger.debug("✅ Saved attendance log {}/20", i + 1);
                    
                } catch (Exception e) {
                    logger.error("❌ Failed to save attendance log {}: {}", i + 1, e.getMessage());
                }
            }

            logger.info("✅ Successfully loaded attendance logs.");
            
        } catch (Exception e) {
            logger.error("❌ Failed to load attendance log data: ", e);
        }
    }

    private String getRoleString(Integer roleId) {
        if (roleId == null) return "Unknown Role";
        switch (roleId) {
            case 1: return "Teacher";
            case 2: return "Accountant"; 
            case 3: return "Admin";
            case 4: return "Manager";
            default: return "Unknown Role";
        }
    }

    private LocalTime getShiftStartTime(Shift shift) {
        try {
            return shift.getStartTime();
        } catch (Exception e) {
            logger.warn("⚠️ Failed to get shift start time, using default");
            return LocalTime.of(8, 0);
        }
    }

    private LocalTime getShiftEndTime(Shift shift) {
        try {
            return shift.getEndTime();
        } catch (Exception e) {
            logger.warn("⚠️ Failed to get shift end time, using default");
            return LocalTime.of(17, 0);
        }
    }
}