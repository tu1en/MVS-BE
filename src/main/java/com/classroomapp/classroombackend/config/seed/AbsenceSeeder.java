package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Absence;
import com.classroomapp.classroombackend.repository.absencemanagement.AbsenceRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AbsenceSeeder {

    private final AbsenceRepository absenceRepository;
    private final UserRepository userRepository;

    public void seed() {
        System.out.println("🌱 [AbsenceSeeder] Starting absence seeding...");
        long existingCount = absenceRepository.count();
        System.out.println("🔍 [AbsenceSeeder] Found " + existingCount + " existing absence records");
        
        if (existingCount > 0) {
            System.out.println("✅ [AbsenceSeeder] Absence data already exists. Skipping seeding.");
            return;
        }

        // Verify that users exist before creating absence records
        long userCount = userRepository.count();
        if (userCount == 0) {
            System.err.println("❌ [AbsenceSeeder] No users found. Please run UserSeeder first.");
            return;
        }
        
        // Verify specific teacher users exist
        Long[] teacherIds = {2L, 5L, 6L, 7L, 13L, 14L}; // Updated with actual IDs
        for (Long teacherId : teacherIds) {
            if (!userRepository.existsById(teacherId)) {
                System.err.println("❌ [AbsenceSeeder] Teacher with ID " + teacherId + " not found. Skipping absence seeding.");
                return;
            }
        }

        try {
            System.out.println("🔨 [AbsenceSeeder] Creating absence records...");
            
            // Nguyễn Văn Minh (ID: 2) - đã dùng 5 ngày, còn 7 ngày
            System.out.println("📝 [AbsenceSeeder] Creating absence for Nguyễn Văn Minh (ID: 2)");
            createAbsence(2L, "teacher@test.com", "Nguyễn Văn Minh", 
                LocalDate.now().minusDays(30), LocalDate.now().minusDays(28), 3, 
                "Nghỉ phép để tham gia hội thảo giáo dục về công nghệ thông tin", "APPROVED");
            
            createAbsence(2L, "teacher@test.com", "Nguyễn Văn Minh",
                LocalDate.now().minusDays(15), LocalDate.now().minusDays(14), 2,
                "Nghỉ ốm do cảm cúm mùa", "APPROVED");

            // Trần Văn Đức (ID: 5) - đã dùng 8 ngày, có đơn chờ 3 ngày (sẽ vượt phép)
            createAbsence(5L, "math@test.com", "Trần Văn Đức",
                LocalDate.now().minusDays(45), LocalDate.now().minusDays(43), 3,
                "Nghỉ phép về quê ăn tết cùng gia đình", "APPROVED");
            
            createAbsence(5L, "math@test.com", "Trần Văn Đức",
                LocalDate.now().minusDays(25), LocalDate.now().minusDays(21), 5,
                "Nghỉ phép chăm sóc mẹ già ốm đau", "APPROVED");
            
            createAbsence(5L, "math@test.com", "Trần Văn Đức",
                LocalDate.now().plusDays(10), LocalDate.now().plusDays(12), 3,
                "Xin nghỉ phép để tham dự đám cưới con trai", "PENDING", true);

            // Phạm Thị Lan (ID: 6) - đã vượt phép (dùng 15 ngày)
            createAbsence(6L, "literature@test.com", "Phạm Thị Lan",
                LocalDate.now().minusDays(60), LocalDate.now().minusDays(53), 8,
                "Nghỉ phép sinh con và chăm sóc sau sinh", "APPROVED");
            
            createAbsence(6L, "literature@test.com", "Phạm Thị Lan",
                LocalDate.now().minusDays(35), LocalDate.now().minusDays(29), 7,
                "Nghỉ phép tiếp tục chăm sóc con nhỏ bị ốm", "APPROVED", true);

            // Lê Hoàng Nam (ID: 7) - đã dùng 4 ngày, có đơn chờ 2 ngày
            createAbsence(7L, "english@test.com", "Lê Hoàng Nam",
                LocalDate.now().minusDays(50), LocalDate.now().minusDays(47), 4,
                "Nghỉ phép đi du lịch Đà Lạt cùng gia đình", "APPROVED");
            
            createAbsence(7L, "english@test.com", "Lê Hoàng Nam",
                LocalDate.now().plusDays(5), LocalDate.now().plusDays(6), 2,
                "Xin nghỉ phép để khám sức khỏe tổng quát định kỳ", "PENDING");

            // Vũ Thị Hương (ID: 13) - có đơn bị từ chối
            createAbsence(13L, "teacher2@test.com", "Vũ Thị Hương",
                LocalDate.now().minusDays(40), LocalDate.now().minusDays(38), 3,
                "Nghỉ phép tham gia khóa đào tạo nâng cao về hóa học", "APPROVED");
            
            createAbsence(13L, "teacher2@test.com", "Vũ Thị Hương",
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), 6,
                "Xin nghỉ phép để đi du lịch nghỉ dưỡng tại Nha Trang", "REJECTED", false, "Thời gian xin nghỉ trùng với lịch thi giữa kỳ của sinh viên");

            // Đặng Minh Tuấn (ID: 14) - mới vào, chưa xin nghỉ
            createAbsence(14L, "teacher3@test.com", "Đặng Minh Tuấn",
                LocalDate.now().plusDays(20), LocalDate.now().plusDays(21), 2,
                "Xin nghỉ phép để tham gia hội nghị quốc tế về vật lý", "PENDING");

            // Accountant (ID: 501) - đã dùng 2 ngày, còn 10 ngày, có 1 đơn chờ duyệt
            if (userRepository.existsById(501L)) {
                createAbsence(501L, "accountant@test.com", "Nguyễn Thị Kế Toán",
                    LocalDate.now().minusDays(10), LocalDate.now().minusDays(9), 2,
                    "Nghỉ phép kiểm toán cuối năm", "APPROVED");
                createAbsence(501L, "accountant@test.com", "Nguyễn Thị Kế Toán",
                    LocalDate.now().plusDays(3), LocalDate.now().plusDays(3), 1,
                    "Nghỉ phép cá nhân", "PENDING");
            }

            System.out.println("✅ [AbsenceSeeder] Created sample absence requests for all teachers.");
        } catch (Exception e) {
            System.err.println("❌ Error creating sample absence requests: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createAbsence(Long userId, String userEmail, String userFullName,
                              LocalDate startDate, LocalDate endDate, Integer numberOfDays,
                              String description, String status) {
        createAbsence(userId, userEmail, userFullName, startDate, endDate, numberOfDays,
                     description, status, false, null);
    }

    private void createAbsence(Long userId, String userEmail, String userFullName,
                              LocalDate startDate, LocalDate endDate, Integer numberOfDays,
                              String description, String status, boolean isOverLimit) {
        createAbsence(userId, userEmail, userFullName, startDate, endDate, numberOfDays,
                     description, status, isOverLimit, null);
    }

    private void createAbsence(Long userId, String userEmail, String userFullName,
                              LocalDate startDate, LocalDate endDate, Integer numberOfDays,
                              String description, String status, boolean isOverLimit, String rejectReason) {
        Absence absence = new Absence();
        absence.setUserId(userId);
        absence.setUserEmail(userEmail);
        absence.setUserFullName(userFullName);
        absence.setStartDate(startDate);
        absence.setEndDate(endDate);
        absence.setNumberOfDays(numberOfDays);
        absence.setDescription(description);
        absence.setStatus(status);
        absence.setIsOverLimit(isOverLimit);
        
        if ("REJECTED".equals(status)) {
            absence.setResultStatus("REJECTED");
            absence.setRejectReason(rejectReason);
            absence.setProcessedAt(LocalDateTime.now().minusDays(1));
        } else if ("APPROVED".equals(status)) {
            absence.setResultStatus("APPROVED");
            absence.setProcessedAt(LocalDateTime.now().minusDays(2));
        }
        
        // Set created time based on start date
        if (startDate.isBefore(LocalDate.now())) {
            absence.setCreatedAt(startDate.minusDays(7).atStartOfDay());
        } else {
            absence.setCreatedAt(LocalDateTime.now().minusDays(3));
        }
        
        Absence savedAbsence = absenceRepository.save(absence);
        System.out.println("✅ [AbsenceSeeder] Saved absence ID: " + savedAbsence.getId() + " for " + userFullName);
    }
} 