package com.classroomapp.classroombackend.config.seed;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.classroomapp.classroombackend.model.Course;
import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.CourseRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

@Component
public class ClassroomSeeder {

    @Autowired
    private ClassroomRepository classroomRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;

    public List<Classroom> seed() {
        List<Classroom> createdClassrooms = new ArrayList<>();
        if (classroomRepository.count() == 0) {
            List<User> teachers = userRepository.findByRoleId(2); // Assuming role ID 2 is for TEACHER
            List<Course> courses = courseRepository.findAll();

            if (teachers.isEmpty() || courses.isEmpty()) {
                System.out.println("⚠️ [ClassroomSeeder] Not enough teachers or courses to seed classrooms. Skipping.");
                return createdClassrooms;
            }
            
            // Find specific teachers by email
            User mainTeacher = userRepository.findByEmail("teacher@test.com").orElse(null);
            User mathTeacher = userRepository.findByEmail("math@test.com").orElse(null);
            User litTeacher = userRepository.findByEmail("literature@test.com").orElse(null);
            User engTeacher = userRepository.findByEmail("english@test.com").orElse(null);
            
            if (mainTeacher == null) {
                System.out.println("⚠️ [ClassroomSeeder] Main teacher account ('teacher@test.com') not found.");
                // Fallback to the first available teacher if the main one isn't there
                if (!teachers.isEmpty()) {
                    mainTeacher = teachers.get(0);
                    System.out.println("✅ [ClassroomSeeder] Using fallback teacher: " + mainTeacher.getEmail());
                } else {
                    System.out.println("❌ [ClassroomSeeder] No teachers found at all. Cannot seed classrooms.");
                    return createdClassrooms;
                }
            } else {
                System.out.println("✅ [ClassroomSeeder] Found main teacher account: " + mainTeacher.getEmail());
            }
            
            // Check for specialized teachers
            if (mathTeacher == null) System.out.println("⚠️ [ClassroomSeeder] Math teacher ('math@test.com') not found.");
            if (litTeacher == null) System.out.println("⚠️ [ClassroomSeeder] Literature teacher ('literature@test.com') not found.");
            if (engTeacher == null) System.out.println("⚠️ [ClassroomSeeder] English teacher ('english@test.com') not found.");

            Course generalCourse = courses.isEmpty() ? null : courses.get(0);
            
            // Create Math class
            Classroom mathClass = new Classroom();
            mathClass.setName("Toán cao cấp A1");
            mathClass.setDescription("Lớp toán cao cấp cho kỳ 1, bao gồm giải tích, đại số tuyến tính.");
            mathClass.setSection("A");
            mathClass.setSubject("Mathematics");
            mathClass.setTeacher(mathTeacher != null ? mathTeacher : mainTeacher);
            mathClass.setCourseId(generalCourse != null ? generalCourse.getId() : 1L);
            createdClassrooms.add(classroomRepository.save(mathClass));
            
            // Create Literature class
            Classroom litClass = new Classroom();
            litClass.setName("Văn học Việt Nam");
            litClass.setDescription("Tìm hiểu về các tác phẩm văn học Việt Nam qua các thời kỳ.");
            litClass.setSection("B");
            litClass.setSubject("Literature");
            litClass.setTeacher(litTeacher != null ? litTeacher : mainTeacher);
            litClass.setCourseId(generalCourse != null ? generalCourse.getId() : 1L);
            createdClassrooms.add(classroomRepository.save(litClass));
            
            // Create English class
            Classroom engClass = new Classroom();
            engClass.setName("Tiếng Anh giao tiếp");
            engClass.setDescription("Rèn luyện kỹ năng giao tiếp tiếng Anh trong môi trường quốc tế.");
            engClass.setSection("C");
            engClass.setSubject("English");
            engClass.setTeacher(engTeacher != null ? engTeacher : mainTeacher);
            engClass.setCourseId(generalCourse != null ? generalCourse.getId() : 1L);
            createdClassrooms.add(classroomRepository.save(engClass));

            // Also create some classes assigned to the main teacher account
            Classroom teacherClass1 = new Classroom();
            teacherClass1.setName("Công nghệ thông tin cơ bản");
            teacherClass1.setDescription("Khái niệm cơ bản về CNTT và ứng dụng.");
            teacherClass1.setSection("D");
            teacherClass1.setSubject("Computer Science");
            teacherClass1.setTeacher(mainTeacher);
            teacherClass1.setCourseId(generalCourse != null ? generalCourse.getId() : 1L);
            createdClassrooms.add(classroomRepository.save(teacherClass1));
            
            Classroom teacherClass2 = new Classroom();
            teacherClass2.setName("Lập trình Java cơ bản");
            teacherClass2.setDescription("Học lập trình hướng đối tượng với Java.");
            teacherClass2.setSection("E");
            teacherClass2.setSubject("Computer Science");
            teacherClass2.setTeacher(mainTeacher);
            teacherClass2.setCourseId(generalCourse != null ? generalCourse.getId() : 1L);
            createdClassrooms.add(classroomRepository.save(teacherClass2));

            System.out.println("✅ [ClassroomSeeder] Created Math, Literature, English classes and assigned to specific teachers");
            if (mainTeacher != null) {
                System.out.println("✅ [ClassroomSeeder] Created additional classes for main teacher account: " + mainTeacher.getEmail());
            }
        } else {
            System.out.println("✅ [ClassroomSeeder] Classrooms already seeded");
            return classroomRepository.findAll();
        }
        return createdClassrooms;
    }
} 