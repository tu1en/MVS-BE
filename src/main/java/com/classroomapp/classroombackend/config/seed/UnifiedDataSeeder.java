package com.classroomapp.classroombackend.config.seed;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.dto.CreateAnnouncementDto;
import com.classroomapp.classroombackend.dto.assignmentmanagement.CreateAssignmentDto;
import com.classroomapp.classroombackend.model.Absence;
import com.classroomapp.classroombackend.model.Accomplishment;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.classroommanagement.Course;
import com.classroomapp.classroombackend.model.exammangement.Exam;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AccomplishmentRepository;
import com.classroomapp.classroombackend.repository.AnnouncementRepository;
import com.classroomapp.classroombackend.repository.CourseRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.absencemanagement.AbsenceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.exammangement.ExamRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.AnnouncementService;
import com.classroomapp.classroombackend.service.AssignmentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Unified Data Seeder - Single seeder for all profiles
 * Consolidates ALL seeding logic to avoid conflicts
 */
@Component
@RequiredArgsConstructor
@Order(1) // Run first and only
@Slf4j
public class UnifiedDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final AnnouncementRepository announcementRepository;
    private final AnnouncementService announcementService;
    private final AssignmentService assignmentService;
    private final AbsenceRepository absenceRepository;
    private final AccomplishmentRepository accomplishmentRepository;
    private final LectureRepository lectureRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final AttendanceRepository attendanceRepository;
    private final ScheduleRepository scheduleRepository;
    private final ExamRepository examRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 [UnifiedDataSeeder] Starting unified data seeding...");
        
        try {
            // Wait for Hibernate to create tables
            Thread.sleep(10000);
            
            // Wait for tables to be created
            waitForTablesToBeCreated();
            
            if (shouldSeedData()) {
                log.info("📊 [UnifiedDataSeeder] Database is empty, starting seeding...");
                seedRoles();
                seedUsers();
                seedCourses();
                seedClassrooms();
                seedEnrollments();
                seedAnnouncements();
                seedAbsences();
                seedAccomplishments();
                seedAssignments();
                seedSchedules();
                seedLectures();
                seedExams();
                seedAttendance();
                log.info("✅ [UnifiedDataSeeder] All data seeded successfully!");
            } else {
                log.info("ℹ️ [UnifiedDataSeeder] Data already exists, skipping seeding");
            }
        } catch (Exception e) {
            log.error("❌ [UnifiedDataSeeder] Error during seeding: {}", e.getMessage(), e);
            // Don't throw exception, just log it
            log.warn("⚠️ [UnifiedDataSeeder] Continuing application startup despite seeding error");
        }
    }

    @Transactional
    private boolean shouldSeedData() {
        try {
            long userCount = userRepository.count();
            long classroomCount = classroomRepository.count();
            long courseCount = courseRepository.count();
            log.info("📈 [UnifiedDataSeeder] Current data count: Users={}, Classrooms={}, Courses={}", userCount, classroomCount, courseCount);
            return userCount == 0 && classroomCount == 0 && courseCount == 0;
        } catch (Exception e) {
            log.warn("⚠️ [UnifiedDataSeeder] Error checking data count: {}", e.getMessage());
            return true; // Assume empty if error
        }
    }

    @Transactional
    private void seedRoles() {
        log.info("👥 [UnifiedDataSeeder] Seeding roles...");
        // Roles are handled by User entity RoleEnum
        log.info("✅ [UnifiedDataSeeder] Roles ready");
    }

    @Transactional
    private void seedUsers() {
        log.info("👤 [UnifiedDataSeeder] Seeding users...");
        
        // Create admin user
        createUserIfNotExists("admin", "admin@test.com", "Admin User", User.RoleEnum.ADMIN);
        
        // Create test students
        createUserIfNotExists("student1", "student1@test.com", "Nguyen Van A", User.RoleEnum.STUDENT);
        createUserIfNotExists("student2", "student2@test.com", "Tran Thi B", User.RoleEnum.STUDENT);
        createUserIfNotExists("student3", "student3@test.com", "Le Van C", User.RoleEnum.STUDENT);
        
        // Create test teachers
        createUserIfNotExists("teacher1", "teacher1@test.com", "Pham Thi D", User.RoleEnum.TEACHER);
        createUserIfNotExists("teacher2", "teacher2@test.com", "Hoang Van E", User.RoleEnum.TEACHER);
        
        // Create manager
        createUserIfNotExists("manager", "manager@test.com", "Manager User", User.RoleEnum.MANAGER);
        
        // Create accountant
        createUserIfNotExists("accountant", "accountant@test.com", "Accountant User", User.RoleEnum.ACCOUNTANT);
        
        log.info("✅ [UnifiedDataSeeder] Users seeded successfully");
    }

    private void createUserIfNotExists(String username, String email, String fullName, User.RoleEnum roleEnum) {
        if (!userRepository.existsByUsername(username) && !userRepository.existsByEmail(email)) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("password123")); // Default password
            user.setFullName(fullName);
            user.setRoleId(roleEnum.getId());
            user.setRoleEnum(roleEnum);
            user.setStatus("active");
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            // Set additional fields for teachers and accountants
            if (roleEnum == User.RoleEnum.TEACHER || roleEnum == User.RoleEnum.ACCOUNTANT) {
                user.setHireDate(LocalDate.now().minusYears(1));
                user.setDepartment(roleEnum == User.RoleEnum.TEACHER ? "Khoa Công Nghệ Thông Tin" : "Phòng Kế Toán");
                user.setPhoneNumber("0912345678");
                user.setAnnualLeaveBalance(12);
                user.setLeaveResetDate(LocalDate.now().plusYears(1));
            }
            
            userRepository.save(user);
            log.info("✅ [UnifiedDataSeeder] Created user: {} with role: {}", username, roleEnum.getName());
        } else {
            log.info("ℹ️ [UnifiedDataSeeder] User already exists: {}", username);
        }
    }

    private void seedCourses() {
        log.info("📚 [UnifiedDataSeeder] Seeding courses...");
        
        if (courseRepository.count() == 0) {
            createCourseIfNotExists("Advanced Mathematics", "A comprehensive study of mathematical concepts and their applications.");
            createCourseIfNotExists("World History", "A survey of major historical events from ancient civilizations to the modern era.");
            createCourseIfNotExists("Vietnamese Literature", "An exploration of Vietnamese literary works throughout history.");
            createCourseIfNotExists("Communicative English", "Developing English communication skills for an international environment.");
            createCourseIfNotExists("Computer Science", "Fundamental concepts of computer science and programming.");
            createCourseIfNotExists("General Physics", "An introduction to the fundamental principles of physics.");
            
            log.info("✅ [UnifiedDataSeeder] Created 6 sample courses.");
        } else {
            log.info("ℹ️ [UnifiedDataSeeder] Courses already exist");
        }
    }

    private void createCourseIfNotExists(String name, String description) {
        // Check if course exists by searching in all courses
        List<Course> existingCourses = courseRepository.findAll();
        boolean exists = existingCourses.stream()
                .anyMatch(course -> course.getName().equalsIgnoreCase(name));
        
        if (!exists) {
            Course course = new Course();
            course.setName(name);
            course.setDescription(description);
            courseRepository.save(course);
            log.info("✅ [UnifiedDataSeeder] Created course: {}", name);
        } else {
            log.info("ℹ️ [UnifiedDataSeeder] Course already exists: {}", name);
        }
    }

    private void seedClassrooms() {
        log.info("🏫 [UnifiedDataSeeder] Seeding classrooms...");
        
        User teacher1 = userRepository.findByUsername("teacher1").orElse(null);
        User teacher2 = userRepository.findByUsername("teacher2").orElse(null);
        
        if (teacher1 != null && teacher2 != null) {
            // Create classrooms for teacher1
            createClassroomIfNotExists(
                "Lập trình Java Nâng cao",
                "Khóa học Java từ cơ bản đến nâng cao, bao gồm OOP, Collections, Threading",
                "Programming",
                "Section A",
                teacher1
            );
            
            createClassroomIfNotExists(
                "Thiết kế Web với React",
                "Khóa học thiết kế web hiện đại với React, JSX, Components, Hooks",
                "Web Development", 
                "Section A",
                teacher1
            );
            
            // Create classrooms for teacher2
            createClassroomIfNotExists(
                "Cấu trúc dữ liệu và giải thuật",
                "Học về các cấu trúc dữ liệu cơ bản và giải thuật tìm kiếm, sắp xếp",
                "Computer Science",
                "Section B", 
                teacher2
            );
            
            createClassroomIfNotExists(
                "Cơ sở dữ liệu MySQL",
                "Khóa học về hệ quản trị cơ sở dữ liệu MySQL, SQL queries, optimization",
                "Database",
                "Section C",
                teacher2
            );
            
            createClassroomIfNotExists(
                "Android Development",
                "Phát triển ứng dụng Android với Java/Kotlin, UI/UX, APIs",
                "Mobile Development",
                "Section A",
                teacher1
            );
        } else {
            log.warn("⚠️ [UnifiedDataSeeder] Teachers not found, cannot create classrooms");
        }
        
        log.info("✅ [UnifiedDataSeeder] Classrooms seeded successfully");
    }

    private void createClassroomIfNotExists(String name, String description, String subject, String section, User teacher) {
        if (classroomRepository.findByNameContainingIgnoreCase(name).isEmpty()) {
            Classroom classroom = new Classroom();
            classroom.setName(name);
            classroom.setDescription(description);
            classroom.setSubject(subject);
            classroom.setSection(section);
            classroom.setTeacher(teacher);
            classroom.setCreatedAt(LocalDateTime.now());
            classroom.setUpdatedAt(LocalDateTime.now());
            
            classroomRepository.save(classroom);
            log.info("✅ [UnifiedDataSeeder] Created classroom: {}", name);
        } else {
            log.info("ℹ️ [UnifiedDataSeeder] Classroom already exists: {}", name);
        }
    }

    private void seedEnrollments() {
        log.info("📚 [UnifiedDataSeeder] Seeding enrollments...");
        
        List<User> students = userRepository.findByRoleId(User.RoleEnum.STUDENT.getId());
        List<Classroom> classrooms = classroomRepository.findAll();
        
        if (!students.isEmpty() && !classrooms.isEmpty()) {
            // Enroll student1 in multiple courses
            User student1 = userRepository.findByUsername("student1").orElse(null);
            if (student1 != null) {
                enrollStudentInClassroom(student1, findClassroomByName(classrooms, "Lập trình Java Nâng cao"), 75.0);
                enrollStudentInClassroom(student1, findClassroomByName(classrooms, "Cấu trúc dữ liệu và giải thuật"), 45.0);
                enrollStudentInClassroom(student1, findClassroomByName(classrooms, "Thiết kế Web với React"), 90.0);
            }
            
            // Enroll student2 in different courses
            User student2 = userRepository.findByUsername("student2").orElse(null);
            if (student2 != null) {
                enrollStudentInClassroom(student2, findClassroomByName(classrooms, "Lập trình Java Nâng cao"), 60.0);
                enrollStudentInClassroom(student2, findClassroomByName(classrooms, "Cơ sở dữ liệu MySQL"), 80.0);
                enrollStudentInClassroom(student2, findClassroomByName(classrooms, "Android Development"), 35.0);
            }
            
            // Enroll student3 in some courses
            User student3 = userRepository.findByUsername("student3").orElse(null);
            if (student3 != null) {
                enrollStudentInClassroom(student3, findClassroomByName(classrooms, "Cấu trúc dữ liệu và giải thuật"), 70.0);
                enrollStudentInClassroom(student3, findClassroomByName(classrooms, "Thiết kế Web với React"), 55.0);
                enrollStudentInClassroom(student3, findClassroomByName(classrooms, "Cơ sở dữ liệu MySQL"), 40.0);
            }
        } else {
            log.warn("⚠️ [UnifiedDataSeeder] No students or classrooms found for enrollment");
        }
        
        log.info("✅ [UnifiedDataSeeder] Enrollments seeded successfully");
    }

    private void seedAnnouncements() {
        log.info("📢 [UnifiedDataSeeder] Seeding announcements...");
        
        // Find users by role for creating announcements
        User admin = userRepository.findByRoleId(4).stream().findFirst().orElse(null); // ADMIN
        User teacher = userRepository.findByRoleId(2).stream().findFirst().orElse(null); // TEACHER
        List<Classroom> classrooms = classroomRepository.findAll();

        if (admin == null || teacher == null) {
            log.warn("⚠️ [UnifiedDataSeeder] Admin or Teacher users not found. Skipping announcement seeding.");
            return;
        }

        if (classrooms.isEmpty()) {
            log.warn("⚠️ [UnifiedDataSeeder] No classrooms found. Skipping classroom-specific announcements.");
        }

        Classroom classroom1 = classrooms.stream().findFirst().orElse(null);

        try {
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
            
            log.info("✅ [UnifiedDataSeeder] Created 3 sample announcements.");
        } catch (Exception e) {
            log.warn("⚠️ [UnifiedDataSeeder] Error creating announcements: {}", e.getMessage());
        }
    }

    private void seedAbsences() {
        log.info("🏃 [UnifiedDataSeeder] Seeding absences...");
        
        if (absenceRepository.count() > 0) {
            log.info("ℹ️ [UnifiedDataSeeder] Absences already exist, skipping");
            return;
        }

        // Verify that users exist before creating absence records
        long userCount = userRepository.count();
        if (userCount == 0) {
            log.warn("⚠️ [UnifiedDataSeeder] No users found. Skipping absence seeding.");
            return;
        }

        try {
            log.info("🔨 [UnifiedDataSeeder] Creating absence records...");
            
            // Get teachers
            List<User> teachers = userRepository.findByRoleId(User.RoleEnum.TEACHER.getId());
            if (!teachers.isEmpty()) {
                User teacher1 = teachers.get(0);
                
                // Create absences for teacher1
                createAbsence(teacher1.getId(), teacher1.getEmail(), teacher1.getFullName(),
                    LocalDate.now().minusDays(30), LocalDate.now().minusDays(28), 3,
                    "Nghỉ phép để tham gia hội thảo giáo dục về công nghệ thông tin", "APPROVED");
                
                createAbsence(teacher1.getId(), teacher1.getEmail(), teacher1.getFullName(),
                    LocalDate.now().minusDays(15), LocalDate.now().minusDays(14), 2,
                    "Nghỉ ốm do cảm cúm mùa", "APPROVED");
            }

            // Get accountant
            List<User> accountants = userRepository.findByRoleId(User.RoleEnum.ACCOUNTANT.getId());
            if (!accountants.isEmpty()) {
                User accountant = accountants.get(0);
                
                createAbsence(accountant.getId(), accountant.getEmail(), accountant.getFullName(),
                    LocalDate.now().minusDays(10), LocalDate.now().minusDays(9), 2,
                    "Nghỉ phép kiểm toán cuối năm", "APPROVED");
                
                createAbsence(accountant.getId(), accountant.getEmail(), accountant.getFullName(),
                    LocalDate.now().plusDays(3), LocalDate.now().plusDays(3), 1,
                    "Nghỉ phép cá nhân", "PENDING");
            }

            log.info("✅ [UnifiedDataSeeder] Created sample absence requests.");
        } catch (Exception e) {
            log.warn("⚠️ [UnifiedDataSeeder] Error creating absences: {}", e.getMessage());
        }
    }

    private void createAbsence(Long userId, String userEmail, String userFullName,
                              LocalDate startDate, LocalDate endDate, Integer numberOfDays,
                              String description, String status) {
        Absence absence = new Absence();
        absence.setUserId(userId);
        absence.setUserEmail(userEmail);
        absence.setUserFullName(userFullName);
        absence.setStartDate(startDate);
        absence.setEndDate(endDate);
        absence.setNumberOfDays(numberOfDays);
        absence.setDescription(description);
        absence.setStatus(status);
        absence.setIsOverLimit(false);
        
        if ("REJECTED".equals(status)) {
            absence.setResultStatus("REJECTED");
            absence.setRejectReason("Lý do từ chối");
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
        
        absenceRepository.save(absence);
        log.info("✅ [UnifiedDataSeeder] Created absence for: {}", userFullName);
    }

    private void seedAccomplishments() {
        log.info("🏆 [UnifiedDataSeeder] Seeding accomplishments...");
        
        if (accomplishmentRepository.count() > 0) {
            log.info("ℹ️ [UnifiedDataSeeder] Accomplishments already exist, skipping");
            return;
        }

        List<User> students = userRepository.findByRoleId(User.RoleEnum.STUDENT.getId());
        List<Classroom> classrooms = classroomRepository.findAll();

        if (students.isEmpty() || classrooms.isEmpty()) {
            log.warn("⚠️ [UnifiedDataSeeder] No students or classrooms found. Skipping accomplishments.");
            return;
        }
        
        log.info("🔄 [UnifiedDataSeeder] Seeding accomplishments...");

        int accomplishmentCount = 0;
        // Seed accomplishments for up to 5 students
        for (int i = 0; i < Math.min(5, students.size()); i++) {
            User student = students.get(i);
            
            // Give each student 2-4 accomplishments
            int numAccomplishments = 2 + random.nextInt(3); // 2 to 4
            for (int j = 0; j < numAccomplishments; j++) {
                // Pick a random classroom to represent a completed course
                Classroom classroom = classrooms.get(random.nextInt(classrooms.size()));
                
                Accomplishment accomplishment = new Accomplishment();
                accomplishment.setStudent(student);
                accomplishment.setCourseTitle(classroom.getName());
                accomplishment.setSubject(classroom.getSubject());
                accomplishment.setTeacherName(classroom.getTeacher() != null ? classroom.getTeacher().getFullName() : "N/A");
                accomplishment.setGrade(65.0 + random.nextDouble() * 35.0); // Grade between 65.0 and 100.0
                accomplishment.setCompletionDate(LocalDate.now().minusDays(30 + random.nextInt(300))); // Completed in the last year
                
                accomplishmentRepository.save(accomplishment);
                accomplishmentCount++;
            }
        }
        log.info("✅ [UnifiedDataSeeder] Created {} sample accomplishments.", accomplishmentCount);
    }

    private void seedAssignments() {
        log.info("📝 [UnifiedDataSeeder] Seeding assignments...");
        
        List<Classroom> classrooms = classroomRepository.findAll();
        
        if (classrooms.isEmpty()) {
            log.warn("⚠️ [UnifiedDataSeeder] No classrooms found. Skipping assignments.");
            return;
        }

        try {
            // Create assignments for each classroom
            for (Classroom classroom : classrooms) {
                if (classroom.getTeacher() != null) {
                    // Assignment 1
                    CreateAssignmentDto assignment1Dto = new CreateAssignmentDto();
                    assignment1Dto.setTitle("Bài tập " + classroom.getName() + " - Phần 1");
                    assignment1Dto.setDescription("Bài tập cơ bản về " + classroom.getSubject());
                    assignment1Dto.setDueDate(LocalDateTime.now().plusDays(7));
                    assignment1Dto.setPoints(100);
                    assignment1Dto.setClassroomId(classroom.getId());
                    assignmentService.CreateAssignment(assignment1Dto, classroom.getTeacher().getEmail());

                    // Assignment 2
                    CreateAssignmentDto assignment2Dto = new CreateAssignmentDto();
                    assignment2Dto.setTitle("Bài tập " + classroom.getName() + " - Phần 2");
                    assignment2Dto.setDescription("Bài tập nâng cao về " + classroom.getSubject());
                    assignment2Dto.setDueDate(LocalDateTime.now().plusDays(14));
                    assignment2Dto.setPoints(100);
                    assignment2Dto.setClassroomId(classroom.getId());
                    assignmentService.CreateAssignment(assignment2Dto, classroom.getTeacher().getEmail());
                }
            }
            
            log.info("✅ [UnifiedDataSeeder] Created assignments for {} classrooms.", classrooms.size());
        } catch (Exception e) {
            log.warn("⚠️ [UnifiedDataSeeder] Error creating assignments: {}", e.getMessage());
        }
    }

    private void seedSchedules() {
        log.info("📅 [UnifiedDataSeeder] Seeding schedules...");
        
        if (scheduleRepository.count() > 0) {
            log.info("ℹ️ [UnifiedDataSeeder] Schedules already exist, skipping");
            return;
        }

        List<Classroom> classrooms = classroomRepository.findAll();
        List<User> teachers = userRepository.findByRoleId(User.RoleEnum.TEACHER.getId());

        if (classrooms.isEmpty() || teachers.isEmpty()) {
            log.warn("⚠️ [UnifiedDataSeeder] Not enough classrooms or teachers to seed schedules. Skipping.");
            return;
        }

        int scheduleCount = 0;
        
        // Create schedules for each classroom
        for (Classroom classroom : classrooms) {
            if (classroom.getTeacher() != null) {
                // Create 2-3 schedules per classroom
                for (int i = 1; i <= 2 + random.nextInt(2); i++) {
                    Schedule schedule = new Schedule();
                    schedule.setTitle(classroom.getName() + " - Buổi " + i);
                    schedule.setDescription("Lịch học " + classroom.getSubject() + " - Buổi " + i);
                    schedule.setStartDatetime(LocalDateTime.now().plusDays(i * 2).withHour(9).withMinute(0));
                    schedule.setEndDatetime(LocalDateTime.now().plusDays(i * 2).withHour(11).withMinute(0));
                    schedule.setLocation("Phòng " + classroom.getSection());
                    schedule.setColor(getRandomColor());
                    schedule.setTeacher(classroom.getTeacher());
                    schedule.setClassroom(classroom);
                    
                    scheduleRepository.save(schedule);
                    scheduleCount++;
                }
            }
        }
        
        log.info("✅ [UnifiedDataSeeder] Created {} schedules.", scheduleCount);
    }

    private String getRandomColor() {
        String[] colors = {"#4CAF50", "#FF9800", "#2196F3", "#9C27B0", "#F44336", "#607D8B"};
        return colors[random.nextInt(colors.length)];
    }

    private void seedLectures() {
        log.info("📚 [UnifiedDataSeeder] Seeding lectures...");
        
        List<Classroom> classrooms = classroomRepository.findAll();
        if (classrooms.isEmpty()) {
            log.warn("⚠️ [UnifiedDataSeeder] No classrooms found. Skipping lectures.");
            return;
        }

        for (Classroom classroom : classrooms) {
            if (lectureRepository.existsByClassroomId(classroom.getId())) {
                log.info("ℹ️ [UnifiedDataSeeder] Classroom '{}' already has lectures. Skipping.", classroom.getName());
                continue;
            }

            log.info("🔧 [UnifiedDataSeeder] Creating lectures for '{}'", classroom.getName());
            createSampleLecturesForClassroom(classroom);
        }

        long totalLectures = lectureRepository.count();
        log.info("✅ [UnifiedDataSeeder] Created {} total lectures.", totalLectures);
    }

    private void createSampleLecturesForClassroom(Classroom classroom) {
        // Create 2-3 lectures per classroom
        for (int i = 1; i <= 2 + random.nextInt(2); i++) {
            Lecture lecture = new Lecture();
            lecture.setTitle(classroom.getName() + " - Bài " + i);
            lecture.setContent("# " + classroom.getName() + " - Bài " + i + "\n\nNội dung bài học về " + classroom.getSubject());
            lecture.setClassroom(classroom);
            lecture.setLectureDate(LocalDate.now().plusDays(i * 3));
            lecture.setCreatedAt(LocalDateTime.now());
            lecture.setUpdatedAt(LocalDateTime.now());
            
            lectureRepository.save(lecture);
            log.info("✅ [UnifiedDataSeeder] Created lecture: {}", lecture.getTitle());
        }
    }

    private void seedExams() {
        log.info("📝 [UnifiedDataSeeder] Seeding exams...");
        
        if (examRepository.count() > 0) {
            log.info("ℹ️ [UnifiedDataSeeder] Exams already exist, skipping");
            return;
        }

        List<Classroom> classrooms = classroomRepository.findAll();
        if (classrooms.isEmpty()) {
            log.warn("⚠️ [UnifiedDataSeeder] No classrooms found. Skipping exams.");
            return;
        }

        int examsCreated = 0;
        
        // Create exams for each classroom
        for (Classroom classroom : classrooms) {
            // Create 1-2 exams per classroom
            for (int i = 1; i <= 1 + random.nextInt(2); i++) {
                try {
                    String examTitle = "Kiểm tra " + classroom.getName() + " - Lần " + i;
                    int duration = 60 + random.nextInt(120); // 60-180 minutes
                    int daysFromNow = 10 + random.nextInt(30); // 10-40 days from now

                    Instant startTime = LocalDateTime.now().plusDays(daysFromNow).withHour(9).withMinute(0).atZone(ZoneId.systemDefault()).toInstant();
                    Instant endTime = startTime.plusSeconds(duration * 60);

                    Exam exam = new Exam();
                    exam.setTitle(examTitle);
                    exam.setStartTime(startTime);
                    exam.setEndTime(endTime);
                    exam.setDurationInMinutes(duration);
                    exam.setClassroom(classroom);

                    examRepository.save(exam);
                    examsCreated++;
                    log.info("✅ [UnifiedDataSeeder] Created exam: {}", examTitle);
                } catch (Exception e) {
                    log.warn("⚠️ [UnifiedDataSeeder] Error creating exam for classroom {}: {}", classroom.getName(), e.getMessage());
                }
            }
        }

        log.info("✅ [UnifiedDataSeeder] Created {} exams.", examsCreated);
    }

    private void seedAttendance() {
        log.info("📊 [UnifiedDataSeeder] Seeding attendance...");
        
        List<Classroom> classrooms = classroomRepository.findAll();
        if (classrooms.isEmpty()) {
            log.warn("⚠️ [UnifiedDataSeeder] No classrooms found, skipping attendance.");
            return;
        }

        for (Classroom classroom : classrooms) {
            seedAttendanceForClassroom(classroom);
        }

        log.info("✅ [UnifiedDataSeeder] Finished seeding attendance data.");
    }

    private void seedAttendanceForClassroom(Classroom classroom) {
        List<Lecture> lectures = lectureRepository.findByClassroomId(classroom.getId());
        List<User> students = enrollmentRepository.findByClassroomId(classroom.getId())
                .stream()
                .map(enrollment -> enrollment.getUser())
                .collect(java.util.stream.Collectors.toList());

        if (lectures.isEmpty()) {
            log.info("⚠️ [UnifiedDataSeeder] No lectures for classroom: {}, skipping.", classroom.getName());
            return;
        }

        // Seed attendance for lectures that have a date in the past or today
        for (Lecture lecture : lectures) {
            // Check if lecture has a date and if it's in the past or today
            if (lecture.getLectureDate() != null && !lecture.getLectureDate().isAfter(LocalDate.now())) {
                
                // Find existing or create a new session
                AttendanceSession session = attendanceSessionRepository.findByLectureId(lecture.getId())
                    .orElse(new AttendanceSession());

                // Always ensure there is a clock-in time for past lectures
                if (session.getTeacherClockInTime() == null) {
                    session.setTeacherClockInTime(lecture.getLectureDate().atTime(8, 30)); // Set a fixed time for consistency
                }
                
                // If it's a new session, set its properties
                if (session.getId() == null) {
                    session.setLecture(lecture);
                    session.setSessionDate(lecture.getLectureDate());
                    session.setClassroom(classroom);
                    attendanceSessionRepository.save(session);

                    // Create attendance records for each student only for the new session
                    if (!students.isEmpty()) {
                        for (int j = 0; j < students.size(); j++) {
                            User student = students.get(j);
                            Attendance attendance = new Attendance();
                            attendance.setSession(session);
                            attendance.setStudent(student);
                            
                            // Alternate status for variety
                            attendance.setStatus(j % 3 == 0 ? AttendanceStatus.ABSENT : (j % 3 == 1 ? AttendanceStatus.LATE : AttendanceStatus.PRESENT));
                            attendanceRepository.save(attendance);
                        }
                    }
                } else {
                    // If the session already exists, just save the updated clock-in time
                    attendanceSessionRepository.save(session);
                }
            }
        }
    }

    private Classroom findClassroomByName(List<Classroom> classrooms, String name) {
        return classrooms.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private void enrollStudentInClassroom(User student, Classroom classroom, Double progressPercentage) {
        if (student == null || classroom == null) {
            log.warn("⚠️ [UnifiedDataSeeder] Cannot enroll: student or classroom is null");
            return;
        }
        
        ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId(classroom.getId(), student.getId());
        
        if (!enrollmentRepository.existsById(enrollmentId)) {
            ClassroomEnrollment enrollment = new ClassroomEnrollment();
            enrollment.setId(enrollmentId);
            enrollment.setClassroom(classroom);
            enrollment.setUser(student);
            enrollment.setStatus(ClassroomEnrollment.EnrollmentStatus.ACTIVE);
            enrollment.setProgressPercentage(progressPercentage);
            enrollment.setEnrolledAt(LocalDateTime.now());
            enrollment.setCreatedAt(LocalDateTime.now());
            enrollment.setUpdatedAt(LocalDateTime.now());
            
            enrollmentRepository.save(enrollment);
            log.info("✅ [UnifiedDataSeeder] Enrolled student {} in classroom {}", student.getUsername(), classroom.getName());
        } else {
            log.info("ℹ️ [UnifiedDataSeeder] Enrollment already exists for student {} in classroom {}", student.getUsername(), classroom.getName());
        }
    }

    private void waitForTablesToBeCreated() {
        log.info("⏳ [UnifiedDataSeeder] Waiting for tables to be created...");
        int maxAttempts = 10;
        int attempt = 0;
        
        while (attempt < maxAttempts) {
            try {
                // Try to check if users table exists
                long userCount = userRepository.count();
                log.info("✅ [UnifiedDataSeeder] Tables are ready! User count: {}", userCount);
                return;
            } catch (Exception e) {
                attempt++;
                log.info("⏳ [UnifiedDataSeeder] Tables not ready yet (attempt {}/{}), waiting...", attempt, maxAttempts);
                try {
                    Thread.sleep(2000); // Wait 2 seconds before next attempt
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        log.warn("⚠️ [UnifiedDataSeeder] Tables may not be ready, proceeding anyway...");
    }
}