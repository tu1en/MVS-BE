package com.classroomapp.classroombackend.config.seed;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.repository.CourseMaterialRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;

@Component // Re-enabled with lazy AssignmentSeeder
@RequiredArgsConstructor
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
    private final CourseMaterialSeeder courseMaterialSeeder;
    private final ExamSeeder examSeeder;
    private final StudentProgressSeeder studentProgressSeeder;
    private final ComprehensiveGradingSeeder comprehensiveGradingSeeder;
    private final AssignmentTestDataSeeder assignmentTestDataSeeder;

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
            lectureSeeder.seed(classrooms);
            assignmentSeeder.seed();
            submissionSeeder.seed();
            blogSeeder.seed();
            accomplishmentSeeder.seed();
            announcementSeeder.seed(classrooms);
            attendanceSeeder.seed();
            examSeeder.seed();
            studentProgressSeeder.seed();

            log.info("============== Main Seeding Complete ==============");
        } else {
            log.info("Database already has users. Skipping main seeding.");
            classrooms = classroomRepository.findAll();
        }

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
    }
} 