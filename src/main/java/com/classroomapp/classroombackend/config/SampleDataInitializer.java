package com.classroomapp.classroombackend.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

// @Component
public class SampleDataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ClassroomRepository classroomRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only initialize data if database is empty
        if (classroomRepository.count() == 0) {
            initializeSampleData();
        }
    }

    private void initializeSampleData() {
        System.out.println("🔄 Initializing sample classroom data...");
        
        // Get existing users
        List<User> teachers = userRepository.findByRoleId(2); // Teachers
        List<User> students = userRepository.findByRoleId(1); // Students
        
        if (teachers.isEmpty() || students.isEmpty()) {
            System.out.println("⚠️  No teachers or students found. Creating additional users...");
            createAdditionalUsers();
            teachers = userRepository.findByRoleId(2);
            students = userRepository.findByRoleId(1);
        }
        
        // Create basic classrooms only
        createBasicClassrooms(teachers);
        
        System.out.println("✅ Sample data initialization completed!");
    }
    
    private void createAdditionalUsers() {
        // Check and create teacher user if it doesn't exist
        if (!userRepository.findByUsername("teacher").isPresent()) {
            User teacher1 = new User();
            teacher1.setUsername("teacher");
            teacher1.setPassword(passwordEncoder.encode("teacher123"));
            teacher1.setEmail("teacher@classroom.com");
            teacher1.setFullName("Thầy Nguyễn Văn Anh");
            teacher1.setRoleId(2);
            teacher1.setDepartment("Khoa Toán");
            teacher1.setStatus("active");
            teacher1.setCreatedAt(LocalDateTime.now());
            teacher1.setHireDate(LocalDate.of(2019, 8, 15));
            userRepository.save(teacher1);
            System.out.println("Created teacher user: teacher");
        }

        // Create second teacher
        User teacher2 = new User();
        teacher2.setUsername("teacher2");
        teacher2.setPassword(passwordEncoder.encode("teacher123"));
        teacher2.setEmail("teacher2@classroom.com");
        teacher2.setFullName("Cô Nguyễn Thị Mai");
        teacher2.setRoleId(2);
        teacher2.setDepartment("Khoa Toán");
        teacher2.setStatus("active");
        teacher2.setCreatedAt(LocalDateTime.now());
        teacher2.setHireDate(LocalDate.of(2020, 9, 1));
        userRepository.save(teacher2);
        
        // Check and create student user if it doesn't exist
        if (!userRepository.findByUsername("student").isPresent()) {
            User student1 = new User();
            student1.setUsername("student");
            student1.setPassword(passwordEncoder.encode("student123"));
            student1.setEmail("student@classroom.com");
            student1.setFullName("Học sinh A");
            student1.setRoleId(1);
            student1.setStatus("active");
            student1.setCreatedAt(LocalDateTime.now());
            student1.setEnrollmentDate(LocalDate.of(2024, 9, 1));
            userRepository.save(student1);
            System.out.println("Created student user: student");
        }
        
        // Create additional students
        User student2 = new User();
        student2.setUsername("student2");
        student2.setPassword(passwordEncoder.encode("student123"));
        student2.setEmail("student2@classroom.com");
        student2.setFullName("Trần Thị Bình");
        student2.setRoleId(1);
        student2.setStatus("active");
        student2.setCreatedAt(LocalDateTime.now());
        student2.setEnrollmentDate(LocalDate.of(2024, 9, 1));
        userRepository.save(student2);
        
        User student3 = new User();
        student3.setUsername("student3");
        student3.setPassword(passwordEncoder.encode("student123"));
        student3.setEmail("student3@classroom.com");
        student3.setFullName("Lê Văn Cường");
        student3.setRoleId(1);
        student3.setStatus("active");
        student3.setCreatedAt(LocalDateTime.now());
        student3.setEnrollmentDate(LocalDate.of(2024, 9, 1));
        userRepository.save(student3);
    }
    
    private void createBasicClassrooms(List<User> teachers) {
        if (teachers.isEmpty()) {
            System.out.println("⚠️  No teachers available to create classrooms");
            return;
        }
        
        // Classroom 1: Math
        Classroom mathClass = new Classroom();
        mathClass.setName("Lớp Học Trực Tuyến");
        mathClass.setSubject("Toán");
        mathClass.setSection("Lớp 12A1");
        mathClass.setDescription("Lớp toán nâng cao dành cho học sinh giỏi");
        mathClass.setTeacher(teachers.get(0));
        classroomRepository.save(mathClass);
        
        // Classroom 2: Vietnamese Literature  
        Classroom literatureClass = new Classroom();
        literatureClass.setName("Văn Học Việt Nam");
        literatureClass.setSubject("Văn");
        literatureClass.setSection("Lớp 12B2");
        literatureClass.setDescription("Học văn học Việt Nam hiện đại");
        literatureClass.setTeacher(teachers.size() > 1 ? teachers.get(1) : teachers.get(0));
        classroomRepository.save(literatureClass);
        
        // Classroom 3: English
        Classroom englishClass = new Classroom();
        englishClass.setName("English Communication");
        englishClass.setSubject("Anh");
        englishClass.setSection("Lớp 12C3");
        englishClass.setDescription("Giao tiếp tiếng Anh nâng cao");
        englishClass.setTeacher(teachers.get(0));
        classroomRepository.save(englishClass);
        
        System.out.println("Created 3 sample classrooms");
    }
}
