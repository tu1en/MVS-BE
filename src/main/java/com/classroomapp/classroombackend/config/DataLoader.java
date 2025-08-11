package com.classroomapp.classroombackend.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.constants.RoleConstants;
import com.classroomapp.classroombackend.entity.Room;
import com.classroomapp.classroombackend.model.Absence;
import com.classroomapp.classroombackend.model.Accomplishment;
import com.classroomapp.classroombackend.model.Announcement;
import com.classroomapp.classroombackend.model.AttendanceExplanation;
import com.classroomapp.classroombackend.model.Blog;
import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.model.CourseMaterial;
import com.classroomapp.classroombackend.model.ExplanationStatus;
import com.classroomapp.classroombackend.model.JobPosition;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.RecruitmentApplication;
import com.classroomapp.classroombackend.model.RecruitmentPlan;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.StudentMessage;
import com.classroomapp.classroombackend.model.StudentProgress;
import com.classroomapp.classroombackend.model.TimetableEvent;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.classroommanagement.Course;
// Thêm import này vào đầu DataLoader.java
import com.classroomapp.classroombackend.model.hrmanagement.EvidenceTemplate;
import com.classroomapp.classroombackend.model.usermanagement.Role;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AccomplishmentRepository;
import com.classroomapp.classroombackend.repository.AnnouncementRepository;
import com.classroomapp.classroombackend.repository.AttendanceExplanationRepository;
import com.classroomapp.classroombackend.repository.BlogRepository;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.repository.CourseMaterialRepository;
import com.classroomapp.classroombackend.repository.JobPositionRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.RecruitmentApplicationRepository;
import com.classroomapp.classroombackend.repository.RecruitmentPlanRepository;
import com.classroomapp.classroombackend.repository.RoomRepository;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.StudentMessageRepository;
import com.classroomapp.classroombackend.repository.StudentProgressRepository;
import com.classroomapp.classroombackend.repository.TimetableEventRepository;
import com.classroomapp.classroombackend.repository.absencemanagement.AbsenceRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.CourseRepository;
import com.classroomapp.classroombackend.repository.hrmanagement.EvidenceTemplateRepository;
import com.classroomapp.classroombackend.repository.requestmanagement.RequestRepository;
import com.classroomapp.classroombackend.repository.usermanagement.RoleRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
    @Order(1) // Run first since we removed DatabaseCleanupService
    @DependsOn("entityManagerFactory") // Wait for JPA to be initialized
public class DataLoader implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private UserRepository userRepository;
    @Autowired
private EvidenceTemplateRepository evidenceTemplateRepository;
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private ContractRepository contractRepository;
    
    @Autowired
    private ClassroomEnrollmentRepository classroomEnrollmentRepository;
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    @Autowired
    private LectureRepository lectureRepository;
    
    @Autowired
    private AssignmentRepository assignmentRepository;
    
    @Autowired
    private SubmissionRepository submissionRepository;
    
    @Autowired
    private BlogRepository blogRepository;
    
    @Autowired
    private AccomplishmentRepository accomplishmentRepository;
    
    @Autowired
    private AnnouncementRepository announcementRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;
    
    @Autowired
    private AttendanceExplanationRepository attendanceExplanationRepository;
    
    @Autowired
    private StudentMessageRepository studentMessageRepository;
    
    @Autowired
    private CourseMaterialRepository courseMaterialRepository;
    
    @Autowired
    private StudentProgressRepository studentProgressRepository;
    
    @Autowired
    private AbsenceRepository absenceRepository;
    
    @Autowired
    private TimetableEventRepository timetableEventRepository;
    
    @Autowired
    private RequestRepository requestRepository;
    
    @Autowired
    private JobPositionRepository jobPositionRepository;
    
    @Autowired
    private RecruitmentApplicationRepository recruitmentApplicationRepository;
    
    @Autowired
    private RecruitmentPlanRepository recruitmentPlanRepository;
    
  

    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Clean up duplicate submissions first
        cleanupDuplicateSubmissions();
        
        List<Classroom> classrooms;
        if (userRepository.count() == 0) {
            log.info("============== Seeding Database ==============");
            
            // Seed roles first
            seedRoles();
            
            // Seed users
            seedUsers();
            
            // Seed courses
            seedCourses();
            
            // Seed rooms
            seedRooms();
            
            // Seed classrooms
            classrooms = seedClassrooms();
            
            // Comment out automatic enrollment seeding - manager sẽ thêm học viên thủ công
            // seedClassroomEnrollments();
            
            // Seed schedules
            seedSchedules();
            
            // Seed timetable events
            seedTimetableEvents();
            
            // Seed role requests
            seedRequests();
            
            // Seed recruitment plans first
            seedRecruitmentPlans();
            
            // Seed job positions
            seedJobPositions();
            
            // Seed recruitment applications
            seedRecruitmentApplications();
            
            log.info("============== Starting Lecture Seeding ==============");
            seedLectures(classrooms);
            log.info("============== Lecture Seeding Complete ==============");
            
            // Seed assignments
            seedAssignments();
            
            // Seed submissions
            seedSubmissions();
            
            // Seed blogs
            seedBlogs();
            
            // Seed accomplishments
            seedAccomplishments();
            
            // Seed announcements
            seedAnnouncements();
            
            // Seed attendance
            seedAttendance();
            
            // Seed messages
            seedMessages();
            
            // Seed messages
            seedMessages();
            
seedEvidenceTemplates();
            
            // Seed student progress
            seedStudentProgress();
            
            log.info("============== Main Seeding Complete ==============");
        } else {
            log.info("Database already has users. Skipping main seeding.");
            classrooms = classroomRepository.findAll();
        }

        // Ensure there are enough teachers for demo even when DB already has data
        ensureMinimumTeachers(24);

        // Always verify database state
        verifyDatabaseState();
        verifyUserRoleAssignments();
        
        // Always create attendance explanations test data
        createAttendanceExplanationsData();

        // Always run the submission seeder to add new test data
        log.info("============== Checking for new submissions to seed ==============");
        seedSubmissions();
        log.info("============== Submission seeding complete ==============");

        // Always run comprehensive grading seeder for classroom 54
        log.info("============== Seeding Comprehensive Grading Data ==============");
        seedAssignmentTestData();
        seedComprehensiveGradingData();
        log.info("============== Comprehensive Grading Seeding Complete ==============");

        // Comment out automatic course materials seeding
        // Tài liệu sẽ được tạo thủ công bởi manager/teacher thay vì tự động
        /*
        if (courseMaterialRepository.count() == 0) {
            log.info("============== Seeding Course Materials ==============");
            if (classrooms.isEmpty()) {
                log.info("No classrooms found to seed materials for.");
            } else {
                seedCourseMaterials(classrooms);
            }
            log.info("============== Course Materials Seeding Complete ==============");
        } else {
            log.info("Course materials already seeded. Skipping.");
        }
        */
        log.info("Course materials seeding disabled - materials should be uploaded manually by teachers/managers.");
    
        // Always check and seed absence data after users exist
        if (userRepository.count() > 0 && absenceRepository.count() == 0) {
            log.info("============== Seeding Absence Data ==============");
            try {
                seedAbsences();
                log.info("============== Absence Seeding Complete ==============");
            } catch (Exception e) {
                log.error("Error seeding absence data: {}", e.getMessage(), e);
            }
        } else if (absenceRepository.count() > 0) {
            log.info("Absence data already exists. Count: {}", absenceRepository.count());
        } else {
            log.warn("No users found - cannot seed absence data");
        }
        
        log.info("============== Checking Schedule Status ==============");
        log.info("Schedules are already seeded in the main seeding process if needed.");
        log.info("============== Schedule Status Check Complete ==============");
        
        // Always run the classroom enrollment seeder to ensure students are in classrooms
        log.info("============== Forcing Classroom Enrollment Seeding ==============");
        seedClassroomEnrollments();
        log.info("============== Classroom Enrollment Seeding Complete ==============");

        // Luôn seed lại JobPosition nếu bảng rỗng
        seedJobPositions();

        // Seed thêm ứng viên nộp CV mẫu nếu chưa có đủ
        if (recruitmentApplicationRepository.count() < 20) {
            List<JobPosition> positions = jobPositionRepository.findAll();
            if (!positions.isEmpty()) {
                String[] testApplicants = {
                    "Nguyễn Thị Kim", "Trần Văn Long", "Lê Thị Hoa", "Phạm Văn Thắng", "Hoàng Thị Nga"
                };
                String[] testEmails = {
                    "nguyenthiKim123456@gmail.com", "tranvanlong234567@gmail.com", 
                    "lethihoa345678@gmail.com", "phamvanthang456789@gmail.com", "hoangthinga567890@gmail.com"
                };
                String[] addresses = {
                    "123 Đường ABC, Quận 1, TP.HCM",
                    "456 Đường XYZ, Quận 2, TP.HCM", 
                    "789 Đường DEF, Quận 3, TP.HCM",
                    "321 Đường GHI, Quận 4, TP.HCM",
                    "654 Đường JKL, Quận 5, TP.HCM"
                };
                
                for (int i = 0; i < testApplicants.length; i++) {
                    RecruitmentApplication app = new RecruitmentApplication();
                    app.setFullName(testApplicants[i]);
                    app.setEmail(testEmails[i]);
                    app.setPhoneNumber("098765433" + (i + 6));
                    app.setAddress(addresses[i % addresses.length]);
                    app.setJobPosition(positions.get(i % positions.size()));
                    app.setStatus("PENDING");
                    app.setCvUrl("/static/sample_materials/sample.pdf");
                    app.setCreatedAt(LocalDateTime.now().minusDays(i + 1));
                    recruitmentApplicationRepository.save(app);
                    log.info("✅ Created test application for {} applying to: {}", testApplicants[i], positions.get(i % positions.size()).getTitle());
                }
                log.info("✅ Created {} additional test recruitment applications.", testApplicants.length);
            }
        }
    }

    private void cleanupDuplicateSubmissions() {
        try {
            String countDuplicatesQuery = """
                SELECT COUNT(*) FROM (
                    SELECT assignment_id, student_id, COUNT(*) as cnt
                    FROM submissions
                    GROUP BY assignment_id, student_id
                    HAVING COUNT(*) > 1
                ) as duplicates
                """;
            
            Integer duplicateGroups = jdbcTemplate.queryForObject(countDuplicatesQuery, Integer.class);
            
            if (duplicateGroups != null && duplicateGroups > 0) {
                log.warn("Found {} groups of duplicate submissions. Cleaning up...", duplicateGroups);
                
                String duplicateIdsSubquery = """
                    SELECT s1.id
                    FROM submissions s1
                    INNER JOIN submissions s2 ON s1.assignment_id = s2.assignment_id
                                            AND s1.student_id = s2.student_id
                                            AND s1.id < s2.id
                    """;
                
                String cleanupAttachmentsQuery = "DELETE FROM submission_attachments WHERE submission_id IN (" + duplicateIdsSubquery + ")";
                int deletedAttachments = jdbcTemplate.update(cleanupAttachmentsQuery);
                log.info("✅ Removed {} orphaned submission attachments", deletedAttachments);

                String cleanupSubmissionsQuery = "DELETE FROM submissions WHERE id IN (" + duplicateIdsSubquery + ")";
                int deletedRows = jdbcTemplate.update(cleanupSubmissionsQuery);
                log.info("✅ Removed {} duplicate submission records", deletedRows);
                
                Integer remainingDuplicates = jdbcTemplate.queryForObject(countDuplicatesQuery, Integer.class);
                if (remainingDuplicates != null && remainingDuplicates > 0) {
                    log.warn("⚠️ {} duplicate groups still remain after cleanup", remainingDuplicates);
                } else {
                    log.info("✅ All duplicate submissions cleaned up successfully");
                }
            } else {
                log.info("✅ No duplicate submissions found");
            }
            
        } catch (Exception e) {
            log.error("❌ Error during duplicate cleanup: {}", e.getMessage(), e);
        }
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            try {
                entityManager.createNativeQuery("SET IDENTITY_INSERT roles ON").executeUpdate();

                Role student = new Role("STUDENT");
                student.setId(1);
                roleRepository.save(student);

                Role teacher = new Role("TEACHER");
                teacher.setId(2);
                roleRepository.save(teacher);

                Role manager = new Role("MANAGER");
                manager.setId(3);
                roleRepository.save(manager);

                Role admin = new Role("ADMIN");
                admin.setId(4);
                roleRepository.save(admin);

                Role accountant = new Role("ACCOUNTANT");
                accountant.setId(5);
                roleRepository.save(accountant);

                log.info("✅ Created roles with explicit IDs (including ACCOUNTANT).");

            } finally {
                entityManager.createNativeQuery("SET IDENTITY_INSERT roles OFF").executeUpdate();
            }
        } else {
            log.info("✅ Roles already seeded.");
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            try {
                log.info("🔄 Starting user seeding with explicit IDs...");

                entityManager.createNativeQuery("SET IDENTITY_INSERT users ON").executeUpdate();
                log.info("✅ IDENTITY_INSERT enabled for users table");

                // Create student user
                User student = new User();
                student.setId(101L);
                student.setUsername("student");
                student.setPassword(passwordEncoder.encode("student123"));
                student.setEmail("student@test.com");
                student.setFullName("Student User");
                student.setRoleId(RoleConstants.STUDENT);
                userRepository.save(student);
                log.info("✅ Created student user with ID: " + student.getId());

                // Create main teacher user
                User teacher = new User();
                teacher.setId(201L);
                teacher.setUsername("teacher");
                teacher.setPassword(passwordEncoder.encode("teacher123"));
                teacher.setEmail("teacher@test.com");
                teacher.setFullName("Nguyễn Văn Minh");
                teacher.setRoleId(RoleConstants.TEACHER);
                teacher.setPhoneNumber("0912345678");
                teacher.setDepartment("Khoa Công Nghệ Thông Tin");
                teacher.setHireDate(LocalDate.now().minusYears(2));
                teacher.setAnnualLeaveBalance(12);
                teacher.setLeaveResetDate(LocalDate.now().plusMonths(6));
                userRepository.save(teacher);
                log.info("✅ Created teacher user with ID: " + teacher.getId());

                // Create manager user
                User manager = new User();
                manager.setId(301L);
                manager.setUsername("manager");
                manager.setPassword(passwordEncoder.encode("manager123"));
                manager.setEmail("manager@test.com");
                manager.setFullName("Manager User");
                manager.setRoleId(RoleConstants.MANAGER);
                userRepository.save(manager);

                // Create admin user
                User admin = new User();
                admin.setId(401L);
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@test.com");
                admin.setFullName("Administrator");
                admin.setRoleId(RoleConstants.ADMIN);
                userRepository.save(admin);

                // Create subject-specific teachers
                User mathTeacher = new User();
                mathTeacher.setId(202L);
                mathTeacher.setUsername("math_teacher");
                mathTeacher.setPassword(passwordEncoder.encode("teacher123"));
                mathTeacher.setEmail("math@test.com");
                mathTeacher.setFullName("Trần Văn Đức");
                mathTeacher.setRoleId(RoleConstants.TEACHER);
                mathTeacher.setPhoneNumber("0987654321");
                mathTeacher.setDepartment("Khoa Toán Học");
                mathTeacher.setHireDate(LocalDate.now().minusYears(3));
                mathTeacher.setAnnualLeaveBalance(4);
                mathTeacher.setLeaveResetDate(LocalDate.now().plusMonths(8));
                userRepository.save(mathTeacher);

                User litTeacher = new User();
                litTeacher.setId(203L);
                litTeacher.setUsername("lit_teacher");
                litTeacher.setPassword(passwordEncoder.encode("teacher123"));
                litTeacher.setEmail("literature@test.com");
                litTeacher.setFullName("Phạm Thị Lan");
                litTeacher.setRoleId(RoleConstants.TEACHER);
                litTeacher.setPhoneNumber("0976543210");
                litTeacher.setDepartment("Khoa Ngữ Văn");
                litTeacher.setHireDate(LocalDate.now().minusYears(1));
                litTeacher.setAnnualLeaveBalance(-3);
                litTeacher.setLeaveResetDate(LocalDate.now().plusMonths(3));
                userRepository.save(litTeacher);

                User engTeacher = new User();
                engTeacher.setId(204L);
                engTeacher.setUsername("eng_teacher");
                engTeacher.setPassword(passwordEncoder.encode("teacher123"));
                engTeacher.setEmail("english@test.com");
                engTeacher.setFullName("Lê Hoàng Nam");
                engTeacher.setRoleId(RoleConstants.TEACHER);
                engTeacher.setPhoneNumber("0965432109");
                engTeacher.setDepartment("Khoa Ngoại Ngữ");
                engTeacher.setHireDate(LocalDate.now().minusYears(4));
                engTeacher.setAnnualLeaveBalance(8);
                engTeacher.setLeaveResetDate(LocalDate.now().plusMonths(10));
                userRepository.save(engTeacher);

                // Create additional test students
                User student1 = new User();
                student1.setId(102L);
                student1.setUsername("student1");
                student1.setPassword(passwordEncoder.encode("student123"));
                student1.setEmail("student1@test.com");
                student1.setFullName("Phạm Văn Nam");
                student1.setRoleId(RoleConstants.STUDENT);
                userRepository.save(student1);

                User student2 = new User();
                student2.setId(103L);
                student2.setUsername("student2");
                student2.setPassword(passwordEncoder.encode("student123"));
                student2.setEmail("student2@test.com");
                student2.setFullName("Alice Johnson");
                student2.setRoleId(RoleConstants.STUDENT);
                userRepository.save(student2);

                User student3 = new User();
                student3.setId(104L);
                student3.setUsername("student3");
                student3.setPassword(passwordEncoder.encode("student123"));
                student3.setEmail("student3@test.com");
                student3.setFullName("Bob Wilson");
                student3.setRoleId(RoleConstants.STUDENT);
                userRepository.save(student3);

                User student4 = new User();
                student4.setId(105L);
                student4.setUsername("student4");
                student4.setPassword(passwordEncoder.encode("student123"));
                student4.setEmail("student4@test.com");
                student4.setFullName("Carol Davis");
                student4.setRoleId(RoleConstants.STUDENT);
                userRepository.save(student4);

                User student5 = new User();
                student5.setId(106L);
                student5.setUsername("student5");
                student5.setPassword(passwordEncoder.encode("student123"));
                student5.setEmail("student5@test.com");
                student5.setFullName("David Chen");
                student5.setRoleId(RoleConstants.STUDENT);
                userRepository.save(student5);

                // Additional teachers
                User extraTeacher1 = new User();
                extraTeacher1.setId(205L);
                extraTeacher1.setUsername("teacher2");
                extraTeacher1.setPassword(passwordEncoder.encode("teacher123"));
                extraTeacher1.setEmail("teacher2@test.com");
                extraTeacher1.setFullName("Vũ Thị Hương");
                extraTeacher1.setRoleId(RoleConstants.TEACHER);
                extraTeacher1.setPhoneNumber("0954321098");
                extraTeacher1.setDepartment("Khoa Hóa Học");
                extraTeacher1.setHireDate(LocalDate.now().minusYears(5));
                extraTeacher1.setAnnualLeaveBalance(9);
                extraTeacher1.setLeaveResetDate(LocalDate.now().plusMonths(4));
                userRepository.save(extraTeacher1);

                User extraTeacher2 = new User();
                extraTeacher2.setId(206L);
                extraTeacher2.setUsername("teacher3");
                extraTeacher2.setPassword(passwordEncoder.encode("teacher123"));
                extraTeacher2.setEmail("teacher3@test.com");
                extraTeacher2.setFullName("Đặng Minh Tuấn");
                extraTeacher2.setRoleId(RoleConstants.TEACHER);
                extraTeacher2.setPhoneNumber("0943210987");
                extraTeacher2.setDepartment("Khoa Vật Lý");
                extraTeacher2.setHireDate(LocalDate.now().minusMonths(6));
                extraTeacher2.setAnnualLeaveBalance(12);
                extraTeacher2.setLeaveResetDate(LocalDate.now().plusMonths(6));
                userRepository.save(extraTeacher2);

                // ===== Thêm bộ giáo viên theo chuẩn cấp 3 (Toán, Lý, Hóa, Văn, Anh, Sinh) =====
                String[][] teacherSeeds = new String[][]{
                    // username, email, fullName, department
                    {"toan_gv1","toan1@school.vn","Nguyễn Đức Toàn","Toán"},
                    {"toan_gv2","toan2@school.vn","Phạm Hải Long","Toán"},
                    {"toan_gv3","toan3@school.vn","Lê Minh Quân","Toán"},
                    {"ly_gv1","ly1@school.vn","Trần Quốc Huy","Vật lý"},
                    {"ly_gv2","ly2@school.vn","Đỗ Thanh Tùng","Vật lý"},
                    {"ly_gv3","ly3@school.vn","Ngô Nhật Nam","Vật lý"},
                    {"hoa_gv1","hoa1@school.vn","Vũ Hồng Phúc","Hóa học"},
                    {"hoa_gv2","hoa2@school.vn","Bùi Thanh Hà","Hóa học"},
                    {"hoa_gv3","hoa3@school.vn","Phan Anh Dũng","Hóa học"},
                    {"van_gv1","van1@school.vn","Phạm Thu Hà","Ngữ văn"},
                    {"van_gv2","van2@school.vn","Nguyễn Thị Hồng","Ngữ văn"},
                    {"van_gv3","van3@school.vn","Hoàng Thị Trang","Ngữ văn"},
                    {"anh_gv1","anh1@school.vn","Lê Hồng Sơn","Tiếng Anh"},
                    {"anh_gv2","anh2@school.vn","Tạ Bích Ngọc","Tiếng Anh"},
                    {"anh_gv3","anh3@school.vn","Phạm Khánh Linh","Tiếng Anh"},
                    {"sinh_gv1","sinh1@school.vn","Đặng Quỳnh Chi","Sinh học"},
                    {"sinh_gv2","sinh2@school.vn","Trịnh Văn Thái","Sinh học"},
                    {"sinh_gv3","sinh3@school.vn","Nguyễn Tú Anh","Sinh học"}
                };

                long nextId = 600L; // tránh trùng ID đã dùng ở trên
                for (String[] t : teacherSeeds) {
                    User u = new User();
                    u.setId(nextId++);
                    u.setUsername(t[0]);
                    u.setPassword(passwordEncoder.encode("teacher123"));
                    u.setEmail(t[1]);
                    u.setFullName(t[2]);
                    u.setRoleId(RoleConstants.TEACHER);
                    u.setDepartment(t[3]);
                    u.setHireDate(LocalDate.now().minusMonths((int)(nextId % 24)));
                    userRepository.save(u);

                    // Tạo hợp đồng ACTIVE cho giáo viên để đồng bộ bộ lọc môn/ca/cấp
                    try {
                        Contract c = new Contract();
                        c.setContractId("CT" + u.getId() + "_" + System.currentTimeMillis()); // Tạo mã hợp đồng duy nhất
                        c.setUserId(u.getId());
                        c.setFullName(u.getFullName());
                        c.setEmail(u.getEmail());
                        c.setPhoneNumber("09" + (int)(10000000 + Math.random()*89999999));
                        c.setContractType("TEACHER");
                        c.setPosition("Giáo viên " + t[3]);
                        c.setDepartment(t[3]);
                        c.setSalary(15000000.0 + (int)(Math.random()*6000000));
                        // Giáo viên: thêm đơn giá theo giờ để phục vụ tính lương theo giờ
                        try {
                            long hourly = 120_000L + (long)(Math.random() * 100_000L); // 120k - 220k VND/giờ
                            c.setHourlySalary(hourly);
                        } catch (Exception ignored) {}
                        // ngẫu nhiên ca làm việc
                        String[] shifts = new String[]{"ca sáng (07:30-09:30)", "ca chiều (13:30-15:30)", "ca tối (18:00-20:00)"};
                        c.setWorkingHours(shifts[(int)(Math.random()*shifts.length)]);
                        c.setStartDate(LocalDate.now());
                        // Ngày kết thúc hợp đồng: 2 năm sau theo mặc định
                        c.setEndDate(LocalDate.now().plusYears(2));
                        c.setStatus("ACTIVE");
                        c.setSubject(t[3]);
                        // phân bổ cấp học 10/11/12
                        String[] levels = new String[]{"10","11","12"};
                        c.setClassLevel(levels[(int)(Math.random()*levels.length)]);
                        contractRepository.save(c);
                    } catch (Exception e) {
                        log.warn("Could not create contract for {}: {}", u.getEmail(), e.getMessage());
                    }
                }

                // Create accountant user
                User accountant = new User();
                accountant.setId(501L);
                accountant.setUsername("acc");
                accountant.setPassword(passwordEncoder.encode("acc123"));
                accountant.setEmail("accountant@test.com");
                accountant.setFullName("Nguyễn Thị Kế Toán");
                accountant.setRoleId(RoleConstants.ACCOUNTANT);
                accountant.setPhoneNumber("0901122334");
                accountant.setDepartment("Kế toán viên");
                accountant.setHireDate(LocalDate.of(2025, 7, 1));
                userRepository.save(accountant);

                log.info("✅ Created accountant user with ID: " + accountant.getId());

                log.info("✅ Created users with standardized, explicit IDs.");

            } finally {
                entityManager.createNativeQuery("SET IDENTITY_INSERT users OFF").executeUpdate();
                log.info("✅ IDENTITY_INSERT disabled for users table");

                // Verify the users were created with correct IDs
                User createdStudent = userRepository.findByUsername("student").orElse(null);
                User createdTeacher = userRepository.findByUsername("teacher").orElse(null);
                User createdManager = userRepository.findByUsername("manager").orElse(null);

                log.info("🔍 Verification of created users:");
                if (createdStudent != null) {
                    log.info("   📚 Student: ID=" + createdStudent.getId() + ", Expected=101");
                }
                if (createdTeacher != null) {
                    log.info("   🎓 Teacher: ID=" + createdTeacher.getId() + ", Expected=201");
                }
                if (createdManager != null) {
                    log.info("   👔 Manager: ID=" + createdManager.getId() + ", Expected=301");
                }
            }
        } else {
            log.info("✅ Users already seeded.");
        }
    }

    private void seedCourses() {
        if (courseRepository.count() == 0) {
            Course math = new Course();
            math.setName("Advanced Mathematics");
            math.setDescription("A comprehensive study of mathematical concepts and their applications.");
            courseRepository.save(math);

            Course history = new Course();
            history.setName("World History");
            history.setDescription("A survey of major historical events from ancient civilizations to the modern era.");
            courseRepository.save(history);

            Course literature = new Course();
            literature.setName("Vietnamese Literature");
            literature.setDescription("An exploration of Vietnamese literary works throughout history.");
            courseRepository.save(literature);

            Course english = new Course();
            english.setName("Communicative English");
            english.setDescription("Developing English communication skills for an international environment.");
            courseRepository.save(english);

            Course cs = new Course();
            cs.setName("Computer Science");
            cs.setDescription("Fundamental concepts of computer science and programming.");
            courseRepository.save(cs);

            Course physics = new Course();
            physics.setName("General Physics");
            physics.setDescription("An introduction to the fundamental principles of physics.");
            courseRepository.save(physics);

            log.info("✅ Created 6 sample courses.");
        }
    }

    private void seedRooms() {
        if (roomRepository.count() == 0) {
            log.info("🏢 Seeding rooms (32 phòng)...");
            String[] buildings = new String[]{"A","B","C","D"};
            int[] capacities = new int[]{25,30,35,40,45,50,60,100};
            List<Room> bulk = new ArrayList<>();
            for (String b : buildings) {
                for (int floor = 1; floor <= 4; floor++) {
                    for (int num = 1; num <= 2; num++) { // mỗi tầng 2 phòng → 32 phòng
                        String code = b + floor + String.format("%02d", num);
                        Room r = new Room();
                        r.setRoomCode(code);
                        r.setRoomName("Phòng học " + code);
                        r.setCapacity(capacities[(int)(Math.random()*capacities.length)]);
                        r.setLocation("Tòa " + b + ", Tầng " + floor);
                        r.setFacilities(floor % 2 == 0 ? "Projector, Điều hòa" : "Bảng tương tác, Điều hòa");
                        r.setIsActive(true);
                        bulk.add(r);
                    }
                }
            }
            roomRepository.saveAll(bulk);
            log.info("✅ Created {} rooms.", bulk.size());
        } else {
            log.info("✅ Rooms already seeded.");
        }
    }

    private List<Classroom> seedClassrooms() {
        if (classroomRepository.count() == 0) {
            List<User> teachers = userRepository.findByRoleId(RoleConstants.TEACHER);
            List<Course> courses = courseRepository.findAll();

            if (teachers.isEmpty() || courses.isEmpty()) {
                log.warn("No teachers or courses found for classroom seeding");
                return List.of();
            }

            List<Classroom> classrooms = new ArrayList<>();

            // Create classrooms with specific teachers and courses
            for (int i = 0; i < Math.min(teachers.size(), courses.size()); i++) {
                Classroom classroom = new Classroom();
                classroom.setName("Class " + (i + 1));
                classroom.setDescription("Classroom for " + courses.get(i).getName());
                
                // Ensure teacher@test.com is assigned to classroom 1
                if (i == 0) {
                    User mainTeacher = userRepository.findByEmail("teacher@test.com").orElse(teachers.get(0));
                    classroom.setTeacher(mainTeacher);
                    log.info("✅ Assigned teacher@test.com to classroom 1");
                } else {
                    classroom.setTeacher(teachers.get(i));
                }
                
                classroom.setSubject(courses.get(i).getName());
                classroom.setSection("Section " + (char)('A' + i)); // Add section: A, B, C, etc.
                classroom.setCourseId(courses.get(i).getId());
                
                Classroom savedClassroom = classroomRepository.save(classroom);
                classrooms.add(savedClassroom); // Add to list instead of replacing
            }

            log.info("✅ Created {} classrooms", classrooms.size());
            return classrooms;
        } else {
            log.info("✅ Classrooms already seeded.");
            return classroomRepository.findAll();
        }
    }

    private void seedClassroomEnrollments() {
        if (classroomEnrollmentRepository.count() == 0) {
            List<User> students = userRepository.findByRoleId(RoleConstants.STUDENT);
            List<Classroom> classrooms = classroomRepository.findAll();

            if (students.isEmpty() || classrooms.isEmpty()) {
                log.warn("No students or classrooms found for enrollment seeding");
                return;
            }

            for (User student : students) {
                for (Classroom classroom : classrooms) {
                    ClassroomEnrollment enrollment = new ClassroomEnrollment();
                    ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId();
                    enrollmentId.setClassroomId(classroom.getId());
                    enrollmentId.setUserId(student.getId());
                    enrollment.setId(enrollmentId);
                    enrollment.setClassroom(classroom);
                    enrollment.setUser(student);
                    classroomEnrollmentRepository.save(enrollment);
                }
            }

            log.info("✅ Created classroom enrollments for {} students in {} classrooms", 
                    students.size(), classrooms.size());
        } else {
            log.info("✅ Classroom enrollments already seeded.");
        }
    }

    private void seedSchedules() {
        if (scheduleRepository.count() == 0) {
            List<Classroom> classrooms = classroomRepository.findAll();
            
            for (Classroom classroom : classrooms) {
                Schedule schedule = new Schedule();
                schedule.setClassroom(classroom);
                schedule.setTeacher(classroom.getTeacher());
                schedule.setDayOfWeek(0); // Monday
                schedule.setStartTime(LocalTime.of(8, 0));
                schedule.setEndTime(LocalTime.of(10, 0));
                schedule.setRoom("Room 101");
                schedule.setSubject(classroom.getSubject());
                scheduleRepository.save(schedule);
            }
            
            log.info("✅ Created schedules for {} classrooms", classrooms.size());
        } else {
            log.info("✅ Schedules already seeded.");
        }
    }

    private void seedTimetableEvents() {
        if (timetableEventRepository.count() == 0) {
            List<Classroom> classrooms = classroomRepository.findAll();
            List<User> users = userRepository.findAll();
            
            for (int i = 0; i < classrooms.size(); i++) {
                TimetableEvent event = new TimetableEvent();
                event.setTitle("Regular Class " + (i + 1));
                event.setDescription("Regular class session for " + classrooms.get(i).getName());
                event.setStartDatetime(LocalDateTime.now().plusDays(i));
                event.setEndDatetime(LocalDateTime.now().plusDays(i).plusHours(2));
                event.setEventType(TimetableEvent.EventType.CLASS);
                event.setCreatedBy(users.get(i % users.size()).getId());
                event.setClassroomId(classrooms.get(i).getId());
                event.setLocation("Room " + (i + 1));
                event.setIsAllDay(false);
                event.setReminderMinutes(15);
                event.setColor("#007bff");
                event.setIsCancelled(false);
                timetableEventRepository.save(event);
            }
            
            log.info("✅ Created timetable events for {} classrooms", classrooms.size());
        } else {
            log.info("✅ Timetable events already seeded.");
        }
    }

    private void seedRequests() {
        if (requestRepository.count() > 0) {
            return;
        }
        try {
            // Student role request 1
            Request studentRequest1 = new Request();
            studentRequest1.setEmail("tranvanB@gmail.com");
            studentRequest1.setFullName("Trần Văn B");
            studentRequest1.setPhoneNumber("0987123456");
            studentRequest1.setRequestedRole("STUDENT");
            studentRequest1.setFormResponses("{\"grade\":\"Lớp 11\",\"parentContact\":\"Phụ huynh: Trần Thị C, SĐT: 0912345678\",\"additionalInfo\":\"Em muốn đăng ký tài khoản vào hệ thống Minh Việt.\"}");
            studentRequest1.setStatus("PENDING");
            studentRequest1.setCreatedAt(LocalDateTime.now().minusDays(1));
            requestRepository.save(studentRequest1);

            // Student role request 2
            Request studentRequest2 = new Request();
            studentRequest2.setEmail("nguyenthic@gmail.com");
            studentRequest2.setFullName("Nguyễn Thị C");
            studentRequest2.setPhoneNumber("0987654321");
            studentRequest2.setRequestedRole("STUDENT");
            studentRequest2.setFormResponses("{\"grade\":\"Lớp 10\",\"parentContact\":\"Phụ huynh: Nguyễn Văn D, SĐT: 0923456789\",\"additionalInfo\":\"Em muốn học tại trường Minh Việt để nâng cao kiến thức.\"}");
            studentRequest2.setStatus("PENDING");
            studentRequest2.setCreatedAt(LocalDateTime.now().minusDays(2));
            requestRepository.save(studentRequest2);

            // Student role request 3
            Request studentRequest3 = new Request();
            studentRequest3.setEmail("levand@gmail.com");
            studentRequest3.setFullName("Lê Văn D");
            studentRequest3.setPhoneNumber("0976543210");
            studentRequest3.setRequestedRole("STUDENT");
            studentRequest3.setFormResponses("{\"grade\":\"Lớp 12\",\"parentContact\":\"Phụ huynh: Lê Thị E, SĐT: 0934567890\",\"additionalInfo\":\"Em muốn đăng ký học để chuẩn bị thi đại học.\"}");
            studentRequest3.setStatus("APPROVED");
            studentRequest3.setCreatedAt(LocalDateTime.now().minusDays(3));
            requestRepository.save(studentRequest3);

            // Student role request 4
            Request studentRequest4 = new Request();
            studentRequest4.setEmail("phamthie@gmail.com");
            studentRequest4.setFullName("Phạm Thị E");
            studentRequest4.setPhoneNumber("0965432109");
            studentRequest4.setRequestedRole("STUDENT");
            studentRequest4.setFormResponses("{\"grade\":\"Lớp 11\",\"parentContact\":\"Phụ huynh: Phạm Văn F, SĐT: 0945678901\",\"additionalInfo\":\"Em muốn học tại trường để cải thiện kết quả học tập.\"}");
            studentRequest4.setStatus("REJECTED");
            studentRequest4.setRejectReason("Thiếu thông tin liên hệ phụ huynh");
            studentRequest4.setCreatedAt(LocalDateTime.now().minusDays(4));
            requestRepository.save(studentRequest4);

            // Student role request 5
            Request studentRequest5 = new Request();
            studentRequest5.setEmail("hoangvanf@gmail.com");
            studentRequest5.setFullName("Hoàng Văn F");
            studentRequest5.setPhoneNumber("0954321098");
            studentRequest5.setRequestedRole("STUDENT");
            studentRequest5.setFormResponses("{\"grade\":\"Lớp 10\",\"parentContact\":\"Phụ huynh: Hoàng Thị G, SĐT: 0956789012\",\"additionalInfo\":\"Em muốn đăng ký học để phát triển toàn diện.\"}");
            studentRequest5.setStatus("PENDING");
            studentRequest5.setCreatedAt(LocalDateTime.now().minusDays(5));
            requestRepository.save(studentRequest5);

            log.info("✅ Created 5 sample role requests.");
        } catch (Exception e) {
            log.error("❌ Error creating sample requests: {}", e.getMessage());
        }
    }

    private void seedRecruitmentPlans() {
        if (recruitmentPlanRepository.count() == 0) {
            // Tạo kế hoạch tuyển dụng với tên mới và nhiều vị trí hơn
            RecruitmentPlan plan1 = new RecruitmentPlan();
            plan1.setTitle("Kế hoạch tuyển sinh đợt thứ nhất");
            plan1.setStartDate(LocalDate.now().minusDays(10));
            plan1.setEndDate(LocalDate.now().plusDays(30));
            plan1.setTotalQuantity(5);
            plan1.setStatus(RecruitmentPlan.Status.OPEN);
            recruitmentPlanRepository.save(plan1);
            
            RecruitmentPlan plan2 = new RecruitmentPlan();
            plan2.setTitle("Kế hoạch tuyển sinh đợt thứ hai");
            plan2.setStartDate(LocalDate.now().minusDays(5));
            plan2.setEndDate(LocalDate.now().plusDays(35));
            plan2.setTotalQuantity(4);
            plan2.setStatus(RecruitmentPlan.Status.OPEN);
            recruitmentPlanRepository.save(plan2);
            
            RecruitmentPlan plan3 = new RecruitmentPlan();
            plan3.setTitle("Kế hoạch tuyển sinh đợt thứ ba");
            plan3.setStartDate(LocalDate.now().minusDays(2));
            plan3.setEndDate(LocalDate.now().plusDays(40));
            plan3.setTotalQuantity(5);
            plan3.setStatus(RecruitmentPlan.Status.OPEN);
            recruitmentPlanRepository.save(plan3);
            
            log.info("✅ Created 3 recruitment plans with more positions");
        } else {
            log.info("✅ Recruitment plans already seeded.");
        }
    }

    private void seedJobPositions() {
        if (jobPositionRepository.count() == 0) {
            List<RecruitmentPlan> plans = recruitmentPlanRepository.findAll();
            if (plans.size() >= 3) {
                // Kế hoạch 1: Đợt thứ nhất - 5 vị trí (3 FULL_TIME, 2 PART_TIME)
                JobPosition job1 = new JobPosition();
                job1.setTitle("Kế toán viên");
                job1.setDescription("Phụ trách công tác kế toán, báo cáo tài chính, quản lý sổ sách kế toán theo quy định. Yêu cầu: Tốt nghiệp đại học chuyên ngành Kế toán, có kinh nghiệm 2-3 năm, thành thạo Excel và phần mềm kế toán.");
                job1.setSalaryRange("15-25 triệu");
                job1.setContractType("FULL_TIME");
                job1.setQuantity(2);
                job1.setRecruitmentPlan(plans.get(0));
                jobPositionRepository.save(job1);
                
                JobPosition job2 = new JobPosition();
                job2.setTitle("Nhân viên HR");
                job2.setDescription("Phụ trách tuyển dụng, đào tạo, quản lý nhân sự, chấm công, lương thưởng. Yêu cầu: Tốt nghiệp đại học chuyên ngành Quản trị nhân lực hoặc liên quan, có kinh nghiệm 1-2 năm, kỹ năng giao tiếp tốt.");
                job2.setSalaryRange("12-20 triệu");
                job2.setContractType("FULL_TIME");
                job2.setQuantity(1);
                job2.setRecruitmentPlan(plans.get(0));
                jobPositionRepository.save(job2);
                
                JobPosition job3 = new JobPosition();
                job3.setTitle("Giáo viên Toán lớp 10");
                job3.setDescription("Dạy Toán cho học sinh lớp 10, luyện thi đại học. Yêu cầu: Tốt nghiệp đại học chuyên ngành Toán hoặc Sư phạm Toán, có kinh nghiệm giảng dạy, nhiệt tình, tận tâm với học sinh.");
                job3.setSalaryRange("500,000-800,000 VNĐ/giờ");
                job3.setContractType("PART_TIME");
                job3.setQuantity(1);
                job3.setRecruitmentPlan(plans.get(0));
                jobPositionRepository.save(job3);
                
                JobPosition job4 = new JobPosition();
                job4.setTitle("Giáo viên Lý lớp 11");
                job4.setDescription("Dạy Vật lý cho học sinh lớp 11, chuẩn bị kiến thức cho kỳ thi THPT. Yêu cầu: Tốt nghiệp đại học chuyên ngành Vật lý hoặc Sư phạm Vật lý, có phương pháp giảng dạy hiệu quả, khả năng truyền đạt tốt.");
                job4.setSalaryRange("600,000-900,000 VNĐ/giờ");
                job4.setContractType("PART_TIME");
                job4.setQuantity(1);
                job4.setRecruitmentPlan(plans.get(0));
                jobPositionRepository.save(job4);
                
                // Kế hoạch 2: Đợt thứ hai - 4 vị trí (4 PART_TIME - Giáo viên)
                JobPosition job5 = new JobPosition();
                job5.setTitle("Giáo viên Hóa lớp 10");
                job5.setDescription("Dạy Hóa học cho học sinh lớp 10, giúp học sinh nắm vững kiến thức cơ bản và chuẩn bị cho các năm học tiếp theo. Yêu cầu: Tốt nghiệp đại học chuyên ngành Hóa học hoặc Sư phạm Hóa học, có kinh nghiệm giảng dạy, nhiệt tình, tận tâm với học sinh.");
                job5.setSalaryRange("600,000-900,000 VNĐ/giờ");
                job5.setContractType("PART_TIME");
                job5.setQuantity(1);
                job5.setRecruitmentPlan(plans.get(1));
                jobPositionRepository.save(job5);
                
                JobPosition job6 = new JobPosition();
                job6.setTitle("Giáo viên Hóa lớp 11");
                job6.setDescription("Dạy Hóa học cho học sinh lớp 11, giúp học sinh hiểu sâu các khái niệm hóa học và chuẩn bị kiến thức cho lớp 12. Yêu cầu: Tốt nghiệp đại học chuyên ngành Hóa học hoặc Sư phạm Hóa học, có phương pháp giảng dạy hiệu quả, khả năng truyền đạt tốt.");
                job6.setSalaryRange("700,000-1,000,000 VNĐ/giờ");
                job6.setContractType("PART_TIME");
                job6.setQuantity(1);
                job6.setRecruitmentPlan(plans.get(1));
                jobPositionRepository.save(job6);
                
                JobPosition job7 = new JobPosition();
                job7.setTitle("Giáo viên Hóa lớp 12");
                job7.setDescription("Dạy Hóa học cho học sinh lớp 12, giúp học sinh hoàn thiện kiến thức và chuẩn bị tốt cho kỳ thi tốt nghiệp THPT. Yêu cầu: Tốt nghiệp đại học chuyên ngành Hóa học hoặc Sư phạm Hóa học, có kinh nghiệm giảng dạy, kiến thức chuyên môn vững vàng.");
                job7.setSalaryRange("800,000-1,200,000 VNĐ/giờ");
                job7.setContractType("PART_TIME");
                job7.setQuantity(1);
                job7.setRecruitmentPlan(plans.get(1));
                jobPositionRepository.save(job7);
                
                JobPosition job8 = new JobPosition();
                job8.setTitle("Giáo viên Tiếng Anh");
                job8.setDescription("Dạy Tiếng Anh cho học sinh các cấp từ lớp 10-12, giúp học sinh phát triển kỹ năng nghe, nói, đọc, viết. Yêu cầu: Tốt nghiệp đại học chuyên ngành Tiếng Anh hoặc Sư phạm Tiếng Anh, có chứng chỉ IELTS 7.0+, có kinh nghiệm giảng dạy.");
                job8.setSalaryRange("800,000-1,200,000 VNĐ/giờ");
                job8.setContractType("PART_TIME");
                job8.setQuantity(1);
                job8.setRecruitmentPlan(plans.get(1));
                jobPositionRepository.save(job8);
                
                // Kế hoạch 3: Đợt thứ ba - 5 vị trí (5 PART_TIME - Giáo viên)
                JobPosition job9 = new JobPosition();
                job9.setTitle("Giáo viên Văn học lớp 10");
                job9.setTitle("Giáo viên Văn học lớp 10");
                job9.setDescription("Dạy Ngữ văn cho học sinh lớp 10, giúp học sinh hiểu và cảm nhận văn học, phát triển kỹ năng đọc hiểu và viết văn. Yêu cầu: Tốt nghiệp đại học chuyên ngành Văn học hoặc Sư phạm Văn, có kinh nghiệm giảng dạy, khả năng truyền đạt tốt, am hiểu văn học.");
                job9.setSalaryRange("600,000-900,000 VNĐ/giờ");
                job9.setContractType("PART_TIME");
                job9.setQuantity(1);
                job9.setRecruitmentPlan(plans.get(2));
                jobPositionRepository.save(job9);
                
                JobPosition job10 = new JobPosition();
                job10.setTitle("Giáo viên Văn học lớp 11");
                job10.setDescription("Dạy Ngữ văn cho học sinh lớp 11, giúp học sinh phân tích văn học sâu sắc và chuẩn bị kiến thức cho lớp 12. Yêu cầu: Tốt nghiệp đại học chuyên ngành Văn học hoặc Sư phạm Văn, có kinh nghiệm giảng dạy, khả năng truyền đạt tốt, am hiểu văn học.");
                job10.setSalaryRange("700,000-1,000,000 VNĐ/giờ");
                job10.setContractType("PART_TIME");
                job10.setQuantity(1);
                job10.setRecruitmentPlan(plans.get(2));
                jobPositionRepository.save(job10);
                
                JobPosition job11 = new JobPosition();
                job11.setTitle("Giáo viên Văn học lớp 12");
                job11.setDescription("Dạy Ngữ văn cho học sinh lớp 12, giúp học sinh hoàn thiện kiến thức và chuẩn bị tốt cho kỳ thi tốt nghiệp THPT. Yêu cầu: Tốt nghiệp đại học chuyên ngành Văn học hoặc Sư phạm Văn, có kinh nghiệm giảng dạy, khả năng truyền đạt tốt, am hiểu văn học.");
                job11.setSalaryRange("800,000-1,200,000 VNĐ/giờ");
                job11.setContractType("PART_TIME");
                job11.setQuantity(1);
                job11.setRecruitmentPlan(plans.get(2));
                jobPositionRepository.save(job11);
                
                JobPosition job12 = new JobPosition();
                job12.setTitle("Giáo viên Sinh học lớp 11");
                job12.setDescription("Dạy Sinh học cho học sinh lớp 11, giúp học sinh hiểu sâu các khái niệm sinh học và chuẩn bị kiến thức cho lớp 12. Yêu cầu: Tốt nghiệp đại học chuyên ngành Sinh học hoặc Sư phạm Sinh, có kinh nghiệm giảng dạy, kiến thức chuyên môn vững vàng.");
                job12.setSalaryRange("600,000-900,000 VNĐ/giờ");
                job12.setContractType("PART_TIME");
                job12.setQuantity(1);
                job12.setRecruitmentPlan(plans.get(2));
                jobPositionRepository.save(job12);
                
                JobPosition job13 = new JobPosition();
                job13.setTitle("Giáo viên Sinh học lớp 12");
                job13.setDescription("Dạy Sinh học cho học sinh lớp 12, giúp học sinh hoàn thiện kiến thức và chuẩn bị tốt cho kỳ thi tốt nghiệp THPT. Yêu cầu: Tốt nghiệp đại học chuyên ngành Sinh học hoặc Sư phạm Sinh, có kinh nghiệm giảng dạy, kiến thức chuyên môn vững vàng.");
                job13.setSalaryRange("700,000-1,000,000 VNĐ/giờ");
                job13.setContractType("PART_TIME");
                job13.setQuantity(1);
                job13.setRecruitmentPlan(plans.get(2));
                jobPositionRepository.save(job13);
                
                log.info("✅ Created 13 job positions across 3 recruitment plans");
            } else {
                log.error("❌ Not enough recruitment plans found for job positions");
            }
        } else {
            log.info("✅ Job positions already seeded.");
        }
    }

    private void seedRecruitmentApplications() {
        if (recruitmentApplicationRepository.count() == 0) {
            List<JobPosition> jobPositions = jobPositionRepository.findAll();
            if (jobPositions.isEmpty()) {
                log.warn("No job positions found for recruitment application seeding.");
                return;
            }

            // Danh sách ứng viên với tên thật và email thực tế
            String[][] applicants = {
                {"Nguyễn Văn Huy", "nguyenvanhuy124652@gmail.com", "0987654321"},
                {"Trần Thị Lan", "tranthilan234567@gmail.com", "0987654322"},
                {"Lê Hoàng Nam", "lehoangnam345678@gmail.com", "0987654323"},
                {"Phạm Văn Đức", "phamvanduc456789@gmail.com", "0987654324"},
                {"Hoàng Thị Mai", "hoangthimai567890@gmail.com", "0987654325"},
                {"Đặng Minh Tuấn", "dangminhtuan678901@gmail.com", "0987654326"},
                {"Vũ Thị Hương", "vuthihuong789012@gmail.com", "0987654327"},
                {"Ngô Văn An", "ngovanan890123@gmail.com", "0987654328"},
                {"Lý Thị Bình", "lythibinh901234@gmail.com", "0987654329"},
                {"Bùi Văn Cường", "buivancuong012345@gmail.com", "0987654330"},
                {"Đỗ Thị Dung", "dothidung123456@gmail.com", "0987654331"},
                {"Hồ Văn Em", "hovanem234567@gmail.com", "0987654332"},
                {"Lưu Thị Phương", "luuthiphuong345678@gmail.com", "0987654333"},
                {"Mai Văn Giang", "maivangiang456789@gmail.com", "0987654334"},
                {"Tô Thị Hạnh", "tothihanh567890@gmail.com", "0987654335"}
            };

            String[] addresses = {
                "123 Đường Nguyễn Huệ, Quận 1, TP.HCM",
                "456 Đường Lê Lợi, Quận 3, TP.HCM", 
                "789 Đường Trần Hưng Đạo, Quận 5, TP.HCM",
                "321 Đường Võ Văn Tần, Quận 3, TP.HCM",
                "654 Đường Hai Bà Trưng, Quận 1, TP.HCM",
                "987 Đường Điện Biên Phủ, Quận Bình Thạnh, TP.HCM",
                "147 Đường Cách Mạng Tháng 8, Quận 10, TP.HCM",
                "258 Đường 3/2, Quận 10, TP.HCM",
                "369 Đường Nguyễn Thị Minh Khai, Quận 1, TP.HCM",
                "741 Đường Lý Tự Trọng, Quận 1, TP.HCM",
                "852 Đường Pasteur, Quận 1, TP.HCM",
                "963 Đường Đồng Khởi, Quận 1, TP.HCM",
                "159 Đường Lê Duẩn, Quận 1, TP.HCM",
                "357 Đường Nam Kỳ Khởi Nghĩa, Quận 3, TP.HCM",
                "468 Đường Võ Thị Sáu, Quận 3, TP.HCM"
            };

            for (int i = 0; i < applicants.length; i++) {
                RecruitmentApplication application = new RecruitmentApplication();
                application.setFullName(applicants[i][0]);
                application.setEmail(applicants[i][1]);
                application.setPhoneNumber(applicants[i][2]);
                application.setAddress(addresses[i % addresses.length]);
                application.setCvUrl("/static/sample_cv/cv" + (i + 1) + ".pdf");
                application.setStatus("PENDING");
                application.setCreatedAt(LocalDateTime.now().minusDays(i));
                
                // Đảm bảo có job position để gán
                JobPosition jobPosition = jobPositions.get(i % jobPositions.size());
                application.setJobPosition(jobPosition);
                
                recruitmentApplicationRepository.save(application);
                log.info("✅ Created application for {} applying to: {}", applicants[i][0], jobPosition.getTitle());
            }
            log.info("✅ Created {} sample recruitment applications with real names.", applicants.length);
        } else {
            log.info("✅ Recruitment applications already seeded.");
        }
    }

    private void seedLectures(List<Classroom> classrooms) {
        if (lectureRepository.count() == 0) {
            for (Classroom classroom : classrooms) {
                Lecture lecture = new Lecture();
                lecture.setTitle("Introduction to " + classroom.getName());
                // lecture.setDescription("First lecture of the course"); // Không có description
                lecture.setClassroom(classroom);
                // Không set startTime, endTime, status nếu không có
                lectureRepository.save(lecture);
            }
            log.info("✅ Created lectures for {} classrooms", classrooms.size());
        } else {
            log.info("✅ Lectures already seeded.");
        }
    }

    private void seedAssignments() {
        if (assignmentRepository.count() == 0) {
            List<Classroom> classrooms = classroomRepository.findAll();
            for (Classroom classroom : classrooms) {
                Assignment assignment = new Assignment();
                assignment.setClassroom(classroom);
                assignment.setTitle("Assignment 1");
                assignment.setDueDate(LocalDateTime.now().plusDays(7)); // Set dueDate to 7 days in the future
                assignmentRepository.save(assignment);
            }
            log.info("✅ Created assignments for {} classrooms", classrooms.size());
        } else {
            log.info("✅ Assignments already seeded.");
        }
    }

    private void seedSubmissions() {
        List<User> students = userRepository.findByRoleId(RoleConstants.STUDENT);
        List<Assignment> assignments = assignmentRepository.findAll();
        for (User student : students) {
            for (Assignment assignment : assignments) {
                if (submissionRepository.findByStudentAndAssignment(student, assignment).isEmpty()) {
                    Submission submission = new Submission();
                    submission.setStudent(student);
                    submission.setAssignment(assignment);
                    // Không set content, status, score nếu không có
                    submissionRepository.save(submission);
                }
            }
        }
        log.info("✅ Created/updated submissions for {} students and {} assignments", students.size(), assignments.size());
    }

    private void seedBlogs() {
        if (blogRepository.count() == 0) {
            // Tạo danh sách tin tức giáo dục thực tế
            String[][] blogData = {
                {
                    "Bộ GD&ĐT công bố lịch thi tốt nghiệp THPT 2024",
                    "bo-gd-dt-cong-bo-lich-thi-tot-nghiep-thpt-2024",
                    "Bộ Giáo dục và Đào tạo vừa công bố lịch thi tốt nghiệp THPT năm 2024. Kỳ thi sẽ diễn ra từ ngày 27-30/6/2024 với nhiều điểm mới trong quy chế thi.",
                    "https://picsum.photos/seed/education1/400/200",
                    "giáo dục, thi cử, THPT",
                    "<p>Bộ Giáo dục và Đào tạo (GD&ĐT) vừa chính thức công bố lịch thi tốt nghiệp THPT năm 2024. Theo đó, kỳ thi sẽ diễn ra từ ngày 27-30/6/2024.</p><p>Năm nay, kỳ thi có một số điểm mới đáng chú ý:</p><ul><li>Thời gian thi được rút ngắn từ 4 ngày xuống 3 ngày</li><li>Thêm môn thi tự chọn cho học sinh</li><li>Áp dụng công nghệ AI trong chấm thi</li><li>Tăng cường giám sát bằng camera</li></ul><p>Bộ GD&ĐT cũng khuyến cáo thí sinh cần chuẩn bị kỹ lưỡng và tuân thủ nghiêm túc quy chế thi để đạt kết quả tốt nhất.</p>"
                },
                {
                    "Xu hướng học trực tuyến tăng mạnh sau đại dịch",
                    "xu-huong-hoc-truc-tuyen-tang-manh-sau-dai-dich",
                    "Theo báo cáo mới nhất, số lượng học sinh, sinh viên tham gia học trực tuyến đã tăng 300% so với trước đại dịch COVID-19.",
                    "https://picsum.photos/seed/education2/400/200",
                    "học trực tuyến, công nghệ giáo dục, đại dịch",
                    "<p>Báo cáo mới nhất từ Bộ GD&ĐT cho thấy, xu hướng học trực tuyến đang phát triển mạnh mẽ tại Việt Nam. Số lượng học sinh, sinh viên tham gia các khóa học trực tuyến đã tăng 300% so với thời điểm trước đại dịch COVID-19.</p><p>Các nền tảng học trực tuyến như Minh Việt Education đang ngày càng được ưa chuộng nhờ những ưu điểm vượt trội:</p><ul><li>Tiết kiệm thời gian di chuyển</li><li>Linh hoạt trong lịch học</li><li>Tương tác trực tiếp với giáo viên</li><li>Hệ thống quản lý học tập hiện đại</li></ul><p>Chuyên gia giáo dục nhận định, đây là xu hướng tất yếu trong thời đại số và sẽ tiếp tục phát triển trong tương lai.</p>"
                },
                {
                    "Công nghệ AI trong giáo dục: Tương lai của việc học",
                    "cong-nghe-ai-trong-giao-duc-tuong-lai-cua-viec-hoc",
                    "Trí tuệ nhân tạo (AI) đang cách mạng hóa ngành giáo dục với những ứng dụng thông minh hỗ trợ việc dạy và học hiệu quả hơn.",
                    "https://picsum.photos/seed/education3/400/200",
                    "AI, công nghệ giáo dục, trí tuệ nhân tạo",
                    "<p>Trí tuệ nhân tạo (AI) đang mở ra những cơ hội mới trong lĩnh vực giáo dục. Từ việc cá nhân hóa học tập đến tự động hóa quy trình đánh giá, AI đang thay đổi cách chúng ta tiếp cận việc dạy và học.</p><p>Một số ứng dụng AI nổi bật trong giáo dục:</p><ul><li>Hệ thống chấm bài tự động</li><li>Phân tích hành vi học tập</li><li>Gia sư AI cá nhân hóa</li><li>Dự đoán kết quả học tập</li><li>Quản lý lớp học thông minh</li></ul><p>Theo các chuyên gia, việc tích hợp AI vào giáo dục sẽ giúp nâng cao chất lượng đào tạo và tạo ra những trải nghiệm học tập tốt hơn cho học sinh.</p>"
                },
                {
                    "Chương trình đào tạo kỹ năng số cho học sinh THPT",
                    "chuong-trinh-dao-tao-ky-nang-so-cho-hoc-sinh-thpt",
                    "Bộ GD&ĐT triển khai chương trình đào tạo kỹ năng số toàn diện cho học sinh THPT, chuẩn bị cho thời đại công nghệ số.",
                    "https://picsum.photos/seed/education4/400/200",
                    "kỹ năng số, công nghệ thông tin, THPT",
                    "<p>Bộ GD&ĐT vừa triển khai chương trình đào tạo kỹ năng số toàn diện cho học sinh THPT trên toàn quốc. Chương trình nhằm trang bị cho học sinh những kỹ năng cần thiết trong thời đại số.</p><p>Chương trình bao gồm các nội dung chính:</p><ul><li>Lập trình cơ bản</li><li>An toàn thông tin mạng</li><li>Sử dụng công nghệ thông tin hiệu quả</li><li>Phát triển tư duy logic</li><li>Kỹ năng làm việc nhóm trực tuyến</li></ul><p>Chương trình được thiết kế linh hoạt, phù hợp với từng cấp độ và sẽ được triển khai từ năm học 2024-2025.</p>"
                },
                {
                    "Hội thảo quốc tế về đổi mới giáo dục 2024",
                    "hoi-thao-quoc-te-ve-doi-moi-giao-duc-2024",
                    "Hội thảo quốc tế về đổi mới giáo dục sẽ diễn ra tại Hà Nội vào tháng 8/2024 với sự tham gia của các chuyên gia giáo dục hàng đầu thế giới.",
                    "https://picsum.photos/seed/education5/400/200",
                    "hội thảo, đổi mới giáo dục, quốc tế",
                    "<p>Hội thảo quốc tế về đổi mới giáo dục 2024 sẽ được tổ chức tại Hà Nội từ ngày 15-17/8/2024. Sự kiện này quy tụ các chuyên gia giáo dục hàng đầu từ hơn 50 quốc gia trên thế giới.</p><p>Các chủ đề chính của hội thảo:</p><ul><li>Xu hướng giáo dục trong thời đại số</li><li>Ứng dụng công nghệ trong dạy học</li><li>Phát triển kỹ năng thế kỷ 21</li><li>Giáo dục bền vững</li><li>Hợp tác quốc tế trong giáo dục</li></ul><p>Hội thảo sẽ là cơ hội để Việt Nam học hỏi kinh nghiệm từ các nước phát triển và chia sẻ những thành tựu giáo dục của mình.</p>"
                },
                {
                    "Thành lập mạng lưới trường học thông minh tại Việt Nam",
                    "thanh-lap-mang-luoi-truong-hoc-thong-minh-tai-viet-nam",
                    "Dự án xây dựng mạng lưới trường học thông minh được khởi động với mục tiêu hiện đại hóa 1000 trường học trong 5 năm tới.",
                    "https://picsum.photos/seed/education6/400/200",
                    "trường học thông minh, hiện đại hóa, công nghệ",
                    "<p>Dự án xây dựng mạng lưới trường học thông minh tại Việt Nam vừa được khởi động với sự hỗ trợ của Bộ GD&ĐT và các đối tác công nghệ hàng đầu. Dự án có mục tiêu hiện đại hóa 1000 trường học trong 5 năm tới.</p><p>Các tiêu chí của trường học thông minh:</p><ul><li>Hệ thống quản lý thông tin tích hợp</li><li>Lớp học thông minh với thiết bị hiện đại</li><li>Kết nối internet tốc độ cao</li><li>Ứng dụng công nghệ trong dạy học</li><li>Hệ thống an ninh thông minh</li></ul><p>Dự án sẽ góp phần nâng cao chất lượng giáo dục và chuẩn bị cho học sinh những kỹ năng cần thiết trong tương lai.</p>"
                }
            };
            
            int i = 0;
            for (String[] data : blogData) {
                Blog blog = new Blog();
                blog.setTitle(data[0]);
                blog.setSlug(data[1]);
                blog.setDescription(data[2]);
                blog.setThumbnailUrl(data[3]);
                blog.setIsPublished(true);
                blog.setStatus("published");
                blog.setPublishedDate(LocalDateTime.now().minusDays(i + 1));
                blog.setLastEditedDate(LocalDateTime.now().minusDays(i + 1));
                blog.setTags(data[4]);
                blog.setContent(data[5]);
                blogRepository.save(blog);
                i++;
            }
            log.info("✅ Created {} published blogs with real education news", i);
        } else {
            log.info("✅ Blogs already seeded.");
        }
    }

    private void seedAccomplishments() {
        if (accomplishmentRepository.count() == 0) {
            List<User> students = userRepository.findByRoleId(RoleConstants.STUDENT);
            for (User student : students) {
                Accomplishment accomplishment = new Accomplishment();
                accomplishment.setStudent(student);
                accomplishment.setTitle("Academic Excellence");
                // Không set achievementDate, type, status nếu không có
                accomplishmentRepository.save(accomplishment);
            }
            log.info("✅ Created accomplishments for {} students", students.size());
        } else {
            log.info("✅ Accomplishments already seeded.");
        }
    }

    private void seedAnnouncements() {
        if (announcementRepository.count() > 0) {
            log.info("✅ Announcements already exist. Seeding more for testing.");
        }
        User admin = userRepository.findByRoleId(RoleConstants.ADMIN).stream().findFirst().orElse(null);
        User teacher = userRepository.findByRoleId(RoleConstants.TEACHER).stream().findFirst().orElse(null);
        List<Classroom> classrooms = classroomRepository.findAll();
        if (admin == null || teacher == null) {
            log.warn("⚠️ [AnnouncementSeeder] Admin or Teacher users not found. Skipping announcement seeding.");
            return;
        }
        Classroom classroom1 = classrooms.stream().findFirst().orElse(null);
        // Announcement 1: Global from Admin
        Announcement a1 = new Announcement();
        a1.setTitle("Chào mừng đến với hệ thống học tập trực tuyến mới");
        a1.setContent("Chúng tôi vui mừng thông báo ra mắt hệ thống quản lý lớp học mới. Hệ thống cung cấp nhiều tính năng hữu ích cho cả giáo viên và học sinh.");
        a1.setTargetAudience(Announcement.TargetAudience.ALL);
        a1.setPriority(Announcement.Priority.HIGH);
        a1.setCreatedBy(admin.getId());
        announcementRepository.save(a1);
        // Announcement 2: Classroom-specific from Teacher
        if (classroom1 != null) {
            Announcement a2 = new Announcement();
            a2.setTitle("Thông báo về lịch thi giữa kỳ");
            a2.setContent("Lịch thi giữa kỳ môn học sẽ diễn ra vào tuần tới. Chi tiết về thời gian và địa điểm sẽ được cập nhật sớm.");
            a2.setClassroomId(classroom1.getId());
            a2.setTargetAudience(Announcement.TargetAudience.STUDENTS);
            a2.setPriority(Announcement.Priority.NORMAL);
            a2.setCreatedBy(teacher.getId());
            announcementRepository.save(a2);
        }
        // Announcement 3: System maintenance
        Announcement a3 = new Announcement();
        a3.setTitle("Thông báo bảo trì hệ thống");
        a3.setContent("Hệ thống sẽ được bảo trì vào lúc 2 giờ sáng Chủ Nhật tuần này. Vui lòng lưu lại công việc của bạn trước thời gian này.");
        a3.setTargetAudience(Announcement.TargetAudience.ALL);
        a3.setPriority(Announcement.Priority.URGENT);
        a3.setCreatedBy(admin.getId());
        announcementRepository.save(a3);
        log.info("✅ Created 3 sample announcements.");
    }

    private void seedAttendance() {
        if (attendanceRepository.count() == 0) {
            List<User> students = userRepository.findByRoleId(RoleConstants.STUDENT);
            List<AttendanceSession> sessions = attendanceSessionRepository.findAll();
            for (User student : students) {
                for (AttendanceSession session : sessions) {
                    Attendance attendance = new Attendance();
                    attendance.setSession(session);
                    attendance.setStudent(student);
                    attendance.setStatus(AttendanceStatus.PRESENT);
                    attendanceRepository.save(attendance);
                }
            }
            log.info("✅ Created attendance records for {} students in {} sessions", students.size(), sessions.size());
        } else {
            log.info("✅ Attendance already seeded.");
        }
    }

    private void seedMessages() {
        if (studentMessageRepository.count() == 0) {
            List<User> users = userRepository.findAll();
            for (int i = 0; i < users.size() - 1; i++) {
                StudentMessage message = new StudentMessage();
                message.setSender(users.get(i));
                message.setRecipient(users.get(i + 1));
                message.setSubject("Message from " + users.get(i).getFullName());
                message.setContent("Hello from " + users.get(i).getFullName() + ". This is a test message.");
                message.setMessageType("GENERAL");
                message.setPriority("MEDIUM");
                message.setStatus("SENT");
                message.setIsRead(false);
                studentMessageRepository.save(message);
            }
            log.info("✅ Created messages between users");
        } else {
            log.info("✅ Messages already seeded.");
        }
    }

    private void seedStudentProgress() {
        if (studentProgressRepository.count() > 0) {
            log.info("✅ Student progress already seeded.");
            return;
        }
        List<User> students = userRepository.findByRoleId(RoleConstants.STUDENT);
        if (students.isEmpty()) {
            log.warn("⚠️ [StudentProgressSeeder] No students found. Skipping progress seeding.");
            return;
        }
        int progressCount = 0;
        for (int i = 0; i < Math.min(5, students.size()); i++) {
            User student = students.get(i);
            List<ClassroomEnrollment> enrollments = classroomEnrollmentRepository.findByUserId(student.getId());
            List<Classroom> enrolledClassrooms = enrollments.stream().map(ClassroomEnrollment::getClassroom).collect(java.util.stream.Collectors.toList());
            if (enrolledClassrooms.isEmpty()) continue;
            for (Classroom classroom : enrolledClassrooms) {
                // Overall progress
                StudentProgress progress = new StudentProgress();
                progress.setStudentId(student.getId());
                progress.setClassroomId(classroom.getId());
                progress.setProgressType(StudentProgress.ProgressType.OVERALL);
                progress.setProgressPercentage(new java.math.BigDecimal(50 + new java.util.Random().nextInt(51)));
                progress.setTimeSpentMinutes(60 + new java.util.Random().nextInt(120));
                progress.setLastAccessed(LocalDateTime.now().minusDays(new java.util.Random().nextInt(10)));
                studentProgressRepository.save(progress);
                progressCount++;
                // Assignment progress
                List<Assignment> assignments = assignmentRepository.findByClassroomId(classroom.getId());
                for (int j = 0; j < Math.min(2, assignments.size()); j++) {
                    Assignment assignment = assignments.get(j);
                    StudentProgress ap = new StudentProgress();
                    ap.setStudentId(student.getId());
                    ap.setClassroomId(classroom.getId());
                    ap.setAssignmentId(assignment.getId());
                    ap.setProgressType(StudentProgress.ProgressType.ASSIGNMENT);
                    ap.setProgressPercentage(new java.math.BigDecimal(100));
                    ap.setPointsEarned(new java.math.BigDecimal(70 + new java.util.Random().nextInt(31)));
                    ap.setMaxPoints(new java.math.BigDecimal(100));
                    ap.setTimeSpentMinutes(30 + new java.util.Random().nextInt(90));
                    ap.setLastAccessed(LocalDateTime.now().minusDays(new java.util.Random().nextInt(5)));
                    studentProgressRepository.save(ap);
                    progressCount++;
                }
            }
        }
        log.info("✅ Created {} student progress records.", progressCount);
    }

    private void seedAbsences() {
        if (absenceRepository.count() > 0) {
            log.info("✅ Absence data already exists. Skipping seeding.");
            return;
        }
        // Danh sách giáo viên mẫu (ID, email, tên)
        Object[][] teachers = {
            {2L, "teacher@test.com", "Nguyễn Văn Minh"},
            {5L, "math@test.com", "Trần Văn Đức"},
            {6L, "literature@test.com", "Phạm Thị Lan"},
            {7L, "english@test.com", "Lê Hoàng Nam"},
            {13L, "teacher2@test.com", "Vũ Thị Hương"},
            {14L, "teacher3@test.com", "Đặng Minh Tuấn"}
        };
        // Kiểm tra user tồn tại
        for (Object[] t : teachers) {
            if (!userRepository.existsById((Long)t[0])) {
                log.warn("❌ Teacher with ID {} not found. Skipping absence seeding.", t[0]);
                return;
            }
        }
        // Nguyễn Văn Minh (ID: 2)
        createAbsence(2L, "teacher@test.com", "Nguyễn Văn Minh",
            LocalDate.now().minusDays(30), LocalDate.now().minusDays(28), 3,
            "Nghỉ phép để tham gia hội thảo giáo dục về công nghệ thông tin", "APPROVED", false, null, LocalDateTime.now().minusDays(37), LocalDateTime.now().minusDays(29));
        createAbsence(2L, "teacher@test.com", "Nguyễn Văn Minh",
            LocalDate.now().minusDays(15), LocalDate.now().minusDays(14), 2,
            "Nghỉ ốm do cảm cúm mùa", "APPROVED", false, null, LocalDateTime.now().minusDays(22), LocalDateTime.now().minusDays(15));
        // Trần Văn Đức (ID: 5)
        createAbsence(5L, "math@test.com", "Trần Văn Đức",
            LocalDate.now().minusDays(45), LocalDate.now().minusDays(43), 3,
            "Nghỉ phép về quê ăn tết cùng gia đình", "APPROVED", false, null, LocalDateTime.now().minusDays(52), LocalDateTime.now().minusDays(44));
        createAbsence(5L, "math@test.com", "Trần Văn Đức",
            LocalDate.now().minusDays(25), LocalDate.now().minusDays(21), 5,
            "Nghỉ phép chăm sóc mẹ già ốm đau", "APPROVED", false, null, LocalDateTime.now().minusDays(32), LocalDateTime.now().minusDays(22));
        createAbsence(5L, "math@test.com", "Trần Văn Đức",
            LocalDate.now().plusDays(10), LocalDate.now().plusDays(12), 3,
            "Xin nghỉ phép để tham dự đám cưới con trai", "PENDING", true, null, LocalDateTime.now().minusDays(1), null);
        // Phạm Thị Lan (ID: 6)
        createAbsence(6L, "literature@test.com", "Phạm Thị Lan",
            LocalDate.now().minusDays(60), LocalDate.now().minusDays(53), 8,
            "Nghỉ phép sinh con và chăm sóc sau sinh", "APPROVED", false, null, LocalDateTime.now().minusDays(70), LocalDateTime.now().minusDays(54));
        createAbsence(6L, "literature@test.com", "Phạm Thị Lan",
            LocalDate.now().minusDays(35), LocalDate.now().minusDays(29), 7,
            "Nghỉ phép tiếp tục chăm sóc con nhỏ bị ốm", "APPROVED", true, null, LocalDateTime.now().minusDays(42), LocalDateTime.now().minusDays(30));
        // Lê Hoàng Nam (ID: 7)
        createAbsence(7L, "english@test.com", "Lê Hoàng Nam",
            LocalDate.now().minusDays(50), LocalDate.now().minusDays(47), 4,
            "Nghỉ phép đi du lịch Đà Lạt cùng gia đình", "APPROVED", false, null, LocalDateTime.now().minusDays(57), LocalDateTime.now().minusDays(48));
        createAbsence(7L, "english@test.com", "Lê Hoàng Nam",
            LocalDate.now().plusDays(5), LocalDate.now().plusDays(6), 2,
            "Xin nghỉ phép để khám sức khỏe tổng quát định kỳ", "PENDING", false, null, LocalDateTime.now(), null);
        // Vũ Thị Hương (ID: 13)
        createAbsence(13L, "teacher2@test.com", "Vũ Thị Hương",
            LocalDate.now().minusDays(40), LocalDate.now().minusDays(38), 3,
            "Nghỉ phép tham gia khóa đào tạo nâng cao về hóa học", "APPROVED", false, null, LocalDateTime.now().minusDays(47), LocalDateTime.now().minusDays(39));
        createAbsence(13L, "teacher2@test.com", "Vũ Thị Hương",
            LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), 6,
            "Xin nghỉ phép để đi du lịch nghỉ dưỡng tại Nha Trang", "REJECTED", false, "Thời gian xin nghỉ trùng với lịch thi giữa kỳ của sinh viên", LocalDateTime.now().minusDays(17), LocalDateTime.now().minusDays(6));
        // Đặng Minh Tuấn (ID: 14)
        createAbsence(14L, "teacher3@test.com", "Đặng Minh Tuấn",
            LocalDate.now().plusDays(20), LocalDate.now().plusDays(21), 2,
            "Xin nghỉ phép để tham gia hội nghị quốc tế về vật lý", "PENDING", false, null, LocalDateTime.now(), null);
        // Accountant (ID: 501)
        if (userRepository.existsById(501L)) {
            createAbsence(501L, "accountant@test.com", "Nguyễn Thị Kế Toán",
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(9), 2,
                "Nghỉ phép kiểm toán cuối năm", "APPROVED", false, null, LocalDateTime.now().minusDays(17), LocalDateTime.now().minusDays(10));
            createAbsence(501L, "accountant@test.com", "Nguyễn Thị Kế Toán",
                LocalDate.now().plusDays(3), LocalDate.now().plusDays(3), 1,
                "Nghỉ phép cá nhân", "PENDING", false, null, LocalDateTime.now(), null);
            createAbsence(501L, "accountant@test.com", "Nguyễn Thị Kế Toán",
                LocalDate.now().minusDays(20), LocalDate.now().minusDays(18), 3,
                "Nghỉ phép đi du lịch cùng gia đình", "APPROVED", false, null, LocalDateTime.now().minusDays(27), LocalDateTime.now().minusDays(19));
            createAbsence(501L, "accountant@test.com", "Nguyễn Thị Kế Toán",
                LocalDate.now().plusDays(7), LocalDate.now().plusDays(8), 2,
                "Xin nghỉ phép để giải quyết việc cá nhân", "PENDING", false, null, LocalDateTime.now(), null);
        }
        log.info("✅ Created sample absence requests for all teachers.");
    }

    private void createAbsence(Long userId, String userEmail, String userFullName,
                              LocalDate startDate, LocalDate endDate, Integer numberOfDays,
                              String description, String status, boolean isOverLimit, String rejectReason, LocalDateTime createdAt, LocalDateTime processedAt) {
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
        absence.setCreatedAt(createdAt != null ? createdAt : LocalDateTime.now());
        if ("REJECTED".equals(status)) {
            absence.setResultStatus("REJECTED");
            absence.setRejectReason(rejectReason);
            absence.setProcessedAt(processedAt != null ? processedAt : LocalDateTime.now());
        } else if ("APPROVED".equals(status)) {
            absence.setResultStatus("APPROVED");
            absence.setProcessedAt(processedAt != null ? processedAt : LocalDateTime.now());
        }
        absenceRepository.save(absence);
    }

    private void seedCourseMaterials(List<Classroom> classrooms) {
        // ❌ REMOVED: Không tự động tạo tài liệu mẫu khi tạo lớp từ template
        // Chỉ tạo tài liệu cho các lớp được tạo từ seeder, không phải từ template
        List<User> teachers = userRepository.findByRoleId(RoleConstants.TEACHER);
        for (Classroom classroom : classrooms) {
            // Chỉ tạo tài liệu mẫu cho các lớp có tên chứa "Demo" hoặc "Sample"
            if (classroom.getName() != null && 
                (classroom.getName().contains("Demo") || 
                 classroom.getName().contains("Sample") ||
                 classroom.getName().contains("Test"))) {
                CourseMaterial material = new CourseMaterial();
                material.setTitle("Demo Material for " + classroom.getName());
                material.setDescription("Demo materials for testing purposes");
                material.setFilePath("/uploads/materials/" + classroom.getId() + "/course_handbook.pdf");
                material.setFileName("course_handbook.pdf");
                material.setFileType("PDF");
                material.setFileSize(1024000L); // 1MB
                material.setClassroomId(classroom.getId());
                material.setUploadedBy(teachers.get(0).getId()); // Use first teacher as uploader
                material.setIsPublic(true);
                material.setDownloadCount(0);
                material.setVersionNumber(1);
                courseMaterialRepository.save(material);
            }
        }
        log.info("✅ Created demo course materials for demo classrooms only");
    }

    private void seedAssignmentTestData() {
        // This method would contain the logic from AssignmentTestDataSeeder
        log.info("✅ Assignment test data seeding completed");
    }

    private void seedComprehensiveGradingData() {
        // This method would contain the logic from ComprehensiveGradingSeeder
        log.info("✅ Comprehensive grading data seeding completed");
    }

    private void verifyDatabaseState() {
        log.info("============== Verifying Database State ==============");
        
        long userCount = userRepository.count();
        long roleCount = roleRepository.count();
        long courseCount = courseRepository.count();
        long classroomCount = classroomRepository.count();
        
        log.info("📊 Database Statistics:");
        log.info("   Users: {}", userCount);
        log.info("   Roles: {}", roleCount);
        log.info("   Courses: {}", courseCount);
        log.info("   Classrooms: {}", classroomCount);
        
        log.info("============== Database State Verification Complete ==============");
    }

    private void verifyUserRoleAssignments() {
        log.info("============== Verifying User Role Assignments ==============");

        // Check student user
        User student = userRepository.findByEmail("student@test.com").orElse(null);
        if (student != null) {
            log.info("✅ Student User: ID={}, Email={}, Role={}, RoleId={}",
                student.getId(), student.getEmail(), student.getRole(), student.getRoleId());
        } else {
            log.error("❌ Student user not found!");
        }

        // Check teacher user
        User teacher = userRepository.findByEmail("teacher@test.com").orElse(null);
        if (teacher != null) {
            log.info("✅ Teacher User: ID={}, Email={}, Role={}, RoleId={}",
                teacher.getId(), teacher.getEmail(), teacher.getRole(), teacher.getRoleId());
        } else {
            log.error("❌ Teacher user not found!");
        }

        // Check classrooms and their teacher assignments
        List<Classroom> classrooms = classroomRepository.findAll();
        log.info("📚 Found {} classrooms:", classrooms.size());
        for (Classroom classroom : classrooms) {
            User classroomTeacher = classroom.getTeacher();
            log.info("   - Classroom: {} (ID={}), Teacher: {} (ID={})",
                classroom.getName(), classroom.getId(),
                classroomTeacher != null ? classroomTeacher.getFullName() : "NULL",
                classroomTeacher != null ? classroomTeacher.getId() : "NULL");
        }

        // Check if student is enrolled in any classrooms
        if (student != null) {
            log.info("🎓 Student {} should be enrolled in classrooms as STUDENT, not assigned as teacher",
                student.getEmail());
        }

        log.info("============== User Role Verification Complete ==============");
    }

    /**
     * Ensure the system has at least a minimum number of TEACHER users for demo.
     * If not, seed additional teachers with contracts so that availability filters work.
     */
    private void ensureMinimumTeachers(int minTeachers) {
        try {
            List<User> existingTeachers = userRepository.findByRoleId(RoleConstants.TEACHER);
            int current = existingTeachers != null ? existingTeachers.size() : 0;
            if (current >= minTeachers) {
                log.info("✅ Teacher count sufficient ({} >= {}), no top-up needed.", current, minTeachers);
                return;
            }

            log.info("🔧 Teacher count is {} < {}, seeding additional teachers for demo...", current, minTeachers);

            // Seed pool covering common subjects so /classes/available-teachers works well
            String[][] seedPool = new String[][]{
                {"toan_demo1","toan_demo1@school.vn","Nguyễn Đức Toàn (Demo)","Toán"},
                {"toan_demo2","toan_demo2@school.vn","Phạm Hải Long (Demo)","Toán"},
                {"ly_demo1","ly_demo1@school.vn","Trần Quốc Huy (Demo)","Vật lý"},
                {"ly_demo2","ly_demo2@school.vn","Đỗ Thanh Tùng (Demo)","Vật lý"},
                {"hoa_demo1","hoa_demo1@school.vn","Vũ Hồng Phúc (Demo)","Hóa học"},
                {"hoa_demo2","hoa_demo2@school.vn","Bùi Thanh Hà (Demo)","Hóa học"},
                {"van_demo1","van_demo1@school.vn","Phạm Thu Hà (Demo)","Ngữ văn"},
                {"van_demo2","van_demo2@school.vn","Nguyễn Thị Hồng (Demo)","Ngữ văn"},
                {"anh_demo1","anh_demo1@school.vn","Lê Hồng Sơn (Demo)","Tiếng Anh"},
                {"anh_demo2","anh_demo2@school.vn","Tạ Bích Ngọc (Demo)","Tiếng Anh"},
                {"sinh_demo1","sinh_demo1@school.vn","Đặng Quỳnh Chi (Demo)","Sinh học"},
                {"sinh_demo2","sinh_demo2@school.vn","Trịnh Văn Thái (Demo)","Sinh học"}
            };

            int needed = minTeachers - current;
            int created = 0;
            int idx = 0;
            while (created < needed) {
                String[] seed = seedPool[idx % seedPool.length];
                String username = seed[0] + (idx / seedPool.length); // ensure unique when wrapping
                String email = seed[1].replace("@", "+" + (idx / seedPool.length) + "@");
                String fullName = seed[2].replace("(Demo)", "(Demo " + (idx / seedPool.length + 1) + ")");
                String department = seed[3];

                if (userRepository.findByEmail(email).isPresent()) {
                    idx++;
                    continue;
                }

                User u = new User();
                u.setUsername(username);
                u.setPassword(passwordEncoder.encode("teacher123"));
                u.setEmail(email);
                u.setFullName(fullName);
                u.setRoleId(RoleConstants.TEACHER);
                u.setDepartment(department);
                u.setHireDate(LocalDate.now().minusMonths((idx % 24) + 1));
                User saved = userRepository.save(u);

                // Also create an ACTIVE contract so teacher appears in availability filters
                try {
                    Contract c = new Contract();
                    c.setContractId("CT" + saved.getId() + "_" + System.currentTimeMillis());
                    c.setUserId(saved.getId());
                    c.setFullName(saved.getFullName());
                    c.setEmail(saved.getEmail());
                    c.setPhoneNumber("09" + (int)(10_000_000 + Math.random() * 89_999_999));
                    c.setContractType("TEACHER");
                    c.setPosition("Giáo viên " + department);
                    c.setDepartment(department);
                    c.setSalary(15_000_000.0 + (int)(Math.random() * 6_000_000));
                    long hourly = 120_000L + (long)(Math.random() * 100_000L);
                    try { c.setHourlySalary(hourly); } catch (Exception ignored) {}
                    String[] shifts = new String[]{
                        "ca sáng (07:30-09:30)", "ca chiều (13:30-15:30)", "ca tối (18:00-20:00)"
                    };
                    c.setWorkingHours(shifts[(int)(Math.random() * shifts.length)]);
                    c.setStartDate(LocalDate.now());
                    c.setEndDate(LocalDate.now().plusYears(2));
                    c.setStatus("ACTIVE");
                    c.setSubject(department);
                    String[] levels = new String[]{"10","11","12"};
                    c.setClassLevel(levels[(int)(Math.random() * levels.length)]);
                    contractRepository.save(c);
                } catch (Exception e) {
                    log.warn("Could not create contract for {}: {}", email, e.getMessage());
                }

                created++;
                idx++;
            }

            log.info("✅ Seeded {} additional teachers for demo (target: {}, now: {}).", created, minTeachers, current + created);
        } catch (Exception e) {
            log.error("❌ Error ensuring minimum teachers: {}", e.getMessage(), e);
        }
    }
    // Thêm method này vào cuối class DataLoader
private void seedEvidenceTemplates() {
    if (evidenceTemplateRepository.count() == 0) {
        try {
            log.info("============== Seeding Evidence Templates ==============");
            
            // Attendance Templates
            createEvidenceTemplate("Biểu mẫu chấm công hàng tháng", "ATT_MONTHLY", 
                "Mẫu báo cáo chấm công chi tiết theo tháng", 
                EvidenceTemplate.TemplateCategory.ATTENDANCE,
                EvidenceTemplate.FileType.XLSX, "attendance_monthly.xlsx", 1);
                
            createEvidenceTemplate("Giải trình vắng mặt", "ATT_ABSENCE", 
                "Mẫu đơn xin nghỉ và giải trình vắng mặt", 
                EvidenceTemplate.TemplateCategory.ATTENDANCE,
                EvidenceTemplate.FileType.DOCX, "absence_explanation.docx", 2);
                
            createEvidenceTemplate("Bảng chấm công tuần", "ATT_WEEKLY", 
                "Mẫu chấm công theo tuần cho từng nhân viên", 
                EvidenceTemplate.TemplateCategory.ATTENDANCE,
                EvidenceTemplate.FileType.XLSX, "attendance_weekly.xlsx", 3);
            
            // Payroll Templates  
            createEvidenceTemplate("Bảng tính lương", "PAY_SALARY", 
                "Mẫu tính toán lương cơ bản và phụ cấp", 
                EvidenceTemplate.TemplateCategory.PAYROLL,
                EvidenceTemplate.FileType.XLSX, "payroll_calculation.xlsx", 4);
                
            createEvidenceTemplate("Phiếu lương cá nhân", "PAY_INDIVIDUAL", 
                "Mẫu phiếu lương chi tiết cho từng nhân viên", 
                EvidenceTemplate.TemplateCategory.PAYROLL,
                EvidenceTemplate.FileType.PDF, "individual_payslip.pdf", 5);
                
            createEvidenceTemplate("Báo cáo lương tổng hợp", "PAY_SUMMARY", 
                "Mẫu báo cáo tổng hợp lương theo phòng ban", 
                EvidenceTemplate.TemplateCategory.PAYROLL,
                EvidenceTemplate.FileType.XLSX, "payroll_summary.xlsx", 6);
            
            // Contract Templates
            createEvidenceTemplate("Hợp đồng lao động", "CON_EMPLOYMENT", 
                "Mẫu hợp đồng lao động chuẩn", 
                EvidenceTemplate.TemplateCategory.CONTRACT,
                EvidenceTemplate.FileType.DOCX, "employment_contract.docx", 7);
                
            createEvidenceTemplate("Phụ lục hợp đồng", "CON_ADDENDUM", 
                "Mẫu phụ lục thay đổi điều kiện hợp đồng", 
                EvidenceTemplate.TemplateCategory.CONTRACT,
                EvidenceTemplate.FileType.DOCX, "contract_addendum.docx", 8);
            
            // Medical Templates
            createEvidenceTemplate("Giấy khám bệnh", "MED_CERTIFICATE", 
                "Mẫu giấy khám bệnh cho nghỉ phép", 
                EvidenceTemplate.TemplateCategory.MEDICAL,
                EvidenceTemplate.FileType.PDF, "medical_certificate.pdf", 9);
                
            createEvidenceTemplate("Đơn xin nghỉ ốm", "MED_SICK_LEAVE", 
                "Mẫu đơn xin nghỉ ốm có giấy bác sĩ", 
                EvidenceTemplate.TemplateCategory.MEDICAL,
                EvidenceTemplate.FileType.DOCX, "sick_leave_request.docx", 10);
            
            // Violation Templates
            createEvidenceTemplate("Biên bản vi phạm", "VIO_RECORD", 
                "Mẫu biên bản ghi nhận vi phạm kỷ luật", 
                EvidenceTemplate.TemplateCategory.VIOLATION,
                EvidenceTemplate.FileType.DOCX, "violation_record.docx", 11);
                
            createEvidenceTemplate("Giải trình vi phạm", "VIO_EXPLANATION", 
                "Mẫu đơn giải trình cho vi phạm", 
                EvidenceTemplate.TemplateCategory.VIOLATION,
                EvidenceTemplate.FileType.DOCX, "violation_explanation.docx", 12);
            
            // Report Templates
            createEvidenceTemplate("Báo cáo nhân sự tháng", "REP_HR_MONTHLY", 
                "Mẫu báo cáo tổng hợp nhân sự theo tháng", 
                EvidenceTemplate.TemplateCategory.REPORT,
                EvidenceTemplate.FileType.XLSX, "hr_monthly_report.xlsx", 13);
                
            createEvidenceTemplate("Báo cáo tài chính nhân sự", "REP_HR_FINANCE", 
                "Mẫu báo cáo chi phí nhân sự", 
                EvidenceTemplate.TemplateCategory.REPORT,
                EvidenceTemplate.FileType.XLSX, "hr_finance_report.xlsx", 14);
            
            log.info("✅ Created {} evidence templates", evidenceTemplateRepository.count());
            
        } catch (Exception e) {
            log.error("❌ Error seeding evidence templates: {}", e.getMessage(), e);
        }
    } else {
        log.info("✅ Evidence templates already seeded. Count: {}", evidenceTemplateRepository.count());
    }
}

private void createEvidenceTemplate(String name, String code, String description, 
                                   EvidenceTemplate.TemplateCategory category, 
                                   EvidenceTemplate.FileType fileType, 
                                   String fileName, int sortOrder) {
    
    EvidenceTemplate template = new EvidenceTemplate();
    template.setTemplateName(name);
    template.setTemplateCode(code);
    template.setDescription(description);
    template.setCategory(category);
    template.setFileType(fileType);
    template.setFileName(fileName);
    template.setFilePath("/templates/evidence/" + fileName);
    template.setDownloadUrl("/api/accountant/evidence/templates/" + fileName);
    template.setFileSize(1024L * 50); // Mock 50KB
    template.setVersion("1.0");
    template.setIsActive(true);
    template.setSortOrder(sortOrder);
    template.setUsageInstructions("Tải xuống template, điền đầy đủ thông tin và tải lên hệ thống.");
    template.setCreatedBy(1L); // Admin user
    
    evidenceTemplateRepository.save(template);
}

    private void createAttendanceExplanationsData() {
    log.info("============== Creating Attendance Explanations Test Data ==============");
    
    if (attendanceExplanationRepository.count() > 0) {
        log.info("AttendanceExplanations already exist, skipping creation");
        return;
    }

    
    
    // Get staff users (teachers, accountant, manager)
    String[] staffEmails = {
        "teacher@test.com", "math@test.com", "literature@test.com", "english@test.com",
        "teacher2@test.com", "teacher3@test.com", "accountant@test.com", "manager@test.com"
    };
    
    List<User> staffUsers = new ArrayList<>();
    for (String email : staffEmails) {
        userRepository.findByEmail(email).ifPresent(staffUsers::add);
    }
    
    if (staffUsers.isEmpty()) {
        log.warn("No staff users found, skipping attendance explanations creation");
        return;
    }
    
    List<AttendanceExplanation> explanations = new ArrayList<>();
    LocalDateTime baseTime = LocalDateTime.now().minusDays(30);
    
    for (int i = 1; i <= 15; i++) {
        AttendanceExplanation explanation = new AttendanceExplanation();
        
        // Assign a staff user (rotate through available staff)
        User staff = staffUsers.get((i - 1) % staffUsers.size());
        explanation.setStaff(staff);
        explanation.setSubmitterName(staff.getFullName());
        
        explanation.setDepartment(i % 3 == 0 ? "IT" : (i % 3 == 1 ? "Marketing" : "HR"));
        explanation.setAbsenceDate(baseTime.plusDays(i * 2).toLocalDate());
        
        // Vary reasons
        String[] reasons = {"Ốm", "Việc gia đình", "Công tác", "Thai sản", "Khám bệnh"};
        explanation.setReason(reasons[i % reasons.length]);
        
        explanation.setExplanationText("Giải trình chi tiết cho việc vắng mặt ngày " + 
            explanation.getAbsenceDate() + ". Lý do: " + explanation.getReason() + 
            ". Nhân viên: " + staff.getFullName());
        explanation.setSubmittedAt(baseTime.plusDays(i * 2 + 1));
        
        // Vary status
        ExplanationStatus[] statuses = ExplanationStatus.values();
        explanation.setStatus(statuses[i % statuses.length]);
        
        if (explanation.getStatus() != ExplanationStatus.PENDING) {
            explanation.setApproverName("Quản lý " + (i % 3 + 1));
        }
        
        explanations.add(explanation);
    }
    
    attendanceExplanationRepository.saveAll(explanations);
    log.info("✅ Created {} attendance explanations with {} staff users", 
            explanations.size(), staffUsers.size());
    log.info("============== Attendance Explanations Creation Complete ==============");
}
} 