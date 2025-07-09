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

            System.out.println("✅ [CourseSeeder] Created 6 sample courses.");
        }
    }
} 