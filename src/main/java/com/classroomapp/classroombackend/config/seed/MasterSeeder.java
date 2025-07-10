package com.classroomapp.classroombackend.config.seed;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.CourseMaterialRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@Order(2) // Run after DatabaseCleanupService
@RequiredArgsConstructor
@DependsOn("entityManagerFactory") // Wait for JPA to be initialized
public class MasterSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(MasterSeeder.class);

    private final UserRepository userRepository;
    private final CourseMaterialRepository courseMaterialRepository;
    private final ClassroomRepository classroomRepository;
    private final UserSeeder userSeeder;
    private final CourseSeeder courseSeeder;
    private final ClassroomSeeder classroomSeeder;
    private final ClassroomEnrollmentSeeder classroomEnrollmentSeeder;
    private final ScheduleSeeder scheduleSeeder;
    private final LectureSeeder lectureSeeder;
    private final AssignmentSeeder assignmentSeeder;
    private final BlogSeeder blogSeeder;
    private final SubmissionSeeder submissionSeeder;
    private final AccomplishmentSeeder accomplishmentSeeder;
    private final AnnouncementSeeder announcementSeeder;
    private final AttendanceSeeder attendanceSeeder;
    private final MessageSeeder messageSeeder;
    private final TeachingHistorySeeder teachingHistorySeeder;
    private final CourseMaterialSeeder courseMaterialSeeder;
    private final ExamSeeder examSeeder;
    private final StudentProgressSeeder studentProgressSeeder;
    private final ComprehensiveGradingSeeder comprehensiveGradingSeeder;
    private final AssignmentTestDataSeeder assignmentTestDataSeeder;
    private final DatabaseVerificationSeeder databaseVerificationSeeder;
    private final DataVerificationSeeder dataVerificationSeeder;
    private final TimetableEventSeeder timetableEventSeeder;
    private final RequestSeeder requestSeeder;


    @Override
    @Transactional
    public void run(String... args) throws Exception {
        List<Classroom> classrooms;
        if (userRepository.count() == 0) {
            log.info("============== Seeding Database ==============");

            userSeeder.seed();
            courseSeeder.seed();
            classrooms = classroomSeeder.seed();
            classroomEnrollmentSeeder.seed();
            scheduleSeeder.seed();
            timetableEventSeeder.seed(); // Seed timetable events
            requestSeeder.seed(); // Seed role requests

            log.info("============== Starting Lecture Seeding ==============");
            lectureSeeder.seed(classrooms);
            log.info("============== Lecture Seeding Complete ==============");

            assignmentSeeder.seed();
            submissionSeeder.seed();
            blogSeeder.seed();
            accomplishmentSeeder.seed();
            announcementSeeder.seed();
            attendanceSeeder.seed();
            messageSeeder.seed();
            teachingHistorySeeder.seed();
            examSeeder.seed();
            studentProgressSeeder.seed();

            log.info("============== Main Seeding Complete ==============");
        } else {
            log.info("Database already has users. Skipping main seeding.");
            classrooms = classroomRepository.findAll();
        }

        // Always verify database state
        databaseVerificationSeeder.verify();

        // Additional verification for the specific issue
        verifyUserRoleAssignments();

        // Run comprehensive data verification
        dataVerificationSeeder.verifyDataIntegrity();
        dataVerificationSeeder.diagnoseStudentTeacherIssue();

        // Always run the submission seeder to add new test data
        log.info("============== Checking for new submissions to seed ==============");
        submissionSeeder.seed();
        log.info("============== Submission seeding complete ==============");

        // Always run comprehensive grading seeder for classroom 54
        log.info("============== Seeding Comprehensive Grading Data ==============");
        assignmentTestDataSeeder.seedAssignmentTestData();
        comprehensiveGradingSeeder.seedGradingData();
        log.info("============== Comprehensive Grading Seeding Complete ==============");

        if (courseMaterialRepository.count() == 0) {
            log.info("============== Seeding Course Materials ==============");
            if (classrooms.isEmpty()) {
                log.info("No classrooms found to seed materials for.");
            } else {
                courseMaterialSeeder.seed(classrooms);
            }
            log.info("============== Course Materials Seeding Complete ==============");
        } else {
            log.info("Course materials already seeded. Skipping.");
        }
        
        // Không gọi lại scheduleSeeder.seed() để tránh xung đột
        log.info("============== Checking Schedule Status ==============");
        log.info("Schedules are already seeded in the main seeding process if needed.");
        log.info("============== Schedule Status Check Complete ==============");
        
        // Always run the classroom enrollment seeder to ensure students are in classrooms
        log.info("============== Forcing Classroom Enrollment Seeding ==============");
        classroomEnrollmentSeeder.seed();
        log.info("============== Classroom Enrollment Seeding Complete ==============");
    }

    /**
     * Verify user role assignments to debug the student/teacher issue
     */
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