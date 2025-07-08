package com.classroomapp.classroombackend.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.config.seed.AccomplishmentSeeder;
import com.classroomapp.classroombackend.config.seed.AnnouncementSeeder;
import com.classroomapp.classroombackend.config.seed.AssignmentSeeder;
import com.classroomapp.classroombackend.config.seed.AttendanceSeeder;
import com.classroomapp.classroombackend.config.seed.BlogSeeder;
import com.classroomapp.classroombackend.config.seed.ClassroomEnrollmentSeeder;
import com.classroomapp.classroombackend.config.seed.ClassroomSeeder;
import com.classroomapp.classroombackend.config.seed.CourseSeeder;
import com.classroomapp.classroombackend.config.seed.LectureSeeder;
import com.classroomapp.classroombackend.config.seed.MessageSeeder;
import com.classroomapp.classroombackend.config.seed.RequestSeeder;
import com.classroomapp.classroombackend.config.seed.RoleSeeder;
import com.classroomapp.classroombackend.config.seed.ScheduleSeeder;
import com.classroomapp.classroombackend.config.seed.SubmissionSeeder;
import com.classroomapp.classroombackend.config.seed.TimetableEventSeeder;
import com.classroomapp.classroombackend.config.seed.UserSeeder;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.repository.AccomplishmentRepository;
import com.classroomapp.classroombackend.repository.AnnouncementRepository;
import com.classroomapp.classroombackend.repository.BlogRepository;
import com.classroomapp.classroombackend.repository.CourseRepository;
import com.classroomapp.classroombackend.repository.LectureRepository;
import com.classroomapp.classroombackend.repository.ScheduleRepository;
import com.classroomapp.classroombackend.repository.StudentMessageRepository;
import com.classroomapp.classroombackend.repository.TimetableEventRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.AssignmentRepository;
import com.classroomapp.classroombackend.repository.assignmentmanagement.SubmissionRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceRepository;
import com.classroomapp.classroombackend.repository.attendancemanagement.AttendanceSessionRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.requestmanagement.RequestRepository;
import com.classroomapp.classroombackend.repository.usermanagement.RoleRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class DataLoader implements CommandLineRunner {

    // Repositories for clearing data
    private final AccomplishmentRepository accomplishmentRepository;
    private final RequestRepository requestRepository;
    private final BlogRepository blogRepository;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final TimetableEventRepository timetableEventRepository;
    private final AnnouncementRepository announcementRepository;
    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final ClassroomEnrollmentRepository classroomEnrollmentRepository;
    private final StudentMessageRepository studentMessageRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final RoleRepository roleRepository;
    private final CourseRepository courseRepository;

    // Seeder services
    private final UserSeeder userSeeder;
    private final ClassroomSeeder classroomSeeder;
    private final ClassroomEnrollmentSeeder classroomEnrollmentSeeder;
    private final BlogSeeder blogSeeder;
    private final AssignmentSeeder assignmentSeeder;
    private final SubmissionSeeder submissionSeeder;
    private final AccomplishmentSeeder accomplishmentSeeder;
    private final RequestSeeder requestSeeder;
    private final ScheduleSeeder scheduleSeeder;
    private final AttendanceSeeder attendanceSeeder;
    private final MessageSeeder messageSeeder;
    private final TimetableEventSeeder timetableEventSeeder;
    private final LectureSeeder lectureSeeder;
    private final RoleSeeder roleSeeder;
    private final AnnouncementSeeder announcementSeeder;
    private final CourseSeeder courseSeeder;

    public DataLoader(AccomplishmentRepository accomplishmentRepository, RequestRepository requestRepository, BlogRepository blogRepository, SubmissionRepository submissionRepository, AssignmentRepository assignmentRepository, ScheduleRepository scheduleRepository, TimetableEventRepository timetableEventRepository, AnnouncementRepository announcementRepository, AttendanceRepository attendanceRepository, AttendanceSessionRepository attendanceSessionRepository, ClassroomEnrollmentRepository classroomEnrollmentRepository, StudentMessageRepository studentMessageRepository, ClassroomRepository classroomRepository, UserRepository userRepository, RoleRepository roleRepository, UserSeeder userSeeder, ClassroomSeeder classroomSeeder, ClassroomEnrollmentSeeder classroomEnrollmentSeeder, BlogSeeder blogSeeder, AssignmentSeeder assignmentSeeder, SubmissionSeeder submissionSeeder, AccomplishmentSeeder accomplishmentSeeder, RequestSeeder requestSeeder, ScheduleSeeder scheduleSeeder, AttendanceSeeder attendanceSeeder, MessageSeeder messageSeeder, TimetableEventSeeder timetableEventSeeder, LectureRepository lectureRepository, LectureSeeder lectureSeeder, RoleSeeder roleSeeder, AnnouncementSeeder announcementSeeder, CourseSeeder courseSeeder, CourseRepository courseRepository) {
        this.accomplishmentRepository = accomplishmentRepository;
        this.requestRepository = requestRepository;
        this.blogRepository = blogRepository;
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.scheduleRepository = scheduleRepository;
        this.timetableEventRepository = timetableEventRepository;
        this.announcementRepository = announcementRepository;
        this.attendanceRepository = attendanceRepository;
        this.attendanceSessionRepository = attendanceSessionRepository;
        this.classroomEnrollmentRepository = classroomEnrollmentRepository;
        this.studentMessageRepository = studentMessageRepository;
        this.classroomRepository = classroomRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userSeeder = userSeeder;
        this.classroomSeeder = classroomSeeder;
        this.classroomEnrollmentSeeder = classroomEnrollmentSeeder;
        this.blogSeeder = blogSeeder;
        this.assignmentSeeder = assignmentSeeder;
        this.submissionSeeder = submissionSeeder;
        this.accomplishmentSeeder = accomplishmentSeeder;
        this.requestSeeder = requestSeeder;
        this.scheduleSeeder = scheduleSeeder;
        this.attendanceSeeder = attendanceSeeder;
        this.messageSeeder = messageSeeder;
        this.timetableEventSeeder = timetableEventSeeder;
        this.lectureRepository = lectureRepository;
        this.lectureSeeder = lectureSeeder;
        this.roleSeeder = roleSeeder;
        this.announcementSeeder = announcementSeeder;
        this.courseSeeder = courseSeeder;
        this.courseRepository = courseRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("console.encoding", "UTF-8");
            System.setProperty("sun.jnu.encoding", "UTF-8");
            System.setProperty("java.awt.headless", "true");

            System.out.println("🔤 DataLoader: Setting UTF-8 character encoding for Vietnamese");

            // NEVER reset data on application startup
            // Check if data already exists
            if (userRepository.count() > 0) {
                System.out.println("✅ DataLoader: Data already exists, skipping data reset and seeding.");
                return;
            }

            // Only seed data if no users exist
            System.out.println("🌱 DataLoader: No existing data found, performing initial data seeding...");

            //--- Seeding data in order of dependencies ---
            roleSeeder.seed();
            userSeeder.seed();
            courseSeeder.seed();
            List<Classroom> classrooms = classroomSeeder.seed();
            classroomEnrollmentSeeder.seed();
            blogSeeder.seed();
            assignmentSeeder.seed();
            submissionSeeder.seed();
            accomplishmentSeeder.seed();
            requestSeeder.seed();
            scheduleSeeder.seed();
            attendanceSeeder.seed();
            messageSeeder.seed();
            timetableEventSeeder.seed();
            lectureSeeder.seed(classrooms);
            announcementSeeder.seed(classrooms);

            System.out.println("✅ DataLoader: All data has been seeded successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error in DataLoader: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // This method is now private and only called during initial setup
    private void clearAllData() {
        System.out.println("🗑️ DataLoader: Clearing all existing data...");
        // Clear data in reverse order of dependencies to avoid foreign key constraints
        accomplishmentRepository.deleteAll();
        requestRepository.deleteAll();
        blogRepository.deleteAll();
        submissionRepository.deleteAll();
        assignmentRepository.deleteAll();
        
        // ------ Fix FK constraint: attendance_sessions -> lectures ------
        // 1️⃣  Remove attendance records first (they reference sessions)
        attendanceRepository.deleteAll();

        // 2️⃣  Then remove attendance sessions (they reference lectures)
        attendanceSessionRepository.deleteAll();

        // 3️⃣  Now we can safely delete lectures (no attendance_sessions referencing them)
        if (lectureRepository != null) {
            lectureRepository.deleteAll();
        }
        
        // Continue clearing remaining dependent data in safe order
        scheduleRepository.deleteAll();
        timetableEventRepository.deleteAll();
        announcementRepository.deleteAll();
        classroomEnrollmentRepository.deleteAll();
        studentMessageRepository.deleteAll();
        
        // Classrooms can now be deleted as enrollments are gone
        classroomRepository.deleteAll();
        
        // Courses can be deleted after classrooms
        courseRepository.deleteAll();
        
        // Finally, delete users as they are at the top of the hierarchy
        userRepository.deleteAll();
        roleRepository.deleteAll();
        
        System.out.println("✅ DataLoader: All existing data cleared successfully!");
    }
} 