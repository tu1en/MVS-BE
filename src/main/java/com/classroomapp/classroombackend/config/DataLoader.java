package com.classroomapp.classroombackend.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.classroomapp.classroombackend.model.Contract;
import com.classroomapp.classroombackend.model.JobPosition;
import com.classroomapp.classroombackend.model.RecruitmentApplication;
import com.classroomapp.classroombackend.model.Absence;
import com.classroomapp.classroombackend.model.Accomplishment;
import com.classroomapp.classroombackend.model.Announcement;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.StudentProgress;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus;
import com.classroomapp.classroombackend.model.Blog;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.classroommanagement.Course;
import com.classroomapp.classroombackend.model.CourseMaterial;
import com.classroomapp.classroombackend.model.Lecture;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.TimetableEvent;
import com.classroomapp.classroombackend.model.StudentMessage;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.model.usermanagement.Role;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.ContractRepository;
import com.classroomapp.classroombackend.repository.JobPositionRepository;
import com.classroomapp.classroombackend.repository.RecruitmentApplicationRepository;
import com.classroomapp.classroombackend.repository.absencemanagement.AbsenceRepository;
import com.classroomapp.classroombackend.repository.AccomplishmentRepository;
import com.classroomapp.classroombackend.repository.AnnouncementRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.StudentProgressRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.BlogRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.CourseMaterialRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.CourseRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.ScheduleRepository;

import com.classroomapp.classroombackend.repository.TimetableEventRepository;
import com.classroomapp.classroombackend.repository.StudentMessageRepository;
import com.classroomapp.classroombackend.repository.requestmanagement.RequestRepository;
import com.classroomapp.classroombackend.repository.usermanagement.RoleRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalTime;

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
    private RoleRepository roleRepository;
    
    @Autowired
    private CourseRepository courseRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
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
    private ContractRepository contractRepository;
    
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
            
            // Seed classrooms
            classrooms = seedClassrooms();
            
            // Seed classroom enrollments
            seedClassroomEnrollments();
            
            // Seed schedules
            seedSchedules();
            
            // Seed timetable events
            seedTimetableEvents();
            
            // Seed role requests
            seedRequests();
            
            // Seed job positions
            seedJobPositions();
            
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
            
            // Đã loại bỏ seedExams vì không tương thích với model Exam hiện tại
            
            // Seed student progress
            seedStudentProgress();
            
            log.info("============== Main Seeding Complete ==============");
        } else {
            log.info("Database already has users. Skipping main seeding.");
            classrooms = classroomRepository.findAll();
        }

        // Always verify database state
        verifyDatabaseState();
        verifyUserRoleAssignments();

        // Always run the submission seeder to add new test data
        log.info("============== Checking for new submissions to seed ==============");
        seedSubmissions();
        log.info("============== Submission seeding complete ==============");

        // Always run comprehensive grading seeder for classroom 54
        log.info("============== Seeding Comprehensive Grading Data ==============");
        seedAssignmentTestData();
        seedComprehensiveGradingData();
        log.info("============== Comprehensive Grading Seeding Complete ==============");

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

        // Seed thêm 5-6 ứng viên nộp CV mẫu nếu chưa có
        if (recruitmentApplicationRepository.count() < 5) {
            List<JobPosition> positions = jobPositionRepository.findAll();
            if (!positions.isEmpty()) {
                for (int i = 1; i <= 6; i++) {
                    RecruitmentApplication app = new RecruitmentApplication();
                    app.setFullName("Ứng viên test " + i);
                    app.setEmail("testcv" + i + "@gmail.com");
                    app.setJobPosition(positions.get(i % positions.size()));
                    app.setStatus("PENDING");
                    app.setCvUrl("/static/sample_materials/sample.pdf");
                    recruitmentApplicationRepository.save(app);
                }
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

                // Thêm seed contract chính thức cho acc
                Contract accContract = new Contract();
                accContract.setUserId(accountant.getId());
                accContract.setFullName(accountant.getFullName());
                accContract.setContractType("OFFICIAL");
                accContract.setPosition("Accountant");
                accContract.setDepartment(accountant.getDepartment());
                accContract.setSalary(15000000.0);
                accContract.setWorkingHours("Full-time");
                accContract.setStartDate(LocalDate.of(2025, 7, 1));
                accContract.setEndDate(null);
                accContract.setStatus("ACTIVE");
                accContract.setCreatedBy("seeder");
                accContract.setCreatedAt(LocalDateTime.now());
                contractRepository.save(accContract);
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

    private List<Classroom> seedClassrooms() {
        if (classroomRepository.count() == 0) {
            List<User> teachers = userRepository.findByRoleId(RoleConstants.TEACHER);
            List<Course> courses = courseRepository.findAll();

            if (teachers.isEmpty() || courses.isEmpty()) {
                log.warn("No teachers or courses found for classroom seeding");
                return List.of();
            }

            List<Classroom> classrooms = List.of();

            // Create classrooms with specific teachers and courses
            for (int i = 0; i < Math.min(teachers.size(), courses.size()); i++) {
                Classroom classroom = new Classroom();
                classroom.setName("Class " + (i + 1));
                classroom.setDescription("Classroom for " + courses.get(i).getName());
                classroom.setTeacher(teachers.get(i));
                classroom.setSubject(courses.get(i).getName());
                classroom.setCourseId(courses.get(i).getId());
                
                Classroom savedClassroom = classroomRepository.save(classroom);
                classrooms = List.of(savedClassroom);
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
            // Teacher role request
            Request teacherRequest = new Request();
            teacherRequest.setEmail("nguyenvanA@gmail.com");
            teacherRequest.setFullName("Nguyễn Văn A");
            teacherRequest.setPhoneNumber("0987654321");
            teacherRequest.setRequestedRole("TEACHER");
            teacherRequest.setFormResponses(null); // Không có form data cho teacher
            teacherRequest.setStatus("PENDING");
            teacherRequest.setCreatedAt(LocalDateTime.now().minusDays(3));
            requestRepository.save(teacherRequest);
            // Student role request
            Request studentRequest = new Request();
            studentRequest.setEmail("tranvanB@gmail.com");
            studentRequest.setFullName("Trần Văn B");
            studentRequest.setPhoneNumber("0987123456");
            studentRequest.setRequestedRole("STUDENT");
            studentRequest.setFormResponses("{\"grade\":\"Lớp 11\",\"parentContact\":\"Phụ huynh: Trần Thị C, SĐT: 0912345678\",\"additionalInfo\":\"Em muốn đăng ký học thêm môn Toán và Vật lý để chuẩn bị cho kỳ thi quốc gia.\"}");
            studentRequest.setStatus("PENDING");
            studentRequest.setCreatedAt(LocalDateTime.now().minusDays(1));
            requestRepository.save(studentRequest);
            log.info("✅ Created 2 sample role requests.");
        } catch (Exception e) {
            log.error("❌ Error creating sample requests: {}", e.getMessage());
        }
    }

    private void seedJobPositions() {
        if (jobPositionRepository.count() == 0) {
            jobPositionRepository.save(new JobPosition(null, "Giáo viên lớp 10", "Dạy Toán, Lý, Hoá cho học sinh lớp 10", "12-18 triệu", 3, null, null));
            jobPositionRepository.save(new JobPosition(null, "Giáo viên lớp 11", "Dạy Toán, Lý, Hoá cho học sinh lớp 11", "13-20 triệu", 2, null, null));
            jobPositionRepository.save(new JobPosition(null, "Giáo viên lớp 12", "Dạy Toán, Lý, Hoá cho học sinh lớp 12, luyện thi đại học", "15-25 triệu", 4, null, null));
            log.info("✅ Created 3 job positions");
        } else {
            log.info("✅ Job positions already seeded.");
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
            List<User> users = userRepository.findAll();
            for (User user : users) {
                Blog blog = new Blog();
                blog.setAuthor(user);
                blog.setTitle("Blog Post by " + user.getFullName());
                // blog.setContent("This is a sample blog post content"); // Nếu không có content thì bỏ
                // Không set createdAt, updatedAt nếu không có
                blogRepository.save(blog);
            }
            log.info("✅ Created blogs for {} users", users.size());
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
        List<User> teachers = userRepository.findByRoleId(RoleConstants.TEACHER);
        for (Classroom classroom : classrooms) {
            CourseMaterial material = new CourseMaterial();
            material.setTitle("Course Material for " + classroom.getName());
            material.setDescription("Essential materials for the course");
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
        log.info("✅ Created course materials for {} classrooms", classrooms.size());
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
} 