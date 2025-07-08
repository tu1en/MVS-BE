package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Accomplishment;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.AccomplishmentRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class AccomplishmentSeeder {

    @Autowired
    private AccomplishmentRepository accomplishmentRepository;

    @Autowired
    private UserRepository userRepository;

    public void seed() {
        if (accomplishmentRepository.count() == 0) {
            // Get the student users we created
            User student1 = userRepository.findByEmail("student@test.com")
                    .orElseThrow(() -> new RuntimeException("Student user not found"));

            User student2 = userRepository.findByEmail("student2@test.com")
                    .orElseThrow(() -> new RuntimeException("Student2 user not found"));


            // Accomplishments for student1
            Accomplishment math = new Accomplishment();
            math.setStudent(student1);
            math.setCourseTitle("Advanced Mathematics");
            math.setSubject("Mathematics");
            math.setTeacherName("Dr. John Smith");
            math.setGrade(85.5);
            math.setCompletionDate(LocalDate.now().minusDays(30));
            accomplishmentRepository.save(math);

            Accomplishment physics = new Accomplishment();
            physics.setStudent(student1);
            physics.setCourseTitle("Classical Physics");
            physics.setSubject("Physics");
            physics.setTeacherName("Prof. Jane Doe");
            physics.setGrade(92.0);
            physics.setCompletionDate(LocalDate.now().minusDays(15));
            accomplishmentRepository.save(physics);

            // Accomplishments for student2
            Accomplishment programming = new Accomplishment();
            programming.setStudent(student2);
            programming.setCourseTitle("Java Programming");
            programming.setSubject("Computer Science");
            programming.setTeacherName("Mr. Bob Wilson");
            programming.setGrade(88.5);
            programming.setCompletionDate(LocalDate.now().minusDays(7));
            accomplishmentRepository.save(programming);

            System.out.println("✅ [AccomplishmentSeeder] Created 3 sample accomplishments.");
        }
    }
} 