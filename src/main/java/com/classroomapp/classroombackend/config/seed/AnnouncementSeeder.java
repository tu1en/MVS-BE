package com.classroomapp.classroombackend.config.seed;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.CreateAnnouncementDto;
import com.classroomapp.classroombackend.model.Announcement;
import com.classroomapp.classroombackend.model.AnnouncementAttachment;
import com.classroomapp.classroombackend.model.AnnouncementRead;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AnnouncementAttachmentRepository;
import com.classroomapp.classroombackend.repository.AnnouncementReadRepository;
import com.classroomapp.classroombackend.repository.AnnouncementRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AnnouncementService;

@Component
public class AnnouncementSeeder {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementAttachmentRepository announcementAttachmentRepository;
    private final AnnouncementReadRepository announcementReadRepository;
    private final UserRepository userRepository;
    private final AnnouncementService announcementService;
    private final ClassroomRepository classroomRepository;
    private final Random random = new Random();

    public AnnouncementSeeder(AnnouncementRepository announcementRepository, 
                            AnnouncementAttachmentRepository announcementAttachmentRepository,
                            AnnouncementReadRepository announcementReadRepository,
                            UserRepository userRepository, 
                            AnnouncementService announcementService, 
                            ClassroomRepository classroomRepository) {
        this.announcementRepository = announcementRepository;
        this.announcementAttachmentRepository = announcementAttachmentRepository;
        this.announcementReadRepository = announcementReadRepository;
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
            
            // Seed announcement attachments and reads
            seedAnnouncementAttachments();
            seedAnnouncementReads();
    }
    
    private void seedAnnouncementAttachments() {
        if (announcementAttachmentRepository.count() > 0) {
            System.out.println("✅ [AnnouncementSeeder] Announcement attachments already exist. Skipping.");
            return;
        }
        
        List<Announcement> announcements = announcementRepository.findAll();
        if (announcements.isEmpty()) {
            System.out.println("⚠️ [AnnouncementSeeder] No announcements found for attachments seeding.");
            return;
        }
        
        String attachmentDir = "backend/doproject/uploads/announcements";
        createDirectoryIfNotExists(attachmentDir);
        
        try {
            // Create sample attachments for each announcement
            for (Announcement announcement : announcements) {
                // PDF attachment
                String pdfFileName = "announcement_" + announcement.getId() + "_document.pdf";
                String pdfPath = attachmentDir + "/" + pdfFileName;
                createSampleFile(pdfPath, "Sample announcement document content for: " + announcement.getTitle());
                
                AnnouncementAttachment pdfAttachment = new AnnouncementAttachment();
                pdfAttachment.setAnnouncementId(announcement.getId());
                pdfAttachment.setFileName(pdfFileName);
                pdfAttachment.setFilePath("/uploads/announcements/" + pdfFileName);
                pdfAttachment.setFileType("application/pdf");
                pdfAttachment.setFileSize(Files.size(Paths.get(pdfPath)));
                pdfAttachment.setUploadedAt(LocalDateTime.now().minusDays(random.nextInt(7)));
                announcementAttachmentRepository.save(pdfAttachment);
                
                // Image attachment
                String imgFileName = "announcement_" + announcement.getId() + "_image.jpg";
                String imgPath = attachmentDir + "/" + imgFileName;
                createSampleFile(imgPath, "Sample image content for announcement: " + announcement.getTitle());
                
                AnnouncementAttachment imgAttachment = new AnnouncementAttachment();
                imgAttachment.setAnnouncementId(announcement.getId());
                imgAttachment.setFileName(imgFileName);
                imgAttachment.setFilePath("/uploads/announcements/" + imgFileName);
                imgAttachment.setFileType("image/jpeg");
                imgAttachment.setFileSize(Files.size(Paths.get(imgPath)));
                imgAttachment.setUploadedAt(LocalDateTime.now().minusDays(random.nextInt(5)));
                announcementAttachmentRepository.save(imgAttachment);
            }
            
            System.out.println("✅ [AnnouncementSeeder] Created " + (announcements.size() * 2) + " announcement attachments.");
            
        } catch (Exception e) {
            System.out.println("❌ [AnnouncementSeeder] Error creating attachments: " + e.getMessage());
        }
    }
    
    private void seedAnnouncementReads() {
        if (announcementReadRepository.count() > 0) {
            System.out.println("✅ [AnnouncementSeeder] Announcement reads already exist. Skipping.");
            return;
        }
        
        List<Announcement> announcements = announcementRepository.findAll();
        List<User> users = userRepository.findAll();
        
        if (announcements.isEmpty() || users.isEmpty()) {
            System.out.println("⚠️ [AnnouncementSeeder] No announcements or users found for reads seeding.");
            return;
        }
        
        // Simulate some users reading announcements
        for (Announcement announcement : announcements) {
            // Random 60-80% of users read each announcement
            int readCount = (int) (users.size() * (0.6 + random.nextDouble() * 0.2));
            
            for (int i = 0; i < readCount && i < users.size(); i++) {
                User user = users.get(i);
                
                AnnouncementRead read = new AnnouncementRead();
                read.setAnnouncementId(announcement.getId());
                read.setUserId(user.getId());
                read.setReadAt(LocalDateTime.now().minusDays(random.nextInt(30)).minusHours(random.nextInt(24)));
                
                announcementReadRepository.save(read);
            }
        }
        
        System.out.println("✅ [AnnouncementSeeder] Created announcement read records for " + announcements.size() + " announcements.");
    }
    
    private void createDirectoryIfNotExists(String dirPath) {
        try {
            Files.createDirectories(Paths.get(dirPath));
        } catch (IOException e) {
            System.out.println("❌ [AnnouncementSeeder] Error creating directory " + dirPath + ": " + e.getMessage());
        }
    }
    
    private void createSampleFile(String filePath, String content) throws IOException {
        Files.write(Paths.get(filePath), content.getBytes());
    }
} 