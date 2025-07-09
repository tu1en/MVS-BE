package com.classroomapp.classroombackend.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.config.seed.AnnouncementSeeder;
import com.classroomapp.classroombackend.config.seed.AssignmentSeeder;
import com.classroomapp.classroombackend.config.seed.ClassroomSeeder;
import com.classroomapp.classroombackend.config.seed.CourseSeeder;
import com.classroomapp.classroombackend.config.seed.LectureSeeder;
import com.classroomapp.classroombackend.config.seed.RoleSeeder;
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
    private final LectureSeeder lectureSeeder;
    private final AssignmentSeeder assignmentSeeder;
    private final AnnouncementSeeder announcementSeeder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("🌱 [DataLoader] Starting data seeding...");
        
        roleSeeder.seed();
        userSeeder.seed();
        courseSeeder.seed();
        List<Classroom> classrooms = classroomSeeder.seed();
        lectureSeeder.seed(classrooms);
        assignmentSeeder.seed(); // Re-enabled with @Lazy dependency
        announcementSeeder.seed(classrooms);

        System.out.println("✅ [DataLoader] Data seeding completed.");
    }
} 