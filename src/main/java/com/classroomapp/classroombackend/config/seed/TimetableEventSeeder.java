package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.TimetableEvent;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.TimetableEventRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class TimetableEventSeeder {

    private final TimetableEventRepository timetableEventRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;

    public TimetableEventSeeder(TimetableEventRepository timetableEventRepository, UserRepository userRepository, ClassroomRepository classroomRepository) {
        this.timetableEventRepository = timetableEventRepository;
        this.userRepository = userRepository;
        this.classroomRepository = classroomRepository;
    }

    public void seed() {
        if (timetableEventRepository.count() > 0) {
            System.out.println("✅ [TimetableEventSeeder] Timetable events already exist, skipping seeding.");
            return;
        }

        System.out.println("🔄 [TimetableEventSeeder] Starting timetable event seeding...");

        List<User> users = userRepository.findAll();
        List<Classroom> classrooms = classroomRepository.findAll();

        // Comprehensive data validation
        if (users.isEmpty()) {
            System.out.println("⚠️ [TimetableEventSeeder] No users found in database. Cannot create timetable events.");
            return;
        }

        if (classrooms.isEmpty()) {
            System.out.println("⚠️ [TimetableEventSeeder] No classrooms found in database. Cannot create timetable events.");
            return;
        }

        System.out.println("📊 [TimetableEventSeeder] Found " + users.size() + " users and " + classrooms.size() + " classrooms");

        // Safe user selection with role-based fallback
        User teacher = findUserByRole(users, 2).orElse(users.isEmpty() ? null : users.get(0)); // Role 2 = TEACHER
        User admin = findUserByRole(users, 4).orElse(users.isEmpty() ? null : users.get(Math.min(users.size() - 1, 0))); // Role 4 = ADMIN

        // Validate that we have required users
        if (teacher == null || admin == null) {
            System.out.println("❌ [TimetableEventSeeder] Cannot find required users (teacher or admin). Skipping seeding.");
            return;
        }

        System.out.println("👨‍🏫 [TimetableEventSeeder] Selected teacher: " + teacher.getFullName() + " (ID: " + teacher.getId() + ")");
        System.out.println("👨‍💼 [TimetableEventSeeder] Selected admin: " + admin.getFullName() + " (ID: " + admin.getId() + ")");

        // Create events dynamically based on available classrooms
        LocalDateTime weekStart = LocalDateTime.now().with(java.time.DayOfWeek.MONDAY).withHour(0).withMinute(0);

        int eventsCreated = 0;

        // Create class events for each available classroom (up to 5 events)
        eventsCreated += createClassEvents(classrooms, teacher, weekStart);

        // Create exam events
        eventsCreated += createExamEvents(classrooms, teacher, weekStart);

        // Create assignment due events
        eventsCreated += createAssignmentDueEvents(classrooms, teacher, weekStart);

        System.out.println("✅ [TimetableEventSeeder] Successfully created " + eventsCreated + " timetable events with Vietnamese content.");
    }

    /**
     * Helper method to find user by role ID with safe fallback
     */
    private Optional<User> findUserByRole(List<User> users, int roleId) {
        return users.stream()
                .filter(user -> user.getRoleId() != null && user.getRoleId().equals(roleId))
                .findFirst();
    }

    /**
     * Create class events for available classrooms with Vietnamese content
     */
    private int createClassEvents(List<Classroom> classrooms, User teacher, LocalDateTime weekStart) {
        String[] classSubjects = {
            "Toán Cao Cấp",
            "Lập Trình Java",
            "Cơ Sở Dữ Liệu",
            "Mạng Máy Tính",
            "Kỹ Thuật Phần Mềm"
        };

        String[] classDescriptions = {
            "Học về đạo hàm, tích phân và ứng dụng trong thực tế",
            "Lập trình hướng đối tượng với Java và Spring Framework",
            "Thiết kế và quản lý cơ sở dữ liệu quan hệ",
            "Kiến thức về giao thức mạng và bảo mật",
            "Quy trình phát triển phần mềm và quản lý dự án"
        };

        String[] locations = {
            "Phòng 101",
            "Phòng Máy Tính 201",
            "Phòng Thực Hành 301",
            "Phòng Hội Thảo 401",
            "Phòng Lab 501"
        };

        String[] colors = {"#52c41a", "#722ed1", "#fa8c16", "#13c2c2", "#eb2f96"};

        int eventsCreated = 0;
        int maxEvents = Math.min(classrooms.size(), 5); // Limit to 5 events or available classrooms

        for (int i = 0; i < maxEvents; i++) {
            Classroom classroom = classrooms.get(i);

            TimetableEvent classEvent = new TimetableEvent();
            classEvent.setTitle(classSubjects[i % classSubjects.length]);
            classEvent.setDescription(classDescriptions[i % classDescriptions.length]);

            // Distribute events across weekdays (Monday to Friday)
            LocalDateTime eventStart = weekStart.plusDays(i % 5).withHour(8 + (i * 2)).withMinute(0);
            classEvent.setStartDatetime(eventStart);
            classEvent.setEndDatetime(eventStart.plusHours(1).plusMinutes(30));

            classEvent.setEventType(TimetableEvent.EventType.CLASS);
            classEvent.setClassroomId(classroom.getId());
            classEvent.setCreatedBy(teacher.getId());
            classEvent.setLocation(locations[i % locations.length]);
            classEvent.setColor(colors[i % colors.length]);

            timetableEventRepository.save(classEvent);
            eventsCreated++;

            System.out.println("📅 [TimetableEventSeeder] Created class event: " + classEvent.getTitle() + " for classroom: " + classroom.getName());
        }

        return eventsCreated;
    }

    /**
     * Create exam events with Vietnamese content
     */
    private int createExamEvents(List<Classroom> classrooms, User teacher, LocalDateTime weekStart) {
        if (classrooms.isEmpty()) return 0;

        LocalDateTime nextWeekStart = weekStart.plusWeeks(1);

        TimetableEvent examEvent = new TimetableEvent();
        examEvent.setTitle("Kiểm Tra Giữa Kỳ - Toán Cao Cấp");
        examEvent.setDescription("Bài kiểm tra toàn diện về đạo hàm, tích phân và ứng dụng. Thời gian: 120 phút.");
        examEvent.setStartDatetime(nextWeekStart.withHour(9).withMinute(0));
        examEvent.setEndDatetime(nextWeekStart.withHour(11).withMinute(0));
        examEvent.setEventType(TimetableEvent.EventType.EXAM);
        examEvent.setClassroomId(classrooms.isEmpty() ? 1L : classrooms.get(0).getId()); // Safe access to first classroom
        examEvent.setCreatedBy(teacher.getId());
        examEvent.setLocation("Phòng Thi A");
        examEvent.setColor("#f5222d");
        examEvent.setReminderMinutes(60);

        timetableEventRepository.save(examEvent);

        System.out.println("📝 [TimetableEventSeeder] Created exam event: " + examEvent.getTitle());
        return 1;
    }

    /**
     * Create assignment due events with Vietnamese content
     */
    private int createAssignmentDueEvents(List<Classroom> classrooms, User teacher, LocalDateTime weekStart) {
        if (classrooms.isEmpty()) return 0;

        TimetableEvent assignmentEvent = new TimetableEvent();
        assignmentEvent.setTitle("Hạn Nộp Bài Tập - Lập Trình Java");
        assignmentEvent.setDescription("Nộp bài tập về lập trình hướng đối tượng và xử lý ngoại lệ trong Java.");
        assignmentEvent.setStartDatetime(weekStart.plusDays(3).withHour(23).withMinute(59));
        assignmentEvent.setEndDatetime(weekStart.plusDays(3).withHour(23).withMinute(59));
        assignmentEvent.setEventType(TimetableEvent.EventType.ASSIGNMENT_DUE);
        assignmentEvent.setClassroomId(classrooms.isEmpty() ? 1L : classrooms.get(0).getId()); // Safe access to first classroom
        assignmentEvent.setCreatedBy(teacher.getId());
        assignmentEvent.setLocation("Nộp Trực Tuyến");
        assignmentEvent.setColor("#faad14");
        assignmentEvent.setReminderMinutes(1440); // 24 hours reminder

        timetableEventRepository.save(assignmentEvent);

        System.out.println("📋 [TimetableEventSeeder] Created assignment due event: " + assignmentEvent.getTitle());
        return 1;
    }
} 