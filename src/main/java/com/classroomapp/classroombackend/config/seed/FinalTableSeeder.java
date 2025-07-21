package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.Absence;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.model.administration.SystemMonitoring;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.Syllabus;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.absencemanagement.AbsenceRepository;
import com.classroomapp.classroombackend.repository.administration.SystemMonitoringRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.SyllabusRepository;
import com.classroomapp.classroombackend.repository.requestmanagement.RequestRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

/**
 * Final seeder để seed những tables quan trọng nhất còn empty
 * Mục tiêu: Giảm tối đa số lượng empty tables
 */
@Component
public class FinalTableSeeder {

    @Autowired private UserRepository userRepository;
    @Autowired private ClassroomRepository classroomRepository;
    @Autowired private AbsenceRepository absenceRepository;
    @Autowired private RequestRepository requestRepository;
    @Autowired private SystemMonitoringRepository systemMonitoringRepository;
    @Autowired private SyllabusRepository syllabusRepository;

    private final Random random = new Random();

    @Transactional
    public void seed() {
        System.out.println("🚀 [FinalTableSeeder] Starting final seeding for remaining empty tables...");
        
        try {
            seedAbsences();
            seedRequests();
            seedSystemMonitoring();
            seedSyllabi();
            
            System.out.println("✅ [FinalTableSeeder] Final seeding completed!");
            
        } catch (Exception e) {
            System.out.println("❌ [FinalTableSeeder] Error during final seeding: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void seedAbsences() {
        if (absenceRepository.count() == 0) {
            System.out.println("🔄 [FinalTableSeeder] Seeding absences...");

            List<User> users = userRepository.findAll();

            if (!users.isEmpty()) {
                for (int i = 0; i < 25; i++) {
                    Absence absence = new Absence();
                    User randomUser = users.get(random.nextInt(users.size()));

                    LocalDate startDate = LocalDate.now().minusDays(random.nextInt(60));
                    LocalDate endDate = startDate.plusDays(1 + random.nextInt(5)); // 1-5 days leave

                    absence.setUserId(randomUser.getId());
                    absence.setUserEmail(randomUser.getEmail());
                    absence.setUserFullName(randomUser.getFullName());
                    absence.setStartDate(startDate);
                    absence.setEndDate(endDate);
                    absence.setNumberOfDays((int) (endDate.toEpochDay() - startDate.toEpochDay() + 1));
                    absence.setDescription("Lý do nghỉ phép: " + getRandomAbsenceReason());
                    absence.setStatus(random.nextBoolean() ? "APPROVED" : "PENDING");
                    absence.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));

                    if ("APPROVED".equals(absence.getStatus())) {
                        absence.setResultStatus("APPROVED");
                        absence.setProcessedAt(absence.getCreatedAt().plusDays(1));
                        absence.setProcessedBy(1L); // Admin user
                    }

                    absenceRepository.save(absence);
                }
                System.out.println("✅ Created 25 absences");
            }
        }
    }
    
    private void seedRequests() {
        if (requestRepository.count() == 0) {
            System.out.println("🔄 [FinalTableSeeder] Seeding requests...");

            List<User> users = userRepository.findAll();

            if (!users.isEmpty()) {
                String[] requestedRoles = {"STUDENT", "TEACHER"};
                String[] statuses = {"PENDING", "APPROVED", "REJECTED", "COMPLETED"};

                for (int i = 0; i < 30; i++) {
                    Request request = new Request();
                    User randomUser = users.get(random.nextInt(users.size()));

                    request.setEmail(randomUser.getEmail());
                    request.setFullName(randomUser.getFullName());
                    request.setPhoneNumber("0" + (900000000 + random.nextInt(99999999))); // Random phone
                    request.setRequestedRole(requestedRoles[random.nextInt(requestedRoles.length)]);
                    request.setFormResponses("{\"reason\":\"Yêu cầu đăng ký tài khoản " + (i + 1) + "\"}");
                    request.setStatus(statuses[random.nextInt(statuses.length)]);
                    request.setCreatedAt(LocalDateTime.now().minusDays(random.nextInt(30)));

                    if (!"PENDING".equals(request.getStatus())) {
                        request.setResultStatus(request.getStatus().equals("APPROVED") ? "APPROVED" : "REJECTED");
                        request.setProcessedAt(request.getCreatedAt().plusDays(1 + random.nextInt(3)));

                        if ("REJECTED".equals(request.getResultStatus())) {
                            request.setRejectReason("Không đủ điều kiện hoặc thông tin không chính xác");
                        }
                    }

                    requestRepository.save(request);
                }
                System.out.println("✅ Created 30 requests");
            }
        }
    }
    
    private void seedSystemMonitoring() {
        if (systemMonitoringRepository.count() == 0) {
            System.out.println("🔄 [FinalTableSeeder] Seeding system monitoring...");
            
            String[] metrics = {"CPU_USAGE", "MEMORY_USAGE", "DISK_USAGE", "NETWORK_IO", "DATABASE_CONNECTIONS"};
            
            // Create monitoring data for last 7 days
            for (int day = 0; day < 7; day++) {
                LocalDateTime monitoringTime = LocalDateTime.now().minusDays(day);
                
                for (String metric : metrics) {
                    for (int hour = 0; hour < 24; hour += 3) { // Every 3 hours
                        SystemMonitoring monitoring = new SystemMonitoring();
                        monitoring.setMetricName(metric);
                        monitoring.setMetricValue(generateMetricValue(metric));
                        monitoring.setMetricUnit(getMetricUnit(metric));
                        monitoring.setTimestamp(monitoringTime.plusHours(hour));
                        monitoring.setHostName("APP-SERVER-01");
                        monitoring.setInstanceId("instance-01");
                        monitoring.setCategory(getMonitoringCategory(metric));
                        monitoring.setStatus(monitoring.getMetricValue() > 80.0 ?
                            SystemMonitoring.MetricStatus.WARNING : SystemMonitoring.MetricStatus.NORMAL);
                        
                        systemMonitoringRepository.save(monitoring);
                    }
                }
            }
            System.out.println("✅ Created system monitoring data for 7 days");
        }
    }
    
    private void seedSyllabi() {
        if (syllabusRepository.count() == 0) {
            System.out.println("🔄 [FinalTableSeeder] Seeding syllabi...");
            
            List<Classroom> classrooms = classroomRepository.findAll();
            
            if (!classrooms.isEmpty()) {
                for (Classroom classroom : classrooms) {
                    if (random.nextDouble() < 0.8) { // 80% of classrooms have syllabus
                        Syllabus syllabus = new Syllabus();
                        syllabus.setClassroom(classroom);
                        syllabus.setTitle("Đề cương môn học - " + classroom.getName());
                        syllabus.setContent(generateSyllabusContent(classroom.getName()));
                        syllabus.setLearningObjectives("Mục tiêu học tập cho môn " + classroom.getName());
                        syllabus.setRequiredMaterials("Tài liệu bắt buộc và tham khảo");
                        syllabus.setGradingCriteria("Tiêu chí đánh giá: Bài tập 30%, Kiểm tra 30%, Thi cuối kỳ 40%");

                        syllabusRepository.save(syllabus);
                    }
                }
                System.out.println("✅ Created syllabi for ~80% of classrooms");
            }
        }
    }
    
    private String getRandomAbsenceReason() {
        String[] reasons = {
            "Ốm đau", "Việc gia đình", "Đi công tác", "Tham gia hoạt động khác",
            "Vấn đề giao thông", "Khám bệnh", "Dự lễ cưới", "Tang lễ"
        };
        return reasons[random.nextInt(reasons.length)];
    }
    
    private Double generateMetricValue(String metric) {
        switch (metric) {
            case "CPU_USAGE":
            case "MEMORY_USAGE":
            case "DISK_USAGE":
                return 20.0 + random.nextDouble() * 60.0; // 20-80%
            case "NETWORK_IO":
                return random.nextDouble() * 100.0; // 0-100 MB/s
            case "DATABASE_CONNECTIONS":
                return (double) (10 + random.nextInt(40)); // 10-50 connections
            default:
                return random.nextDouble() * 100.0;
        }
    }
    
    private String getMetricUnit(String metric) {
        switch (metric) {
            case "CPU_USAGE":
            case "MEMORY_USAGE":
            case "DISK_USAGE":
                return "%";
            case "NETWORK_IO":
                return "MB/s";
            case "DATABASE_CONNECTIONS":
                return "connections";
            default:
                return "units";
        }
    }

    private SystemMonitoring.MonitoringCategory getMonitoringCategory(String metric) {
        switch (metric) {
            case "CPU_USAGE":
                return SystemMonitoring.MonitoringCategory.CPU;
            case "MEMORY_USAGE":
                return SystemMonitoring.MonitoringCategory.MEMORY;
            case "DISK_USAGE":
                return SystemMonitoring.MonitoringCategory.DISK;
            case "NETWORK_IO":
                return SystemMonitoring.MonitoringCategory.NETWORK;
            case "DATABASE_CONNECTIONS":
                return SystemMonitoring.MonitoringCategory.DATABASE;
            default:
                return SystemMonitoring.MonitoringCategory.SYSTEM;
        }
    }
    
    private String generateSyllabusContent(String className) {
        return String.format(
            "# Đề cương môn học: %s\n\n" +
            "## Mục tiêu học tập\n" +
            "- Nắm vững kiến thức cơ bản về môn học\n" +
            "- Phát triển kỹ năng thực hành\n" +
            "- Áp dụng kiến thức vào thực tế\n\n" +
            "## Nội dung chương trình\n" +
            "### Chương 1: Giới thiệu\n" +
            "### Chương 2: Kiến thức cơ bản\n" +
            "### Chương 3: Thực hành\n" +
            "### Chương 4: Ứng dụng\n\n" +
            "## Phương pháp đánh giá\n" +
            "- Bài tập: 30%%\n" +
            "- Kiểm tra giữa kỳ: 30%%\n" +
            "- Thi cuối kỳ: 40%%\n\n" +
            "## Tài liệu tham khảo\n" +
            "- Giáo trình chính\n" +
            "- Tài liệu bổ sung\n" +
            "- Bài tập thực hành",
            className
        );
    }
}
