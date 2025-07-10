package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Accomplishment;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AccomplishmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class AccomplishmentSeeder {

    @Autowired
    private AccomplishmentRepository accomplishmentRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;

    private final Random random = new Random();

    public void seed() {
        if (accomplishmentRepository.count() > 0) {
            System.out.println("✅ [AccomplishmentSeeder] Accomplishments already seeded.");
            return;
        }

        List<User> students = userRepository.findByRoleId(1); // STUDENT role
        List<Classroom> classrooms = classroomRepository.findAll();

        if (students.isEmpty() || classrooms.isEmpty()) {
            System.out.println("⚠️ [AccomplishmentSeeder] No students or classrooms found. Skipping.");
            return;
        }
        
        System.out.println("🔄 [AccomplishmentSeeder] Seeding accomplishments...");

        int accomplishmentCount = 0;
        // Seed accomplishments for up to 5 students
        for (int i = 0; i < Math.min(5, students.size()); i++) {
            User student = students.get(i);
            
            // Give each student 2-4 accomplishments
            int numAccomplishments = 2 + random.nextInt(3); // 2 to 4
            for (int j = 0; j < numAccomplishments; j++) {
                // Pick a random classroom to represent a completed course
                Classroom classroom = classrooms.get(random.nextInt(classrooms.size()));
                
                Accomplishment accomplishment = new Accomplishment();
                accomplishment.setStudent(student);
                accomplishment.setCourseTitle(classroom.getName());
                accomplishment.setSubject(classroom.getSubject());
                accomplishment.setTeacherName(classroom.getTeacher() != null ? classroom.getTeacher().getFullName() : "N/A");
                accomplishment.setGrade(65.0 + random.nextDouble() * 35.0); // Grade between 65.0 and 100.0
                accomplishment.setCompletionDate(LocalDate.now().minusDays(30 + random.nextInt(300))); // Completed in the last year
                
                accomplishmentRepository.save(accomplishment);
                accomplishmentCount++;
            }
        }
        System.out.println("✅ [AccomplishmentSeeder] Created " + accomplishmentCount + " sample accomplishments.");
    }
} 