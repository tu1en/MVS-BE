package com.classroomapp.classroombackend.config.seed;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.Course;
import com.classroomapp.classroombackend.model.usermanagement.Role;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.CourseRepository;
import com.classroomapp.classroombackend.repository.usermanagement.RoleRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
@Transactional
public class ClassroomSeeder {

    @Autowired
    private ClassroomRepository classroomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CourseRepository courseRepository;

    public List<Classroom> seed() {
        if (classroomRepository.count() > 0) {
            System.out.println("✅ [ClassroomSeeder] Classrooms already exist, skipping seeding.");
            return classroomRepository.findAll();
        }

        System.out.println("🔄 [ClassroomSeeder] Seeding classrooms...");

        // Ensure TEACHER role exists
        Role teacherRole = roleRepository.findById(2).orElseGet(() -> {
            Role r = new Role();
            r.setId(2);
            r.setName("TEACHER");
            return roleRepository.save(r);
        });

        // Ensure at least 2 teacher users
        List<User> teachers = userRepository.findByRoleId(teacherRole.getId());
        if (teachers.size() < 2) {
            System.out.println("⚠️ [ClassroomSeeder] Not enough teachers found. Auto-seeding test teachers...");
            int needed = 2 - teachers.size();
            for (int i = 0; i < needed; i++) {
                User teacher = new User();
                teacher.setFullName("Teacher Demo " + (i + 1));
                teacher.setUsername("teacher_demo" + (i + 1));
                teacher.setPassword("123456"); // NOTE: hash in real apps
                teacher.setEmail("teacher_demo" + (i + 1) + "@example.com");
                teacher.setRoleId(teacherRole.getId());
                teacher.setStatus("active");
                teacher = userRepository.save(teacher);
                teachers.add(teacher);
            }
        }

        // Ensure at least 1 course exists
        List<Course> courses = courseRepository.findAll();
        if (courses.isEmpty()) {
            Course course = new Course();
            course.setName("Course Demo");
            course.setDescription("Demo Course For Classroom Seeder");
            course = courseRepository.save(course);
            courses.add(course);
            System.out.println("⚠️ [ClassroomSeeder] Auto-seeded a demo course.");
        }

        // Seed classrooms
        List<Classroom> classrooms = new ArrayList<>();

        Classroom classroom1 = new Classroom();
        classroom1.setName("Lập trình Java cơ bản - Kỳ 3");
        classroom1.setSubject("Khoa học máy tính");
        classroom1.setTeacher(teachers.get(0));
        classroom1.setCourseId(courses.get(0).getId());
        classrooms.add(classroomRepository.save(classroom1));
        System.out.println("✅ [ClassroomSeeder] Created classroom: " + classroom1.getName());

        Classroom classroom2 = new Classroom();
        classroom2.setName("Toán rời rạc - Kỳ 2");
        classroom2.setSubject("Toán học nâng cao");
        classroom2.setTeacher(teachers.get(1));
        classroom2.setCourseId(courses.get(0).getId());
        classrooms.add(classroomRepository.save(classroom2));
        System.out.println("✅ [ClassroomSeeder] Created classroom: " + classroom2.getName());

        return classrooms;
    }
}
