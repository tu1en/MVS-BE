package com.classroomapp.classroombackend.config;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.config.seed.ClassroomEnrollmentSeeder;
import com.classroomapp.classroombackend.config.seed.ClassroomSeeder;
import com.classroomapp.classroombackend.config.seed.LectureSeeder;
import com.classroomapp.classroombackend.config.seed.RoleSeeder;
import com.classroomapp.classroombackend.config.seed.UserSeeder;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;

@Component
public class DataSeederRunner implements CommandLineRunner {
    
    private final RoleSeeder roleSeeder;
    private final UserSeeder userSeeder;
    private final ClassroomSeeder classroomSeeder;
    private final ClassroomEnrollmentSeeder enrollmentSeeder;
    private final LectureSeeder lectureSeeder;
    
    public DataSeederRunner(RoleSeeder roleSeeder, UserSeeder userSeeder, 
                           ClassroomSeeder classroomSeeder, ClassroomEnrollmentSeeder enrollmentSeeder, 
                           LectureSeeder lectureSeeder) {
        this.roleSeeder = roleSeeder;
        this.userSeeder = userSeeder;
        this.classroomSeeder = classroomSeeder;
        this.enrollmentSeeder = enrollmentSeeder;
        this.lectureSeeder = lectureSeeder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- Seeding Database ---");
        roleSeeder.seed();
        userSeeder.seed();
        List<Classroom> seededClassrooms = classroomSeeder.seed();

        // Enroll students. This seeder will find users and classrooms on its own.
        enrollmentSeeder.seed();

        // Now create lectures for those classrooms
        lectureSeeder.seed(seededClassrooms);
        System.out.println("--- Seeding Complete ---");
    }
} 