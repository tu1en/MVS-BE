package com.classroomapp.classroombackend.config;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.classroomapp.classroombackend.model.usermanagement.Role;

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
import com.classroomapp.classroombackend.model.Parent;
import com.classroomapp.classroombackend.model.ParentLeaveNotice;
import com.classroomapp.classroombackend.model.ParentMessage;
import com.classroomapp.classroombackend.model.ParentMessage.SenderType;
import com.classroomapp.classroombackend.model.RecruitmentApplication;
import com.classroomapp.classroombackend.model.RecruitmentPlan;
import com.classroomapp.classroombackend.model.Request;
import com.classroomapp.classroombackend.model.Schedule;
import com.classroomapp.classroombackend.model.StudentMessage;
import com.classroomapp.classroombackend.model.StudentParent;
import com.classroomapp.classroombackend.model.StudentProgress;
import com.classroomapp.classroombackend.model.TeacherEvaluation;
import com.classroomapp.classroombackend.model.TimetableEvent;
import com.classroomapp.classroombackend.model.assignmentmanagement.Assignment;
import com.classroomapp.classroombackend.model.assignmentmanagement.Submission;
import com.classroomapp.classroombackend.model.attendancemanagement.Attendance;
import com.classroomapp.classroombackend.model.attendancemanagement.AttendanceSession;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.classroommanagement.Course;
// Thêm import này vào đầu DataLoader.java
import com.classroomapp.classroombackend.model.hrmanagement.EvidenceTemplate;
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
import com.classroomapp.classroombackend.repository.parentmanagement.ParentLeaveNoticeRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.ParentMessageRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.ParentRepository;
import com.classroomapp.classroombackend.repository.parentmanagement.StudentParentRepository;
import com.classroomapp.classroombackend.repository.requestmanagement.RequestRepository;
import com.classroomapp.classroombackend.repository.usermanagement.RoleRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.service.ContractService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Component
    @Order(1) // Run first since we removed DatabaseCleanupService
    @DependsOn("entityManagerFactory") // Wait for JPA to be initialized
public class DataLoader implements CommandLineRunner {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DataLoader.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ContractService contractService;
    
    @Autowired
    private com.classroomapp.classroombackend.repository.TeacherEvaluationRepository teacherEvaluationRepository;
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
    
    @Autowired
    private ParentRepository parentRepository;
    
    @Autowired
    private StudentParentRepository studentParentRepository;
    
    @Autowired
    private ParentMessageRepository parentMessageRepository;
    
    @Autowired
    private ParentLeaveNoticeRepository parentLeaveNoticeRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Clean up duplicate submissions first
        cleanupDuplicateSubmissions();
        
        // Clean up duplicate schedules and timetable events
        cleanupDuplicateSchedules();
        cleanupDuplicateTimetableEvents();
        
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
            log.info("============== Seeding Schedules ==============");
            seedSchedules();
            log.info("============== Schedule Seeding Complete ==============");
            
            // Seed timetable events
            log.info("============== Seeding Timetable Events ==============");
            seedTimetableEvents();
            log.info("============== Timetable Events Seeding Complete ==============");
            
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
            
            // Seed teacher evaluations
            seedTeacherEvaluations();
            
            // Seed student progress
            seedStudentProgress();
            
            log.info("============== Main Seeding Complete ==============");
        } else {
            log.info("Database already has users. Skipping main seeding.");
            classrooms = classroomRepository.findAll();
            
            // Check if schedules and timetable events need to be created
            if (scheduleRepository.count() == 0) {
                log.info("============== Creating Missing Schedules ==============");
                seedSchedules();
                log.info("============== Schedule Creation Complete ==============");
            }
            
            if (timetableEventRepository.count() == 0) {
                log.info("============== Creating Missing Timetable Events ==============");
                seedTimetableEvents();
                log.info("============== Timetable Events Creation Complete ==============");
            }
        }

        // Ensure there are enough teachers for demo even when DB already has data
        ensureMinimumTeachers(24);
        // Make sure any teacher without status is activated for demo visibility
        try {
            List<User> teachers = userRepository.findByRoleId(RoleConstants.TEACHER);
            int activated = 0;
            for (User t : teachers) {
                if (t.getStatus() == null || t.getStatus().isBlank()) {
                    t.setStatus("active");
                    userRepository.save(t);
                    activated++;
                }
            }
            if (activated > 0) {
                log.info("✅ Activated {} teacher accounts missing status", activated);
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not normalize teacher statuses: {}", e.getMessage());
        }
        // Create contracts for active teachers without contracts (300k - 600k VND/hour)
        try {
            contractService.createContractsForActiveTeachers(300000L, 600000L, false);
            log.info("✅ Seeded contracts for active teachers without existing contracts");
        } catch (Exception e) {
            log.error("❌ Error seeding teacher contracts: {}", e.getMessage(), e);
        }
        // Post-seed normalization sweep to fix legacy data inconsistencies
        try {
            normalizeUserPhones();
            normalizeContractsData();
            log.info("✅ Post-seed normalization complete: user phones and contract salaries standardized");
        } catch (Exception e) {
            log.error("❌ Error during post-seed normalization: {}", e.getMessage(), e);
        }
        // Ensure sample requests and blogs are seeded if missing
        // These methods are idempotent and will skip if data already exists
        seedRequests();
        seedBlogs();

        // Always verify database state
        verifyDatabaseState();
        verifyUserRoleAssignments();
        ensureParentRoleAndUser();
        
        // Seed parent test data if needed
        log.info("============== Checking Parent Test Data =============");
        seedParentTestData();
        log.info("============== Parent Test Data Complete ============");
        
        // Seed parent-teacher messages
        log.info("============== Seeding Parent-Teacher Messages =============");
        seedParentMessages();
        log.info("============== Parent-Teacher Messages Complete ============");
        
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
                log.error("Lỗi khi seed dữ liệu vắng mặt: {}", e.getMessage(), e);
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

        // Đảm bảo dữ liệu tuyển dụng luôn sẵn sàng ngay cả khi user đã tồn tại từ trước
        // 1) Kế hoạch tuyển dụng
        seedRecruitmentPlans();
        // 2) Vị trí tuyển dụng (phụ thuộc kế hoạch)
        seedJobPositions();
        // 3) Đơn ứng tuyển (phụ thuộc vị trí)
        seedRecruitmentApplications();

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
                    // Đặt createdAt nằm trong khoảng thời gian của kế hoạch của vị trí tương ứng
                    try {
                        JobPosition job = positions.get(i % positions.size());
                        LocalDate planStart = job.getRecruitmentPlan().getStartDate();
                        LocalDate planEnd = job.getRecruitmentPlan().getEndDate();
                        LocalDate today = LocalDate.now();
                        LocalDate effectiveEnd = planEnd.isAfter(today) ? today : planEnd;
                        if (effectiveEnd.isBefore(planStart)) {
                            effectiveEnd = planStart;
                        }
                        long days = java.time.temporal.ChronoUnit.DAYS.between(planStart, effectiveEnd);
                        long safeDays = Math.max(0, days);
                        long offset = (safeDays == 0) ? 0 : (i % (safeDays + 1));
                        LocalDate chosen = planStart.plusDays(offset);
                        if (chosen.isAfter(effectiveEnd)) chosen = effectiveEnd;
                        app.setCreatedAt(chosen.atTime(9 + (i % 8), 0));
                    } catch (Exception ex) {
                        app.setCreatedAt(LocalDateTime.now().minusDays(i + 1));
                    }
                    recruitmentApplicationRepository.save(app);
                    log.info("✅ Created test application for {} applying to: {}", testApplicants[i], positions.get(i % positions.size()).getTitle());
                }
                log.info("✅ Created {} additional test recruitment applications.", testApplicants.length);
            }
        }
    }

    /**
     * Normalize all user phone numbers to Vietnamese 10-digit format if possible.
     * - Converts +84 / 84 prefixes to 0
     * - Keeps only digits
     * - Validates prefixes: 03x, 05x, 07x, 08x, 09x
     */
    private void normalizeUserPhones() {
        try {
            List<User> users = userRepository.findAll();
            int updated = 0;
            for (User u : users) {
                boolean changed = false;
                if (u.getPhoneNumber() != null && !u.getPhoneNumber().isBlank()) {
                    String normalized = normalizeVietnamPhone(u.getPhoneNumber());
                    if (normalized != null && !normalized.equals(u.getPhoneNumber())) {
                        u.setPhoneNumber(normalized);
                        changed = true;
                    }
                }
                if (u.getParentPhone() != null && !u.getParentPhone().isBlank()) {
                    String normalized = normalizeVietnamPhone(u.getParentPhone());
                    if (normalized != null && !normalized.equals(u.getParentPhone())) {
                        u.setParentPhone(normalized);
                        changed = true;
                    }
                }
                if (changed) {
                    userRepository.save(u);
                    updated++;
                }
            }
            if (updated > 0) {
                log.info("📞 Normalized phone numbers for {} users", updated);
            } else {
                log.info("📞 No user phone numbers required normalization");
            }
        } catch (Exception e) {
            log.warn("⚠️ Failed to normalize user phones: {}", e.getMessage());
        }
    }

    /**
     * Normalize existing contracts:
     * - Normalize phone numbers
     * - Round hourlySalary to nearest 10,000 VND
     * - If no hourlySalary but legacy salary present, round legacy salary to nearest 10,000 VND
     */
    private void normalizeContractsData() {
        try {
            List<Contract> contracts = contractRepository.findAll();
            int updated = 0;
            for (Contract c : contracts) {
                boolean changed = false;

                // Normalize phone number
                if (c.getPhoneNumber() != null && !c.getPhoneNumber().isBlank()) {
                    String normalized = normalizeVietnamPhone(c.getPhoneNumber());
                    if (normalized != null && !normalized.equals(c.getPhoneNumber())) {
                        c.setPhoneNumber(normalized);
                        changed = true;
                    }
                }

                // Round hourly salary if present
                if (c.getHourlySalary() != null && c.getHourlySalary() > 0) {
                    long rounded = roundToNearest(c.getHourlySalary(), 10_000L);
                    if (!Long.valueOf(rounded).equals(c.getHourlySalary())) {
                        c.setHourlySalary(rounded);
                        changed = true;
                    }
                } else if (c.getSalary() != null && c.getSalary() > 0) {
                    // Fallback: round legacy salary field
                    Double rounded = roundDoubleToNearest(c.getSalary(), 10_000L);
                    if (!rounded.equals(c.getSalary())) {
                        c.setSalary(rounded);
                        changed = true;
                    }
                }

                if (changed) {
                    contractRepository.save(c);
                    updated++;
                }
            }
            if (updated > 0) {
                log.info("📑 Normalized {} contracts (phones and salaries)", updated);
            } else {
                log.info("📑 No contracts required normalization");
            }
        } catch (Exception e) {
            log.warn("⚠️ Failed to normalize contracts: {}", e.getMessage());
        }
    }

    // Helpers
    private String normalizeVietnamPhone(String input) {
        try {
            String digits = input.replaceAll("[^0-9]", "");
            if (digits.startsWith("84")) {
                digits = "0" + digits.substring(2);
            }
            if (digits.length() == 10 && digits.charAt(0) == '0') {
                char second = digits.charAt(1);
                if (second == '3' || second == '5' || second == '7' || second == '8' || second == '9') {
                    return digits;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private long roundToNearest(long value, long unit) {
        if (unit <= 0) return value;
        long half = unit / 2;
        return ((value + half) / unit) * unit;
    }

    private Double roundDoubleToNearest(Double value, long unit) {
        if (value == null) return null;
        long rounded = Math.round(value / unit) * unit;
        return (double) rounded;
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
            log.error("❌ Lỗi trong quá trình dọn dẹp bản ghi trùng lặp: {}", e.getMessage(), e);
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

                Role parent = new Role("PARENT");
                parent.setId(6);
                roleRepository.save(parent);

                log.info("✅ Created roles with explicit IDs (including ACCOUNTANT, PARENT).");

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
                student.setParentPhone("+84901234567");
                student.setParentName("Phụ huynh Student");
                student.setStatus("active");
                userRepository.save(student);
                log.info("✅ Created student user with ID: " + student.getId());

                // Create main teacher user - Fixed to ensure proper creation
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
                teacher.setStatus("active");
                User savedTeacher = userRepository.save(teacher);
                log.info("✅ Created teacher user with ID: {} and email: {}", savedTeacher.getId(), savedTeacher.getEmail());
                
                // Verify teacher was created
                if (userRepository.findByEmail("teacher@test.com").isPresent()) {
                    log.info("✅ Verified teacher@test.com exists in database");
                } else {
                    log.error("❌ Failed to verify teacher@test.com in database");
                }

                // Create manager user
                User manager = new User();
                manager.setId(301L);
                manager.setUsername("manager");
                manager.setPassword(passwordEncoder.encode("manager123"));
                manager.setEmail("manager@test.com");
                manager.setFullName("Manager User");
                manager.setRoleId(RoleConstants.MANAGER);
                manager.setStatus("active");
                userRepository.save(manager);

                // Create admin user
                User admin = new User();
                admin.setId(401L);
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@test.com");
                admin.setFullName("Administrator");
                admin.setRoleId(RoleConstants.ADMIN);
                admin.setStatus("active");
                userRepository.save(admin);

                // Create parent user
                User parent = new User();
                parent.setId(601L);
                parent.setUsername("parent");
                parent.setPassword(passwordEncoder.encode("parent123"));
                parent.setEmail("parent@test.com");
                parent.setFullName("Parent User");
                parent.setRoleId(RoleConstants.PARENT);
                parent.setStatus("active");
                userRepository.save(parent);

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
                mathTeacher.setStatus("active");
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
                litTeacher.setStatus("active");
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
                engTeacher.setStatus("active");
                userRepository.save(engTeacher);

                // Create additional test students
                User student1 = new User();
                student1.setId(102L);
                student1.setUsername("student1");
                student1.setPassword(passwordEncoder.encode("student123"));
                student1.setEmail("student1@test.com");
                student1.setFullName("Phạm Văn Nam");
                student1.setRoleId(RoleConstants.STUDENT);
                student1.setParentPhone("+84987654321");
                student1.setParentName("Phạm Thị Hoa");
                student1.setStatus("active");
                userRepository.save(student1);

                User student2 = new User();
                student2.setId(103L);
                student2.setUsername("student2");
                student2.setPassword(passwordEncoder.encode("student123"));
                student2.setEmail("student2@test.com");
                student2.setFullName("Alice Johnson");
                student2.setRoleId(RoleConstants.STUDENT);
                student2.setParentPhone("+84976543210");
                student2.setParentName("Mrs. Johnson");
                student2.setStatus("active");
                userRepository.save(student2);

                User student3 = new User();
                student3.setId(104L);
                student3.setUsername("student3");
                student3.setPassword(passwordEncoder.encode("student123"));
                student3.setEmail("student3@test.com");
                student3.setFullName("Bob Wilson");
                student3.setRoleId(RoleConstants.STUDENT);
                student3.setParentPhone("+84965432109");
                student3.setParentName("Mr. Wilson");
                student3.setStatus("active");
                userRepository.save(student3);

                User student4 = new User();
                student4.setId(105L);
                student4.setUsername("student4");
                student4.setPassword(passwordEncoder.encode("student123"));
                student4.setEmail("student4@test.com");
                student4.setFullName("Carol Davis");
                student4.setRoleId(RoleConstants.STUDENT);
                student4.setParentPhone("+84954321098");
                student4.setParentName("Mrs. Davis");
                student4.setStatus("active");
                userRepository.save(student4);

                User student5 = new User();
                student5.setId(106L);
                student5.setUsername("student5");
                student5.setPassword(passwordEncoder.encode("student123"));
                student5.setEmail("student5@test.com");
                student5.setFullName("David Chen");
                student5.setRoleId(RoleConstants.STUDENT);
                student5.setParentPhone("+84943210987");
                student5.setParentName("Mr. Chen");
                student5.setStatus("active");
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
                extraTeacher1.setStatus("active");
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
                extraTeacher2.setStatus("active");
                userRepository.save(extraTeacher2);

                // ===== Thêm bộ giáo viên theo chuẩn cấp 3 (Toán, Lý, Hóa, Văn, Anh, Sinh) =====
                String[][] teacherSeeds = new String[][]{
                    // username, email, fullName, department, phone, birthDate, citizenId, address
                    {"toan_gv1","toan1@school.vn","Nguyễn Đức Toàn","Toán","0901001001","1985-03-15","037185123456","Thanh Hóa"},
                    {"toan_gv2","toan2@school.vn","Phạm Hải Long","Toán","0901001002","1982-07-22","037182234567","Thanh Hóa"},
                    {"toan_gv3","toan3@school.vn","Lê Minh Quân","Toán","0901001003","1988-11-08","037188345678","Thanh Hóa"},
                    {"ly_gv1","ly1@school.vn","Trần Quốc Huy","Vật lý","0912002001","1984-05-12","037184456789","Thanh Hóa"},
                    {"ly_gv2","ly2@school.vn","Đỗ Thanh Tùng","Vật lý","0912002002","1987-09-30","037187567890","Thanh Hóa"},
                    {"ly_gv3","ly3@school.vn","Ngô Nhật Nam","Vật lý","0912002003","1983-12-18","037183678901","Thanh Hóa"},
                    {"hoa_gv1","hoa1@school.vn","Vũ Hồng Phúc","Hóa học","0903003001","1986-02-25","037186789012","Thanh Hóa"},
                    {"hoa_gv2","hoa2@school.vn","Bùi Thanh Hà","Hóa học","0903003002","1989-06-14","037289890123","Thanh Hóa"},
                    {"hoa_gv3","hoa3@school.vn","Phan Anh Dũng","Hóa học","0903003003","1981-10-07","037181901234","Thanh Hóa"},
                    {"van_gv1","van1@school.vn","Phạm Thu Hà","Ngữ văn","0934004001","1990-04-20","037290012345","Thanh Hóa"},
                    {"van_gv2","van2@school.vn","Nguyễn Thị Hồng","Ngữ văn","0934004002","1985-08-16","037285123456","Thanh Hóa"},
                    {"van_gv3","van3@school.vn","Hoàng Thị Trang","Ngữ văn","0934004003","1992-01-11","037292234567","Thanh Hóa"},
                    {"anh_gv1","anh1@school.vn","Lê Hồng Sơn","Tiếng Anh","0975005001","1987-03-28","037187345678","Thanh Hóa"},
                    {"anh_gv2","anh2@school.vn","Tạ Bích Ngọc","Tiếng Anh","0975005002","1991-07-05","037291456789","Thanh Hóa"},
                    {"anh_gv3","anh3@school.vn","Phạm Khánh Linh","Tiếng Anh","0975005003","1988-11-23","037288567890","Thanh Hóa"},
                    {"sinh_gv1","sinh1@school.vn","Đặng Quỳnh Chi","Sinh học","0986006001","1986-09-12","037286678901","Thanh Hóa"},
                    {"sinh_gv2","sinh2@school.vn","Trịnh Văn Thái","Sinh học","0986006002","1984-12-03","037184789012","Thanh Hóa"},
                    {"sinh_gv3","sinh3@school.vn","Nguyễn Tú Anh","Sinh học","0986006003","1993-05-17","037293890123","Thanh Hóa"}
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
                    u.setPhoneNumber(t[4]);
                    // Parse and set new fields: birthDate, citizenId, address
                    u.setBirthDate(LocalDate.parse(t[5])); // birthDate from seed data
                    u.setCitizenId(t[6]); // citizenId from seed data
                    u.setAddress(t[7]); // address from seed data
                    u.setStatus("active");
                    u.setHireDate(LocalDate.now().minusMonths((int)(Math.random() * 24) + 1));
                    u.setAnnualLeaveBalance(12);
                    u.setLeaveResetDate(LocalDate.now().plusMonths(6));
                    userRepository.save(u);
                    log.info("✅ Created teacher: {} ({}) - Born: {}, CCCD: {}, Address: {}", 
                            t[2], t[3], t[5], t[6], t[7]);
                }

                // Create accountant user
                User accountant = new User();
                accountant.setId(501L);
                accountant.setUsername("acc");
                // ... (rest of the code remains the same)
                accountant.setPassword(passwordEncoder.encode("acc123"));
                accountant.setEmail("accountant@test.com");
                accountant.setFullName("Nguyễn Thị Kế Toán");
                accountant.setRoleId(RoleConstants.ACCOUNTANT);
                accountant.setPhoneNumber("0901122334");
                accountant.setDepartment("Kế toán viên");
                accountant.setHireDate(LocalDate.of(2025, 7, 1));
                accountant.setStatus("active");
                userRepository.save(accountant);

                log.info("✅ Created accountant user with ID: " + accountant.getId());

                // Create Teaching Assistant users
                User teachingAssistant1 = new User();
                teachingAssistant1.setId(8L);
                teachingAssistant1.setUsername("ta1");
                teachingAssistant1.setPassword(passwordEncoder.encode("ta123"));
                teachingAssistant1.setEmail("ta1@test.com");
                teachingAssistant1.setFullName("Nguyễn Thị Hỗ Trợ");
                teachingAssistant1.setRoleId(RoleConstants.TEACHING_ASSISTANT);
                teachingAssistant1.setPhoneNumber("0912345689");
                teachingAssistant1.setDepartment("Trợ giảng Toán");
                teachingAssistant1.setHireDate(LocalDate.now().minusMonths(6));
                teachingAssistant1.setStatus("active");
                userRepository.save(teachingAssistant1);

                User teachingAssistant2 = new User();
                teachingAssistant2.setId(9L);
                teachingAssistant2.setUsername("ta2");
                teachingAssistant2.setPassword(passwordEncoder.encode("ta123"));
                teachingAssistant2.setEmail("ta2@test.com");
                teachingAssistant2.setFullName("Trần Văn Hỗ Trợ");
                teachingAssistant2.setRoleId(RoleConstants.TEACHING_ASSISTANT);
                teachingAssistant2.setPhoneNumber("0987654322");
                teachingAssistant2.setDepartment("Trợ giảng Văn");
                teachingAssistant2.setHireDate(LocalDate.now().minusMonths(3));
                teachingAssistant2.setStatus("active");
                userRepository.save(teachingAssistant2);

                log.info("✅ Created Teaching Assistant users");

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

    /**
     * Ensure PARENT role and a default parent user exist when DB is not empty.
     */
    private void ensureParentRoleAndUser() {
        try {
            // Ensure role exists
            Role parentRole = roleRepository.findByName("PARENT").orElse(null);
            if (parentRole == null) {
                parentRole = new Role("PARENT");
                parentRole = roleRepository.save(parentRole);
                log.info("✅ Ensured PARENT role exists with ID: {}.", parentRole.getId());
            } else {
                log.info("✅ PARENT role already exists with ID: {}.", parentRole.getId());
            }

            // Update RoleConstants.PARENT if it doesn't match the actual database ID
            if (parentRole.getId() != RoleConstants.PARENT) {
                log.warn("⚠️ Database PARENT role ID ({}) doesn't match RoleConstants.PARENT ({}). " +
                        "Consider updating RoleConstants.PARENT to match the database.", 
                        parentRole.getId(), RoleConstants.PARENT);
            }

            // Ensure default parent user exists
            User parentUser = null;
            if (!userRepository.existsByUsername("parent")) {
                parentUser = new User();
                parentUser.setUsername("parent");
                parentUser.setPassword(passwordEncoder.encode("parent123"));
                parentUser.setEmail("parent@test.com");
                parentUser.setFullName("Parent User");
                parentUser.setRoleId(parentRole.getId()); // Use the actual role ID from database
                parentUser.setParentPhone("0901234567");
                parentUser.setParentName("Parent User");
                parentUser = userRepository.save(parentUser);
                log.info("✅ Ensured default parent user exists.");
            } else {
                parentUser = userRepository.findByUsername("parent").orElse(null);
            }

            if (parentUser != null) {
                // Create Parent entity if not exists
                if (!parentRepository.existsByUserId(parentUser.getId())) {
                    Parent parent = new Parent();
                    parent.setUserId(parentUser.getId());
                    parent.setName("Parent User");
                    parent.setEmail("parent@test.com");
                    parent.setPhone("0901234567");
                    parent = parentRepository.save(parent);
                    log.info("✅ Created Parent entity for default parent user.");

                    // Create 2 dedicated student users for the default parent
                    User child1 = new User();
                    child1.setUsername("child1_of_parent");
                    child1.setPassword(passwordEncoder.encode("student123"));
                    child1.setEmail("child1@test.com");
                    child1.setFullName("Nguyễn Văn An");
                    child1.setRoleId(RoleConstants.STUDENT);
                    child1.setParentPhone("0901234567");
                    child1.setParentName("Parent User");
                    User savedChild1 = userRepository.save(child1);

                    User child2 = new User();
                    child2.setUsername("child2_of_parent");
                    child2.setPassword(passwordEncoder.encode("student123"));
                    child2.setEmail("child2@test.com");
                    child2.setFullName("Nguyễn Thị Bình");
                    child2.setRoleId(RoleConstants.STUDENT);
                    child2.setParentPhone("0901234567");
                    child2.setParentName("Parent User");
                    User savedChild2 = userRepository.save(child2);

                    log.info("✅ Created 2 student users for default parent.");

                    // Create StudentParent relationships
                    StudentParent relationship1 = new StudentParent();
                    relationship1.setStudentId(savedChild1.getId());
                    relationship1.setParentId(parent.getId());
                    relationship1.setRelationType(StudentParent.RelationType.FATHER);
                    relationship1.setIsPrimary(true);
                    relationship1.setLegalGuardian(true);
                    relationship1.setStartAt(LocalDate.now());
                    studentParentRepository.save(relationship1);

                    StudentParent relationship2 = new StudentParent();
                    relationship2.setStudentId(savedChild2.getId());
                    relationship2.setParentId(parent.getId());
                    relationship2.setRelationType(StudentParent.RelationType.FATHER);
                    relationship2.setIsPrimary(true);
                    relationship2.setLegalGuardian(true);
                    relationship2.setStartAt(LocalDate.now());
                    studentParentRepository.save(relationship2);

                    log.info("✅ Created StudentParent relationships for default parent and 2 children.");
                } else {
                    log.info("✅ Parent entity and children already exist for default parent user.");
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not ensure PARENT role/user: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedCourses() {
        if (courseRepository.count() == 0) {
            Course math = new Course();
            math.setName("Toán học nâng cao");
            math.setDescription("Nghiên cứu toàn diện về các khái niệm toán học và ứng dụng của chúng.");
            courseRepository.save(math);

            Course history = new Course();
            history.setName("Lịch sử thế giới");
            history.setDescription("Khảo sát các sự kiện lịch sử quan trọng từ các nền văn minh cổ đại đến thời hiện đại.");
            courseRepository.save(history);

            Course literature = new Course();
            literature.setName("Ngữ văn Việt Nam");
            literature.setDescription("Khám phá các tác phẩm văn học Việt Nam xuyên suốt lịch sử.");
            courseRepository.save(literature);

            Course english = new Course();
            english.setName("Tiếng Anh giao tiếp");
            english.setDescription("Phát triển kỹ năng giao tiếp tiếng Anh cho môi trường quốc tế.");
            courseRepository.save(english);

            Course cs = new Course();
            cs.setName("Khoa học máy tính");
            cs.setDescription("Các khái niệm cơ bản về khoa học máy tính và lập trình.");
            courseRepository.save(cs);

            Course physics = new Course();
            physics.setName("Vật lý đại cương");
            physics.setDescription("Giới thiệu về các nguyên lý cơ bản của vật lý.");
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
                classroom.setName("Lớp " + (i + 1));
                classroom.setDescription("Lớp cho môn học: " + courses.get(i).getName());
                
                // Ensure teacher@test.com is assigned to classroom 1
                if (i == 0) {
                    User mainTeacher = userRepository.findByEmail("teacher@test.com").orElse(teachers.get(0));
                    classroom.setTeacher(mainTeacher);
                    log.info("✅ Assigned teacher@test.com to classroom 1");
                } else {
                    classroom.setTeacher(teachers.get(i));
                }
                
                classroom.setSubject(courses.get(i).getName());
                classroom.setSection("" + (char)('A' + i)); // Add section: A, B, C, etc.
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
            List<User> teachers = userRepository.findByRoleId(RoleConstants.TEACHER);
            List<Room> rooms = roomRepository.findAll();
            
            if (classrooms.isEmpty() || teachers.isEmpty() || rooms.isEmpty()) {
                log.warn("❌ Cannot seed schedules: classrooms={}, teachers={}, rooms={}", 
                    classrooms.size(), teachers.size(), rooms.size());
                return;
            }
            
            log.info("🔄 Creating comprehensive schedules for {} classrooms", classrooms.size());
            
            // Create detailed schedules for each classroom
            for (int i = 0; i < classrooms.size(); i++) {
                Classroom classroom = classrooms.get(i);
                User teacher = teachers.get(i % teachers.size());
                Room room = rooms.get(i % rooms.size());
                
                // Create weekly schedule for each classroom
                createWeeklySchedule(classroom, teacher, room, i);
            }
            
            log.info("✅ Created comprehensive schedules for {} classrooms", classrooms.size());
        } else {
            log.info("✅ Schedules already seeded.");
        }
    }
    
    private void createWeeklySchedule(Classroom classroom, User teacher, Room room, int classroomIndex) {
        // Monday - Friday schedule
        String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6"};
        int[] dayOfWeek = {0, 1, 2, 3, 4}; // Monday = 0, Tuesday = 1, etc.
        
        for (int day = 0; day < days.length; day++) {
            // Morning session: 8:00 - 9:30
            createScheduleForDay(classroom, teacher, dayOfWeek[day], 
                LocalTime.of(8, 0), LocalTime.of(9, 30), room.getRoomCode(), "Ca sáng");
            
            // Afternoon session: 14:00 - 15:30  
            createScheduleForDay(classroom, teacher, dayOfWeek[day], 
                LocalTime.of(14, 0), LocalTime.of(15, 30), room.getRoomCode(), "Ca chiều");
            
            // Evening session for some classrooms (alternate days)
            if (classroomIndex % 2 == 0 && day % 2 == 0) {
                createScheduleForDay(classroom, teacher, dayOfWeek[day], 
                    LocalTime.of(18, 0), LocalTime.of(19, 30), room.getRoomCode(), "Ca tối");
            }
        }
        
        log.info("✅ Created weekly schedule for {} with teacher {} in room {}", 
            classroom.getName(), teacher.getFullName(), room.getRoomCode());
    }
    
    private void createScheduleForDay(Classroom classroom, User teacher, int dayOfWeek, LocalTime startTime, LocalTime endTime, String roomCode, String sessionType) {
        Schedule schedule = new Schedule();
        schedule.setClassroom(classroom);
        schedule.setTeacher(teacher);
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(startTime);
        schedule.setEndTime(endTime);
        schedule.setRoom(roomCode);
        schedule.setSubject(classroom.getSubject());
        
        // Add materials and meeting URLs based on subject
        String subjectKey = classroom.getSubject().toLowerCase().replaceAll(" ", "-");
        schedule.setMaterialsUrl("https://drive.google.com/folder/" + subjectKey + "-materials");
        schedule.setMeetUrl("https://meet.google.com/" + subjectKey + "-" + dayOfWeek + "-" + sessionType.toLowerCase().replaceAll(" ", ""));
        
        // Note: Schedule model doesn't have description field
        // schedule.setDescription(sessionType + " - " + classroom.getSubject() + " - " + classroom.getName());
        
        scheduleRepository.save(schedule);
    }

    private void seedTimetableEvents() {
        if (timetableEventRepository.count() == 0) {
            List<Schedule> schedules = scheduleRepository.findAll();
            
            if (schedules.isEmpty()) {
                log.warn("❌ Cannot seed timetable events: no schedules found");
                return;
            }
            
            log.info("🔄 Creating timetable events based on {} schedules", schedules.size());
            
            // Generate events based on actual schedule data
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfWeek = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            
            int eventCount = 0;
            
            // Create events for the next 8 weeks (instead of 4) to cover all needs
            for (int week = 0; week < 8; week++) {
                for (Schedule schedule : schedules) {
                    LocalDateTime eventStart = startOfWeek
                        .plusWeeks(week)
                        .plusDays(schedule.getDayOfWeek())
                        .withHour(schedule.getStartTime().getHour())
                        .withMinute(schedule.getStartTime().getMinute())
                        .withSecond(0);
                    
                    LocalDateTime eventEnd = startOfWeek
                        .plusWeeks(week)
                        .plusDays(schedule.getDayOfWeek())
                        .withHour(schedule.getEndTime().getHour())
                        .withMinute(schedule.getEndTime().getMinute())
                        .withSecond(0);
                    
                    // Skip past events
                    if (eventStart.isBefore(now)) {
                        continue;
                    }
                    
                    TimetableEvent classEvent = new TimetableEvent();
                    classEvent.setTitle(schedule.getSubject());
                    classEvent.setDescription("Lớp học " + schedule.getSubject() + " - " + schedule.getClassroom().getName());
                    classEvent.setStartDatetime(eventStart);
                    classEvent.setEndDatetime(eventEnd);
                    classEvent.setEventType(TimetableEvent.EventType.CLASS);
                    classEvent.setClassroomId(schedule.getClassroom().getId());
                    classEvent.setCreatedBy(schedule.getTeacher().getId());
                    classEvent.setLocation(schedule.getRoom());
                    classEvent.setColor("#1890ff");
                    classEvent.setCreatedAt(now);
                    classEvent.setUpdatedAt(now);
                    
                    timetableEventRepository.save(classEvent);
                    eventCount++;
                    
                    // Add exam events every 3 weeks
                    if (week % 3 == 2) {
                        LocalDateTime examStart = eventStart.plusDays(2).withHour(14);
                        LocalDateTime examEnd = examStart.plusHours(1);
                        
                        TimetableEvent examEvent = new TimetableEvent();
                        examEvent.setTitle("Kiểm tra " + schedule.getSubject());
                        examEvent.setDescription("Bài kiểm tra môn " + schedule.getSubject());
                        examEvent.setStartDatetime(examStart);
                        examEvent.setEndDatetime(examEnd);
                        examEvent.setEventType(TimetableEvent.EventType.EXAM);
                        examEvent.setClassroomId(schedule.getClassroom().getId());
                        examEvent.setCreatedBy(schedule.getTeacher().getId());
                        examEvent.setLocation("Phòng thi " + schedule.getRoom().replaceAll("Room", "Exam"));
                        examEvent.setColor("#f5222d");
                        examEvent.setCreatedAt(now);
                        examEvent.setUpdatedAt(now);
                        
                        timetableEventRepository.save(examEvent);
                        eventCount++;
                    }
                }
            }
            
            log.info("✅ Created {} timetable events for {} weeks", eventCount, 8);
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
            log.error("❌ Lỗi khi tạo dữ liệu yêu cầu mẫu: {}", e.getMessage());
        }
    }

    private void seedRecruitmentPlans() {
        if (recruitmentPlanRepository.count() == 0) {
            // Tạo kế hoạch tuyển dụng với tên mới và nhiều vị trí hơn
            RecruitmentPlan plan1 = new RecruitmentPlan();
            plan1.setTitle("Kế hoạch tuyển dụng Q1");
            plan1.setStartDate(LocalDate.now().minusDays(40));
            plan1.setEndDate(LocalDate.now().minusDays(10));
            plan1.setTotalQuantity(5);
            plan1.setStatus(RecruitmentPlan.Status.OPEN);
            recruitmentPlanRepository.save(plan1);
            
            RecruitmentPlan plan2 = new RecruitmentPlan();
            plan2.setTitle("Kế hoạch tuyển dụng Q2");
            plan2.setStartDate(LocalDate.now());
            plan2.setEndDate(LocalDate.now().plusDays(30));
            plan2.setTotalQuantity(4);
            plan2.setStatus(RecruitmentPlan.Status.OPEN);
            recruitmentPlanRepository.save(plan2);
            
            RecruitmentPlan plan3 = new RecruitmentPlan();
            plan3.setTitle("Kế hoạch tuyển dụng Q3");
            plan3.setStartDate(LocalDate.now().plusDays(40));
            plan3.setEndDate(LocalDate.now().plusDays(70));
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
                // Kế hoạch 1: Đợt thứ nhất - 5 vị trí (5 PART_TIME - Giáo viên)
                JobPosition job1 = new JobPosition();
                job1.setTitle("Giáo viên Toán lớp 12");
                job1.setDescription("Dạy Toán cho học sinh lớp 12, luyện thi đại học. Yêu cầu: Tốt nghiệp đại học chuyên ngành Toán hoặc Sư phạm Toán, có kinh nghiệm giảng dạy, nhiệt tình, tận tâm với học sinh.");
                job1.setSalaryRange("700000"); // FE sẽ hiển thị đuôi 'VNĐ/giờ'
                job1.setContractType("PART_TIME");
                job1.setQuantity(2);
                job1.setRecruitmentPlan(plans.get(0)); // Q1 window
                jobPositionRepository.save(job1);
                
                JobPosition job2 = new JobPosition();
                job2.setTitle("Giáo viên Lý lớp 12");
                job2.setDescription("Dạy Vật lý cho học sinh lớp 12, chuẩn bị kiến thức cho kỳ thi THPT. Yêu cầu: Tốt nghiệp đại học chuyên ngành Vật lý hoặc Sư phạm Vật lý, có phương pháp giảng dạy hiệu quả, khả năng truyền đạt tốt.");
                job2.setSalaryRange("750000"); // FE sẽ hiển thị đuôi 'VNĐ/giờ'
                job2.setContractType("PART_TIME");
                job2.setQuantity(1);
                job2.setRecruitmentPlan(plans.get(0)); // Q1 window
                jobPositionRepository.save(job2);
                
                JobPosition job3 = new JobPosition();
                job3.setTitle("Giáo viên Toán lớp 10");
                job3.setDescription("Dạy Toán cho học sinh lớp 10, luyện thi đại học. Yêu cầu: Tốt nghiệp đại học chuyên ngành Toán hoặc Sư phạm Toán, có kinh nghiệm giảng dạy, nhiệt tình, tận tâm với học sinh.");
                job3.setSalaryRange("500000"); // FE sẽ hiển thị đuôi 'VNĐ/giờ'
                job3.setContractType("PART_TIME");
                job3.setQuantity(1);
                job3.setRecruitmentPlan(plans.get(0)); // Q1 window
                jobPositionRepository.save(job3);
                
                JobPosition job4 = new JobPosition();
                job4.setTitle("Giáo viên Lý lớp 11");
                job4.setDescription("Dạy Vật lý cho học sinh lớp 11, chuẩn bị kiến thức cho kỳ thi THPT. Yêu cầu: Tốt nghiệp đại học chuyên ngành Vật lý hoặc Sư phạm Vật lý, có phương pháp giảng dạy hiệu quả, khả năng truyền đạt tốt.");
                job4.setSalaryRange("600000");
                job4.setContractType("PART_TIME");
                job4.setQuantity(1);
                job4.setRecruitmentPlan(plans.get(0)); // Q1 window
                jobPositionRepository.save(job4);
                
                // Kế hoạch 2: Đợt thứ hai - 4 vị trí (4 PART_TIME - Giáo viên)
                JobPosition job5 = new JobPosition();
                job5.setTitle("Giáo viên Hóa lớp 10");
                job5.setDescription("Dạy Hóa học cho học sinh lớp 10, giúp học sinh nắm vững kiến thức cơ bản và chuẩn bị cho các năm học tiếp theo. Yêu cầu: Tốt nghiệp đại học chuyên ngành Hóa học hoặc Sư phạm Hóa học, có kinh nghiệm giảng dạy, nhiệt tình, tận tâm với học sinh.");
                job5.setSalaryRange("600000");
                job5.setContractType("PART_TIME");
                job5.setQuantity(1);
                job5.setRecruitmentPlan(plans.get(1)); // Q2 window
                jobPositionRepository.save(job5);
                
                JobPosition job6 = new JobPosition();
                job6.setTitle("Giáo viên Hóa lớp 11");
                job6.setDescription("Dạy Hóa học cho học sinh lớp 11, giúp học sinh hiểu sâu các khái niệm hóa học và chuẩn bị kiến thức cho lớp 12. Yêu cầu: Tốt nghiệp đại học chuyên ngành Hóa học hoặc Sư phạm Hóa học, có phương pháp giảng dạy hiệu quả, khả năng truyền đạt tốt.");
                job6.setSalaryRange("700000");
                job6.setContractType("PART_TIME");
                job6.setQuantity(1);
                job6.setRecruitmentPlan(plans.get(1)); // Q2 window
                jobPositionRepository.save(job6);
                
                JobPosition job7 = new JobPosition();
                job7.setTitle("Giáo viên Hóa lớp 12");
                job7.setDescription("Dạy Hóa học cho học sinh lớp 12, giúp học sinh hoàn thiện kiến thức và chuẩn bị tốt cho kỳ thi tốt nghiệp THPT. Yêu cầu: Tốt nghiệp đại học chuyên ngành Hóa học hoặc Sư phạm Hóa học, có kinh nghiệm giảng dạy, kiến thức chuyên môn vững vàng.");
                job7.setSalaryRange("800000");
                job7.setContractType("PART_TIME");
                job7.setQuantity(1);
                job7.setRecruitmentPlan(plans.get(1)); // Q2 window
                jobPositionRepository.save(job7);
                
                JobPosition job8 = new JobPosition();
                job8.setTitle("Giáo viên Tiếng Anh");
                job8.setDescription("Dạy Tiếng Anh cho học sinh các cấp từ lớp 10-12, giúp học sinh phát triển kỹ năng nghe, nói, đọc, viết. Yêu cầu: Tốt nghiệp đại học chuyên ngành Tiếng Anh hoặc Sư phạm Tiếng Anh, có chứng chỉ IELTS 7.0+, có kinh nghiệm giảng dạy.");
                job8.setSalaryRange("800000");
                job8.setContractType("PART_TIME");
                job8.setQuantity(1);
                job8.setRecruitmentPlan(plans.get(1)); // Q2 window
                jobPositionRepository.save(job8);
                
                // Kế hoạch 3: Đợt thứ ba - 5 vị trí (5 PART_TIME - Giáo viên)
                JobPosition job9 = new JobPosition();
                job9.setTitle("Giáo viên Văn học lớp 10");
                job9.setDescription("Dạy Ngữ văn cho học sinh lớp 10, giúp học sinh hiểu và cảm nhận văn học, phát triển kỹ năng đọc hiểu và viết văn. Yêu cầu: Tốt nghiệp đại học chuyên ngành Văn học hoặc Sư phạm Văn, có kinh nghiệm giảng dạy, khả năng truyền đạt tốt, am hiểu văn học.");
                job9.setSalaryRange("600000");
                job9.setContractType("PART_TIME");
                job9.setQuantity(1);
                job9.setRecruitmentPlan(plans.get(2)); // Q3 window
                jobPositionRepository.save(job9);
                
                JobPosition job10 = new JobPosition();
                job10.setTitle("Giáo viên Văn học lớp 11");
                job10.setDescription("Dạy Ngữ văn cho học sinh lớp 11, giúp học sinh phân tích văn học sâu sắc và chuẩn bị kiến thức cho lớp 12. Yêu cầu: Tốt nghiệp đại học chuyên ngành Văn học hoặc Sư phạm Văn, có kinh nghiệm giảng dạy, khả năng truyền đạt tốt, am hiểu văn học.");
                job10.setSalaryRange("700000");
                job10.setContractType("PART_TIME");
                job10.setQuantity(1);
                job10.setRecruitmentPlan(plans.get(2)); // Q3 window
                jobPositionRepository.save(job10);
                
                JobPosition job11 = new JobPosition();
                job11.setTitle("Giáo viên Văn học lớp 12");
                job11.setDescription("Dạy Ngữ văn cho học sinh lớp 12, giúp học sinh hoàn thiện kiến thức và chuẩn bị tốt cho kỳ thi tốt nghiệp THPT. Yêu cầu: Tốt nghiệp đại học chuyên ngành Văn học hoặc Sư phạm Văn, có kinh nghiệm giảng dạy, khả năng truyền đạt tốt, am hiểu văn học.");
                job11.setSalaryRange("800000");
                job11.setContractType("PART_TIME");
                job11.setQuantity(1);
                job11.setRecruitmentPlan(plans.get(2)); // Q3 window
                jobPositionRepository.save(job11);
                
                JobPosition job12 = new JobPosition();
                job12.setTitle("Giáo viên Sinh học lớp 11");
                job12.setDescription("Dạy Sinh học cho học sinh lớp 11, giúp học sinh hiểu sâu các khái niệm sinh học và chuẩn bị kiến thức cho lớp 12. Yêu cầu: Tốt nghiệp đại học chuyên ngành Sinh học hoặc Sư phạm Sinh, có kinh nghiệm giảng dạy, kiến thức chuyên môn vững vàng.");
                job12.setSalaryRange("600000");
                job12.setContractType("PART_TIME");
                job12.setQuantity(1);
                job12.setRecruitmentPlan(plans.get(2)); // Q3 window
                jobPositionRepository.save(job12);
                
                JobPosition job13 = new JobPosition();
                job13.setTitle("Giáo viên Sinh học lớp 12");
                job13.setDescription("Dạy Sinh học cho học sinh lớp 12, giúp học sinh hoàn thiện kiến thức và chuẩn bị tốt cho kỳ thi tốt nghiệp THPT. Yêu cầu: Tốt nghiệp đại học chuyên ngành Sinh học hoặc Sư phạm Sinh, có kinh nghiệm giảng dạy, kiến thức chuyên môn vững vàng.");
                job13.setSalaryRange("700000");
                job13.setContractType("PART_TIME");
                job13.setQuantity(1);
                job13.setRecruitmentPlan(plans.get(2)); // Q3 window
                jobPositionRepository.save(job13);
                
                log.info("✅ Created 13 job positions across 3 recruitment plans");
            } else {
                log.error("❌ Không đủ kế hoạch tuyển dụng cho việc tạo vị trí công việc");
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
                // Mặc định PENDING; đặt một phần thành APPROVED để hiển thị ở tab "Lên lịch"
                application.setStatus("PENDING");
                // Đặt createdAt nằm trong khoảng thời gian của kế hoạch tuyển dụng tương ứng
                JobPosition jobPosition = jobPositions.get(i % jobPositions.size());
                application.setJobPosition(jobPosition);
                try {
                    LocalDate planStart = jobPosition.getRecruitmentPlan().getStartDate();
                    LocalDate planEnd = jobPosition.getRecruitmentPlan().getEndDate();
                    LocalDate today = LocalDate.now();
                    LocalDate effectiveEnd = planEnd.isAfter(today) ? today : planEnd;
                    if (effectiveEnd.isBefore(planStart)) {
                        effectiveEnd = planStart; // nếu kế hoạch ở tương lai, lấy ngày bắt đầu
                    }
                    long days = java.time.temporal.ChronoUnit.DAYS.between(planStart, effectiveEnd);
                    long safeDays = Math.max(0, days);
                    long offset = (safeDays == 0) ? 0 : (i % (safeDays + 1));
                    LocalDate chosen = planStart.plusDays(offset);
                    if (chosen.isAfter(effectiveEnd)) {
                        chosen = effectiveEnd;
                    }
                    application.setCreatedAt(chosen.atTime(9 + (i % 8), 0));
                } catch (Exception ex) {
                    application.setCreatedAt(LocalDateTime.now().minusDays(i));
                }
                
                recruitmentApplicationRepository.save(application);
                log.info("✅ Created application for {} applying to: {} (status={})", applicants[i][0], jobPosition.getTitle(), application.getStatus());
            }
            log.info("✅ Created {} sample recruitment applications with real names.", applicants.length);
        } else {
            log.info("✅ Recruitment applications already seeded.");
        }
    }

    private void seedLectures(List<Classroom> classrooms) {
        if (lectureRepository.count() == 0) {
            for (Classroom classroom : classrooms) {
                String subject = classroom.getSubject();
                List<Lecture> lectures = createLecturesForSubject(subject, classroom);
                
                for (Lecture lecture : lectures) {
                    lectureRepository.save(lecture);
                }
            }
            log.info("✅ Created detailed lectures for {} classrooms", classrooms.size());
        } else {
            log.info("✅ Lectures already seeded.");
        }
    }
    
    private List<Lecture> createLecturesForSubject(String subject, Classroom classroom) {
        List<Lecture> lectures = new ArrayList<>();
        
        switch (subject) {
            case "Toán học nâng cao":
                lectures.add(createLecture("Chương 1: Hàm số và đồ thị", "Khái niệm hàm số, các loại hàm số cơ bản, vẽ đồ thị", classroom, 1));
                lectures.add(createLecture("Chương 2: Phương trình và bất phương trình", "Giải phương trình bậc nhất, bậc hai, bất phương trình", classroom, 2));
                lectures.add(createLecture("Chương 3: Hình học không gian", "Các khối đa diện, thể tích, diện tích bề mặt", classroom, 3));
                lectures.add(createLecture("Chương 4: Đạo hàm và ứng dụng", "Định nghĩa đạo hàm, quy tắc tính đạo hàm, ứng dụng", classroom, 4));
                lectures.add(createLecture("Chương 5: Tích phân và ứng dụng", "Định nghĩa tích phân, các phương pháp tính tích phân", classroom, 5));
                break;
                
            case "Lịch sử thế giới":
                lectures.add(createLecture("Chương 1: Các nền văn minh cổ đại", "Ai Cập, Lưỡng Hà, Ấn Độ, Trung Quốc cổ đại", classroom, 1));
                lectures.add(createLecture("Chương 2: Hy Lạp và La Mã cổ đại", "Văn hóa, chính trị, nghệ thuật Hy Lạp - La Mã", classroom, 2));
                lectures.add(createLecture("Chương 3: Thời kỳ Trung cổ", "Chế độ phong kiến, các cuộc Thập tự chinh", classroom, 3));
                lectures.add(createLecture("Chương 4: Thời kỳ Phục hưng", "Văn hóa, nghệ thuật, khoa học thời Phục hưng", classroom, 4));
                lectures.add(createLecture("Chương 5: Cách mạng công nghiệp", "Những thay đổi kinh tế, xã hội thế kỷ 18-19", classroom, 5));
                break;
                
            case "Ngữ văn Việt Nam":
                lectures.add(createLecture("Chương 1: Văn học dân gian", "Truyện cổ tích, ca dao, tục ngữ Việt Nam", classroom, 1));
                lectures.add(createLecture("Chương 2: Văn học trung đại", "Văn học chữ Hán, chữ Nôm thời phong kiến", classroom, 2));
                lectures.add(createLecture("Chương 3: Văn học hiện đại", "Văn học từ đầu thế kỷ 20 đến 1945", classroom, 3));
                lectures.add(createLecture("Chương 4: Văn học kháng chiến", "Văn học thời kỳ kháng chiến chống Pháp, Mỹ", classroom, 4));
                lectures.add(createLecture("Chương 5: Văn học đương đại", "Văn học từ 1975 đến nay", classroom, 5));
                break;
                
            case "Tiếng Anh giao tiếp":
                lectures.add(createLecture("Unit 1: Greetings and Introductions", "Chào hỏi, giới thiệu bản thân và người khác", classroom, 1));
                lectures.add(createLecture("Unit 2: Daily Activities", "Mô tả các hoạt động hàng ngày", classroom, 2));
                lectures.add(createLecture("Unit 3: Family and Friends", "Từ vựng và cấu trúc về gia đình, bạn bè", classroom, 3));
                lectures.add(createLecture("Unit 4: Shopping and Services", "Giao tiếp khi mua sắm và sử dụng dịch vụ", classroom, 4));
                lectures.add(createLecture("Unit 5: Travel and Transportation", "Từ vựng và cấu trúc về du lịch, giao thông", classroom, 5));
                break;
                
            case "Khoa học máy tính":
                lectures.add(createLecture("Chương 1: Giới thiệu về máy tính", "Lịch sử, cấu trúc và nguyên lý hoạt động của máy tính", classroom, 1));
                lectures.add(createLecture("Chương 2: Hệ điều hành", "Windows, Linux, macOS và các chức năng cơ bản", classroom, 2));
                lectures.add(createLecture("Chương 3: Lập trình cơ bản", "Giới thiệu về thuật toán và lập trình", classroom, 3));
                lectures.add(createLecture("Chương 4: Mạng máy tính", "Internet, mạng LAN, bảo mật thông tin", classroom, 4));
                lectures.add(createLecture("Chương 5: Ứng dụng thực tế", "Word, Excel, PowerPoint và các ứng dụng khác", classroom, 5));
                break;
                
            case "Vật lý đại cương":
                lectures.add(createLecture("Chương 1: Cơ học", "Chuyển động, lực, năng lượng và định luật Newton", classroom, 1));
                lectures.add(createLecture("Chương 2: Nhiệt học", "Nhiệt độ, nhiệt lượng, các định luật nhiệt động lực học", classroom, 2));
                lectures.add(createLecture("Chương 3: Điện học", "Điện tích, điện trường, dòng điện và mạch điện", classroom, 3));
                lectures.add(createLecture("Chương 4: Quang học", "Ánh sáng, gương, thấu kính và các hiện tượng quang học", classroom, 4));
                lectures.add(createLecture("Chương 5: Vật lý hạt nhân", "Cấu trúc nguyên tử, phóng xạ và năng lượng hạt nhân", classroom, 5));
                break;
                
            default:
                // Fallback cho các môn học khác
                lectures.add(createLecture("Bài 1: Giới thiệu môn học", "Tổng quan về môn học " + subject, classroom, 1));
                lectures.add(createLecture("Bài 2: Nội dung cơ bản", "Các kiến thức cơ bản của môn học", classroom, 2));
                lectures.add(createLecture("Bài 3: Thực hành", "Các bài tập và thực hành", classroom, 3));
                break;
        }
        
        return lectures;
    }
    
    private Lecture createLecture(String title, String description, Classroom classroom, int order) {
        Lecture lecture = new Lecture();
        lecture.setTitle(title);
        lecture.setContent(description); // Sử dụng content thay vì description
        lecture.setClassroom(classroom);
        // Tạo ngày khác nhau cho các bài giảng để test validation
        // order 1,2 = hôm qua, hôm nay | order 3,4,5 = ngày mai và các ngày sau
        lecture.setLectureDate(LocalDate.now().plusDays(order - 2)); // order 1=-1, order 2=0 (today), order 3=+1, etc.
        // lecture.setOrder(order); // Không có field order
        // lecture.setStatus("ACTIVE"); // Không có field status
        // lecture.setCreatedAt(LocalDateTime.now().minusDays(order)); // Không có field createdAt
        return lecture;
    }

    private void seedAssignments() {
        if (assignmentRepository.count() == 0) {
            List<Classroom> classrooms = classroomRepository.findAll();
            for (Classroom classroom : classrooms) {
                Assignment assignment = new Assignment();
                assignment.setClassroom(classroom);
                assignment.setTitle("Bài tập số 1");
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
                    attendance.setStatus(com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus.PRESENT);
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
            log.error("❌ Không tìm thấy người dùng STUDENT!");
        }

        // Check teacher user
        User teacher = userRepository.findByEmail("teacher@test.com").orElse(null);
        if (teacher != null) {
            log.info("✅ Teacher User: ID={}, Email={}, Role={}, RoleId={}",
                teacher.getId(), teacher.getEmail(), teacher.getRole(), teacher.getRoleId());
        } else {
            log.error("❌ Không tìm thấy người dùng TEACHER!");
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
                // Ensure seeded teachers are active so they appear in filters
                if (saved.getStatus() == null || saved.getStatus().isBlank()) {
                    saved.setStatus("active");
                    userRepository.save(saved);
                }

                // Removed: creating ACTIVE contracts for seeded teachers.
                // Per requirement, do not auto-generate any test contract data here.
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
            log.error("❌ Lỗi khi seed mẫu minh chứng: {}", e.getMessage(), e);
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

    /**
     * Seed sample teacher evaluations for demo purposes
     */
    private void seedTeacherEvaluations() {
        if (teacherEvaluationRepository.count() > 0) {
            log.info("Teacher evaluations already exist, skipping seeding");
            return;
        }

        try {
            log.info("============== Seeding Teacher Evaluations ==============");

            // Get teaching assistants and teachers
            List<User> teachingAssistants = userRepository.findByRoleId(RoleConstants.TEACHING_ASSISTANT);
            List<User> teachers = userRepository.findByRoleId(RoleConstants.TEACHER);

            if (teachingAssistants.isEmpty() || teachers.isEmpty()) {
                log.warn("No teaching assistants or teachers found, skipping evaluation seeding");
                return;
            }

            List<TeacherEvaluation> evaluations = new ArrayList<>();
            LocalDateTime baseTime = LocalDateTime.now().minusDays(30);

            // Create evaluations for each teacher by different teaching assistants
            int evalCount = 0;
            for (User teacher : teachers) {
                for (int i = 0; i < Math.min(teachingAssistants.size(), 3); i++) { // Max 3 evaluations per teacher
                    User evaluator = teachingAssistants.get(i % teachingAssistants.size());
                    
                    TeacherEvaluation evaluation = new TeacherEvaluation();
                    evaluation.setTeacher(teacher);
                    evaluation.setEvaluator(evaluator);
                    evaluation.setEvaluationDate(baseTime.plusDays(evalCount * 2));
                    evaluation.setClassSessionId(1L + (evalCount % 5)); // Mock class session IDs
                    
                    // Generate random but realistic scores (3-5 to simulate good teachers)
                    int teachingQuality = 3 + (int)(Math.random() * 3); // 3-5
                    int studentInteraction = 3 + (int)(Math.random() * 3); // 3-5  
                    int punctuality = 4 + (int)(Math.random() * 2); // 4-5 (most teachers are punctual)
                    
                    evaluation.setTeachingQualityScore(teachingQuality);
                    evaluation.setStudentInteractionScore(studentInteraction);
                    evaluation.setPunctualityScore(punctuality);
                    
                    // Calculate overall score (average)
                    int overallScore = Math.round((teachingQuality + studentInteraction + punctuality) / 3.0f);
                    evaluation.setOverallScore(overallScore);
                    
                    // Add sample comments
                    String[] comments = {
                        "Giảng viên giảng dạy rất tốt, học sinh tương tác tích cực.",
                        "Phương pháp giảng dạy hiệu quả, cần cải thiện thêm về tương tác.",
                        "Giảng viên nhiệt tình, đúng giờ, học sinh hài lòng.",
                        "Cần cải thiện cách truyền đạt kiến thức cho dễ hiểu hơn.",
                        "Rất tốt! Học sinh học được nhiều kiến thức bổ ích.",
                        "Giảng viên chuẩn bị bài kỹ, giải thích rõ ràng."
                    };
                    evaluation.setComments(comments[evalCount % comments.length]);
                    
                    evaluations.add(evaluation);
                    evalCount++;
                }
            }

            teacherEvaluationRepository.saveAll(evaluations);
            log.info("✅ Created {} teacher evaluations for {} teachers by {} teaching assistants", 
                    evaluations.size(), teachers.size(), teachingAssistants.size());
            log.info("============== Teacher Evaluations Seeding Complete ==============");

        } catch (Exception e) {
            log.error("❌ Error seeding teacher evaluations: {}", e.getMessage(), e);
        }
    }

    /**
     * Seed comprehensive parent role test data
     * Creates parents, students, relationships, courses, enrollments, attendance and timetable data for testing parent functionality
     */
    private void seedParentTestData() {
        try {
            log.info("🚀 Starting parent role seeder data creation...");
            
            // Create parent users
            List<User> parentUsers = createParentUsers();
            log.info("✅ Created {} parent users", parentUsers.size());
            
            // Create student users with parent information
            List<User> studentUsers = createStudentUsers(parentUsers);
            log.info("✅ Created {} student users", studentUsers.size());
            
            // Create parent entities
            List<Parent> parents = createParentEntities(parentUsers);
            log.info("✅ Created {} parent entities", parents.size());
            
            // Create parent-student relationships
            createParentStudentRelationships(parents, studentUsers);
            log.info("✅ Created parent-student relationships");
            
            // Create sample courses for students
            List<Course> parentTestCourses = createParentTestCourses();
            log.info("✅ Created {} test courses", parentTestCourses.size());
            
            // Enroll students in courses
            createStudentEnrollments(studentUsers, parentTestCourses);
            log.info("✅ Created student course enrollments");
            
            // Create attendance sessions and records
            createParentTestAttendance(parentTestCourses, studentUsers);
            log.info("✅ Created attendance data for parent testing");
            
            // Create timetable events for students
            // createTimetableEventsForStudents(studentUsers, parentTestCourses);
            log.info("✅ Skipped duplicate timetable events creation for students (already handled in main seeding)");
            
            // Create sample leave notices
            createSampleLeaveNotices(parents, studentUsers);
            log.info("✅ Created sample leave notices for testing");
            
            log.info("🎉 Parent role seeder data creation completed successfully!");
            
        } catch (Exception e) {
            log.error("❌ Error creating parent seeder data: {}", e.getMessage(), e);
        }
    }
    
    private List<User> createParentUsers() {
        List<User> parentUsers = new ArrayList<>();
        
        // Parent 1: Trần Văn Nam (will have 2 children)
        String email1 = "tran.van.nam@email.com";
        if (!userRepository.existsByEmail(email1)) {
            User parent1 = new User();
            parent1.setUsername("parent_tran_van_nam");
            parent1.setPassword(passwordEncoder.encode("password123"));
            parent1.setEmail(email1);
            parent1.setFullName("Trần Văn Nam");
            parent1.setPhoneNumber("0901234567");
            parent1.setRoleId(RoleConstants.PARENT);
            parent1.setStatus("active");
            parentUsers.add(userRepository.save(parent1));
            log.info("✅ Created parent user: " + email1);
        } else {
            parentUsers.add(userRepository.findByEmail(email1).orElse(null));
            log.info("ℹ️ Parent user already exists: " + email1);
        }
        
        // Parent 2: Nguyễn Thị Lan (will have 1 child)
        String email2 = "nguyen.thi.lan@email.com";
        if (!userRepository.existsByEmail(email2)) {
            User parent2 = new User();
            parent2.setUsername("parent_nguyen_thi_lan");
            parent2.setPassword(passwordEncoder.encode("password123"));
            parent2.setEmail(email2);
            parent2.setFullName("Nguyễn Thị Lan");
            parent2.setPhoneNumber("0912345678");
            parent2.setRoleId(RoleConstants.PARENT);
            parent2.setStatus("active");
            parentUsers.add(userRepository.save(parent2));
            log.info("✅ Created parent user: " + email2);
        } else {
            parentUsers.add(userRepository.findByEmail(email2).orElse(null));
            log.info("ℹ️ Parent user already exists: " + email2);
        }
        
        // Parent 3: Lê Minh Đức (will have 1 child)
        String email3 = "le.minh.duc@email.com";
        if (!userRepository.existsByEmail(email3)) {
            User parent3 = new User();
            parent3.setUsername("parent_le_minh_duc");
            parent3.setPassword(passwordEncoder.encode("password123"));
            parent3.setEmail(email3);
            parent3.setFullName("Lê Minh Đức");
            parent3.setPhoneNumber("0923456789");
            parent3.setRoleId(RoleConstants.PARENT);
            parent3.setStatus("active");
            parentUsers.add(userRepository.save(parent3));
            log.info("✅ Created parent user: " + email3);
        } else {
            parentUsers.add(userRepository.findByEmail(email3).orElse(null));
            log.info("ℹ️ Parent user already exists: " + email3);
        }
        
        // Parent 4: Phạm Thị Mai (will have 1 child)
        String email4 = "pham.thi.mai@email.com";
        if (!userRepository.existsByEmail(email4)) {
            User parent4 = new User();
            parent4.setUsername("parent_pham_thi_mai");
            parent4.setPassword(passwordEncoder.encode("password123"));
            parent4.setEmail(email4);
            parent4.setFullName("Phạm Thị Mai");
            parent4.setPhoneNumber("0934567890");
            parent4.setRoleId(RoleConstants.PARENT);
            parent4.setStatus("active");
            parentUsers.add(userRepository.save(parent4));
            log.info("✅ Created parent user: " + email4);
        } else {
            parentUsers.add(userRepository.findByEmail(email4).orElse(null));
            log.info("ℹ️ Parent user already exists: " + email4);
        }
        
        return parentUsers;
    }
    
    private List<User> createStudentUsers(List<User> parentUsers) {
        List<User> studentUsers = new ArrayList<>();
        
        // Student 1: Child of Trần Văn Nam
        String studentEmail1 = "tran.minh.anh@student.edu.vn";
        if (!userRepository.existsByEmail(studentEmail1)) {
            User student1 = new User();
            student1.setUsername("student_tran_minh_anh");
            student1.setPassword(passwordEncoder.encode("password123"));
            student1.setEmail(studentEmail1);
            student1.setFullName("Trần Minh Anh");
            student1.setPhoneNumber("0987654321");
            student1.setRoleId(RoleConstants.STUDENT);
            student1.setParentPhone("0901234567");
            student1.setParentName("Trần Văn Nam");
            student1.setStatus("active");
            studentUsers.add(userRepository.save(student1));
            log.info("✅ Created student user: " + studentEmail1);
        } else {
            studentUsers.add(userRepository.findByEmail(studentEmail1).orElse(null));
            log.info("ℹ️ Student user already exists: " + studentEmail1);
        }
        
        // Student 2: Another child of Trần Văn Nam
        String studentEmail2 = "tran.thu.ha@student.edu.vn";
        if (!userRepository.existsByEmail(studentEmail2)) {
            User student2 = new User();
            student2.setUsername("student_tran_thu_ha");
            student2.setPassword(passwordEncoder.encode("password123"));
            student2.setEmail(studentEmail2);
            student2.setFullName("Trần Thu Hà");
            student2.setPhoneNumber("0976543210");
            student2.setRoleId(RoleConstants.STUDENT);
            student2.setParentPhone("0901234567");
            student2.setParentName("Trần Văn Nam");
            student2.setStatus("active");
            studentUsers.add(userRepository.save(student2));
            log.info("✅ Created student user: " + studentEmail2);
        } else {
            studentUsers.add(userRepository.findByEmail(studentEmail2).orElse(null));
            log.info("ℹ️ Student user already exists: " + studentEmail2);
        }
        
        // Student 3: Child of Nguyễn Thị Lan
        String studentEmail3 = "nguyen.hoang.long@student.edu.vn";
        if (!userRepository.existsByEmail(studentEmail3)) {
            User student3 = new User();
            student3.setUsername("student_nguyen_hoang_long");
            student3.setPassword(passwordEncoder.encode("password123"));
            student3.setEmail(studentEmail3);
            student3.setFullName("Nguyễn Hoàng Long");
            student3.setPhoneNumber("0965432109");
            student3.setRoleId(RoleConstants.STUDENT);
            student3.setParentPhone("0912345678");
            student3.setParentName("Nguyễn Thị Lan");
            student3.setStatus("active");
            studentUsers.add(userRepository.save(student3));
            log.info("✅ Created student user: " + studentEmail3);
        } else {
            studentUsers.add(userRepository.findByEmail(studentEmail3).orElse(null));
            log.info("ℹ️ Student user already exists: " + studentEmail3);
        }
        
        // Student 4: Child of Lê Minh Đức
        String studentEmail4 = "le.thi.hong@student.edu.vn";
        if (!userRepository.existsByEmail(studentEmail4)) {
            User student4 = new User();
            student4.setUsername("student_le_thi_hong");
            student4.setPassword(passwordEncoder.encode("password123"));
            student4.setEmail(studentEmail4);
            student4.setFullName("Lê Thị Hồng");
            student4.setPhoneNumber("0954321098");
            student4.setRoleId(RoleConstants.STUDENT);
            student4.setParentPhone("0923456789");
            student4.setParentName("Lê Minh Đức");
            student4.setStatus("active");
            studentUsers.add(userRepository.save(student4));
            log.info("✅ Created student user: " + studentEmail4);
        } else {
            studentUsers.add(userRepository.findByEmail(studentEmail4).orElse(null));
            log.info("ℹ️ Student user already exists: " + studentEmail4);
        }
        
        // Student 5: Child of Phạm Thị Mai
        String studentEmail5 = "pham.van.duc@student.edu.vn";
        if (!userRepository.existsByEmail(studentEmail5)) {
            User student5 = new User();
            student5.setUsername("student_pham_van_duc");
            student5.setPassword(passwordEncoder.encode("password123"));
            student5.setEmail(studentEmail5);
            student5.setFullName("Phạm Văn Đức");
            student5.setPhoneNumber("0943210987");
            student5.setRoleId(RoleConstants.STUDENT);
            student5.setParentPhone("0934567890");
            student5.setParentName("Phạm Thị Mai");
            student5.setStatus("active");
            studentUsers.add(userRepository.save(student5));
            log.info("✅ Created student user: " + studentEmail5);
        } else {
            studentUsers.add(userRepository.findByEmail(studentEmail5).orElse(null));
            log.info("ℹ️ Student user already exists: " + studentEmail5);
        }
        
        return studentUsers;
    }
    
    private List<Parent> createParentEntities(List<User> parentUsers) {
        List<Parent> parents = new ArrayList<>();
        
        for (User parentUser : parentUsers) {
            Parent parent = new Parent();
            parent.setUserId(parentUser.getId());
            parent.setName(parentUser.getFullName());
            parent.setPhone(parentUser.getPhoneNumber());
            parent.setEmail(parentUser.getEmail());
            parent.setStatus(Parent.ParentStatus.ACTIVE);
            parents.add(entityManager.merge(parent));
        }
        
        return parents;
    }
    
    private void createParentStudentRelationships(List<Parent> parents, List<User> students) {
        // Parent 1 (Trần Văn Nam) has 2 children
        createStudentParentRelation(parents.get(0), students.get(0), StudentParent.RelationType.FATHER);
        createStudentParentRelation(parents.get(0), students.get(1), StudentParent.RelationType.FATHER);
        
        // Parent 2 (Nguyễn Thị Lan) has 1 child  
        createStudentParentRelation(parents.get(1), students.get(2), StudentParent.RelationType.MOTHER);
        
        // Parent 3 (Lê Minh Đức) has 1 child
        createStudentParentRelation(parents.get(2), students.get(3), StudentParent.RelationType.FATHER);
        
        // Parent 4 (Phạm Thị Mai) has 1 child
        createStudentParentRelation(parents.get(3), students.get(4), StudentParent.RelationType.MOTHER);
    }
    
    private void createStudentParentRelation(Parent parent, User student, StudentParent.RelationType relationType) {
        StudentParent relationship = new StudentParent();
        relationship.setStudentId(student.getId());
        relationship.setParentId(parent.getId());
        relationship.setRelationType(relationType);
        relationship.setIsPrimary(true);
        relationship.setLegalGuardian(true);
        relationship.setStartAt(LocalDate.now());
        entityManager.merge(relationship);
    }

    /**
     * Creates sample courses for parent testing
     */
    private List<Course> createParentTestCourses() {
        List<Course> courses = new ArrayList<>();
        
        // Create simple courses using the actual Course entity structure
        String[] courseNames = {
            "Toán 11A - Học kỳ 1", 
            "Văn 11B - Học kỳ 1",
            "Lý 12A - Ôn thi THPT"
        };
        
        String[] descriptions = {
            "Lớp học môn Toán cho học sinh khối 11",
            "Lớp học môn Ngữ văn cho học sinh khối 11", 
            "Lớp ôn tập Vật lý cho học sinh lớp 12 chuẩn bị thi THPT Quốc gia"
        };
        
        for (int i = 0; i < courseNames.length; i++) {
            Course course = new Course();
            course.setName(courseNames[i]);
            course.setDescription(descriptions[i]);
            
            courses.add(entityManager.merge(course));
        }
        
        return courses;
    }
    
    /**
     * Creates student enrollments in courses using direct SQL
     */
    private void createStudentEnrollments(List<User> studentUsers, List<Course> courses) {
        if (courses.isEmpty()) {
            log.warn("No courses available for student enrollment");
            return;
        }
        
        // Enroll each student in 1 course
        for (int i = 0; i < studentUsers.size(); i++) {
            User student = studentUsers.get(i);
            Course course = courses.get(i % courses.size());
            
            try {
                // Check if enrollment already exists
                String checkQuery = "SELECT COUNT(*) FROM enrollments WHERE student_id = ? AND course_id = ?";
                Integer count = jdbcTemplate.queryForObject(checkQuery, Integer.class, student.getId(), course.getId());
                
                if (count != null && count == 0) {
                    // Create new enrollment using direct SQL
                    String insertQuery = "INSERT INTO enrollments (student_id, course_id, enrollment_date, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
                    jdbcTemplate.update(insertQuery,
                        student.getId(),
                        course.getId(),
                        LocalDate.now().minusMonths(2),
                        "ACTIVE",
                        LocalDateTime.now(),
                        LocalDateTime.now()
                    );
                    
                    log.debug("Enrolled student {} in course {}", student.getFullName(), course.getName());
                }
            } catch (Exception e) {
                log.debug("Error enrolling student {}: {}", student.getFullName(), e.getMessage());
            }
        }
    }
    
    /**
     * Creates a single enrollment record - simplified version
     */
    private void createEnrollment(User student, Course course) {
        try {
            String insertQuery = "INSERT INTO enrollments (student_id, course_id, enrollment_date, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertQuery,
                student.getId(),
                course.getId(),
                LocalDate.now().minusMonths(2),
                "ACTIVE",
                LocalDateTime.now(),
                LocalDateTime.now()
            );
        } catch (Exception e) {
            log.debug("Error creating enrollment: {}", e.getMessage());
        }
    }
    
    /**
     * Creates attendance sessions and records for parent testing
     * Simplified version that works with existing Classroom structure
     */
    private void createParentTestAttendance(List<Course> courses, List<User> studentUsers) {
        try {
            // Get available classrooms
            List<Classroom> classrooms = classroomRepository.findAll();
            if (classrooms.isEmpty()) {
                log.warn("No classrooms available for attendance creation");
                return;
            }
            
            // Create some attendance sessions for each classroom
            for (int i = 0; i < Math.min(classrooms.size(), 3); i++) {
                Classroom classroom = classrooms.get(i);
                createAttendanceSessionsForClassroom(classroom, studentUsers);
            }
            
        } catch (Exception e) {
            log.error("Error creating parent test attendance: {}", e.getMessage());
        }
    }
    
    /**
     * Creates attendance sessions for a classroom
     */
    private void createAttendanceSessionsForClassroom(Classroom classroom, List<User> studentUsers) {
        try {
            // Create 2-3 attendance sessions for this classroom
            for (int i = 0; i < 3; i++) {
                AttendanceSession session = new AttendanceSession();
                session.setClassroom(classroom);
                session.setSessionDate(LocalDate.now().minusDays(i + 1));
                session.setStartTime(Instant.now().minusSeconds(3600 * (i + 1)));
                session.setEndTime(Instant.now().minusSeconds(1800 * (i + 1)));
                session.setStatus(AttendanceSession.SessionStatus.CLOSED);
                session.setCreatedAt(LocalDateTime.now().minusDays(i + 1));
                
                AttendanceSession savedSession = attendanceSessionRepository.save(session);
                
                // Create attendance records for students in this classroom
                createAttendanceRecordsForSession(savedSession, studentUsers);
            }
        } catch (Exception e) {
            log.error("Error creating attendance sessions for classroom {}: {}", classroom.getId(), e.getMessage());
        }
    }
    
    /**
     * Creates attendance records for students in a session
     */
    private void createAttendanceRecordsForSession(AttendanceSession session, List<User> studentUsers) {
        // Create attendance records for first few students (simulate enrollment)
        for (int i = 0; i < Math.min(studentUsers.size(), 3); i++) {
            User student = studentUsers.get(i);
            createAttendanceRecordForStudent(session, student);
        }
    }
    
    /**
     * Creates a single attendance record for a student
     */
    private void createAttendanceRecordForStudent(AttendanceSession session, User student) {
        try {
            Attendance attendance = new Attendance();
            attendance.setSession(session);
            attendance.setStudent(student);
            
            // Randomize attendance status
            int rand = student.getId().intValue() % 100;
            if (rand < 80) {
                attendance.setStatus(com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus.PRESENT);
            } else if (rand < 95) {
                attendance.setStatus(com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus.LATE);
                attendance.setNote("Đến muộn 10 phút");
            } else {
                attendance.setStatus(com.classroomapp.classroombackend.model.attendancemanagement.AttendanceStatus.ABSENT);
                attendance.setNote("Vắng không phép");
            }
            
            attendanceRepository.save(attendance);
            
        } catch (Exception e) {
            log.error("Error creating attendance record for student {}: {}", student.getFullName(), e.getMessage());
        }
    }

    /**
     * Creates timetable events for students based on their actual course schedules
     */
    private void createTimetableEventsForStudents(List<User> studentUsers, List<Course> courses) {
        try {
            if (courses.isEmpty()) {
                log.warn("No courses available for timetable event creation");
                return;
            }
            
            // Get classrooms and schedules for the events
            List<Classroom> classrooms = classroomRepository.findAll();
            List<Schedule> schedules = scheduleRepository.findAll();
            
            if (classrooms.isEmpty()) {
                log.warn("No classrooms available for timetable events");
                return;
            }
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfWeek = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
            
            // Create events based on actual schedules if available
            if (!schedules.isEmpty()) {
                for (int week = 0; week < 8; week++) { // Create 8 weeks of events
                    for (Schedule schedule : schedules) {
                        LocalDateTime eventStart = startOfWeek
                            .plusWeeks(week)
                            .plusDays(schedule.getDayOfWeek())
                            .withHour(schedule.getStartTime().getHour())
                            .withMinute(schedule.getStartTime().getMinute())
                            .withSecond(0);
                        
                        LocalDateTime eventEnd = startOfWeek
                            .plusWeeks(week)
                            .plusDays(schedule.getDayOfWeek())
                            .withHour(schedule.getEndTime().getHour())
                            .withMinute(schedule.getEndTime().getMinute())
                            .withSecond(0);
                        
                        // Skip past events
                        if (eventStart.isBefore(now)) {
                            continue;
                        }
                        
                        TimetableEvent classEvent = new TimetableEvent();
                        classEvent.setTitle(schedule.getSubject());
                        classEvent.setDescription("Lớp học " + schedule.getSubject() + " - " + schedule.getClassroom().getName());
                        classEvent.setStartDatetime(eventStart);
                        classEvent.setEndDatetime(eventEnd);
                        classEvent.setEventType(TimetableEvent.EventType.CLASS);
                        classEvent.setClassroomId(schedule.getClassroom().getId());
                        classEvent.setCreatedBy(schedule.getTeacher().getId());
                        classEvent.setLocation(schedule.getRoom());
                        classEvent.setColor("#1890ff");
                        classEvent.setCreatedAt(now);
                        classEvent.setUpdatedAt(now);
                        
                        timetableEventRepository.save(classEvent);
                        
                        // Add exam events every 3 weeks
                        if (week % 3 == 2) {
                            LocalDateTime examStart = eventStart.plusDays(2).withHour(14);
                            LocalDateTime examEnd = examStart.plusHours(1);
                            
                            TimetableEvent examEvent = new TimetableEvent();
                            examEvent.setTitle("Kiểm tra " + schedule.getSubject());
                            examEvent.setDescription("Bài kiểm tra môn " + schedule.getSubject());
                            examEvent.setStartDatetime(examStart);
                            examEvent.setEndDatetime(examEnd);
                            examEvent.setEventType(TimetableEvent.EventType.EXAM);
                            examEvent.setClassroomId(schedule.getClassroom().getId());
                            examEvent.setCreatedBy(schedule.getTeacher().getId());
                            examEvent.setLocation("Phòng thi " + schedule.getRoom().replaceAll("Room", "Exam"));
                            examEvent.setColor("#f5222d");
                            examEvent.setCreatedAt(now);
                            examEvent.setUpdatedAt(now);
                            
                            timetableEventRepository.save(examEvent);
                        }
                    }
                }
                log.info("Created timetable events based on {} schedules for 8 weeks", schedules.size());
            } else {
                // Fallback: create basic events for courses
                // createBasicTimetableEventsForCourses(courses, classrooms, now);
                log.warn("Skipped basic timetable events creation (already handled in main seeding)");
            }
            
        } catch (Exception e) {
            log.error("Error creating timetable events: {}", e.getMessage(), e);
        }
    }
    
    private void createBasicTimetableEventsForCourses(List<Course> courses, List<Classroom> classrooms, LocalDateTime now) {
        LocalDateTime startOfWeek = now.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
        
        for (int courseIndex = 0; courseIndex < courses.size(); courseIndex++) {
            Course course = courses.get(courseIndex);
            
            for (int week = 0; week < 4; week++) {
                int dayOfWeek = (courseIndex % 5); // 0=Monday, 4=Friday
                int startHour = 8 + (courseIndex % 4); // 8AM, 9AM, 10AM, 11AM
                
                LocalDateTime eventStart = startOfWeek
                    .plusWeeks(week)
                    .plusDays(dayOfWeek)
                    .withHour(startHour)
                    .withMinute(0)
                    .withSecond(0);
                    
                LocalDateTime eventEnd = eventStart.plusHours(2);
                
                if (eventStart.isBefore(now)) {
                    continue;
                }
                
                TimetableEvent classEvent = new TimetableEvent();
                classEvent.setTitle(course.getName());
                classEvent.setDescription("Lớp học " + course.getName());
                classEvent.setStartDatetime(eventStart);
                classEvent.setEndDatetime(eventEnd);
                classEvent.setEventType(TimetableEvent.EventType.CLASS);
                classEvent.setClassroomId(classrooms.get(courseIndex % classrooms.size()).getId());
                classEvent.setCreatedBy(1L);
                classEvent.setLocation("Phòng " + (101 + courseIndex));
                classEvent.setColor("#1890ff");
                classEvent.setCreatedAt(now);
                classEvent.setUpdatedAt(now);
                
                timetableEventRepository.save(classEvent);
            }
        }
    }
    
    private void seedParentMessages() {
        try {
            log.info("🔍 Starting parent messages seeding check...");
            long existingCount = parentMessageRepository.count();
            log.info("🔍 Found {} existing parent messages", existingCount);
            
            if (existingCount > 0) {
                log.info("✅ Parent messages already exist, skipping seeding");
                return;
            }
            
            // Get all teachers
            log.info("🔍 Getting teachers...");
            List<User> teachers = userRepository.findAllTeachers();
            log.info("🔍 Found {} teachers", teachers.size());
            if (teachers.isEmpty()) {
                log.warn("⚠️ No teachers found, cannot seed parent messages");
                return;
            }
            
            // Get all parents
            log.info("🔍 Getting parents...");
            List<Parent> parents = parentRepository.findAll();
            log.info("🔍 Found {} parents", parents.size());
            if (parents.isEmpty()) {
                log.warn("⚠️ No parents found, cannot seed parent messages");
                return;
            }
            
            // Get all student-parent relationships
            log.info("🔍 Getting student-parent relationships...");
            List<StudentParent> studentParentRelations = studentParentRepository.findAll();
            log.info("🔍 Found {} student-parent relationships", studentParentRelations.size());
            if (studentParentRelations.isEmpty()) {
                log.warn("⚠️ No student-parent relationships found, cannot seed parent messages");
                return;
            }
            
            // Sample message subjects and contents for parent-teacher communication
            String[] messageSubjects = {
                "Thông báo về tình hình học tập của con",
                "Cần trao đổi về việc học bài tập về nhà",
                "Con em có tiến bộ tốt trong môn học",
                "Cần hỗ trợ thêm cho con trong học tập",
                "Thông báo về điểm danh và tham gia lớp",
                "Lịch nghỉ học và bù học",
                "Tư vấn về định hướng học tập",
                "Phản hồi về bài kiểm tra gần đây"
            };
            
            String[] teacherMessages = {
                "Xin chào quý phụ huynh, tôi muốn cập nhật về tình hình học tập của con em trong thời gian qua. Con em đã có những tiến bộ đáng kể trong việc học.",
                "Con em cần được nhắc nhở thêm về việc làm bài tập về nhà. Mong quý phụ huynh quan tâm hỗ trợ con ở nhà.",
                "Tôi rất vui mừng thông báo rằng con em đã đạt được kết quả tốt trong bài kiểm tra gần đây. Hãy tiếp tục động viên con.",
                "Con em có một số khó khăn trong việc theo kịp bài học. Tôi đề xuất chúng ta cùng nhau tìm phương pháp hỗ trợ phù hợp.",
                "Con em đã tham gia lớp học rất tích cực. Điểm danh đều đặn và tương tác tốt với các bạn trong lớp.",
                "Thông báo lịch nghỉ học do lễ tết. Lịch bù học sẽ được thông báo cụ thể trong tuần tới.",
                "Dựa trên khả năng và sở thích của con, tôi có một số gợi ý về định hướng học tập cho con em.",
                "Kết quả bài kiểm tra cho thấy con em cần ôn tập thêm một số phần. Mong phụ huynh hỗ trợ con ở nhà."
            };
            
            String[] parentMessages = {
                "Cảm ơn cô/thầy đã quan tâm. Tôi sẽ theo dõi và hỗ trợ con học tập tốt hơn ở nhà.",
                "Con em có phản ánh gì về bài học không? Tôi muốn hiểu rõ hơn để hỗ trợ con.",
                "Cảm ơn cô/thầy đã động viên con. Gia đình rất vui mừng về tiến bộ này.",
                "Tôi sẽ dành thêm thời gian để hỗ trợ con học tập. Xin cô/thầy tư vấn thêm phương pháp học hiệu quả.",
                "Con em có gặp khó khăn gì trong việc giao tiếp với các bạn không?",
                "Cảm ơn thông báo. Gia đình sẽ sắp xếp lịch trình phù hợp.",
                "Tôi rất quan tâm đến định hướng học tập của con. Xin cô/thầy tư vấn chi tiết hơn.",
                "Tôi sẽ giúp con ôn tập phần này. Có tài liệu nào cô/thầy đề xuất không?"
            };
            
            int messageCount = 0;
            
            // Create conversations between each parent and teachers of their children
            for (StudentParent relation : studentParentRelations) {
                Parent parent = relation.getParent();
                User student = relation.getStudent();
                
                // Assign 1-2 random teachers to communicate with this parent about their child
                List<User> studentTeachers = new ArrayList<>();
                int numTeachers = Math.min(2, teachers.size());
                for (int t = 0; t < numTeachers; t++) {
                    studentTeachers.add(teachers.get((parent.getId().intValue() + student.getId().intValue() + t) % teachers.size()));
                }
                
                // Create 2-4 message conversations per parent-teacher pair
                for (User teacher : studentTeachers.subList(0, Math.min(2, studentTeachers.size()))) {
                    int conversationLength = 2 + (messageCount % 3); // 2-4 messages per conversation
                    
                    for (int i = 0; i < conversationLength; i++) {
                        boolean isTeacherMessage = (i % 2 == 0); // Alternate between teacher and parent
                        
                        ParentMessage message = new ParentMessage();
                        message.setParentId(parent.getId());
                        message.setTeacherId(teacher.getId());
                        message.setStudentId(student.getId());
                        
                        if (isTeacherMessage) {
                            message.setSenderType(SenderType.TEACHER);
                            message.setSubject(messageSubjects[messageCount % messageSubjects.length]);
                            message.setMessageContent(teacherMessages[messageCount % teacherMessages.length]);
                        } else {
                            message.setSenderType(SenderType.PARENT);
                            message.setSubject("Re: " + messageSubjects[messageCount % messageSubjects.length]);
                            message.setMessageContent(parentMessages[messageCount % parentMessages.length]);
                        }
                        
                        // Set read status (most messages are read, some recent ones unread)
                        message.setIsRead(messageCount % 4 != 0); // 25% unread
                        if (message.getIsRead()) {
                            message.setReadAt(LocalDateTime.now().minusDays(messageCount % 10));
                        }
                        
                        // Set creation time (recent messages)
                        message.setCreatedAt(LocalDateTime.now().minusDays(messageCount % 30));
                        
                        parentMessageRepository.save(message);
                        messageCount++;
                    }
                }
            }
            
            log.info("✅ Created {} parent-teacher messages", messageCount);
            
        } catch (Exception e) {
            log.error("❌ Error seeding parent messages: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Clean up duplicate schedules by keeping only one record per unique combination
     */
    private void cleanupDuplicateSchedules() {
        try {
            log.info("🔄 Cleaning up duplicate schedules...");
            
            // Delete duplicate schedules keeping the one with lowest ID
            String deleteDuplicatesSQL = """
                DELETE s1 FROM schedules s1
                INNER JOIN schedules s2 ON 
                    s1.id > s2.id 
                    AND s1.start_time = s2.start_time 
                    AND s1.end_time = s2.end_time
                    AND s1.classroom_id = s2.classroom_id
                    AND s1.subject = s2.subject
                    AND s1.day_of_week = s2.day_of_week
                """;
            
            int deletedCount = jdbcTemplate.update(deleteDuplicatesSQL);
            log.info("✅ Cleaned up {} duplicate schedules", deletedCount);
            
        } catch (Exception e) {
            log.error("❌ Error cleaning up duplicate schedules: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Clean up duplicate timetable events by keeping only one record per unique combination
     */
    private void cleanupDuplicateTimetableEvents() {
        try {
            log.info("🔄 Cleaning up duplicate timetable events...");
            
            // Since we have duplicate data from multiple seeding methods, 
            // let's clear all and re-seed properly
            if (timetableEventRepository.count() > 0) {
                log.info("🗑️ Clearing all existing timetable events to prevent duplicates...");
                timetableEventRepository.deleteAll();
                log.info("✅ Cleared all timetable events");
            } else {
                log.info("✅ No existing timetable events to clean up");
            }
            
        } catch (Exception e) {
            log.error("❌ Error cleaning up duplicate timetable events: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Create sample leave notices for testing teacher leave-notices page
     */
    private void createSampleLeaveNotices(List<Parent> parents, List<User> studentUsers) {
        if (parentLeaveNoticeRepository.count() > 0) {
            log.info("ℹ️ Leave notices already exist, skipping creation");
            return;
        }
        
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime now = LocalDateTime.now();
            
            // Create leave notices for different scenarios
            for (int i = 0; i < Math.min(parents.size(), studentUsers.size()); i++) {
                Parent parent = parents.get(i);
                User student = studentUsers.get(i);
                
                // Leave notice for today (pending)
                ParentLeaveNotice todayNotice = new ParentLeaveNotice();
                todayNotice.setParentId(parent.getId());
                todayNotice.setStudentId(student.getId());
                todayNotice.setDate(today);
                todayNotice.setType(ParentLeaveNotice.NoticeType.FULL_DAY);
                todayNotice.setReasonCode(ParentLeaveNotice.ReasonCode.SICK);
                todayNotice.setNote("Em bị sốt cao, xin phép nghỉ học");
                todayNotice.setStatus(ParentLeaveNotice.NoticeStatus.SENT);
                todayNotice.setCreatedAt(now.minusHours(2));
                todayNotice.setUpdatedAt(now.minusHours(2));
                parentLeaveNoticeRepository.save(todayNotice);
                
                // Leave notice for tomorrow (pending)
                if (i < 2) {
                    ParentLeaveNotice tomorrowNotice = new ParentLeaveNotice();
                    tomorrowNotice.setParentId(parent.getId());
                    tomorrowNotice.setStudentId(student.getId());
                    tomorrowNotice.setDate(today.plusDays(1));
                    tomorrowNotice.setType(ParentLeaveNotice.NoticeType.LATE);
                    tomorrowNotice.setArriveAt(LocalTime.of(9, 30));
                    tomorrowNotice.setReasonCode(ParentLeaveNotice.ReasonCode.EMERGENCY);
                    tomorrowNotice.setNote("Gia đình có việc đột xuất, em sẽ đến trường muộn");
                    tomorrowNotice.setStatus(ParentLeaveNotice.NoticeStatus.DELIVERED);
                    tomorrowNotice.setCreatedAt(now.minusHours(1));
                    tomorrowNotice.setUpdatedAt(now.minusHours(1));
                    parentLeaveNoticeRepository.save(tomorrowNotice);
                }
                
                // Leave notice for yesterday (acknowledged)
                if (i < 3) {
                    ParentLeaveNotice yesterdayNotice = new ParentLeaveNotice();
                    yesterdayNotice.setParentId(parent.getId());
                    yesterdayNotice.setStudentId(student.getId());
                    yesterdayNotice.setDate(today.minusDays(1));
                    yesterdayNotice.setType(ParentLeaveNotice.NoticeType.EARLY);
                    yesterdayNotice.setLeaveAt(LocalTime.of(14, 30));
                    yesterdayNotice.setReasonCode(ParentLeaveNotice.ReasonCode.APPOINTMENT);
                    yesterdayNotice.setNote("Em có lịch khám bác sĩ");
                    yesterdayNotice.setStatus(ParentLeaveNotice.NoticeStatus.ACKNOWLEDGED);
                    yesterdayNotice.setAckAt(now.minusDays(1).plusHours(1));
                    yesterdayNotice.setAckByUserId(1L); // Assume teacher user ID
                    yesterdayNotice.setCreatedAt(now.minusDays(1));
                    yesterdayNotice.setUpdatedAt(now.minusDays(1).plusHours(1));
                    parentLeaveNoticeRepository.save(yesterdayNotice);
                }
            }
            
            log.info("✅ Created {} sample leave notices", parentLeaveNoticeRepository.count());
            
        } catch (Exception e) {
            log.error("❌ Error creating sample leave notices: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate a random birth date before year 2000
     */
    private LocalDate generateBirthDate() {
        Random random = new Random();
        // Generate birth year between 1960 and 1999
        int year = 1960 + random.nextInt(40);
        int month = 1 + random.nextInt(12);
        int day = 1 + random.nextInt(28); // Use 28 to avoid invalid dates
        return LocalDate.of(year, month, day);
    }

    /**
     * Generate a valid 12-digit CCCD number
     * Format: 3-digit province code + 1-digit gender code + 2-digit birth year + 6 random digits
     */
    private String generateCCCD(LocalDate birthDate, String fullName) {
        Random random = new Random();
        
        // Province code for Thanh Hóa: 037
        String provinceCode = "037";
        
        // Gender code: determine from name (simple heuristic)
        String genderCode = determineGenderFromName(fullName) ? "1" : "2"; // 1=male, 2=female
        
        // Birth year code (last 2 digits of birth year)
        String birthYearCode = String.format("%02d", birthDate.getYear() % 100);
        
        // 6 random digits
        String randomDigits = String.format("%06d", random.nextInt(1000000));
        
        return provinceCode + genderCode + birthYearCode + randomDigits;
    }

    /**
     * Simple heuristic to determine gender from Vietnamese name
     */
    private boolean determineGenderFromName(String fullName) {
        if (fullName == null) return true; // default to male
        
        String lowerName = fullName.toLowerCase();
        // Common Vietnamese female name indicators
        String[] femaleIndicators = {"thị", "hà", "hồng", "trang", "ngọc", "linh", "chi", "anh"};
        
        for (String indicator : femaleIndicators) {
            if (lowerName.contains(indicator)) {
                return false; // female
            }
        }
        return true; // default to male
    }
}