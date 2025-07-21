package com.classroomapp.classroombackend.config.seed;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;

/**
 * Simple DatabaseSeeder to ensure LectureSeeder gets called with classrooms
 * This can be used as an alternative to DataLoader if needed
 */
@Configuration
@Profile("dev") // Use a different profile to avoid conflicts
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(
            ClassroomRepository classroomRepository, 
            ClassroomSeeder classroomSeeder, 
            LectureSeeder lectureSeeder) {
        return args -> {
            System.out.println("🌱 [DatabaseSeeder] Starting simple database seeding...");

            // Step 1: Ensure classrooms exist
            List<Classroom> classrooms = classroomRepository.findAll();
            if (classrooms.isEmpty()) {
                System.out.println("📚 [DatabaseSeeder] No classrooms found. Creating sample classrooms...");
                classrooms = classroomSeeder.seed();
            } else {
                System.out.println("✅ [DatabaseSeeder] Found " + classrooms.size() + " existing classrooms");
            }

            // Step 2: Seed lectures for classrooms
            if (!classrooms.isEmpty()) {
                System.out.println("📖 [DatabaseSeeder] Seeding lectures for " + classrooms.size() + " classrooms...");
                lectureSeeder.seed(classrooms);
            } else {
                System.out.println("⚠️ [DatabaseSeeder] No classrooms available for lecture seeding");
            }

            System.out.println("✅ [DatabaseSeeder] Simple database seeding completed successfully!");
        };
    }
}
