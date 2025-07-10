package com.classroomapp.classroombackend.config.seed;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.CourseRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
@Transactional
public class ClassroomSeeder {

    @Autowired
    private ClassroomRepository classroomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;

    public List<Classroom> seed() {
        if (classroomRepository.count() > 0) {
            System.out.println("✅ [ClassroomSeeder] Classrooms already exist, skipping seeding.");
            return classroomRepository.findAll();
        }

        System.out.println("🔄 [ClassroomSeeder] Seeding classrooms...");

        // Find teachers by Role ID (assuming 2 is the ID for TEACHER)
        List<User> teachers = userRepository.findByRoleId(Integer.valueOf(2));

        if (teachers.isEmpty()) {
            System.out.println("⚠️ [ClassroomSeeder] No teachers found (Role ID 2). Cannot create classrooms.");
            return new ArrayList<>();
        }

        List<Classroom> classrooms = new ArrayList<>();

        Classroom classroom1 = new Classroom();
        classroom1.setName("Lập trìnhh Java cơ bản - Kỳ 3");
        classroom1.setTeacher(teachers.get(0));
        classrooms.add(classroomRepository.save(classroom1));
        System.out.println("✅ [ClassroomSeeder] Created classroom: " + classroom1.getName());


        Classroom classroom2 = new Classroom();
        classroom2.setName("Toán rời rạc - Kỳ 2");
        classroom2.setTeacher(teachers.size() > 1 ? teachers.get(1) : teachers.get(0));
        classrooms.add(classroomRepository.save(classroom2));
        System.out.println("✅ [ClassroomSeeder] Created classroom: " + classroom2.getName());

        return classrooms;
    }
} 