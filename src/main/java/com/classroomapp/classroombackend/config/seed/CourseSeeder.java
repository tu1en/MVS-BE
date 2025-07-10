package com.classroomapp.classroombackend.config.seed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.classroommanagement.Course;
import com.classroomapp.classroombackend.repository.classroommanagement.CourseRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CourseSeeder {

    @Autowired
    private CourseRepository courseRepository;

    public void seed() {
        if (courseRepository.count() == 0) {
            Course math = new Course();
            math.setName("Advanced Mathematics");
            math.setDescription("A comprehensive study of mathematical concepts and their applications.");
            courseRepository.save(math);

            Course history = new Course();
            history.setName("World History");
            history.setDescription("A survey of major historical events from ancient civilizations to the modern era.");
            courseRepository.save(history);

            Course literature = new Course();
            literature.setName("Vietnamese Literature");
            literature.setDescription("An exploration of Vietnamese literary works throughout history.");
            courseRepository.save(literature);

            Course english = new Course();
            english.setName("Communicative English");
            english.setDescription("Developing English communication skills for an international environment.");
            courseRepository.save(english);

            Course cs = new Course();
            cs.setName("Computer Science");
            cs.setDescription("Fundamental concepts of computer science and programming.");
            courseRepository.save(cs);

            Course physics = new Course();
            physics.setName("General Physics");
            physics.setDescription("An introduction to the fundamental principles of physics.");
            courseRepository.save(physics);

            System.out.println("✅ [CourseSeeder] Created 6 sample courses.");
        }
    }
} 