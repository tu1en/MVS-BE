package com.classroomapp.classroombackend.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.AttendanceLog;
import com.classroomapp.classroombackend.repository.AttendanceLogRepository;

@Component
public class AttendanceLogDataSeeder implements CommandLineRunner {

    @Autowired
    private AttendanceLogRepository attendanceLogRepository;

    @Override
    public void run(String... args) throws Exception {
        // Chỉ tạo dữ liệu nếu chưa có
        if (attendanceLogRepository.count() == 0) {
            seedAttendanceLogData();
        }
    }

    private void seedAttendanceLogData() {
        System.out.println("🌱 Seeding AttendanceLog data...");

        List<AttendanceLog> attendanceLogs = new ArrayList<>();
        
        // Dữ liệu cho tất cả userId có trong hệ thống (khớp với DataLoader.java)
        Long[] userIds = {201L, 301L, 401L, 202L, 203L, 204L, 205L, 206L, 501L};
        String[] userNames = {
            "Nguyễn Văn Minh", "Manager User", "Administrator",
            "Trần Văn Đức", "Phạm Thị Lan", "Lê Hoàng Nam", 
            "Vũ Thị Hương", "Đặng Minh Tuấn", "Nguyễn Thị Kế Toán"
        };
        String[] roles = {
            "TEACHER", "MANAGER", "ADMIN",
            "TEACHER", "TEACHER", "TEACHER", 
            "TEACHER", "TEACHER", "ACCOUNTANT"
        };
        String[] departments = {
            "Khoa Công Nghệ Thông Tin", "Phòng Manager", "Phòng Hành chính",
            "Khoa Toán Học", "Khoa Ngữ Văn", "Khoa Ngoại Ngữ", 
            "Khoa Hóa Học", "Khoa Vật Lý", "Kế toán viên"
        };

        // Tạo dữ liệu cho từng user trong 30 ngày qua
        for (int i = 0; i < userIds.length; i++) {
            Long userId = userIds[i];
            String userName = userNames[i];
            String role = roles[i];
            String department = departments[i];
            
            // Chỉ tạo dữ liệu cho nhân viên (không phải STUDENT)
            if (!"STUDENT".equals(role)) {
                for (int day = 1; day <= 30; day++) {
                    // Random tạo dữ liệu chấm công (80% có mặt, 15% đi muộn, 5% vắng)
                    double random = Math.random();
                    String status;
                    LocalTime checkIn;
                    LocalTime checkOut;
                    String shift = day % 3 == 0 ? "afternoon" : "morning";
                    
                    if (random < 0.05) { // 5% vắng mặt
                        status = "ABSENT";
                        checkIn = null;
                        checkOut = null;
                    } else if (random < 0.20) { // 15% đi muộn
                        status = "LATE";
                        checkIn = LocalTime.of(8, 30 + (int)(Math.random() * 30)); // 8:30-9:00
                        checkOut = "morning".equals(shift) ? LocalTime.of(12, 0) : LocalTime.of(17, 0);
                    } else { // 80% có mặt
                        status = "PRESENT";
                        checkIn = LocalTime.of(8, 0);
                        checkOut = "morning".equals(shift) ? LocalTime.of(12, 0) : LocalTime.of(17, 0);
                    }
                    
                    attendanceLogs.add(new AttendanceLog(
                        userId, userName, role, department,
                        LocalDate.now().minusDays(day),
                        shift, checkIn, checkOut, status
                    ));
                }
            }
        }

        attendanceLogRepository.saveAll(attendanceLogs);
        System.out.println("✅ Đã tạo " + attendanceLogs.size() + " bản ghi AttendanceLog cho tất cả user");
    }
}