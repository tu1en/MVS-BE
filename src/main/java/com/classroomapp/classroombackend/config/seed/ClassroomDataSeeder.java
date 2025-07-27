package com.classroomapp.classroombackend.config.seed;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.classroomapp.classroombackend.model.classroommanagement.Classroom;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollment;
import com.classroomapp.classroombackend.model.classroommanagement.ClassroomEnrollmentId;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomEnrollmentRepository;
import com.classroomapp.classroombackend.repository.classroommanagement.ClassroomRepository;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Order(1) // Run this seeder first
@Slf4j
public class ClassroomDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomEnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting ClassroomDataSeeder...");
        
        if (shouldSeedData()) {
            seedUsers();
            seedClassrooms();
            seedEnrollments();
            log.info("ClassroomDataSeeder completed successfully!");
        } else {
            log.info("Data already exists, skipping ClassroomDataSeeder");
        }
    }

    private boolean shouldSeedData() {
        // Check if basic data already exists
        long userCount = userRepository.count();
        long classroomCount = classroomRepository.count();
        return userCount < 4 || classroomCount == 0;
    }

    private void seedUsers() {
        log.info("Seeding users...");
        
        // Create test students - Using RoleEnum.STUDENT
        createUserIfNotExists("student1", "student1@test.com", "Nguyen Van A", User.RoleEnum.STUDENT);
        createUserIfNotExists("student2", "student2@test.com", "Tran Thi B", User.RoleEnum.STUDENT);
        createUserIfNotExists("student3", "student3@test.com", "Le Van C", User.RoleEnum.STUDENT);
        
        // Create test teachers - Using RoleEnum.TEACHER
        createUserIfNotExists("teacher1", "teacher1@test.com", "Pham Thi D", User.RoleEnum.TEACHER);
        createUserIfNotExists("teacher2", "teacher2@test.com", "Hoang Van E", User.RoleEnum.TEACHER);
        
        log.info("Users seeded successfully");
    }

    private void createUserIfNotExists(String username, String email, String fullName, User.RoleEnum roleEnum) {
        if (!userRepository.existsByUsername(username) && !userRepository.existsByEmail(email)) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode("password123")); // Default password
            user.setFullName(fullName);
            
            // Set both roleId and roleEnum for consistency
            user.setRoleId(roleEnum.getId());
            user.setRoleEnum(roleEnum);
            
            // Set status using String (not enum)
            user.setStatus("active");
            
            userRepository.save(user);
            log.info("Created user: {} with role: {}", username, roleEnum.getName());
        }
    }

    private void seedClassrooms() {
        log.info("Seeding classrooms...");
        
        User teacher1 = userRepository.findByUsername("teacher1").orElse(null);
        User teacher2 = userRepository.findByUsername("teacher2").orElse(null);
        
        if (teacher1 != null && teacher2 != null) {
            // Create classrooms for teacher1
            createClassroomIfNotExists(
                "Lập trình Java Nâng cao",
                "Khóa học Java từ cơ bản đến nâng cao, bao gồm OOP, Collections, Threading",
                "Programming",
                "Section A",
                teacher1
            );
            
            createClassroomIfNotExists(
                "Thiết kế Web với React",
                "Khóa học thiết kế web hiện đại với React, JSX, Components, Hooks",
                "Web Development", 
                "Section A",
                teacher1
            );
            
            // Create classrooms for teacher2
            createClassroomIfNotExists(
                "Cấu trúc dữ liệu và giải thuật",
                "Học về các cấu trúc dữ liệu cơ bản và giải thuật tìm kiếm, sắp xếp",
                "Computer Science",
                "Section B", 
                teacher2
            );
            
            createClassroomIfNotExists(
                "Cơ sở dữ liệu MySQL",
                "Khóa học về hệ quản trị cơ sở dữ liệu MySQL, SQL queries, optimization",
                "Database",
                "Section C",
                teacher2
            );
            
            createClassroomIfNotExists(
                "Android Development",
                "Phát triển ứng dụng Android với Java/Kotlin, UI/UX, APIs",
                "Mobile Development",
                "Section A",
                teacher1
            );
        }
        
        log.info("Classrooms seeded successfully");
    }

    private void createClassroomIfNotExists(String name, String description, String subject, String section, User teacher) {
        if (!classroomRepository.findByNameContainingIgnoreCase(name).isEmpty()) {
            log.info("Classroom already exists: {}", name);
            return;
        }
        
        Classroom classroom = new Classroom();
        classroom.setName(name);
        classroom.setDescription(description);
        classroom.setSubject(subject);
        classroom.setSection(section);
        classroom.setTeacher(teacher);
        
        // Note: Remove setStatus() call since Classroom doesn't have status field
        // If you need status, add it to Classroom entity first
        
        classroomRepository.save(classroom);
        log.info("Created classroom: {}", name);
    }

    private void seedEnrollments() {
        log.info("Seeding enrollments...");
        
        // Find students using roleId instead of Role enum
        List<User> students = userRepository.findByRoleId(User.RoleEnum.STUDENT.getId());
        List<Classroom> classrooms = classroomRepository.findAll();
        
        if (!students.isEmpty() && !classrooms.isEmpty()) {
            // Enroll student1 in multiple courses
            User student1 = userRepository.findByUsername("student1").orElse(null);
            if (student1 != null) {
                enrollStudentInClassroom(student1, findClassroomByName(classrooms, "Lập trình Java Nâng cao"), 75.0);
                enrollStudentInClassroom(student1, findClassroomByName(classrooms, "Cấu trúc dữ liệu và giải thuật"), 45.0);
                enrollStudentInClassroom(student1, findClassroomByName(classrooms, "Thiết kế Web với React"), 90.0);
            }
            
            // Enroll student2 in different courses
            User student2 = userRepository.findByUsername("student2").orElse(null);
            if (student2 != null) {
                enrollStudentInClassroom(student2, findClassroomByName(classrooms, "Lập trình Java Nâng cao"), 60.0);
                enrollStudentInClassroom(student2, findClassroomByName(classrooms, "Cơ sở dữ liệu MySQL"), 80.0);
                enrollStudentInClassroom(student2, findClassroomByName(classrooms, "Android Development"), 35.0);
            }
            
            // Enroll student3 in some courses
            User student3 = userRepository.findByUsername("student3").orElse(null);
            if (student3 != null) {
                enrollStudentInClassroom(student3, findClassroomByName(classrooms, "Cấu trúc dữ liệu và giải thuật"), 70.0);
                enrollStudentInClassroom(student3, findClassroomByName(classrooms, "Thiết kế Web với React"), 55.0);
                enrollStudentInClassroom(student3, findClassroomByName(classrooms, "Cơ sở dữ liệu MySQL"), 40.0);
            }
        }
        
        log.info("Enrollments seeded successfully");
    }

    private Classroom findClassroomByName(List<Classroom> classrooms, String name) {
        return classrooms.stream()
                .filter(c -> c.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    private void enrollStudentInClassroom(User student, Classroom classroom, Double progressPercentage) {
        if (student == null || classroom == null) {
            log.warn("Cannot enroll: student or classroom is null");
            return;
        }
        
        ClassroomEnrollmentId enrollmentId = new ClassroomEnrollmentId(classroom.getId(), student.getId());
        
        // Check if enrollment already exists
        if (enrollmentRepository.existsById(enrollmentId)) {
            log.info("Enrollment already exists for student {} in classroom {}", 
                    student.getUsername(), classroom.getName());
            return;
        }
        
        ClassroomEnrollment enrollment = new ClassroomEnrollment();
        enrollment.setId(enrollmentId);
        enrollment.setClassroom(classroom);
        enrollment.setUser(student);
        enrollment.setStatus(ClassroomEnrollment.EnrollmentStatus.ACTIVE);
        enrollment.setProgressPercentage(progressPercentage);
        enrollment.setEnrolledAt(LocalDateTime.now());
        
        enrollmentRepository.save(enrollment);
        log.info("Enrolled student {} in classroom {} with {}% progress", 
                student.getUsername(), classroom.getName(), progressPercentage);
    }
}