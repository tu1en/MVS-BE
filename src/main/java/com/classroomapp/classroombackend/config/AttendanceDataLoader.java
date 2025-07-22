package com.classroomapp.classroombackend.config;

import com.classroomapp.classroombackend.model.AttendanceExplanation;
import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.model.ExplanationStatus;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AttendanceExplanationRepository;
import com.classroomapp.classroombackend.repository.AttendanceLogRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Random;

@Component
public class AttendanceDataLoader implements CommandLineRunner {

    @Autowired
    private AttendanceExplanationRepository explanationRepository;
    
    @Autowired
    private AttendanceLogRepository attendanceLogRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        // Only load data if tables are empty
        if (explanationRepository.count() == 0) {
            loadExplanationData();
        }
        
        if (attendanceLogRepository.count() == 0) {
            loadAttendanceLogData();
        }
    }

    private void loadExplanationData() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            System.out.println("No users found. Skipping explanation data loading.");
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

        System.out.println("Loading 10 sample explanation reports...");

        for (int i = 0; i < 10; i++) {
            User user = users.get(random.nextInt(users.size()));
            
            AttendanceExplanation explanation = new AttendanceExplanation();
            explanation.setSubmitterName(user.getFullName());
            explanation.setDepartment(user.getDepartment() != null ? user.getDepartment() : "Phòng " + getRoleString(user.getRoleId()));
            explanation.setReason(reasons[i]);
            explanation.setAbsenceDate(LocalDate.now().minusDays(random.nextInt(30)));
            explanation.setStatus(statuses[random.nextInt(statuses.length)]);
            explanation.setSubmittedAt(LocalDateTime.now().minusDays(random.nextInt(7)));
            
            if (!explanation.getStatus().equals(ExplanationStatus.PENDING)) {
                explanation.setApproverName("Manager " + (i % 3 + 1));
            }

            explanationRepository.save(explanation);
        }

        System.out.println("Successfully loaded 10 explanation reports.");
    }

    private void loadAttendanceLogData() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            System.out.println("No users found. Skipping attendance log data loading.");
            return;
        }

        Random random = new Random();
        String[] statuses = {"PRESENT", "ABSENT", "LATE"};
        String[] shifts = {"MORNING", "AFTERNOON", "EVENING"};

        System.out.println("Loading 20 sample attendance logs...");

        for (int i = 0; i < 20; i++) {
            User user = users.get(random.nextInt(users.size()));
            
            AttendanceLog log = new AttendanceLog();
            log.setUserId(user.getId());
            log.setUserName(user.getFullName());
            log.setRole(getRoleString(user.getRoleId()));
            log.setDepartment(user.getDepartment() != null ? user.getDepartment() : "Phòng " + getRoleString(user.getRoleId()));
            log.setDate(LocalDate.now().minusDays(random.nextInt(7)));
            log.setShift(shifts[random.nextInt(shifts.length)]);
            log.setStatus(statuses[random.nextInt(statuses.length)]);
            
            // Set check-in and check-out times based on shift and status
            LocalTime baseCheckIn = getShiftStartTime(log.getShift());
            LocalTime baseCheckOut = getShiftEndTime(log.getShift());
            
            if (log.getStatus().equals("PRESENT")) {
                log.setCheckIn(baseCheckIn.plusMinutes(random.nextInt(30) - 15)); // ±15 minutes
                log.setCheckOut(baseCheckOut.plusMinutes(random.nextInt(60) - 30)); // ±30 minutes
            } else if (log.getStatus().equals("LATE")) {
                log.setCheckIn(baseCheckIn.plusMinutes(15 + random.nextInt(45))); // 15-60 minutes late
                log.setCheckOut(baseCheckOut.plusMinutes(random.nextInt(60) - 30));
            }
            // ABSENT status has null check-in/check-out times

            attendanceLogRepository.save(log);
        }

        System.out.println("Successfully loaded 20 attendance logs.");
    }

    private String getRoleString(Integer roleId) {
        if (roleId == null) return "Staff";
        switch (roleId) {
            case 1: return "Teacher";
            case 2: return "Accountant"; 
            case 3: return "Admin";
            case 4: return "Manager";
            default: return "Staff";
        }
    }

    private LocalTime getShiftStartTime(String shift) {
        switch (shift) {
            case "MORNING": return LocalTime.of(8, 0);
            case "AFTERNOON": return LocalTime.of(13, 0);
            case "EVENING": return LocalTime.of(18, 0);
            default: return LocalTime.of(8, 0);
        }
    }

    private LocalTime getShiftEndTime(String shift) {
        switch (shift) {
            case "MORNING": return LocalTime.of(12, 0);
            case "AFTERNOON": return LocalTime.of(17, 0);
            case "EVENING": return LocalTime.of(22, 0);
            default: return LocalTime.of(17, 0);
        }
    }
}
