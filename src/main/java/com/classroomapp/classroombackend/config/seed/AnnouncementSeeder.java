package com.classroomapp.classroombackend.config.seed;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.CreateAnnouncementDto;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AnnouncementRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AnnouncementService;

@Component
public class AnnouncementSeeder {

    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;
    private final AnnouncementService announcementService;
    private final ClassroomRepository classroomRepository;

    public AnnouncementSeeder(AnnouncementRepository announcementRepository, UserRepository userRepository, AnnouncementService announcementService, ClassroomRepository classroomRepository) {
        this.announcementRepository = announcementRepository;
        this.userRepository = userRepository;
        this.announcementService = announcementService;
        this.classroomRepository = classroomRepository;
    }

    @Transactional
    public void seed() {
        if (announcementRepository.count() > 0) {
            System.out.println("✅ [AnnouncementSeeder] Announcements already exist. Seeding more for testing.");
        }

        // Find users by role for creating announcements
        User admin = userRepository.findByRoleId(4).stream().findFirst().orElse(null); // ADMIN
        User teacher = userRepository.findByRoleId(2).stream().findFirst().orElse(null); // TEACHER
        List<Classroom> classrooms = classroomRepository.findAll();

        if (admin == null || teacher == null) {
            System.out.println("⚠️ [AnnouncementSeeder] Admin or Teacher users not found. Skipping announcement seeding.");
            return;
        }

            if (classrooms.isEmpty()) {
                System.out.println("⚠️ [AnnouncementSeeder] No classrooms found. Skipping classroom-specific announcements.");
            }

            Classroom classroom1 = classrooms.stream().findFirst().orElse(null);

            // Announcement 1: Global from Admin
            CreateAnnouncementDto announcement1Dto = new CreateAnnouncementDto();
            announcement1Dto.setTitle("Chào mừng đến với hệ thống học tập trực tuyến mới");
            announcement1Dto.setContent("Chúng tôi vui mừng thông báo ra mắt hệ thống quản lý lớp học mới. Hệ thống cung cấp nhiều tính năng hữu ích cho cả giáo viên và học sinh.");
            announcement1Dto.setTargetAudience("ALL");
            announcement1Dto.setPriority("HIGH");
            announcementService.createAnnouncement(announcement1Dto, admin.getId());

            // Announcement 2: Classroom-specific from Teacher
            if (classroom1 != null) {
                CreateAnnouncementDto announcement2Dto = new CreateAnnouncementDto();
                announcement2Dto.setTitle("Thông báo về lịch thi giữa kỳ");
                announcement2Dto.setContent("Lịch thi giữa kỳ môn học sẽ diễn ra vào tuần tới. Chi tiết về thời gian và địa điểm sẽ được cập nhật sớm.");
                announcement2Dto.setClassroomId(classroom1.getId());
                announcement2Dto.setTargetAudience("STUDENTS");
                announcement2Dto.setPriority("NORMAL");
                announcementService.createAnnouncement(announcement2Dto, teacher.getId());
            }

            // Announcement 3: System maintenance
            CreateAnnouncementDto announcement3Dto = new CreateAnnouncementDto();
            announcement3Dto.setTitle("Thông báo bảo trì hệ thống");
            announcement3Dto.setContent("Hệ thống sẽ được bảo trì vào lúc 2 giờ sáng Chủ Nhật tuần này. Vui lòng lưu lại công việc của bạn trước thời gian này.");
            announcement3Dto.setTargetAudience("ALL");
            announcement3Dto.setPriority("URGENT");
            announcementService.createAnnouncement(announcement3Dto, admin.getId());
            
            System.out.println("✅ [AnnouncementSeeder] Created 3 sample announcements and their notifications.");
    }
} 