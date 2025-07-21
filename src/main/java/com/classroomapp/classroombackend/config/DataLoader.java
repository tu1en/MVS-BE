package com.classroomapp.classroombackend.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.config.seed.AbsenceSeeder;
import com.classroomapp.classroombackend.config.seed.AccomplishmentSeeder;
import com.classroomapp.classroombackend.config.seed.AnnouncementSeeder;
import com.classroomapp.classroombackend.config.seed.AssignmentSeeder;
import com.classroomapp.classroombackend.config.seed.AttendanceSeeder;
import com.classroomapp.classroombackend.config.seed.BlogSeeder;
import com.classroomapp.classroombackend.config.seed.ClassroomEnrollmentSeeder;
import com.classroomapp.classroombackend.config.seed.ClassroomSeeder;
import com.classroomapp.classroombackend.config.seed.ComprehensiveTableSeeder;
import com.classroomapp.classroombackend.config.seed.CourseMaterialSeeder;
import com.classroomapp.classroombackend.config.seed.CourseSeeder;
import com.classroomapp.classroombackend.config.seed.ExamSeeder;
import com.classroomapp.classroombackend.config.seed.LectureSeeder;
import com.classroomapp.classroombackend.config.seed.MessageSeeder;
import com.classroomapp.classroombackend.config.seed.QuizSeeder;
import com.classroomapp.classroombackend.config.seed.RequestSeeder;
import com.classroomapp.classroombackend.config.seed.RoleSeeder;
import com.classroomapp.classroombackend.config.seed.ScheduleSeeder;
import com.classroomapp.classroombackend.config.seed.StudentProgressSeeder;
import com.classroomapp.classroombackend.config.seed.SubmissionSeeder;
import com.classroomapp.classroombackend.config.seed.SystemConfigSeeder;
import com.classroomapp.classroombackend.config.seed.SystemRoleSeeder;
import com.classroomapp.classroombackend.config.seed.TeachingHistorySeeder;
import com.classroomapp.classroombackend.config.seed.TimetableEventSeeder;
import com.classroomapp.classroombackend.config.seed.UserSeeder;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;

import lombok.RequiredArgsConstructor;

@Component
@Profile("local")
@Order(2) // Run after DatabaseCleanupService
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final RoleSeeder roleSeeder;
    private final UserSeeder userSeeder;
    private final CourseSeeder courseSeeder;
    private final ClassroomSeeder classroomSeeder;
    private final ClassroomEnrollmentSeeder enrollmentSeeder;
    private final LectureSeeder lectureSeeder;
    private final CourseMaterialSeeder courseMaterialSeeder;
    private final AssignmentSeeder assignmentSeeder;
    private final SubmissionSeeder submissionSeeder;
    private final ExamSeeder examSeeder;
    private final AttendanceSeeder attendanceSeeder;
    private final ScheduleSeeder scheduleSeeder;
    private final BlogSeeder blogSeeder;
    private final MessageSeeder messageSeeder;
    private final StudentProgressSeeder studentProgressSeeder;
    private final TimetableEventSeeder timetableEventSeeder;
    private final AccomplishmentSeeder accomplishmentSeeder;
    private final AbsenceSeeder absenceSeeder;
    private final AnnouncementSeeder announcementSeeder;
    private final RequestSeeder requestSeeder;
    private final QuizSeeder quizSeeder;
    private final SystemConfigSeeder systemConfigSeeder;
    private final SystemRoleSeeder systemRoleSeeder;
    private final TeachingHistorySeeder teachingHistorySeeder;
    private final ComprehensiveTableSeeder comprehensiveTableSeeder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("🌱 [DataLoader] Starting comprehensive data seeding...");
        
        // Core entities first
        roleSeeder.seed();
        systemRoleSeeder.seed();
        userSeeder.seed();
        courseSeeder.seed();
        List<Classroom> classrooms = classroomSeeder.seed();
        enrollmentSeeder.seed();
        
        // Course content and structure
        lectureSeeder.seed(classrooms);
        courseMaterialSeeder.seed(classrooms);
        scheduleSeeder.seed();
        
        // Assessment and activities
        assignmentSeeder.seed();
        submissionSeeder.seed();
        examSeeder.seed();
        
        // Attendance and tracking
        attendanceSeeder.seed();
        teachingHistorySeeder.seed(); // ✅ Teaching History Seeder for create-drop restart
        studentProgressSeeder.seed();
        
        // Communication and events
        blogSeeder.seed();
        messageSeeder.seed();
        announcementSeeder.seed();
        timetableEventSeeder.seed();
        
        // HR and admin
        accomplishmentSeeder.seed();
        absenceSeeder.seed();
        requestSeeder.seed();
     //   igSeeder.seed();
        
        // Comprehensive seeding cuối cùng
        comprehensiveTableSeeder.seed();
        
        System.out.println("✅ [DataLoader] Comprehensive data seeding completed - Target: 0 empty tables!");
    }
}
