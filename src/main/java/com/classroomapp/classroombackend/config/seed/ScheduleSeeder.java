package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ScheduleSeeder {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    public ScheduleSeeder(ScheduleRepository scheduleRepository, UserRepository userRepository, ClassroomRepository classroomRepository) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
    }

    public void seed() {
        if (scheduleRepository.count() > 0) {
            log.info("✅ [ScheduleSeeder] Schedules already exist, skipping seeding.");
            return;
        }

        log.info("🔄 [ScheduleSeeder] No schedules found, starting seeding process...");

        List<Classroom> classrooms = classroomRepository.findAll();
        List<User> teachers = userRepository.findByRoleId(2); // Role ID for TEACHER

        if (classrooms.isEmpty() || teachers.isEmpty()) {
            log.warn("⚠️ [ScheduleSeeder] Not enough classrooms or teachers to seed schedules. Skipping.");
            return;
        }

        int scheduleCount = 0;
        scheduleCount += createJulyAugustSchedules(classrooms, teachers);

        if (scheduleCount > 0) {
            log.info("✅ [ScheduleSeeder] Successfully created {} schedules.", scheduleCount);
        } else {
            log.warn("⚠️ [ScheduleSeeder] Could not create any schedules.");
        }
    }

    private int createJulyAugustSchedules(List<Classroom> classrooms, List<User> teachers) {
        int count = 0;

        User teacher2 = teachers.stream().filter(t -> t.getId().equals(2L)).findFirst().orElse(null);
        User teacher5 = teachers.stream().filter(t -> t.getId().equals(5L)).findFirst().orElse(null);
        Classroom classroom1 = classrooms.stream().filter(c -> c.getId().equals(1L)).findFirst().orElse(null);
        Classroom classroom2 = classrooms.stream().filter(c -> c.getId().equals(2L)).findFirst().orElse(null);

        // Teacher 2 schedules (Nguyễn Văn Minh - Java)
        if (teacher2 != null && classroom1 != null) {
            ScheduleData[] teacher2Schedules = {
                new ScheduleData("Lập trình Java cơ bản - Kỳ 3", "Giới thiệu về Java Programming",
                        "2025-07-28T09:00", "2025-07-28T11:00", "Phòng A101", "#4CAF50"),
                new ScheduleData("Java Collections Framework", "Thực hành Java Collections",
                        "2025-07-30T14:00", "2025-07-30T16:00", "Phòng A101", "#4CAF50"),
                new ScheduleData("Java OOP Advanced", "Inheritance và Polymorphism",
                        "2025-08-01T09:00", "2025-08-01T11:00", "Phòng A101", "#4CAF50"),
                new ScheduleData("Java Spring Boot", "RESTful API Development",
                        "2025-08-05T14:00", "2025-08-05T16:00", "Phòng A101", "#4CAF50")
            };

            for (ScheduleData data : teacher2Schedules) {
                Schedule schedule = createSchedule(teacher2, classroom1, data);
                if (schedule != null) {
                    scheduleRepository.save(schedule);
                    count++;
                    log.info("📅 Created Java schedule: {} on {}", data.title, data.startDateTime);
                }
            }
        }

        // Teacher 5 schedules (Trần Văn Đức - Math)
        if (teacher5 != null && classroom2 != null) {
            ScheduleData[] teacher5Schedules = {
                new ScheduleData("Toán rời rạc - Kỳ 2", "Giới thiệu về Đạo hàm",
                        "2025-07-29T08:00", "2025-07-29T10:00", "Phòng B201", "#FF9800"),
                new ScheduleData("Giải tích 1", "Tích phân và Ứng dụng",
                        "2025-08-01T14:00", "2025-08-01T16:00", "Phòng B201", "#FF9800"),
                new ScheduleData("Đại số tuyến tính", "Ma trận và Định thức",
                        "2025-08-02T08:00", "2025-08-02T10:00", "Phòng B201", "#FF9800"),
                new ScheduleData("Xác suất thống kê", "Phân phối xác suất",
                        "2025-08-06T10:00", "2025-08-06T12:00", "Phòng B201", "#FF9800")
            };

            for (ScheduleData data : teacher5Schedules) {
                Schedule schedule = createSchedule(teacher5, classroom2, data);
                if (schedule != null) {
                    scheduleRepository.save(schedule);
                    count++;
                    log.info("📅 Created Math schedule: {} on {}", data.title, data.startDateTime);
                }
            }
        }

        // Add more schedules for other teachers
        count += createAdditionalSchedules(classrooms, teachers);
        return count;
    }

    private int createAdditionalSchedules(List<Classroom> classrooms, List<User> teachers) {
        int count = 0;

        // Create schedules for other teachers (ID 6, 7, 13, 14)
        for (User teacher : teachers) {
            Long teacherId = teacher.getId();
            
            // Skip teachers we already handled
            if (teacherId.equals(2L) || teacherId.equals(5L)) {
                continue;
            }
            
            // Find classroom for this teacher
            Classroom classroom = classrooms.stream()
                .filter(c -> c.getTeacher() != null && c.getTeacher().getId().equals(teacherId))
                .findFirst()
                .orElse(null);
                
            if (classroom == null) continue;

            String subject = getSubjectByTeacherId(teacherId);
            String roomCode = "Room-" + (classroom.getId() * 100);
            String colorCode = getColorByTeacherId(teacherId);

            ScheduleData[] additionalSchedules = {
                new ScheduleData(subject + " - Buổi 1", "Bài giảng lý thuyết",
                        "2025-07-29T10:00", "2025-07-29T12:00", roomCode, colorCode),
                new ScheduleData(subject + " - Buổi 2", "Thực hành và Bài tập",
                        "2025-08-01T08:00", "2025-08-01T10:00", roomCode, colorCode),
                new ScheduleData(subject + " - Buổi 3", "Ôn tập và Kiểm tra",
                        "2025-08-05T14:00", "2025-08-05T16:00", roomCode, colorCode)
            };

            for (ScheduleData data : additionalSchedules) {
                Schedule schedule = createSchedule(teacher, classroom, data);
                if (schedule != null) {
                    scheduleRepository.save(schedule);
                    count++;
                    log.info("📅 Created {} schedule: {} on {}", subject, data.title, data.startDateTime);
                }
            }
        }

        return count;
    }

    private Schedule createSchedule(User teacher, Classroom classroom, ScheduleData data) {
        try {
            LocalDateTime startDateTime = LocalDateTime.parse(data.startDateTime);
            LocalDateTime endDateTime = LocalDateTime.parse(data.endDateTime);

            Schedule schedule = new Schedule();
            schedule.setTeacher(teacher);
            schedule.setClassroom(classroom);
            schedule.setTitle(data.title);
            schedule.setDescription(data.description);
            schedule.setStartDatetime(startDateTime);
            schedule.setEndDatetime(endDateTime);
            schedule.setLocation(data.location);
            schedule.setColor(data.color);
            schedule.setMaterialsUrl("https://docs.google.com/document/d/example");
            schedule.setMeetUrl("https://meet.google.com/lookup/example");

            // ✅ FIXED: Auto-calculation will happen in @PrePersist
            // No need to manually set dayOfWeek, startTime, endTime
            // They will be calculated from startDatetime and endDatetime

            // Set other fields
            schedule.setSubject(data.title);
            schedule.setRoom(data.location);
            schedule.setIsCancelled(false);
            schedule.setIsRecurring(false);

            return schedule;

        } catch (Exception e) {
            log.error("❌ Error creating schedule for teacher {}: {}", 
                teacher.getId(), e.getMessage(), e);
            return null;
        }
    }

    private String getSubjectByTeacherId(Long teacherId) {
        switch (teacherId.intValue()) {
            case 6: return "Văn Học Việt Nam"; // Phạm Thị Lan
            case 7: return "English Literature"; // Lê Hoàng Nam
            case 13: return "Hóa Học Đại Cương"; // Vũ Thị Hương
            case 14: return "Vật Lý Đại Cương"; // Đặng Minh Tuấn
            default: return "Môn Học Chung";
        }
    }

    private String getColorByTeacherId(Long teacherId) {
        switch (teacherId.intValue()) {
            case 6: return "#E91E63"; // Pink for Literature
            case 7: return "#3F51B5"; // Indigo for English
            case 13: return "#009688"; // Teal for Chemistry
            case 14: return "#795548"; // Brown for Physics
            default: return "#607D8B"; // Blue Grey
        }
    }

    private static class ScheduleData {
        final String title;
        final String description;
        final String startDateTime;
        final String endDateTime;
        final String location;
        final String color;

        ScheduleData(String title, String description, String startDateTime, String endDateTime, String location, String color) {
            this.title = title;
            this.description = description;
            this.startDateTime = startDateTime;
            this.endDateTime = endDateTime;
            this.location = location;
            this.color = color;
        }
    }
}